import { ENV } from "../../env";

/**
 * Contrato de señal de stock vía Pusher / Alset Pulse + fan-out local.
 * Canal Pusher: stock-updates (o VITE_PUSHER_STOCK_CHANNEL)
 * Evento: stock:changed
 *
 * Sin backend Pulse el cliente NO puede disparar Pusher (falta secret).
 * Por eso siempre emitimos:
 * 1) CustomEvent en window (mismo tab)
 * 2) BroadcastChannel (otras pestañas del mismo origen)
 * y si hay Pulse HTTP, también al servidor.
 */
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

/** Emite en el mismo tab + otras pestañas (siempre). */
function emitLocalStockChanged(body: StockChangedPayload): void {
    if (typeof window !== "undefined") {
        try {
            window.dispatchEvent(
                new CustomEvent(STOCK_CHANGED_EVENT, { detail: body })
            );
            logStock(
                "info",
                `local CustomEvent ${STOCK_CHANGED_EVENT} reason=${body.reason} ids=${body.productIds.join(",")}`
            );
        } catch (e) {
            logStock("warn", `CustomEvent falló: ${e instanceof Error ? e.message : String(e)}`);
        }
    }

    if (typeof BroadcastChannel !== "undefined") {
        try {
            const bc = new BroadcastChannel(STOCK_BROADCAST_NAME);
            bc.postMessage({ type: "stock:changed", data: body });
            bc.close();
            logStock(
                "info",
                `BroadcastChannel ${STOCK_BROADCAST_NAME} reason=${body.reason} ids=${body.productIds.join(",")}`
            );
        } catch (e) {
            logStock(
                "warn",
                `BroadcastChannel falló: ${e instanceof Error ? e.message : String(e)}`
            );
        }
    }
}

/**
 * Publica stock:changed (local siempre + Pulse HTTP si está configurado).
 */
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
        timestamp: payload.timestamp || new Date().toISOString()
    };

    const channel = getStockChannelName();
    logStock(
        "info",
        `publish start reason=${body.reason} channel=${channel} saleId=${body.saleId ?? "-"} ids=${productIds.join(",")}`
    );

    // 1) Siempre notificar localmente (mismo origen)
    emitLocalStockChanged(body);

    // 2) Backend Pulse → Pusher (otros dispositivos / usuarios)
    const base = ENV.pulseBaseUrl?.trim().replace(/\/$/, "");
    const apiKey = ENV.pulseApiKey?.trim();

    if (base && apiKey) {
        const url = `${base}/pulse/stock`;
        try {
            logStock("info", `publish HTTP → ${url}`);
            const res = await fetch(url, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${apiKey}`,
                    "X-Api-Key": apiKey
                },
                body: JSON.stringify({
                    channel,
                    event: "stock:changed",
                    data: body
                })
            });
            if (!res.ok) {
                const text = await res.text().catch(() => "");
                logStock(
                    "warn",
                    `publish HTTP ${res.status} saleId=${body.saleId ?? "-"} body=${text.slice(0, 200)}`
                );
            } else {
                logStock(
                    "info",
                    `publish OK (Pulse→Pusher) reason=${body.reason} ids=${productIds.join(",")} saleId=${body.saleId ?? "-"}`
                );
            }
        } catch (e) {
            logStock(
                "error",
                `publish HTTP failed: ${e instanceof Error ? e.message : String(e)}`
            );
        }
        return;
    }

    logStock(
        "warn",
        "Pulse HTTP no configurado (VITE_ALSET_PULSE_*). " +
            "Otros dispositivos no recibirán stock:changed por Pusher; " +
            "solo este origen (CustomEvent + BroadcastChannel)."
    );
}
