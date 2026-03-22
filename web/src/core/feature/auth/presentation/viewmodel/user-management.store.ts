import { derived, writable } from "svelte/store";
import { authContainer } from "../../di/auth.container";
import type { User } from "../../domain/entity/User";

export type BusinessRole = "owner" | "admin" | "sales" | "viewer";

export interface ManagedBusinessUser {
    id: string;
    name: string;
    email: string;
    photoUrl: string;
    role: BusinessRole;
    blocked: boolean;
    verified: boolean;
    passwordResetRequested: boolean;
}

interface UserManagementState {
    items: ManagedBusinessUser[];
    loading: boolean;
    saving: boolean;
    error: string | null;
}

const initialState: UserManagementState = {
    items: [],
    loading: false,
    saving: false,
    error: null
};

function mapRole(role: unknown): BusinessRole {
    if (role === "owner" || role === "admin" || role === "sales" || role === "viewer") {
        return role;
    }

    return "viewer";
}

function mapDomainUserToManagedUser(user: User): ManagedBusinessUser {
    return {
        id: user.id,
        name: user.name || user.email,
        email: user.email,
        photoUrl: user.photo_url || "",
        role: mapRole(user.role),
        blocked: user.status === false,
        verified: Boolean(user.verification),
        passwordResetRequested: false,
    };
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function createUserManagementStore() {
    const {subscribe, update} = writable<UserManagementState>(initialState);
    let snapshot: UserManagementState = initialState;
    subscribe((s) => (snapshot = s));
    let loadUsersInFlight: Promise<void> | null = null;
    let lastSearch = "";

    async function runSaving<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({...state, saving: true, error: null}));
        try {
            return await task();
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}));
            throw error;
        } finally {
            update((state) => ({...state, saving: false}));
        }
    }

    async function runLoading<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            return await task();
        } catch (error) {
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function syncAll(): Promise<void> {
        await loadUsers(lastSearch);
    }

    async function loadUsers(search?: string): Promise<void> {
        if (loadUsersInFlight) {
            return loadUsersInFlight;
        }

        loadUsersInFlight = runLoading(async () => {
            lastSearch = (search ?? "").trim();
            const res = await authContainer.useCases.accounts.getAllUserCaseUse(lastSearch);
            const users: User[] = res.users;

            const managedUsers: ManagedBusinessUser[] = users.map((u) => mapDomainUserToManagedUser(u));

            update((state) => ({ ...state, items: managedUsers }));
        }).finally(() => {
            loadUsersInFlight = null;
        });

        return loadUsersInFlight;
    }

    async function createUser(payload: Pick<ManagedBusinessUser, "name" | "email" | "role"> & { password: string }): Promise<void> {
        await runSaving(async () => {
            const labels =
                payload.role === "owner"
                    ? ["owner", "admin"]
                    : payload.role === "admin"
                      ? ["admin"]
                      : payload.role === "sales"
                        ? ["sales"]
                        : ["viewer"];

            await authContainer.useCases.accounts.adminCreateUser({
                name: payload.name,
                email: payload.email,
                password: payload.password,
                labels
            });

            await loadUsers(lastSearch);
        });
    }

    async function setRole(id: string, role: BusinessRole): Promise<void> {
        await runSaving(async () => {
            const labels =
                role === "owner"
                    ? ["owner", "admin"]
                    : role === "admin"
                      ? ["admin"]
                      : role === "sales"
                        ? ["sales"]
                        : ["viewer"];

            await authContainer.useCases.accounts.adminUpdateLabels(id, labels);
            update((state) => ({ ...state, items: state.items.map((u) => (u.id === id ? { ...u, role } : u)) }));
        });
    }

    async function toggleBlocked(id: string): Promise<void> {
        const current = snapshot.items.find((u) => u.id === id);
        if (!current) return;
        const nextBlocked = !current.blocked;

        await runSaving(async () => {
            await authContainer.useCases.accounts.adminUpdateStatus(id, !nextBlocked);
            update((state) => ({
                ...state,
                items: state.items.map((u) => (u.id === id ? { ...u, blocked: nextBlocked } : u))
            }));
        });
    }

    async function requestPasswordReset(id: string, newPassword: string): Promise<void> {
        await runSaving(async () => {
            await authContainer.useCases.accounts.adminUpdatePassword(id, newPassword);
            update((state) => ({
                ...state,
                items: state.items.map((u) => (u.id === id ? { ...u, passwordResetRequested: true } : u))
            }));
        });
    }

    const hasUsers = derived({ subscribe }, ($state) => $state.items.length > 0);

    return {
        subscribe,
        hasUsers,
        syncAll,
        createUser,
        setRole,
        toggleBlocked,
        loadUsers,
        requestPasswordReset
    };
}

export const userManagementStore = createUserManagementStore();
