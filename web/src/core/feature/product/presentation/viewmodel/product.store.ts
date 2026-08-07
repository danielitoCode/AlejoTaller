import {derived, writable} from "svelte/store";
import type {Product} from "../../domain/entity/Product";
import {productContainer} from "../../di/product.container";
import {
    subscribeStockUpdates,
    unsubscribeStockUpdates
} from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import type {StockChangedPayload} from "../../../../infrastructure/data/alset-pulse/stock-pulse";
import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";

interface ProductState {
    items: Product[]
    selected: Product | null
    loading: boolean
    /** true mientras se refresca stock (inicial o realtime) */
    stockSyncing: boolean
    /** true solo cuando el sync viene de señal Pusher stock:changed */
    realtimeUpdating: boolean
    /** mensaje corto para banner de UI */
    syncMessage: string | null
    saving: boolean
    error: string | null
}

const initialState: ProductState = {
    items: [],
    selected: null,
    loading: false,
    stockSyncing: false,
    realtimeUpdating: false,
    syncMessage: null,
    saving: false,
    error: null
}

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error"
}

function sortNewestFirst(products: Product[]): Product[] {
    return [...products].sort((a, b) => (b.createdAtIso ?? "").localeCompare(a.createdAtIso ?? ""))
}

function reasonLabel(reason: StockChangedPayload["reason"]): string {
    switch (reason) {
        case "hold":
            return "reservas"
        case "release":
            return "liberación de stock"
        case "consume":
            return "confirmación de venta"
        default:
            return "cambios de inventario"
    }
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

    async function handleStockChanged(
        payload: StockChangedPayload,
        options: { silent?: boolean; fromRealtime?: boolean } = {}
    ): Promise<void> {
        const fromRealtime = options.fromRealtime === true
        const silent = options.silent === true
        const count = payload.productIds.length

        if (import.meta.env.DEV) {
            console.info(
                `[productStore] stock:changed reason=${payload.reason} ids=${payload.productIds.join(",")} realtime=${fromRealtime}`
            )
        }

        if (fromRealtime && !silent) {
            toastStore.info(
                `Hemos recibido actualizaciones de productos (${reasonLabel(payload.reason)}). Actualizando…`,
                3200
            )
        }

        update((state) => ({
            ...state,
            stockSyncing: true,
            realtimeUpdating: fromRealtime,
            syncMessage: fromRealtime
                ? `Actualizando ${count} producto${count === 1 ? "" : "s"}…`
                : "Sincronizando stock…"
        }))

        try {
            const refreshed = await productContainer.useCases.refreshByIds.execute(payload.productIds)
            update((state) => ({
                ...state,
                items: mergeProducts(state.items, refreshed),
                selected:
                    state.selected && refreshed.some((p) => p.id === state.selected!.id)
                        ? refreshed.find((p) => p.id === state.selected!.id) ?? state.selected
                        : state.selected,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null
            }))

            if (fromRealtime && !silent) {
                toastStore.success(
                    refreshed.length > 0
                        ? `Catálogo actualizado (${refreshed.length} producto${refreshed.length === 1 ? "" : "s"})`
                        : "Sincronización de stock completada",
                    2800
                )
            }
        } catch (error) {
            update((state) => ({
                ...state,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null,
                error: normalizeError(error)
            }))
            if (fromRealtime && !silent) {
                toastStore.warning("No se pudieron aplicar todas las actualizaciones de stock")
            }
        }
    }

    /** Arranca listener Pusher stock:changed (idempotente). */
    function startStockRealtime(): void {
        if (stockUnsub) return
        stockUnsub = subscribeStockUpdates((payload) => {
            void handleStockChanged(payload, { fromRealtime: true })
        })
        if (import.meta.env.DEV) {
            console.info("[productStore] listener stock:changed activo")
        }
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
            realtimeUpdating: false,
            syncMessage: "Sincronizando catálogo…",
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
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null
            }))
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                loading: false,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null
            }))
        }
    }

    async function syncById(id: string): Promise<Product | null> {
        update((state) => ({
            ...state,
            stockSyncing: true,
            syncMessage: "Actualizando producto…",
            error: null
        }))
        try {
            const product = await productContainer.useCases.getById.execute(id)
            update((state) => ({
                ...state,
                selected: product,
                stockSyncing: false,
                syncMessage: null,
                items: product
                    ? mergeProducts(state.items, [product])
                    : state.items
            }))
            return product
        } catch (error) {
            update((state) => ({
                ...state,
                error: normalizeError(error),
                stockSyncing: false,
                syncMessage: null
            }))
            throw error
        }
    }

    /** Refresh parcial (local, sin toast de “recibimos actualización”). */
    async function refreshByIds(productIds: string[]): Promise<void> {
        await handleStockChanged(
            {
                productIds,
                reason: "hold",
                timestamp: new Date().toISOString()
            },
            { silent: true, fromRealtime: false }
        )
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
