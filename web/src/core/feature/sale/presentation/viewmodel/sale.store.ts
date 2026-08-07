import type { DeliveryType } from "../../domain/entity/enums";
import type { Sale } from "../../domain/entity/Sale";
import { derived, get, writable } from "svelte/store";
import { saleContainer } from "../../di/sale.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { subscribeSaleVerification, unsubscribeSaleVerification } from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
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

function createSaleStore() {
    const { subscribe, update } = writable<SaleState>(initialState);
    let pusherUnsubscribe: (() => void) | null = null;
    let isSubscriptionPending = false;
    let subscribedUserId: string | null = null;

    async function managePusherSubscription(): Promise<void> {
        if (isSubscriptionPending) return;
        isSubscriptionPending = true;

        try {
            const state = get({ subscribe });
            if (get(sessionStore).isGuest) {
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                unsubscribeSaleVerification();
                subscribedUserId = null;
                return;
            }
            const currentUser = await sessionStore.getCurrentUser().catch(() => null);
            const currentUserId = currentUser?.$id ?? null;

            if (!currentUserId) {
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                unsubscribeSaleVerification();
                subscribedUserId = null;
                return;
            }

            const hasUnverified = state.items.some(
                (sale) => sale.userId === currentUserId && sale.verified === BuyState.UNVERIFIED
            );

            if (subscribedUserId && subscribedUserId !== currentUserId) {
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                unsubscribeSaleVerification();
                subscribedUserId = null;
            }

            if (hasUnverified && !pusherUnsubscribe) {
                console.info(
                    `[SaleStore] suscribiendo sale-verification userId=${currentUserId}`
                );
                pusherUnsubscribe = subscribeSaleVerification(currentUserId, (eventName, payload) => {
                    console.info(
                        `[SaleStore] evento sale-verification recibido event=${eventName}`,
                        payload
                    );
                    handleSaleVerificationEvent(eventName, payload);
                });
                subscribedUserId = currentUserId;
            } else if (!hasUnverified && pusherUnsubscribe) {
                console.info("[SaleStore] sin pedidos UNVERIFIED → unsuscribe sale-verification");
                pusherUnsubscribe();
                pusherUnsubscribe = null;
                unsubscribeSaleVerification();
                subscribedUserId = null;
            }
        } finally {
            isSubscriptionPending = false;
        }
    }

    function handleSaleVerificationEvent(
        eventName: string,
        payload: { saleId: string; decision: "confirmed" | "rejected"; timestamp: string; amount?: number; productCount?: number }
    ): void {
        const { saleId, decision } = payload;

        if (!saleId || !decision) {
            logger.error(`[Pusher Event] Invalid payload: ${JSON.stringify(payload)}`);
            console.error("[SaleStore] payload inválido sale-verification", payload);
            return;
        }

        console.info(
            `[SaleStore] aplicando decisión saleId=${saleId} decision=${decision} event=${eventName}`
        );

        const newState = decision === "confirmed" ? BuyState.VERIFIED : BuyState.DELETED;
        update((state) => ({
            ...state,
            items: state.items.map((sale) =>
                sale.id === saleId ? { ...sale, verified: newState } : sale
            )
        }));

        saleAlertStore.addAlert({
            saleId,
            decision,
            timestamp: payload.timestamp,
            amount: payload.amount,
            productCount: payload.productCount
        });

        const toastMessage = decision === "confirmed"
            ? `Tu pedido ${saleId.slice(0, 8)} fue confirmado`
            : `Tu pedido ${saleId.slice(0, 8)} fue rechazado`;

        if (decision === "confirmed") {
            toastStore.success(toastMessage, 3600);
        } else {
            toastStore.error(toastMessage, 4200);
        }

        console.info("[SaleStore] toast mostrado + refresh catálogo tras decisión operador");
        void productStore.syncAll().catch((e) => {
            console.warn("[SaleStore] syncAll tras verificación falló", e);
        });

        void managePusherSubscription();
    }

    async function syncAll(): Promise<void> {
        // Asegura listeners de stock aunque no estemos en la grilla de productos
        productStore.startStockRealtime();

        update((state) => ({ ...state, loading: true, error: null }));
        try {
            if (get(sessionStore).isGuest) {
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                unsubscribeSaleVerification();
                subscribedUserId = null;
                update((state) => ({ ...state, items: [] }));
                return;
            }
            const currentUser = await sessionStore.getCurrentUser().catch(() => null);

            if (!currentUser?.$id) {
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                unsubscribeSaleVerification();
                subscribedUserId = null;
                update((state) => ({ ...state, items: [] }));
                return;
            }

            const sales = await saleContainer.repositories.offlineFirst.getByUser(currentUser.$id);
            update((state) => ({ ...state, items: sales }));
            await managePusherSubscription();
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
            await managePusherSubscription();
        } catch (error: any) {
            logger.error(error?.message ?? error, error?.stack);
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    async function create(sale: Sale): Promise<Sale> {
        // Crítico: listeners activos antes del soft-hold / publish local
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
                console.info(`[SaleStore] soft-hold aplicado saleId=${created.id} → refresh visible`);
            } else {
                toastStore.success("Pedido registrado");
            }

            // Refresh con feedback (banner/toast stock-rt) aunque no haya Pulse
            if (ids.length) {
                try {
                    await productStore.refreshByIdsVisible(ids, "hold");
                } catch {
                    void productStore.syncAll().catch(() => {});
                }
            } else {
                void productStore.syncAll().catch(() => {});
            }

            await managePusherSubscription();

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
            await managePusherSubscription();
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
        if (pusherUnsubscribe) {
            pusherUnsubscribe();
            pusherUnsubscribe = null;
        }
        unsubscribeSaleVerification();
        subscribedUserId = null;
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
        managePusherSubscription
    };
}

export const saleStore = createSaleStore();
