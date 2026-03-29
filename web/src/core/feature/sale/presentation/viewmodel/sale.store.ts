import type { DeliveryType } from "../../domain/entity/enums";
import type { Sale } from "../../domain/entity/Sale";
import { derived, get, writable } from "svelte/store";
import { saleContainer } from "../../di/sale.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { subscribeSaleVerification, unsubscribeSaleVerification } from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
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
                logger.log("Suscribiendo a eventos de verificacion de ventas");
                pusherUnsubscribe = subscribeSaleVerification(currentUserId, (eventName, payload) => {
                    handleSaleVerificationEvent(eventName, payload);
                });
                subscribedUserId = currentUserId;
            } else if (!hasUnverified && pusherUnsubscribe) {
                logger.log("Desuscribiendo de eventos de verificacion de ventas");
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
            logger.error("[Pusher Event] Invalid payload:", payload.decision);
            return;
        }

        logger.log(`[Pusher Event] ${eventName} - Sale: ${saleId}, Decision: ${decision}`);

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

        if (window.Notification && Notification.permission === "granted") {
            const title = decision === "confirmed" ? "Pedido confirmado" : "Pedido rechazado";
            const body = decision === "confirmed"
                ? `Tu pedido ha sido confirmado${payload.amount ? ` por $${payload.amount} CUP` : ""}`
                : "Tu pedido ha sido rechazado";
            const notif = new Notification(title, {
                body,
                icon: "/alejoicon_clean.svg",
                data: { saleId }
            });

            notif.onclick = () => {
                window.focus();
                window.dispatchEvent(
                    new CustomEvent("sale-verification-open", {
                        detail: { saleId }
                    })
                );
                notif.close();
            };
        }

        void managePusherSubscription();
    }

    async function syncAll(): Promise<void> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
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
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const created = await saleContainer.useCases.create.execute(sale);
            update((state) => ({
                ...state,
                items: [created, ...state.items]
            }));
            await managePusherSubscription();
            return created;
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
        setVerified,
        updateDeliveryType,
        clearError,
        reset,
        managePusherSubscription
    };
}

export const saleStore = createSaleStore();
