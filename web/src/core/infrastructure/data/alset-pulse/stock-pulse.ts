import { triggerPusherEvent } from "./pusher-trigger";

export type StockChangeReason = "hold" | "release" | "consume";

export interface StockChangedPayload {
    productIds: string[];
    reason: StockChangeReason;
    saleId?: string | null;
    timestamp: string;
}

export const STOCK_CHANGED_EVENT = "alejo:stock-changed";
export const STOCK_BROADCAST_NAME = "alejo-stock-updates";

export function getStockChannelName(): string {
    return (
        (import.meta.env.VITE_PUSHER_STOCK_CHANNEL as string | undefined)?.trim() ||
        "stock-updates"
    );
}

function logStock(level: "info" | "warn" | "error", message: string, extra?: unknown): void {
    const prefix = "[stock-rt]";
    if (level === "info") {
        if (extra !== undefined) console.info(prefix, message, extra);
        else console.info(prefix, message);
    } else if (level === "warn") {
        if (extra !== undefined) console.warn(prefix, message, extra);
        else console.warn(prefix, message);
    } else {
        if (extra !== undefined) console.error(prefix, message, extra);
        else console.error(prefix, message);
    }
}

function emitLocalStockChanged(body: StockChangedPayload): void {
    if (typeof window !== "undefined") {
        try {
            window.dispatchEvent(new CustomEvent(STOCK_CHANGED_EVENT, { detail: body }));
            logStock("info", `local CustomEvent reason=${body.reason} ids=${body.productIds.join(",")}`);
        } catch (e) {
            logStock("warn", `CustomEvent falló: ${e instanceof Error ? e.message : String(e)}`);
        }
    }
    if (typeof BroadcastChannel !== "undefined") {
        try {
            const bc = new BroadcastChannel(STOCK_BROADCAST_NAME);
            bc.postMessage({ type: "stock:changed", data: body });
            bc.close();
        } catch (e) {
            logStock("warn", `BroadcastChannel falló: ${e instanceof Error ? e.message : String(e)}`);
        }
    }
}

export function parseStockChangedPayload(payload: unknown): StockChangedPayload | null {
    if (!payload || typeof payload !== "object") return null;
    const any = payload as Record<string, unknown>;
    const rawIds = any.productIds ?? any.product_ids;
    const productIds = Array.isArray(rawIds)
        ? rawIds.filter((id): id is string => typeof id === "string" && id.length > 0)
        : [];
    if (productIds.length === 0) return null;
    return {
        productIds,
        reason: (any.reason as StockChangedPayload["reason"]) || "hold",
        saleId: (any.saleId as string | null | undefined) ?? null,
        timestamp: typeof any.timestamp === "string" ? any.timestamp : new Date().toISOString(),
    };
}

export function subscribeStockChanged(handler: (body: StockChangedPayload) => void): () => void {
    const onEvent = (ev: Event) => {
        const parsed = parseStockChangedPayload((ev as CustomEvent).detail);
        if (parsed) handler(parsed);
    };
    let bc: BroadcastChannel | null = null;
    if (typeof window !== "undefined") window.addEventListener(STOCK_CHANGED_EVENT, onEvent);
    if (typeof BroadcastChannel !== "undefined") {
        try {
            bc = new BroadcastChannel(STOCK_BROADCAST_NAME);
            bc.onmessage = (msg) => {
                const data = msg?.data?.data ?? msg?.data;
                const parsed = parseStockChangedPayload(data);
                if (parsed) handler(parsed);
            };
        } catch {
            bc = null;
        }
    }
    return () => {
        if (typeof window !== "undefined") window.removeEventListener(STOCK_CHANGED_EVENT, onEvent);
        bc?.close();
    };
}

export async function publishStockChanged(payload: StockChangedPayload): Promise<void> {
    const productIds = [...new Set(payload.productIds.filter(Boolean))];
    if (productIds.length === 0) {
        logStock("warn", "publish omitido: productIds vacío");
        return;
    }
    const body: StockChangedPayload = {
        productIds,
        reason: payload.reason,
        saleId: payload.saleId ?? null,
        timestamp: payload.timestamp || new Date().toISOString(),
    };
    const channel = getStockChannelName();
    logStock("info", `publish start reason=${body.reason} channel=${channel} ids=${productIds.join(",")}`);
    emitLocalStockChanged(body);
    const result = await triggerPusherEvent(channel, "stock:changed", body);
    if (result.ok) {
        logStock("info", `publish OK via=${result.via} reason=${body.reason}`);
    } else {
        logStock("warn", `publish remoto omitido: ${result.reason}`);
    }
}
