import type {Sale} from "../../domain/entity/Sale";
import {derived, writable} from "svelte/store";
import {saleContainer} from "../../di/sale.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

interface SaleState {
    items: Sale[]
    loading: boolean
    error: string | null
}

const initialState: SaleState = {
    items: [],
    loading: false,
    error: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function createSaleStore() {
    const {subscribe, update} = writable<SaleState>(initialState)

    async function syncAll(): Promise<void> {
        update((state) => ({...state, loading: true, error: null}))
        try {
            const sales = await saleContainer.useCases.getAll.execute()
            update((state) => ({...state, items: sales}))
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}))
            throw error
        } finally {
            update((state) => ({...state, loading: false}))
        }
    }

    async function setVerified(id: string, verified: string): Promise<void> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const updated = await saleContainer.useCases.updateVerified.execute(id, verified);
            update((state) => ({
                ...state,
                items: state.items.map((s) => (s.id === id ? updated : s))
            }));
        } catch (error: any) {
            logger.error(error?.message ?? error, error?.stack);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    function clearError(): void {
        update((state) => ({...state, error: null}))
    }

    function reset(): void {
        update(() => initialState)
    }

    const hasData = derived({subscribe}, ($state) => $state.items.length > 0)

    return {
        subscribe,
        hasData,
        syncAll,
        setVerified,
        clearError,
        reset
    }
}

export const saleStore = createSaleStore()
