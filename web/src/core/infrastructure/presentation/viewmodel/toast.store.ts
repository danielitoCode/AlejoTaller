import { writable } from "svelte/store";

export type ToastType = "success" | "error" | "info";

export interface ToastMessage {
    id: number;
    type: ToastType;
    text: string;
    timeoutMs: number;
}

interface ToastState {
    queue: ToastMessage[];
}

const initialState: ToastState = {
    queue: []
};

function createToastStore() {
    const { subscribe, update } = writable<ToastState>(initialState);

    function push(type: ToastType, text: string, timeoutMs = 2800): void {
        const id = Date.now() + Math.floor(Math.random() * 1000);
        const toast: ToastMessage = { id, type, text, timeoutMs };

        update((state) => ({
            ...state,
            queue: [...state.queue, toast]
        }));

        setTimeout(() => remove(id), timeoutMs);
    }

    function remove(id: number): void {
        update((state) => ({
            ...state,
            queue: state.queue.filter((toast) => toast.id !== id)
        }));
    }

    function success(text: string, timeoutMs?: number): void {
        push("success", text, timeoutMs);
    }

    function error(text: string, timeoutMs?: number): void {
        push("error", text, timeoutMs);
    }

    function info(text: string, timeoutMs?: number): void {
        push("info", text, timeoutMs);
    }

    return {
        subscribe,
        remove,
        success,
        error,
        info
    };
}

export const toastStore = createToastStore();