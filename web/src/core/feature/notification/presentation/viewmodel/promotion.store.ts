import { derived, writable, get } from "svelte/store";
import type { Promotion } from "../../domain/entity/Promotion";
import { notificationContainer } from "../../di/notification.container";
import {
    subscribePromotionUpdates,
    unsubscribePromotionUpdates,
    type PromotionEventPayload
} from "../../../../infrastructure/data/alset-pulse/pulse.realtime";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";
import { promotionFromDTO } from "../../data/mapper/Mappers";

interface PromotionState {
    items: Promotion[];
    loading: boolean;
    saving: boolean;
    error: string | null;
}

const initialState: PromotionState = {
    items: [],
    loading: false,
    saving: false,
    error: null
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}



function createPromotionStore() {
    const { subscribe, update } = writable<PromotionState>(initialState);
    let pusherUnsubscribe: (() => void) | null = null;
    let isSubscriptionPending = false;

    async function runLoading<T>(fn: () => Promise<T>): Promise<T> {
        update((state) => ({ ...state, loading: true, error: null }));

        try {
            return await fn();
        } catch (error) {
            update((state) => ({ ...state, error: normalizeError(error) }));
            throw error;
        } finally {
            update((state) => ({ ...state, loading: false }));
        }
    }

    // ...existing code...
    /**
     * Gestiona automáticamente la suscripción a Pusher
     * Activa si hay promociones, desactiva si no hay
     */
    async function managePusherSubscription(): Promise<void> {
        // Evitar race conditions
        if (isSubscriptionPending) return;
        isSubscriptionPending = true;

        try {
            const state = get({ subscribe });
            const hasPromotions = state.items.length > 0;

            if (hasPromotions && !pusherUnsubscribe) {
                // Activar suscripción a eventos realtime
                logger.log("[PromotionStore] Activating promotion realtime updates");
                pusherUnsubscribe = subscribePromotionUpdates((eventName, payload) => {
                    handlePromotionEvent(eventName, payload);
                });
            } else if (!hasPromotions && pusherUnsubscribe) {
                // Desactivar suscripción cuando no hay promociones
                logger.log("[PromotionStore] Deactivating promotion realtime updates");
                pusherUnsubscribe();
                unsubscribePromotionUpdates();
                pusherUnsubscribe = null;
            }
        } finally {
            isSubscriptionPending = false;
        }
    }

    /**
     * Maneja eventos de promociones recibidos desde Pusher
     * Sincroniza con el estado local en tiempo real
     */
    function handlePromotionEvent(eventName: string, payload: PromotionEventPayload): void {
        logger.log(`[PromotionEvent] Received: ${eventName} \n ${{
            id: payload.id,
            title: payload.title,
            currentPrice: payload.currentPrice
        }}`);

        const promotion = promotionFromDTO(payload as any);

        update((state) => {
            switch (eventName) {
                case "promotion:created":
                    logger.log(`[PromotionEvent] Adding new promotion: ${promotion.id}`);
                    return {
                        ...state,
                        items: [...state.items, promotion],
                    };

                case "promotion:updated":
                    logger.log(`[PromotionEvent] Updating promotion: ${promotion.id}`);
                    return {
                        ...state,
                        items: state.items.map((p) =>
                            p.id === promotion.id ? promotion : p
                        ),
                    };

                case "promotion:deleted":
                    logger.log(`[PromotionEvent] Deleting promotion: ${promotion.id}`);
                    return {
                        ...state,
                        items: state.items.filter((p) => p.id !== promotion.id),
                    };

                default:
                    logger.warn(`[PromotionEvent] Unknown event: ${eventName}`);
                    return state;
            }
        });

        // Enviar notificación del navegador si está permitida
        if (window.Notification && Notification.permission === "granted") {
            const title = eventName === "promotion:created"
                ? "¡Nueva promoción!"
                : eventName === "promotion:updated"
                ? "Promoción actualizada"
                : "Promoción removida";

            new Notification(title, {
                body: promotion.title,
                icon: promotion.imageUrl || undefined,
                tag: `promotion-${promotion.id}`
            });
        }
    }

    async function syncAll(): Promise<void> {
        await runLoading(async () => {
            const items = await notificationContainer.useCases.promo.getAll();
            update((state) => ({ ...state, items }));

            // Activar suscripción Pusher después de sincronizar
            await managePusherSubscription();
        });
    }

    /**
     * Limpia las suscripciones cuando el store se destruye
     */
    function cleanup(): void {
        if (pusherUnsubscribe) {
            pusherUnsubscribe();
            unsubscribePromotionUpdates();
            pusherUnsubscribe = null;
        }
    }

    const hasData = derived({ subscribe }, ($state) => $state.items.length > 0);
    const activePromotions = derived(
        { subscribe },
        ($state) => {
            const now = Date.now();
            return $state.items.filter(
                (p) => now >= p.validFromEpochMillis && now <= p.validUntilEpochMillis
            );
        }
    );

    return {
        subscribe,
        hasData,
        activePromotions,
        syncAll,
        cleanup
    };
}

export const promotionStore = createPromotionStore();