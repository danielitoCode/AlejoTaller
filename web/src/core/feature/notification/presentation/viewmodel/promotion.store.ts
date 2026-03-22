import { derived, writable } from "svelte/store";
import type { Promotion } from "../../domain/entity/Promotion";
import { notificationContainer } from "../../di/notification.container";

interface PromotionState {
    items: Promotion[];
    loading: boolean;
    saving: boolean;
    error: string | null;
}

const initialState: PromotionState = {
    items: [],
    loading: false,
    saving: false,
    error: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function createPromotionStore() {
    const { subscribe, update } = writable<PromotionState>(initialState);

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

    async function runSaving<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({ ...state, saving: true, error: null }));
        try {
            return await task();
        } catch (error) {
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, saving: false }));
        }
    }

    async function syncAll(): Promise<void> {
        await runLoading(async () => {
            const items = await notificationContainer.useCases.promo.getAll();
            update((state) => ({ ...state, items }));
        });
    }

    async function create(data: Promotion): Promise<void> {
        await runSaving(async () => {
            await notificationContainer.useCases.promo.generate(data);
            await syncAll();
        });
    }

    const hasData = derived({ subscribe }, ($state) => $state.items.length > 0);

    return { subscribe, hasData, syncAll, create };
}

export const promotionStore = createPromotionStore();