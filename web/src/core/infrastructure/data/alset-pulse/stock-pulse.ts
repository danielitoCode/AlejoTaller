import { ENV } from "../../env";

/**
 * Contrato de señal de stock vía Pusher / Alset Pulse.
 * Canal: stock-updates (o VITE_PUSHER_STOCK_CHANNEL)
 * Evento: stock:changed
 */
export type StockChangeReason = "hold" | "release" | "consume";

export interface StockChangedPayload {
    productIds: string[];
    reason: StockChangeReason;
    saleId?: string | null;
    timestamp: string;
}

export function getStockChannelName(): string {
    return (
        (import.meta.env.VITE_PUSHER_STOCK_CHANNEL as string | undefined)?.trim() ||
        "stock-updates"
    );
}

/**
 * Publica stock:changed.
 * Preferencia: Alset Pulse HTTP (API key) → evita secretos Pusher en el cliente.
 * Fallback: log best-effort si no hay endpoint.
 */
export async function publishStockChanged(payload: StockChangedPayload): Promise<void> {
    const productIds = [...new Set(payload.productIds.filter(Boolean))];
    if (productIds.length === 0) return;

    const body: StockChangedPayload = {
        productIds,
        reason: payload.reason,
        saleId: payload.saleId ?? null,
        timestamp: payload.timestamp || new Date().toISOString()
    };

    const base = ENV.pulseBaseUrl?.trim().replace(/\/$/, "");
    const apiKey = ENV.pulseApiKey?.trim();

    if (base && apiKey) {
        try {
            const res = await fetch(`${base}/pulse/stock`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${apiKey}`,
                    "X-Api-Key": apiKey
                },
                body: JSON.stringify({
                    channel: getStockChannelName(),
                    event: "stock:changed",
                    data: body
                })
            });
            if (!res.ok) {
                console.warn(
                    `[stock-pulse] publish HTTP ${res.status} saleId=${body.saleId ?? "-"}`
                );
            } else if (import.meta.env.DEV) {
                console.info(
                    `[stock-pulse] published reason=${body.reason} ids=${productIds.join(",")}`
                );
            }
            return;
        } catch (e) {
            console.warn(
                `[stock-pulse] publish failed: ${e instanceof Error ? e.message : String(e)}`
            );
        }
    }

    // Sin backend de trigger: los clientes solo se actualizan por sync propio.
    if (import.meta.env.DEV) {
        console.info(
            `[stock-pulse] (no pulse endpoint) local-only reason=${body.reason} ids=${productIds.join(",")}`
        );
    }
}
