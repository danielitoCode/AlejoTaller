import {derived, writable} from "svelte/store";
import type {UserDTO} from "../../data/dto/UserDTO";
import {authContainer} from "../../di/auth.container";

interface AuthState {
    loading: boolean
    error: string | null
    lastAction: string | null
}

const initialState: AuthState = {
    loading: false,
    error: null,
    lastAction: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function createAuthStore() {
    const {subscribe, update} = writable<AuthState>(initialState)

    async function runAction(actionName: string, task: () => Promise<void>): Promise<void> {
        update((state) => ({...state, loading: true, error: null}))
        try {
            await task()
            update((state) => ({...state, lastAction: actionName}))
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}))
            throw error
        } finally {
            update((state) => ({...state, loading: false}))
        }
    }

    async function createAccount(data: Partial<UserDTO>): Promise<void> {
        await runAction("createAccount", async () => {
            await authContainer.repositories.accounts.createAccount(data)
        })
    }

    async function updateName(newName: string): Promise<void> {
        await runAction("updateName", async () => {
            await authContainer.repositories.accounts.updateName(newName)
        })
    }

    async function updatePassword(newPassword: string): Promise<void> {
        await runAction("updatePassword", async () => {
            await authContainer.repositories.accounts.updatePassword(newPassword)
        })
    }

    async function updatePhotoUrl(newPhotoUrl: string): Promise<void> {
        await runAction("updatePhotoUrl", async () => {
            await authContainer.repositories.accounts.updatePhotoUrl(newPhotoUrl)
        })
    }

    async function updatePhone(newPhone: string): Promise<void> {
        await runAction("updatePhone", async () => {
            await authContainer.repositories.accounts.updatePhone(newPhone)
        })
    }

    async function updateRole(newRole: string): Promise<void> {
        await runAction("updateRole", async () => {
            await authContainer.repositories.accounts.updateRole(newRole)
        })
    }

    async function deleteUser(user: Partial<UserDTO>): Promise<void> {
        await runAction("deleteUser", async () => {
            await authContainer.repositories.accounts.deleteUser(user)
        })
    }

    function clearError(): void {
        update((state) => ({...state, error: null}))
    }

    function reset(): void {
        update(() => initialState)
    }

    const hasError = derived({subscribe}, ($state) => $state.error !== null)

    return {
        subscribe,
        hasError,
        createAccount,
        updateName,
        updatePassword,
        updatePhotoUrl,
        updatePhone,
        updateRole,
        deleteUser,
        clearError,
        reset
    }
}

export const authStore = createAuthStore()