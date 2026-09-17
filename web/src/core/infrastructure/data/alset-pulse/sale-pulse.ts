import { ENV } from "../../env";
import { triggerPusherEvent } from "./pusher-trigger";
import Pusher, { type Channel } from "pusher-js";

/**
 * Canal ventas: sale-updates (o VITE_PUSHER_SALES_CHANNEL)
 * Eventos: sale:created | sale:updated | sale:confirmed | sale:rejected
 * + canal por usuario: sale-verification-{userId}
 */

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
        console.info(
            `[Pusher] sales client key=${ENV.pusherKey.slice(0, 6)}… cluster=${ENV.pusherCluster}`,
        );
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
    if (result.ok) {
        console.info(`[sale-rt] publish ${event} via=${result.via} saleId=${body.saleId}`);
    } else {
        console.warn(`[sale-rt] publish ${event} omitido: ${result.reason}`);
    }

    if (body.userId && (event === "sale:confirmed" || event === "sale:rejected")) {
        const userChannel = `sale-verification-${body.userId}`;
        const decision: SalePulseDecision =
            event === "sale:confirmed" ? "confirmed" : "rejected";
        const r2 = await triggerPusherEvent(userChannel, event, {
            saleId: body.saleId,
            decision,
            timestamp: body.timestamp,
        });
        if (r2.ok) {
            console.info(
                `[sale-rt] user notify ${event} via=${r2.via} user=${body.userId.slice(0, 12)}…`,
            );
        }
    }
}

export type SalePulseUnsubscribe = () => void;

export function subscribeSaleUpdates(
    handler: (eventName: string, payload: SalePulsePayload) => void,
): SalePulseUnsubscribe {
    const pusher = getPusher();
    if (!pusher) {
        console.warn("[sale-rt] subscribe omitido: Pusher no configurado");
        return () => {};
    }
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
                timestamp:
                    typeof p.timestamp === "string" ? p.timestamp : new Date().toISOString(),
            });
        });
    }

    return () => {
        for (const eventName of events) channel.unbind(eventName);
        pusher.unsubscribe(channelName);
    };
}
