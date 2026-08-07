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

/**
 * Publica stock:changed.
 * Preferencia: Alset Pulse HTTP (API key) → evita secretos Pusher en el cliente.
 * Fallback: log best-effort si no hay endpoint.
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
    const base = ENV.pulseBaseUrl?.trim().replace(/\/$/, "");
    const apiKey = ENV.pulseApiKey?.trim();

    logStock("info", `publish start reason=${body.reason} channel=${channel} saleId=${body.saleId ?? "-"} ids=${productIds.join(",")}`);

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
                    `publish OK reason=${body.reason} ids=${productIds.join(",")} saleId=${body.saleId ?? "-"}`
                );
            }
            return;
        } catch (e) {
            logStock(
                "error",
                `publish failed: ${e instanceof Error ? e.message : String(e)}`
            );
            return;
        }
    }

    logStock(
        "warn",
        `publish skipped: sin VITE_ALSET_PULSE_BASE_URL / API_KEY — solo sync local. reason=${body.reason} ids=${productIds.join(",")}`
    );
}
