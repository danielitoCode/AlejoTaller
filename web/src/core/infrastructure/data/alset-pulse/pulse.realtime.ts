import Pusher, { type Channel } from "pusher-js";
import { ENV } from "../../env";
import { getStockChannelName, type StockChangedPayload } from "./stock-pulse";

export type PulseUnsubscribe = () => void;

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
let stockSubscription: { channel: Channel; unsubscribe: PulseUnsubscribe } | null = null;

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

export function subscribeSaleVerification(
    userId: string,
    handler: (eventName: string, payload: { saleId: string; decision: "confirmed" | "rejected"; timestamp: string; amount?: number; productCount?: number }) => void
): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher || !userId) return () => {};

    if (saleVerificationSubscription) {
        saleVerificationSubscription.unsubscribe();
    }

    const channelName = `sale-verification-${userId}`;
    const channel = pusher.subscribe(channelName);
    console.log(`[Pusher] Sale verification subscribe requested -> channel=${channelName} userId=${userId}`);

    const events = ["sale:confirmed", "sale:rejected"];
    for (const eventName of events) {
        channel.bind(eventName, (payload: unknown) => {
            console.log(`[Pusher] Sale verification event received -> channel=${channelName} event=${eventName}`, payload);
            handler(eventName, payload as any);
        });
    }

    saleVerificationSubscription = {
        channel,
        unsubscribe: () => {
            console.log(`[Pusher] Sale verification unsubscribe -> channel=${channelName}`);
            for (const eventName of events) channel.unbind(eventName);
            pusher.unsubscribe(channelName);
            saleVerificationSubscription = null;
        }
    };

    return saleVerificationSubscription.unsubscribe;
}

export function unsubscribeSaleVerification(): void {
    if (saleVerificationSubscription) {
        saleVerificationSubscription.unsubscribe();
        saleVerificationSubscription = null;
    }
}

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

export function unsubscribePromotionUpdates(): void {
    if (promotionSubscription) {
        promotionSubscription.unsubscribe();
        promotionSubscription = null;
    }
}

/**
 * Escucha stock:changed → productIds a refrescar desde Appwrite (offline-first parcial).
 */
export function subscribeStockUpdates(
    handler: (payload: StockChangedPayload) => void
): PulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) return () => {};

    if (stockSubscription) {
        stockSubscription.unsubscribe();
    }

    const channelName = getStockChannelName();
    const channel = pusher.subscribe(channelName);
    const eventName = "stock:changed";

    console.log(`[Pusher] Stock updates subscribe -> channel=${channelName}`);

    channel.bind(eventName, (payload: unknown) => {
        const data = payload as Partial<StockChangedPayload>;
        const productIds = Array.isArray(data?.productIds)
            ? data.productIds.filter((id): id is string => typeof id === "string" && id.length > 0)
            : [];
        if (productIds.length === 0) {
            console.warn("[Pusher] stock:changed sin productIds", payload);
            return;
        }
        handler({
            productIds,
            reason: (data.reason as StockChangedPayload["reason"]) || "hold",
            saleId: data.saleId ?? null,
            timestamp: typeof data.timestamp === "string" ? data.timestamp : new Date().toISOString()
        });
    });

    stockSubscription = {
        channel,
        unsubscribe: () => {
            console.log(`[Pusher] Stock updates unsubscribe -> channel=${channelName}`);
            channel.unbind(eventName);
            pusher.unsubscribe(channelName);
            stockSubscription = null;
        }
    };

    return stockSubscription.unsubscribe;
}

export function unsubscribeStockUpdates(): void {
    if (stockSubscription) {
        stockSubscription.unsubscribe();
        stockSubscription = null;
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
