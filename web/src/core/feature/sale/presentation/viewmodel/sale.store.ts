import type { DeliveryType } from "../../domain/entity/enums";
import type {Sale} from "../../domain/entity/Sale";
import {derived, writable, get} from "svelte/store";
import {saleContainer} from "../../di/sale.container";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { subscribeSaleVerification, unsubscribeSaleVerification } from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
import { saleAlertStore } from "./sale-alert.store";
import { BuyState } from "../../domain/entity/enums";

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
    let pusherUnsubscribe: (() => void) | null = null;
    let isSubscriptionPending = false;

    /**
     * Verifica si hay ventas sin verificar y gestiona suscripción Pusher
     */
    async function managePusherSubscription(): Promise<void> {
        // Evitar race conditions
        if (isSubscriptionPending) return;
        isSubscriptionPending = true;

        try {
            const state = get({subscribe});
            const currentUser = await sessionStore.getCurrentUser().catch(() => null);
            
            if (!currentUser?.$id) {
                // Usuario no autenticado, cerrar suscripción
                if (pusherUnsubscribe) {
                    pusherUnsubscribe();
                    pusherUnsubscribe = null;
                }
                return;
            }

            const hasUnverified = state.items.some((s) => s.verified === BuyState.UNVERIFIED);

            if (hasUnverified && !pusherUnsubscribe) {
                // Hay ventas sin verificar y no estamos suscritos -> suscribirse
                logger.log("Suscribiendo a eventos de verificación de ventas");
                pusherUnsubscribe = subscribeSaleVerification(
                    currentUser.$id,
                    (eventName, payload) => {
                        handleSaleVerificationEvent(eventName, payload);
                    }
                );
            } else if (!hasUnverified && pusherUnsubscribe) {
                // No hay ventas sin verificar y estamos suscritos -> desuscribirse
                logger.log("Desuscribiendo de eventos de verificación de ventas");
                pusherUnsubscribe();
                pusherUnsubscribe = null;
                unsubscribeSaleVerification();
            }
        } finally {
            isSubscriptionPending = false;
        }
    }

    /**
     * Maneja eventos de verificación de ventas desde Pusher
     */
    function handleSaleVerificationEvent(
        eventName: string,
        payload: { saleId: string; decision: "confirmed" | "rejected"; timestamp: string; amount?: number; productCount?: number }
    ): void {
        const { saleId, decision } = payload;
        
        // Validar payload
        if (!saleId || !decision) {
            logger.error(`[Pusher Event] Invalid payload:`, payload.decision);
            return;
        }
        
        logger.log(`[Pusher Event] ${eventName} - Sale: ${saleId}, Decision: ${decision}` );

        // Actualizar estado local
        const newState = decision === "confirmed" ? BuyState.VERIFIED : BuyState.DELETED;
        update((state) => ({
            ...state,
            items: state.items.map((s) =>
                s.id === saleId ? { ...s, verified: newState } : s
            ),
        }));

        // Agregar alerta
        saleAlertStore.addAlert({
            saleId,
            decision,
            timestamp: payload.timestamp,
            amount: payload.amount,
            productCount: payload.productCount,
        });

        // Notificación del sistema
        if (window.Notification && Notification.permission === "granted") {
            const title = decision === "confirmed"
                ? "¡Pedido confirmado!"
                : "Pedido rechazado";
            const body = decision === "confirmed"
                ? `Tu pedido ha sido confirmado${payload.amount ? ` por $${payload.amount} CUP` : ''}`
                : "Tu pedido ha sido rechazado";
            const icon = "/public/alejoicon_clean.svg";
            const notif = new Notification(title, {
                body,
                icon,
                data: { saleId }
            });
            notif.onclick = () => {
                window.focus();
                // Aquí podrías navegar al detalle del pedido si tienes router global
            };
        }

        // Remanager suscripción si es necesario
        managePusherSubscription();
    }

    async function syncAll(): Promise<void> {
        update((state) => ({...state, loading: true, error: null}))
        try {
            const sales = await saleContainer.useCases.getAll.execute()
            update((state) => ({...state, items: sales}))
            await managePusherSubscription();
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
        if (pusherUnsubscribe) {
            pusherUnsubscribe();
            pusherUnsubscribe = null;
        }
        update(() => initialState)
    }

    const hasData = derived({subscribe}, ($state) => $state.items.length > 0)
    const unverifiedCount = derived({subscribe}, ($state) => $state.items.filter((s) => s.verified === BuyState.UNVERIFIED).length)

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
    }
}

export const saleStore = createSaleStore()
