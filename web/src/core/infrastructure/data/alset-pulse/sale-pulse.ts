import { ENV } from "../../env";
import { triggerPusherEvent } from "./pusher-trigger";
import Pusher, { type Channel } from "pusher-js";

export type SalePulseDecision = "confirmed" | "rejected";

export interface SalePulsePayload {
    saleId: string;
    userId?: string | null;
    decision?: SalePulseDecision;
    verified?: string;
    productIds?: string[];
    timestamp: string;
}

export function getSalesChannelName(): string {
    return (
        ENV.pusherSalesChannel?.trim() ||
        (import.meta.env.VITE_PUSHER_SALES_CHANNEL as string | undefined)?.trim() ||
        "sale-updates"
    );
}

let pusherSingleton: Pusher | null = null;

function getPusher(): Pusher | null {
    if (!ENV.pusherKey || !ENV.pusherCluster) return null;
    if (pusherSingleton) return pusherSingleton;
    try {
        pusherSingleton = new Pusher(ENV.pusherKey, {
            cluster: ENV.pusherCluster,
            forceTLS: true,
        });
        return pusherSingleton;
    } catch (e) {
        console.error("[Pusher] sales init failed", e);
        return null;
    }
}

export async function publishSaleEvent(
    event: "sale:created" | "sale:updated" | "sale:confirmed" | "sale:rejected",
    payload: Omit<SalePulsePayload, "timestamp"> & { timestamp?: string },
): Promise<void> {
    const body: SalePulsePayload = {
        saleId: payload.saleId,
        userId: payload.userId ?? null,
        decision: payload.decision,
        verified: payload.verified,
        productIds: payload.productIds ?? [],
        timestamp: payload.timestamp || new Date().toISOString(),
    };
    const channel = getSalesChannelName();
    const result = await triggerPusherEvent(channel, event, body);
    if (result.ok) console.info(`[sale-rt] publish ${event} via=${result.via} ch=${channel}`);
    else console.warn(`[sale-rt] publish ${event} omitido: ${result.reason}`);

    if (body.userId && (event === "sale:confirmed" || event === "sale:rejected")) {
        await triggerPusherEvent(`sale-verification-${body.userId}`, event, {
            saleId: body.saleId,
            userId: body.userId,
            decision: event === "sale:confirmed" ? "confirmed" : "rejected",
            productIds: body.productIds ?? [],
            timestamp: body.timestamp,
        });
    }
}

export type SalePulseUnsubscribe = () => void;

export function subscribeSaleUpdates(
    handler: (eventName: string, payload: SalePulsePayload) => void,
): SalePulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) return () => {};
    const channelName = getSalesChannelName();
    const channel: Channel = pusher.subscribe(channelName);
    const events = ["sale:created", "sale:updated", "sale:confirmed", "sale:rejected"];
    console.info(`[sale-rt] subscribe channel=${channelName}`);
    for (const eventName of events) {
        channel.bind(eventName, (payload: unknown) => {
            const p = (payload ?? {}) as Partial<SalePulsePayload>;
            handler(eventName, {
                saleId: String(p.saleId ?? ""),
                userId: p.userId ?? null,
                decision: p.decision,
                verified: p.verified,
                productIds: Array.isArray(p.productIds) ? p.productIds : [],
                timestamp: typeof p.timestamp === "string" ? p.timestamp : new Date().toISOString(),
            });
        });
    }
    return () => {
        for (const eventName of events) channel.unbind(eventName);
        pusher.unsubscribe(channelName);
    };
}

export function subscribeSaleVerification(
    userId: string,
    handler: (eventName: string, payload: SalePulsePayload) => void,
): SalePulseUnsubscribe {
    const uid = String(userId || "").trim();
    if (!uid) return () => {};
    const pusher = getPusher();
    if (!pusher) return () => {};
    const channelName = `sale-verification-${uid}`;
    const channel: Channel = pusher.subscribe(channelName);
    const events = ["sale:confirmed", "sale:rejected"] as const;
    console.info(`[sale-rt] subscribe verification channel=${channelName}`);
    for (const eventName of events) {
        channel.bind(eventName, (payload: unknown) => {
            const p = (payload ?? {}) as Partial<SalePulsePayload>;
            handler(eventName, {
                saleId: String(p.saleId ?? ""),
                userId: p.userId ?? uid,
                decision:
                    p.decision ??
                    (eventName === "sale:confirmed" ? "confirmed" : "rejected"),
                verified: p.verified,
                productIds: Array.isArray(p.productIds) ? p.productIds : [],
                timestamp:
                    typeof p.timestamp === "string"
                        ? p.timestamp
                        : new Date().toISOString(),
            });
        });
    }
    return () => {
        for (const eventName of events) channel.unbind(eventName);
        pusher.unsubscribe(channelName);
    };
}
