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
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import {productContainer} from "../../../product/di/product.container";
import { productStore } from "../../../product/presentation/viewmodel/product.store";
import { saleAlertStore } from "./sale-alert.store";
import { BuyState } from "../../domain/entity/enums";
import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";

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

    /**
     * Appwrite Realtime sobre colección `sale` (sin Pusher ni secret de publish).
     * Se mantiene suscripción mientras haya sesión (no solo UNVERIFIED) para
     * no perder el update del operador si el timing es justo tras el create.
     */
    async function manageSaleRealtimeSubscription(): Promise<void> {
        if (isSubscriptionPending) return;
        isSubscriptionPending = true;

        try {
            if (get(sessionStore).isGuest) {
                stopSaleRealtime();
                return;
            }

            const currentUser = await sessionStore.getCurrentUser().catch(() => null);
            const currentUserId = currentUser?.$id ?? null;

            if (!currentUserId) {
                stopSaleRealtime();
                return;
            }

            if (subscribedUserId && subscribedUserId !== currentUserId) {
                stopSaleRealtime();
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
                // re-bind handler con userId actual
                startAppwriteSaleRealtime((signal) => {
                    void handleAppwriteSaleSignal(signal, currentUserId);
                });
                subscribedUserId = currentUserId;
            }
        } finally {
            isSubscriptionPending = false;
        }
    }

    /** Alias de compatibilidad con callers antiguos */
    async function managePusherSubscription(): Promise<void> {
        return manageSaleRealtimeSubscription();
    }

    function stopSaleRealtime(): void {
        if (realtimeUnsub) {
            realtimeUnsub();
            realtimeUnsub = null;
        }
        stopAppwriteSaleRealtime();
        subscribedUserId = null;
    }

    async function handleAppwriteSaleSignal(
        signal: AppwriteSaleChangeSignal,
        currentUserId: string
    ): Promise<void> {
        const snap = signal.snapshot;
        const ownerId = String(snap.user_id ?? "");
        const buyState = String(snap.buy_state ?? "");

        // Solo ventas del usuario autenticado
        if (ownerId && ownerId !== currentUserId) {
            console.info(
                `[SaleStore] Appwrite sale ignorada (otro user) saleId=${signal.saleId} owner=${ownerId}`
            );
            return;
        }

        const decision = decisionFromBuyState(buyState);
        if (!decision) {
            // create / update aún UNVERIFIED: solo sincroniza cache local
            console.info(
                `[SaleStore] Appwrite sale snapshot sin decisión final saleId=${signal.saleId} buy_state=${buyState}`
            );
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
            } catch (e) {
                console.warn("[SaleStore] apply snapshot UNVERIFIED falló", e);
            }
            return;
        }

        console.info(
            `[SaleStore] Appwrite decisión saleId=${signal.saleId} decision=${decision} buy_state=${buyState}`
        );

        toastStore.info("Se está actualizando el estado de tu pedido…", 2200);

        let applied: Sale | null = null;
        try {
            applied = await saleContainer.useCases.applyRealtimeSnapshot.execute(snap);
        } catch (e) {
            console.error("[SaleStore] applyRealtimeSnapshot FAIL", e);
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

        // Stock se actualiza por Appwrite Realtime de product; no forzar syncAll
        console.info(
            `[SaleStore] decisión aplicada vía Appwrite RT (sin Pusher) saleId=${signal.saleId}`
        );
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

            if (!currentUser?.$id) {
                stopSaleRealtime();
                update((state) => ({ ...state, items: [] }));
                return;
            }

            const sales = await saleContainer.repositories.offlineFirst.getByUser(currentUser.$id);
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

        update((state) => ({
            ...state,
            loading: true,
            error: null
        }));

        try {
            await productContainer.useCases.checkAProductExistence.execute(sale);

            const created = await saleContainer.useCases.create.execute(sale);

            update((state) => ({
                ...state,
                items: [created, ...state.items]
            }));

            const ids = created.products.map((p) => p.productId).filter(Boolean);
            console.info(
                `[SaleStore] create OK saleId=${created.id} hold=${created.stockHoldApplied} ids=${ids.join(",")}`
            );

            const softHoldError = (created as Sale & { softHoldError?: string }).softHoldError;
            if (softHoldError) {
                toastStore.warning(
                    `Pedido creado, pero no se pudo reservar stock: ${softHoldError}`
                );
            } else if (created.stockHoldApplied) {
                toastStore.info(
                    "Pedido registrado. Actualizando disponibilidad de productos…",
                    3000
                );
            } else {
                toastStore.success("Pedido registrado");
            }

            // Stock: Appwrite product RT aplicará snapshot; refresh local opcional
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

            update((state) => ({
                ...state,
                error: normalizeError(error)
            }));

            throw error;
        } finally {
            update((state) => ({
                ...state,
                loading: false
            }));
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
