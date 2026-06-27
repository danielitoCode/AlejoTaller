import { derived, writable } from "svelte/store";
import { infrastructureContainer } from "../../../../infrastructure/di/infrastructure.container";

const GUEST_SESSION_STORAGE_KEY = "talleralejo.session.isGuest";

interface SessionState {
    loading: boolean;
    error: string | null;
    lastAction: string | null;
    isGuest: boolean;
}

function readStoredGuestState(): boolean {
    if (typeof window === "undefined") return false;
    return window.localStorage.getItem(GUEST_SESSION_STORAGE_KEY) === "true";
}

function persistGuestState(isGuest: boolean): void {
    if (typeof window === "undefined") return;
    if (isGuest) {
        window.localStorage.setItem(GUEST_SESSION_STORAGE_KEY, "true");
        return;
    }
    window.localStorage.removeItem(GUEST_SESSION_STORAGE_KEY);
}

const initialState: SessionState = {
    loading: false,
    error: null,
    lastAction: null,
    isGuest: readStoredGuestState()
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function createSessionStore() {
    const { subscribe, update, set } = writable<SessionState>(initialState);

    async function runAction<T>(actionName: string, task: () => Promise<T>): Promise<T> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const result = await task();
            update((state) => ({ ...state, lastAction: actionName }));
            return result;
        } catch (error) {
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function getCurrentUser() {
        return runAction("getCurrentUser", async () => infrastructureContainer.appwrite.account.get());
    }

    function setGuestSession(): void {
        persistGuestState(true);
        update((state) => ({ ...state, isGuest: true, error: null, lastAction: "openGuestSession" }));
    }

    function setAuthenticatedSession(): void {
        persistGuestState(false);
        update((state) => ({ ...state, isGuest: false, error: null }));
    }


    function clearError(): void {
        update((state) => ({ ...state, error: null }));
    }

    function reset(): void {
        persistGuestState(false);
        set({ ...initialState, isGuest: false });
    }

    const hasError = derived({ subscribe }, ($state) => $state.error !== null);
    const isGuest = derived({ subscribe }, ($state) => $state.isGuest);

    return {
        subscribe,
        hasError,
        isGuest,
        getCurrentUser,
        setGuestSession,
        setAuthenticatedSession,
        clearError,
        reset
    };
}

export const sessionStore = createSessionStore();