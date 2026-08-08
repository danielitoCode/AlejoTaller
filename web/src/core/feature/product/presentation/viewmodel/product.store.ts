import {derived, writable} from "svelte/store";
import type {Product} from "../../domain/entity/Product";
import {productContainer} from "../../di/product.container";
import {
    STOCK_BROADCAST_NAME,
    STOCK_CHANGED_EVENT,
    type StockChangedPayload
} from "../../../../infrastructure/data/alset-pulse/stock-pulse";
import {
    startAppwriteProductRealtime,
    stopAppwriteProductRealtime,
    type AppwriteProductChangeSignal
} from "../../../../infrastructure/data/appwrite/appwrite-product-realtime";
import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";

interface ProductState {
    items: Product[]
    selected: Product | null
    loading: boolean
    stockSyncing: boolean
    realtimeUpdating: boolean
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

function isStockPayload(value: unknown): value is StockChangedPayload {
    if (!value || typeof value !== "object") return false
    const v = value as Partial<StockChangedPayload>
    return Array.isArray(v.productIds) && v.productIds.length > 0
}

function createProductStore() {
    const {subscribe, update} = writable<ProductState>(initialState)
    let appwriteUnsub: (() => void) | null = null
    let localEventBound = false
    let broadcast: BroadcastChannel | null = null

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

    /** Appwrite Realtime: aplicar snapshots del evento a Dexie + UI (sin red). */
    async function handleAppwriteSignal(signal: AppwriteProductChangeSignal): Promise<void> {
        const count = signal.productIds.length
        const t0 = performance.now()

        console.info(
            `[stock-rt] Appwrite snapshot apply start count=${count} ids=${signal.productIds.join(",")}`
        )

        toastStore.info("Se están actualizando los datos de productos…", 2800)

        update((state) => ({
            ...state,
            stockSyncing: true,
            realtimeUpdating: true,
            syncMessage:
                count === 1
                    ? "Actualizando producto…"
                    : `Actualizando ${count} productos…"`
        }))

        try {
            const applied = await productContainer.useCases.applyRealtimeSnapshots.execute(
                signal.snapshots
            )
            const ms = Math.round(performance.now() - t0)

            update((state) => ({
                ...state,
                items: mergeProducts(state.items, applied),
                selected:
                    state.selected && applied.some((p) => p.id === state.selected!.id)
                        ? applied.find((p) => p.id === state.selected!.id) ?? state.selected
                        : state.selected,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null
            }))

            console.info(
                `[stock-rt] Appwrite snapshot merge done applied=${applied.length}/${count} in ${ms}ms`
            )

            toastStore.success(
                applied.length > 0
                    ? `Datos actualizados (${applied.length})`
                    : "Sincronización local completada",
                2400
            )
        } catch (error) {
            const msg = normalizeError(error)
            console.error(`[stock-rt] Appwrite snapshot FAIL: ${msg}`, error)
            update((state) => ({
                ...state,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null,
                error: msg
            }))
            toastStore.warning("No se pudieron aplicar todas las actualizaciones")
        }
    }

    /** Fallback local / refreshByIds (puede ir a red). */
    async function handleStockChanged(
        payload: StockChangedPayload,
        options: { silent?: boolean; fromRealtime?: boolean; source?: string } = {}
    ): Promise<void> {
        const fromRealtime = options.fromRealtime === true
        const silent = options.silent === true
        const source = options.source ?? "local"
        const count = payload.productIds.length
        const t0 = performance.now()

        console.info(
            `[stock-rt] UI handle start source=${source} realtime=${fromRealtime} silent=${silent} ` +
                `reason=${payload.reason} count=${count} ids=${payload.productIds.join(",")}`
        )

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
            const ms = Math.round(performance.now() - t0)

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

            console.info(
                `[stock-rt] UI merge done source=${source} refreshed=${refreshed.length}/${count} in ${ms}ms`
            )

            if (fromRealtime && !silent) {
                toastStore.success(
                    refreshed.length > 0
                        ? `Catálogo actualizado (${refreshed.length})`
                        : "Sincronización de stock completada",
                    2800
                )
            }
        } catch (error) {
            const msg = normalizeError(error)
            console.error(`[stock-rt] UI handle FAIL source=${source}: ${msg}`, error)
            update((state) => ({
                ...state,
                stockSyncing: false,
                realtimeUpdating: false,
                syncMessage: null,
                error: msg
            }))
            if (fromRealtime && !silent) {
                toastStore.warning("No se pudieron aplicar todas las actualizaciones de stock")
            }
        }
    }

    function onLocalStockEvent(ev: Event): void {
        const detail = (ev as CustomEvent).detail
        if (!isStockPayload(detail)) return
        // Same-tab: Appwrite RT también llegará con snapshot; este path es silent backup
        void handleStockChanged(detail, {
            fromRealtime: true,
            silent: true,
            source: "custom-event"
        })
    }

    function onBroadcastMessage(ev: MessageEvent): void {
        const msg = ev.data
        if (!msg || msg.type !== "stock:changed" || !isStockPayload(msg.data)) return
        void handleStockChanged(msg.data, {
            fromRealtime: true,
            silent: true,
            source: "broadcast"
        })
    }

    function startLocalStockListeners(): void {
        if (typeof window === "undefined") return

        if (!localEventBound) {
            window.addEventListener(STOCK_CHANGED_EVENT, onLocalStockEvent)
            localEventBound = true
            console.info(`[stock-rt] listener CustomEvent ${STOCK_CHANGED_EVENT} (fallback)`)
        }

        if (!broadcast && typeof BroadcastChannel !== "undefined") {
            try {
                broadcast = new BroadcastChannel(STOCK_BROADCAST_NAME)
                broadcast.onmessage = onBroadcastMessage
                console.info(`[stock-rt] listener BroadcastChannel ${STOCK_BROADCAST_NAME} (fallback)`)
            } catch {
                /* ignore */
            }
        }
    }

    function stopLocalStockListeners(): void {
        if (typeof window !== "undefined" && localEventBound) {
            window.removeEventListener(STOCK_CHANGED_EVENT, onLocalStockEvent)
            localEventBound = false
        }
        if (broadcast) {
            broadcast.close()
            broadcast = null
        }
    }

    function startStockRealtime(): void {
        startLocalStockListeners()

        if (appwriteUnsub) {
            console.info("[stock-rt] Appwrite RT ya activo, skip")
            startAppwriteProductRealtime((signal) => {
                void handleAppwriteSignal(signal)
            })
            return
        }

        console.info("[stock-rt] suscribiendo Appwrite Realtime (snapshots → local)…")
        appwriteUnsub = startAppwriteProductRealtime((signal) => {
            void handleAppwriteSignal(signal)
        })
        console.info("[stock-rt] Appwrite product realtime cableado a applyRealtimeSnapshots")
    }

    function stopStockRealtime(): void {
        if (appwriteUnsub) {
            appwriteUnsub()
            appwriteUnsub = null
        }
        stopAppwriteProductRealtime()
        stopLocalStockListeners()
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
                items: product ? mergeProducts(state.items, [product]) : state.items
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

    async function refreshByIds(productIds: string[]): Promise<void> {
        await handleStockChanged(
            {
                productIds,
                reason: "hold",
                timestamp: new Date().toISOString()
            },
            { silent: true, fromRealtime: false, source: "refreshByIds" }
        )
    }

    async function refreshByIdsVisible(
        productIds: string[],
        reason: StockChangedPayload["reason"] = "hold"
    ): Promise<void> {
        await handleStockChanged(
            {
                productIds,
                reason,
                timestamp: new Date().toISOString()
            },
            { silent: false, fromRealtime: true, source: "refreshByIdsVisible" }
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
        refreshByIdsVisible,
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
