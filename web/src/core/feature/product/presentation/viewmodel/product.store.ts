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

function mapAppwriteActionToReason(
    action: AppwriteProductChangeSignal["action"]
): StockChangedPayload["reason"] {
    // Appwrite no distingue hold/release/consume; tratamos update como hold genérico de inventario
    if (action === "delete") return "release"
    return "hold"
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

    async function handleStockChanged(
        payload: StockChangedPayload,
        options: { silent?: boolean; fromRealtime?: boolean; source?: string } = {}
    ): Promise<void> {
        const fromRealtime = options.fromRealtime === true
        const silent = options.silent === true
        const source = options.source ?? (fromRealtime ? "appwrite" : "local")
        const count = payload.productIds.length
        const t0 = performance.now()

        console.info(
            `[stock-rt] UI handle start source=${source} realtime=${fromRealtime} silent=${silent} ` +
                `reason=${payload.reason} saleId=${payload.saleId ?? "-"} count=${count} ` +
                `ids=${payload.productIds.join(",")}`
        )

        if (fromRealtime && !silent) {
            console.info("[stock-rt] UI toast:info «Hemos recibido actualizaciones…»")
            toastStore.info(
                `Hemos recibido actualizaciones de productos (${reasonLabel(payload.reason)}). Actualizando…`,
                3200
            )
        }

        const syncMessage = fromRealtime
            ? `Actualizando ${count} producto${count === 1 ? "" : "s"}…`
            : "Sincronizando stock…"

        console.info(`[stock-rt] UI banner ON message="${syncMessage}"`)

        update((state) => ({
            ...state,
            stockSyncing: true,
            realtimeUpdating: fromRealtime,
            syncMessage
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
                `[stock-rt] UI merge done source=${source} refreshed=${refreshed.length}/${count} in ${ms}ms — banner OFF`
            )

            if (fromRealtime && !silent) {
                const okMsg =
                    refreshed.length > 0
                        ? `Catálogo actualizado (${refreshed.length} producto${refreshed.length === 1 ? "" : "s"})`
                        : "Sincronización de stock completada"
                console.info(`[stock-rt] UI toast:success «${okMsg}»`)
                toastStore.success(okMsg, 2800)
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
                console.warn("[stock-rt] UI toast:warning fallo de sincronización")
                toastStore.warning("No se pudieron aplicar todas las actualizaciones de stock")
            }
        }
    }

    function onAppwriteProductSignal(signal: AppwriteProductChangeSignal): void {
        console.info(
            `[stock-rt] Appwrite signal → refreshByIds action=${signal.action} ids=${signal.productIds.join(",")}`
        )
        void handleStockChanged(
            {
                productIds: signal.productIds,
                reason: mapAppwriteActionToReason(signal.action),
                timestamp: signal.timestamp
            },
            {
                fromRealtime: true,
                silent: false,
                source: "appwrite-realtime"
            }
        )
    }

    function onLocalStockEvent(ev: Event): void {
        const detail = (ev as CustomEvent).detail
        if (!isStockPayload(detail)) {
            console.warn("[stock-rt] CustomEvent inválido", detail)
            return
        }
        // Misma pestaña: el hold local ya escribió Dexie vía soft-hold;
        // Appwrite Realtime también disparará. Marcamos silent para no duplicar toast.
        console.info("[stock-rt] CustomEvent recibido → handle silent (Appwrite RT es canónico)")
        void handleStockChanged(detail, {
            fromRealtime: true,
            silent: true,
            source: "custom-event"
        })
    }

    function onBroadcastMessage(ev: MessageEvent): void {
        const msg = ev.data
        if (!msg || msg.type !== "stock:changed" || !isStockPayload(msg.data)) {
            return
        }
        console.info("[stock-rt] BroadcastChannel recibido → handle silent")
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
            console.info(`[stock-rt] listener CustomEvent ${STOCK_CHANGED_EVENT} activo (fallback same-tab)`)
        }

        if (!broadcast && typeof BroadcastChannel !== "undefined") {
            try {
                broadcast = new BroadcastChannel(STOCK_BROADCAST_NAME)
                broadcast.onmessage = onBroadcastMessage
                console.info(`[stock-rt] listener BroadcastChannel ${STOCK_BROADCAST_NAME} activo (fallback tabs)`)
            } catch (e) {
                console.warn(
                    `[stock-rt] BroadcastChannel no disponible: ${e instanceof Error ? e.message : String(e)}`
                )
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

    /**
     * Canal primario: Appwrite Realtime.
     * Pusher stock-updates queda fuera (sin backend Pulse).
     * CustomEvent / BroadcastChannel siguen como fallback same-origin.
     */
    function startStockRealtime(): void {
        startLocalStockListeners()

        if (appwriteUnsub) {
            console.info("[stock-rt] startStockRealtime: Appwrite RT ya activo, skip")
            // re-bind handler por si el store se reinició parcialmente
            startAppwriteProductRealtime(onAppwriteProductSignal)
            return
        }

        console.info("[stock-rt] startStockRealtime: suscribiendo Appwrite Realtime (primario)…")
        appwriteUnsub = startAppwriteProductRealtime(onAppwriteProductSignal)
        console.info("[stock-rt] listener Appwrite product documents activo en productStore")
    }

    function stopStockRealtime(): void {
        if (appwriteUnsub) {
            console.info("[stock-rt] stopStockRealtime Appwrite")
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

    async function refreshByIds(productIds: string[]): Promise<void> {
        console.info(`[stock-rt] refreshByIds local (silent) ids=${productIds.join(",")}`)
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
        console.info(
            `[stock-rt] refreshByIdsVisible reason=${reason} ids=${productIds.join(",")}`
        )
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
