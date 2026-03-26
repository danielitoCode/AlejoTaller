import Pusher, { type Channel } from "pusher-js";
import { ENV } from "../../env";

export type PulseUnsubscribe = () => void;

let pusherSingleton: Pusher | null = null;
let saleVerificationSubscription: { channel: Channel; unsubscribe: PulseUnsubscribe } | null = null;

function getPusher(): Pusher | null {
    if (!ENV.pusherKey || !ENV.pusherCluster) return null;
    if (pusherSingleton) return pusherSingleton;
    pusherSingleton = new Pusher(ENV.pusherKey, {
        cluster: ENV.pusherCluster,
        forceTLS: true
    });
    return pusherSingleton;
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
