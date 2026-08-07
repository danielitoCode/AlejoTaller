import {derived, writable} from "svelte/store";
import type {Product} from "../../domain/entity/Product";
import {productContainer} from "../../di/product.container";
import {
    subscribeStockUpdates,
    unsubscribeStockUpdates
} from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import type {StockChangedPayload} from "../../../../infrastructure/data/alset-pulse/stock-pulse";

interface ProductState {
    items: Product[]
    selected: Product | null
    loading: boolean
    stockSyncing: boolean
    saving: boolean
    error: string | null
}

const initialState: ProductState = {
    items: [],
    selected: null,
    loading: false,
    stockSyncing: false,
    saving: false,
    error: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function sortNewestFirst(products: Product[]): Product[] {
    return [...products].sort((a, b) => (b.createdAtIso ?? "").localeCompare(a.createdAtIso ?? ""))
}

function createProductStore() {
    const {subscribe, update} = writable<ProductState>(initialState)
    let stockUnsub: (() => void) | null = null

    async function runSaving<T>(task: () => Promise<T>): Promise<T> {
        update((state) => ({...state, saving: true, error: null}))
        try {
            return await task()
        } catch (error) {
            update((state) => ({...state, error: normalizeError(error)}))
            throw error
        } finally {
            update((state) => ({...state, saving: false}))
        }
    }

    function mergeProducts(current: Product[], incoming: Product[]): Product[] {
        const map = new Map(current.map((p) => [p.id, p]))
        for (const p of incoming) {
            map.set(p.id, p)
        }
        return sortNewestFirst([...map.values()])
    }

    async function handleStockChanged(payload: StockChangedPayload): Promise<void> {
        if (import.meta.env.DEV) {
            console.info(
                `[productStore] stock:changed reason=${payload.reason} ids=${payload.productIds.join(",")}`
            )
        }
        update((state) => ({...state, stockSyncing: true}))
        try {
            const refreshed = await productContainer.useCases.refreshByIds.execute(payload.productIds)
            update((state) => ({
                ...state,
                items: mergeProducts(state.items, refreshed),
                selected:
                    state.selected && refreshed.some((p) => p.id === state.selected!.id)
                        ? refreshed.find((p) => p.id === state.selected!.id) ?? state.selected
                        : state.selected,
                stockSyncing: false
            }))
        } catch (error) {
            update((state) => ({
                ...state,
                stockSyncing: false,
                error: normalizeError(error)
            }))
        }
    }

    /** Arranca listener Pusher stock:changed (idempotente). */
    function startStockRealtime(): void {
        if (stockUnsub) return
        stockUnsub = subscribeStockUpdates((payload) => {
            void handleStockChanged(payload)
        })
    }

    function stopStockRealtime(): void {
        if (stockUnsub) {
            stockUnsub()
            stockUnsub = null
        }
        unsubscribeStockUpdates()
    }

    async function syncAll(): Promise<void> {
        startStockRealtime()

        update((state) => ({
            ...state,
            error: null,
            stockSyncing: true,
            loading: state.items.length === 0
        }))

        try {
            const offline = productContainer.repositories.offlineFirst
            if (typeof offline.getLocalAll === "function") {
                try {
                    const local = await offline.getLocalAll()
                    if (local.length > 0) {
                        update((state) => ({
                            ...state,
                            items: sortNewestFirst(local),
                            loading: false
                        }))
                    }
                } catch {
                    /* ignore */
                }
            }

            const products = await productContainer.useCases.getAll.execute()
            update((state) => ({
                ...state,
                items: sortNewestFirst(products),
                loading: false,
                stockSyncing: false
            }))
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                loading: false,
                stockSyncing: false
            }))
        }
    }

    async function syncById(id: string): Promise<Product | null> {
        update((state) => ({...state, stockSyncing: true, error: null}))
        try {
            const product = await productContainer.useCases.getById.execute(id)
            update((state) => ({
                ...state,
                selected: product,
                stockSyncing: false,
                items: product
                    ? mergeProducts(state.items, [product])
                    : state.items
            }))
            return product
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                stockSyncing: false
            }))
            throw error
        }
    }

    /** Refresh parcial por ids (también usable tras hold local). */
    async function refreshByIds(productIds: string[]): Promise<void> {
        await handleStockChanged({
            productIds,
            reason: "hold",
            timestamp: new Date().toISOString()
        })
    }

    async function create(data: Product): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.create.execute(data)
            await syncAll()
        })
    }

    async function updatePrice(product: Product, newPrice: number): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.updatePrice.execute(newPrice, product)
            await syncAll()
            const synced = await productContainer.useCases.getById.execute(product.id)
            update((state) => ({...state, selected: synced}))
        })
    }

    async function removeById(id: string): Promise<void> {
        await runSaving(async () => {
            await productContainer.useCases.delete.execute(id)
            await syncAll()
            update((state) => ({
                ...state,
                selected: state.selected?.id === id ? null : state.selected
            }))
        })
    }

    function clearError(): void {
        update((state) => ({...state, error: null}))
    }

    function reset(): void {
        stopStockRealtime()
        update(() => initialState)
    }

    const hasData = derived({subscribe}, ($state) => $state.items.length > 0)

    return {
        subscribe,
        hasData,
        syncAll,
        syncById,
        refreshByIds,
        startStockRealtime,
        stopStockRealtime,
        create,
        updatePrice,
        removeById,
        clearError,
        reset
    }
}

export const productStore = createProductStore()
