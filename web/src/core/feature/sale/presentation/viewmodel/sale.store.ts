import type { DeliveryType } from "../../domain/entity/enums";
import type { Sale } from "../../domain/entity/Sale";
import { derived, get, writable } from "svelte/store";
import { saleContainer } from "../../di/sale.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import {
    startAppwriteSaleRealtime,
    stopAppwriteSaleRealtime,
    type AppwriteSaleChangeSignal
} from "../../../../infrastructure/data/appwrite/appwrite-sale-realtime";
import {
    subscribeSaleVerification,
    subscribeSaleUpdates,
    type SalePulsePayload,
} from "../../../../infrastructure/data/alset-pulse/sale-pulse";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import {productContainer} from "../../../product/di/product.container";
import { productStore } from "../../../product/presentation/viewmodel/product.store";
import { saleAlertStore } from "./sale-alert.store";
import { BuyState } from "../../domain/entity/enums";
import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
import { isAppwriteDataStackDisabled } from "../../../../infrastructure/platform.flags";

interface SaleState {
    items: Sale[];
    loading: boolean;
    error: string | null;
}

const initialState: SaleState = {
    items: [],
    loading: false,
    error: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function decisionFromBuyState(
    buyState: string
): "confirmed" | "rejected" | null {
    if (buyState === BuyState.VERIFIED || buyState === "VERIFIED") return "confirmed";
    if (buyState === BuyState.DELETED || buyState === "DELETED") return "rejected";
    return null;
}

function createSaleStore() {
    const { subscribe, update } = writable<SaleState>(initialState);
    let realtimeUnsub: (() => void) | null = null;
    let isSubscriptionPending = false;
    let subscribedUserId: string | null = null;
    /** Fallback cuando Pusher REST no puede publicar desde el browser (CORS). */
    let pollTimer: ReturnType<typeof setInterval> | null = null;
    let lastVerifiedMap: Record<string, string> = {};

    async function manageSaleRealtimeSubscription(): Promise<void> {
        if (isSubscriptionPending) return;
        isSubscriptionPending = true;

        try {
            if (get(sessionStore).isGuest) {
                stopSaleRealtime();
                return;
            }

            const currentUser = await sessionStore.getCurrentUser().catch(() => null);
            const currentUserId =
                (currentUser as any)?.$id ?? (currentUser as any)?.id ?? null;

            if (!currentUserId) {
                stopSaleRealtime();
                return;
            }

            if (subscribedUserId && subscribedUserId !== currentUserId) {
                stopSaleRealtime();
            }

            if (isAppwriteDataStackDisabled()) {
                if (!realtimeUnsub) {
                    console.info(
                        `[SaleStore] Pusher sale RT userId=${String(currentUserId).slice(0, 12)}…`
                    );
                    const unsubVerify = subscribeSaleVerification(
                        String(currentUserId),
                        (eventName, payload) => {
                            void handlePusherSaleDecision(eventName, payload, String(currentUserId));
                        }
                    );
                    const unsubUpdates = subscribeSaleUpdates((eventName, payload) => {
                        void handlePusherSaleDecision(eventName, payload, String(currentUserId));
                    });
                    realtimeUnsub = () => {
                        unsubVerify();
                        unsubUpdates();
                    };
                    subscribedUserId = currentUserId;
                }
                startSaleStatusPolling(String(currentUserId));
                return;
            }

            if (!realtimeUnsub) {
                console.info(
                    `[SaleStore] suscribiendo Appwrite sale realtime userId=${currentUserId}`
                );
                realtimeUnsub = startAppwriteSaleRealtime((signal) => {
                    void handleAppwriteSaleSignal(signal, currentUserId);
                });
                subscribedUserId = currentUserId;
            } else {
                startAppwriteSaleRealtime((signal) => {
                    void handleAppwriteSaleSignal(signal, currentUserId);
                });
                subscribedUserId = currentUserId;
            }
        } finally {
            isSubscriptionPending = false;
        }
    }

    async function managePusherSubscription(): Promise<void> {
        return manageSaleRealtimeSubscription();
    }

    function stopSaleStatusPolling(): void {
        if (pollTimer != null) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    function startSaleStatusPolling(userId: string): void {
        stopSaleStatusPolling();
        const uid = String(userId || "").trim();
        if (!uid) return;

        const tick = async () => {
            try {
                const sales = await saleContainer.repositories.offlineFirst.getByUser(uid);
                for (const sale of sales) {
                    const prev = lastVerifiedMap[sale.id];
                    const now = String(sale.verified ?? "");
                    if (prev !== undefined && prev !== now) {
                        const decision =
                            now === BuyState.VERIFIED || now === "VERIFIED"
                                ? "confirmed"
                                : now === BuyState.DELETED || now === "DELETED"
                                  ? "rejected"
                                  : null;
                        if (decision) {
                            console.info(
                                `[SaleStore] poll detect ${decision} saleId=${sale.id.slice(0, 8)}…`
                            );
                            await handlePusherSaleDecision(
                                decision === "confirmed" ? "sale:confirmed" : "sale:rejected",
                                {
                                    saleId: sale.id,
                                    userId: uid,
                                    decision,
                                    productIds: (sale.products ?? []).map((p) => p.productId).filter(Boolean),
                                    timestamp: new Date().toISOString(),
                                },
                                uid
                            );
                        }
                    }
                    lastVerifiedMap[sale.id] = now;
                }
                update((state) => ({ ...state, items: sales }));
            } catch (e) {
                console.warn(
                    `[SaleStore] poll failed: ${e instanceof Error ? e.message : String(e)}`
                );
            }
        };

        void (async () => {
            try {
                const sales = await saleContainer.repositories.offlineFirst.getByUser(uid);
                for (const sale of sales) {
                    lastVerifiedMap[sale.id] = String(sale.verified ?? "");
                }
                update((state) => ({ ...state, items: sales }));
            } catch { /* */ }
        })();

        pollTimer = setInterval(() => {
            void tick();
        }, 3500);
        console.info(`[SaleStore] poll Turso cada 3.5s userId=${uid.slice(0, 12)}…`);
    }

    function stopSaleRealtime(): void {
        stopSaleStatusPolling();
        if (realtimeUnsub) {
            realtimeUnsub();
            realtimeUnsub = null;
        }
        stopAppwriteSaleRealtime();
        subscribedUserId = null;
    }

    async function handlePusherSaleDecision(
        eventName: string,
        payload: SalePulsePayload,
        currentUserId: string
    ): Promise<void> {
        const name = String(eventName ?? "").toLowerCase();
        if (name !== "sale:confirmed" && name !== "sale:rejected") return;

        const ownerId = String(payload.userId ?? "").trim();
        if (ownerId && ownerId !== currentUserId) return;

        const saleId = String(payload.saleId ?? "").trim();
        if (!saleId) return;

        const decision: "confirmed" | "rejected" =
            payload.decision === "rejected" || name === "sale:rejected"
                ? "rejected"
                : "confirmed";

        const expected = decision === "confirmed" ? BuyState.VERIFIED : BuyState.DELETED;
        if (lastVerifiedMap[saleId] === String(expected)) {
            return;
        }

        toastStore.info("Se está actualizando el estado de tu pedido…", 2200);

        const newState = decision === "confirmed" ? BuyState.VERIFIED : BuyState.DELETED;
        lastVerifiedMap[saleId] = String(newState);

        update((state) => ({
            ...state,
            items: state.items.map((sale) =>
                sale.id === saleId ? { ...sale, verified: newState } : sale
            ),
        }));

        try {
            const sales = await saleContainer.repositories.offlineFirst.getByUser(currentUserId);
            update((state) => ({ ...state, items: sales }));
        } catch {
            /* keep optimistic */
        }

        saleAlertStore.addAlert({
            saleId,
            decision,
            timestamp: payload.timestamp || new Date().toISOString(),
            amount: undefined,
            productCount: payload.productIds?.length,
        });

        const shortId = saleId.slice(0, 8);
        if (decision === "confirmed") {
            toastStore.success(`Tu pedido ${shortId} fue confirmado`, 3600);
        } else {
            toastStore.error(`Tu pedido ${shortId} fue rechazado`, 4200);
        }

        if (payload.productIds?.length) {
            void productStore
                .refreshByIdsVisible(
                    payload.productIds,
                    decision === "confirmed" ? "consume" : "release"
                )
                .catch(() => productStore.syncAll().catch(() => {}));
        } else {
            void productStore.syncAll().catch(() => {});
        }
    }

    async function handleAppwriteSaleSignal(
        signal: AppwriteSaleChangeSignal,
        currentUserId: string
    ): Promise<void> {
        if (isAppwriteDataStackDisabled()) return;
        const snap = signal.snapshot;
        const ownerId = String(snap.user_id ?? "");
        const buyState = String(snap.buy_state ?? "");

        if (ownerId && ownerId !== currentUserId) {
            return;
        }

        const decision = decisionFromBuyState(buyState);
        if (!decision) {
            try {
                const sale = await saleContainer.useCases.applyRealtimeSnapshot.execute(snap);
                if (sale) {
                    update((state) => {
                        const exists = state.items.some((s) => s.id === sale.id);
                        return {
                            ...state,
                            items: exists
                                ? state.items.map((s) => (s.id === sale.id ? sale : s))
                                : [sale, ...state.items]
                        };
                    });
                }
            } catch {
                /* ignore */
            }
            return;
        }

        toastStore.info("Se está actualizando el estado de tu pedido…", 2200);

        let applied: Sale | null = null;
        try {
            applied = await saleContainer.useCases.applyRealtimeSnapshot.execute(snap);
        } catch {
            /* ignore */
        }

        const newState = decision === "confirmed" ? BuyState.VERIFIED : BuyState.DELETED;

        update((state) => ({
            ...state,
            items: state.items.map((sale) => {
                if (sale.id !== signal.saleId) return sale;
                if (applied) return applied;
                return { ...sale, verified: newState };
            })
        }));

        const amount =
            typeof snap.amount === "number"
                ? snap.amount
                : applied?.amount;

        let productCount: number | undefined;
        try {
            if (typeof snap.products === "string") {
                const parsed = JSON.parse(snap.products);
                if (Array.isArray(parsed)) productCount = parsed.length;
            } else if (Array.isArray(snap.products)) {
                productCount = snap.products.length;
            } else if (applied?.products) {
                productCount = applied.products.length;
            }
        } catch {
            /* ignore */
        }

        saleAlertStore.addAlert({
            saleId: signal.saleId,
            decision,
            timestamp: signal.timestamp,
            amount,
            productCount
        });

        const shortId = signal.saleId.slice(0, 8);
        if (decision === "confirmed") {
            toastStore.success(`Tu pedido ${shortId} fue confirmado`, 3600);
        } else {
            toastStore.error(`Tu pedido ${shortId} fue rechazado`, 4200);
        }
    }

    async function syncAll(): Promise<void> {
        productStore.startStockRealtime();

        update((state) => ({ ...state, loading: true, error: null }));
        try {
            if (get(sessionStore).isGuest) {
                stopSaleRealtime();
                update((state) => ({ ...state, items: [] }));
                return;
            }
            const currentUser = await sessionStore.getCurrentUser().catch(() => null);
            const uid = (currentUser as any)?.$id ?? (currentUser as any)?.id ?? null;

            if (!uid) {
                stopSaleRealtime();
                update((state) => ({ ...state, items: [] }));
                return;
            }

            const sales = await saleContainer.repositories.offlineFirst.getByUser(uid);
            for (const sale of sales) {
                lastVerifiedMap[sale.id] = String(sale.verified ?? "");
            }
            update((state) => ({ ...state, items: sales }));
            await manageSaleRealtimeSubscription();
        } catch (error) {
            logger.error(`[SaleStore] syncAll failed: ${normalizeError(error)}`);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function setVerified(id: string, verified: string): Promise<void> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const updated = await saleContainer.useCases.updateVerified.execute(id, verified);
            update((state) => ({
                ...state,
                items: state.items.map((sale) => (sale.id === id ? updated : sale))
            }));
            await manageSaleRealtimeSubscription();
        } catch (error: any) {
            logger.error(error?.message ?? error, error?.stack);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function create(sale: Sale): Promise<Sale> {
        productStore.startStockRealtime();
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            await productContainer.useCases.checkAProductExistence.execute(sale);
            const created = await saleContainer.useCases.create.execute(sale);
            update((state) => ({ ...state, items: [created, ...state.items] }));
            const ids = created.products.map((p) => p.productId).filter(Boolean);
            const softHoldError = (created as Sale & { softHoldError?: string }).softHoldError;
            if (softHoldError) {
                toastStore.warning(`Pedido creado, pero no se pudo reservar stock: ${softHoldError}`);
            } else if (created.stockHoldApplied) {
                toastStore.info("Pedido registrado. Actualizando disponibilidad de productos…", 3000);
            } else {
                toastStore.success("Pedido registrado");
            }
            if (ids.length) {
                try {
                    await productStore.refreshByIdsVisible(ids, "hold");
                } catch {
                    void productStore.syncAll().catch(() => {});
                }
            }
            await manageSaleRealtimeSubscription();
            return created;
        } catch (error: any) {
            logger.error(error?.message ?? error, error?.stack);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function cancelUnverified(sale: Sale): Promise<Sale> {
        productStore.startStockRealtime();
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const updated = await saleContainer.useCases.cancelUnverified.execute(sale);
            update((state) => ({
                ...state,
                items: state.items.map((s) => (s.id === updated.id ? updated : s))
            }));
            const ids = sale.products.map((p) => p.productId).filter(Boolean);
            if (ids.length) {
                try {
                    await productStore.refreshByIdsVisible(ids, "release");
                } catch {
                    void productStore.refreshByIds(ids).catch(() => {});
                }
            }
            toastStore.info("Pedido cancelado. Stock liberado.");
            await manageSaleRealtimeSubscription();
            return updated;
        } catch (error: any) {
            logger.error(error?.message ?? error, error?.stack);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function updateDeliveryType(id: string, deliveryType: DeliveryType): Promise<void> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const updated = await saleContainer.useCases.updateDeliveryType.execute(id, deliveryType);
            update((state) => ({
                ...state,
                items: state.items.map((sale) => (sale.id === id ? updated : sale))
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
        update((state) => ({ ...state, error: null }));
    }

    function reset(): void {
        stopSaleRealtime();
        update(() => initialState);
    }

    const hasData = derived({ subscribe }, ($state) => $state.items.length > 0);
    const unverifiedCount = derived(
        { subscribe },
        ($state) => $state.items.filter((sale) => sale.verified === BuyState.UNVERIFIED).length
    );

    return {
        subscribe,
        hasData,
        unverifiedCount,
        syncAll,
        create,
        cancelUnverified,
        setVerified,
        updateDeliveryType,
        clearError,
        reset,
        managePusherSubscription,
        manageSaleRealtimeSubscription
    };
}

export const saleStore = createSaleStore();
