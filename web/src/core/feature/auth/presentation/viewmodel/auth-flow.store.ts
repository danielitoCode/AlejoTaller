import { writable } from "svelte/store";

export type LoginProvider = "password" | "google" | "unknown";

export interface AuthFlowSnapshot {
    userId: string | null;
    email: string | null;
    provider: LoginProvider;
    error: string | null;
    updatedAt: number | null;
}

const initialState: AuthFlowSnapshot = {
    userId: null,
    email: null,
    provider: "unknown",
    error: null,
    updatedAt: null
};

function createAuthFlowStore() {
    const { subscribe, set, update } = writable<AuthFlowSnapshot>(initialState);

    function setSuccess(payload: { userId: string; email?: string | null; provider?: LoginProvider }) {
        set({
            userId: payload.userId,
            email: payload.email ?? null,
            provider: payload.provider ?? "unknown",
            error: null,
            updatedAt: Date.now()
        });
    }

    function setError(message: string, payload?: { email?: string | null; provider?: LoginProvider }) {
        update((state) => ({
            ...state,
            email: payload?.email ?? state.email,
            provider: payload?.provider ?? state.provider,
            error: message,
            updatedAt: Date.now()
        }));
    }

    function reset() {
        set(initialState);
    }

    return {
        subscribe,
        setSuccess,
        setError,
        reset
    };
}

export const authFlowStore = createAuthFlowStore();
