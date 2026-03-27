import Pusher, { type Channel } from "pusher-js";
import { ENV } from "../../env";

export type PulseUnsubscribe = () => void;

// Event payload types
export interface PromotionEventPayload {
    id: string;
    title: string;
    message: string;
    imageUrl?: string | null;
    oldPrice?: number | null;
    currentPrice?: number | null;
    validFromEpochMillis: number;
    validUntilEpochMillis: number;
}

export type PromotionEventHandler = (eventName: string, payload: PromotionEventPayload) => void;

let pusherSingleton: Pusher | null = null;
let saleVerificationSubscription: { channel: Channel; unsubscribe: PulseUnsubscribe } | null = null;
let promotionSubscription: { channel: Channel; unsubscribe: PulseUnsubscribe } | null = null;

function getPusher(): Pusher | null {
    if (!ENV.pusherKey || !ENV.pusherCluster) {
        console.warn('[Pusher] Missing configuration: pusherKey or pusherCluster');
        return null;
    }
    
    if (pusherSingleton) return pusherSingleton;
    
    try {
        pusherSingleton = new Pusher(ENV.pusherKey, {
            cluster: ENV.pusherCluster,
            forceTLS: true
        });
        console.log('[Pusher] Initialized successfully');
        
        // Manejo de cambios de conexión
        pusherSingleton.connection.bind('state_change', (states: any) => {
            const previous = states.previous;
            const current = states.current;
            
            if (current === 'failed' || current === 'disconnected') {
                console.error(`[Pusher] Connection failed: ${previous} -> ${current}`);
            } else if (current === 'connected') {
                console.log('[Pusher] Reconnected successfully');
            }
        });
        
        return pusherSingleton;
    } catch (error) {
        console.error('[Pusher] Initialization failed:', error);
        pusherSingleton = null;
        return null;
    }
}

export function subscribeSupportInbox(handler: (eventName: string, payload: unknown) => void): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) return () => {};

    const channelName = ENV.pusherSupportChannel || "support-inbox";
    const channel = pusher.subscribe(channelName);

    return subscribePulseChannelInternal(pusher, channelName, channel, ["support:new", "support:updated", "support:status"], handler);
}

export function subscribePulseRefresh(handler: (eventName: string, payload: unknown) => void): PulseUnsubscribe {
    const refreshEvents = [
        "refresh:all",
        "refresh:support",
        "refresh:sales",
        "all:refresh",
        "support:refresh",
        "sales:refresh"
    ];

    return subscribePulseChannelAll((eventName, payload) => {
        if (!refreshEvents.includes(String(eventName))) return;
        handler(eventName, payload);
    });
}

/**
 * Suscribirse a eventos de verificación de ventas (confirmación/rechazo)
 * Solo debe estar activo si el usuario tiene ventas en estado UNVERIFIED
 * @param userId - ID del usuario (cliente)
 * @param handler - Manejador de eventos
 */
export function subscribeSaleVerification(
    userId: string,
    handler: (eventName: string, payload: { saleId: string; decision: "confirmed" | "rejected"; timestamp: string; amount?: number; productCount?: number }) => void
): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher || !userId) return () => {};

    const channelName = `sale-verification-${userId}`;
    const channel = pusher.subscribe(channelName);

    const events = ["sale:confirmed", "sale:rejected"];
    for (const eventName of events) {
        channel.bind(eventName, (payload: unknown) => {
            handler(eventName, payload as any);
        });
    }

    saleVerificationSubscription = {
        channel,
        unsubscribe: () => {
            for (const eventName of events) channel.unbind(eventName);
            pusher.unsubscribe(channelName);
            saleVerificationSubscription = null;
        }
    };

    return saleVerificationSubscription.unsubscribe;
}

/**
 * Cerrar la suscripción actual a eventos de verificación de ventas
 */
export function unsubscribeSaleVerification(): void {
    if (saleVerificationSubscription) {
        saleVerificationSubscription.unsubscribe();
        saleVerificationSubscription = null;
    }
}

/**
 * Suscribirse a eventos de promociones en tiempo real
 * Escucha cambios: creación, actualización y eliminación de promociones
 * @param handler - Manejador de eventos
 */
export function subscribePromotionUpdates(handler: PromotionEventHandler): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) return () => {};

    const channelName = ENV.pusherPromoChannel || "promotions";
    const channel = pusher.subscribe(channelName);

    const events = ["promotion:created", "promotion:updated", "promotion:deleted"];

    for (const eventName of events) {
        channel.bind(eventName, (payload: unknown) => {
            try {
                handler(eventName, payload as PromotionEventPayload);
            } catch (error) {
                console.error(`[PromotionEvent] Error handling ${eventName}:`, error);
            }
        });
    }

    promotionSubscription = {
        channel,
        unsubscribe: () => {
            for (const eventName of events) channel.unbind(eventName);
            pusher.unsubscribe(channelName);
            promotionSubscription = null;
        }
    };

    return promotionSubscription.unsubscribe;
}

/**
 * Cerrar la suscripción actual a eventos de promociones
 */
export function unsubscribePromotionUpdates(): void {
    if (promotionSubscription) {
        promotionSubscription.unsubscribe();
        promotionSubscription = null;
    }
}

function subscribePulseChannelInternal(
    pusher: Pusher,
    channelName: string,
    channel: Channel,
    events: string[],
    handler: (eventName: string, payload: unknown) => void
): PulseUnsubscribe {
    for (const eventName of events) channel.bind(eventName, (payload: unknown) => handler(eventName, payload));

    return () => {
        for (const eventName of events) channel.unbind(eventName);
        pusher.unsubscribe(channelName);
    };
}

export function subscribePulseChannelAll(handler: (eventName: string, payload: unknown) => void): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) return () => {};

    const channelName = ENV.pusherSupportChannel || "support-inbox";
    const channel = pusher.subscribe(channelName);

    const globalHandler = (eventName: string, payload: unknown) => handler(eventName, payload);
    channel.bind_global(globalHandler);

    return () => {
        channel.unbind_global(globalHandler);
        pusher.unsubscribe(channelName);
    };
}
