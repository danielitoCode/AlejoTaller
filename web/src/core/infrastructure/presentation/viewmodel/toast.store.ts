import { writable } from "svelte/store";

export type ToastType = "success" | "error" | "warning" | "info" | "promo" | "loading";

export interface ToastMessage {
    id: number;
    type: ToastType;
    text: string;
    timeoutMs: number;
    sticky?: boolean;
}

interface ToastState {
    queue: ToastMessage[];
}

const initialState: ToastState = {
    queue: []
};

function createToastStore() {
    const { subscribe, update } = writable<ToastState>(initialState);

    function push(type: ToastType, text: string, timeoutMs = 2800, sticky = false): number {
        const id = Date.now() + Math.floor(Math.random() * 1000);
        const toast: ToastMessage = {
            id,
            type,
            text,
            timeoutMs: sticky ? 0 : timeoutMs,
            sticky
        };

        update((state) => ({
            ...state,
            queue: [...state.queue, toast]
        }));

        if (!sticky && timeoutMs > 0) {
            setTimeout(() => remove(id), timeoutMs);
        }
        return id;
    }

    function remove(id: number): void {
        update((state) => ({
            ...state,
            queue: state.queue.filter((toast) => toast.id !== id)
        }));
    }

    function patch(
        id: number,
        partial: Partial<Pick<ToastMessage, "type" | "text" | "timeoutMs" | "sticky">>
    ): void {
        update((state) => ({
            ...state,
            queue: state.queue.map((t) => {
                if (t.id !== id) return t;
                const next = { ...t, ...partial };
                if (partial.sticky === false && (partial.timeoutMs ?? t.timeoutMs) > 0) {
                    const ms = partial.timeoutMs ?? 2800;
                    setTimeout(() => remove(id), ms);
                }
                return next;
            })
        }));
    }

    function success(text: string, timeoutMs?: number): number {
        return push("success", text, timeoutMs ?? 2800);
    }

    function error(text: string, timeoutMs?: number): number {
        return push("error", text, timeoutMs ?? 4200);
    }

    function info(text: string, timeoutMs?: number): number {
        return push("info", text, timeoutMs ?? 2800);
    }

    function warning(text: string, timeoutMs?: number): number {
        return push("warning", text, timeoutMs ?? 3600);
    }

    function promo(text: string, timeoutMs?: number): number {
        return push("promo", text, timeoutMs ?? 3600);
    }

    function loading(text: string): number {
        return push("loading", text, 0, true);
    }

    async function run<T>(
        promise: Promise<T>,
        opts: {
            loading: string;
            success?: string | ((result: T) => string);
            error?: string | ((err: unknown) => string);
        }
    ): Promise<T> {
        const id = loading(opts.loading);
        try {
            const result = await promise;
            const msg =
                typeof opts.success === "function"
                    ? opts.success(result)
                    : opts.success ?? "Listo";
            patch(id, { type: "success", text: msg, sticky: false, timeoutMs: 2800 });
            return result;
        } catch (err) {
            const msg =
                typeof opts.error === "function"
                    ? opts.error(err)
                    : opts.error ?? (err instanceof Error ? err.message : "Error");
            patch(id, { type: "error", text: msg, sticky: false, timeoutMs: 4800 });
            throw err;
        }
    }

    return {
        subscribe,
        remove,
        patch,
        push,
        success,
        error,
        info,
        warning,
        promo,
        loading,
        run
    };
}

export const toastStore = createToastStore();
