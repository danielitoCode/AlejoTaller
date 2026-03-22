import { derived, writable } from "svelte/store";
import { supportContainer } from "../../di/support.container";
import type { SupportMessage, SupportStatus } from "../../domain/entity/SupportMessage";

type SupportInboxState = {
    items: SupportMessage[];
    loading: boolean;
    error: string | null;
};

const initialState: SupportInboxState = {
    items: [],
    loading: false,
    error: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function createSupportInboxStore() {
    const { subscribe, update } = writable<SupportInboxState>(initialState);
    let unsubscribe: (() => void) | null = null;
    let syncTimer: number | null = null;

    async function syncAll(): Promise<void> {
        update((s) => ({ ...s, loading: true, error: null }));
        try {
            const items = await supportContainer.useCases.inbox.getAll();
            update((s) => ({ ...s, items }));
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
            throw e;
        } finally {
            update((s) => ({ ...s, loading: false }));
        }
    }

    async function setStatus(id: string, status: SupportStatus): Promise<void> {
        await supportContainer.useCases.inbox.updateStatus(id, status);
        update((s) => ({
            ...s,
            items: s.items.map((m) => (m.id === id ? { ...m, status } : m))
        }));
    }

    function startRealtime(): () => void {
        stopRealtime();
        unsubscribe = supportContainer.useCases.inbox.subscribe(() => {
            if (syncTimer) window.clearTimeout(syncTimer);
            syncTimer = window.setTimeout(() => {
                syncAll().catch(() => {});
            }, 220);
        });

        return stopRealtime;
    }

    function stopRealtime(): void {
        if (syncTimer) {
            window.clearTimeout(syncTimer);
            syncTimer = null;
        }
        if (unsubscribe) {
            try {
                unsubscribe();
            } catch {
                // ignore
            }
            unsubscribe = null;
        }
    }

    const counts = derived({ subscribe }, ($s) => {
        const total = $s.items.length;
        const nuevo = $s.items.filter((m) => m.status === "nuevo").length;
        const enProceso = $s.items.filter((m) => m.status === "en_proceso").length;
        const resuelto = $s.items.filter((m) => m.status === "resuelto").length;
        return { total, nuevo, enProceso, resuelto };
    });

    return { subscribe, syncAll, setStatus, startRealtime, stopRealtime, counts };
}

export const supportInboxStore = createSupportInboxStore();

