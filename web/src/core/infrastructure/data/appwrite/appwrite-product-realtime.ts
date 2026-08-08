import type { RealtimeResponseEvent } from "appwrite";
import { client } from "../../di/appwrite.config";
import { ENV } from "../../env";
import { APPWRITE_COLLECTIONS } from "./public-data-contract";

/**
 * Canal ALTERNATIVO a Pusher `stock-updates` / `stock:changed`.
 *
 * Usa Appwrite Realtime sobre la colección de productos.
 * Core 1 (probe): SOLO registra en logs que la notificación llegó.
 * NO dispara casos de uso de dominio ni actualiza el offline-first todavía.
 *
 * Canal:
 *   databases.{DATABASE_ID}.collections.{product}.documents
 *
 * Eventos típicos:
 *   databases.*.collections.*.documents.*.create|update|delete
 *   (incluye mutaciones por increment/decrement attribute)
 */

export type AppwriteProductRealtimeUnsubscribe = () => void;

const LOG = "[appwrite-rt]";

let activeUnsub: AppwriteProductRealtimeUnsubscribe | null = null;

function buildProductDocumentsChannel(): string | null {
    const databaseId = ENV.databaseId?.trim();
    if (!databaseId) {
        console.warn(`${LOG} omitido: falta VITE_APPWRITE_DATABASE_ID`);
        return null;
    }
    const collectionId = APPWRITE_COLLECTIONS.product;
    // Sintaxis oficial sin corchetes (no usar databases.[id].collections.[id]...)
    return `databases.${databaseId}.collections.${collectionId}.documents`;
}

function classifyAction(events: string[]): "create" | "update" | "delete" | "unknown" {
    const joined = events.join(" ").toLowerCase();
    if (joined.includes(".create")) return "create";
    if (joined.includes(".delete")) return "delete";
    if (joined.includes(".update")) return "update";
    return "unknown";
}

function readStockFields(payload: Record<string, unknown>): {
    id: string;
    existence: number | null;
    reserved: number | null;
    available: number | null;
    name: string | null;
} {
    const id = String(payload.$id ?? payload.id ?? "");
    const existenceRaw = payload.existence ?? payload.status ?? payload.Estado ?? null;
    const reservedRaw = payload.reserved ?? payload.reservado ?? null;
    const existence =
        existenceRaw == null ? null : Math.max(0, Math.floor(Number(existenceRaw)));
    const reserved =
        reservedRaw == null ? null : Math.max(0, Math.floor(Number(reservedRaw)));
    const available =
        existence != null && reserved != null
            ? Math.max(0, existence - reserved)
            : null;
    const name = typeof payload.name === "string" ? payload.name : null;
    return { id, existence, reserved, available, name };
}

/**
 * Handler de probe: solo logs. Paridad conceptual con stock:changed de Pusher.
 */
function onRealtimeMessage(response: RealtimeResponseEvent<Record<string, unknown>>): void {
    const events = Array.isArray(response.events) ? response.events : [];
    const channels = Array.isArray(response.channels) ? response.channels : [];
    const action = classifyAction(events);
    const payload = (response.payload ?? {}) as Record<string, unknown>;
    const stock = readStockFields(payload);

    // Equivalente liviano al payload stock:changed (solo para inspección en consola)
    const probeLikeStockChanged = {
        source: "appwrite-realtime",
        action,
        productIds: stock.id ? [stock.id] : [],
        reason: action === "update" ? "stock-or-doc-update" : action,
        timestamp: new Date().toISOString(),
        existence: stock.existence,
        reserved: stock.reserved,
        available: stock.available
    };

    console.info(
        `${LOG} NOTIFICATION RECEIVED action=${action} ` +
            `productId=${stock.id || "-"} name=${stock.name ?? "-"} ` +
            `existence=${stock.existence ?? "?"} reserved=${stock.reserved ?? "?"} ` +
            `available=${stock.available ?? "?"}`
    );
    console.info(`${LOG} events=`, events);
    console.info(`${LOG} channels=`, channels);
    console.info(`${LOG} probe-payload (paridad conceptual stock:changed)=`, probeLikeStockChanged);
    console.info(`${LOG} raw payload keys=`, Object.keys(payload));
}

/**
 * Arranca la suscripción (idempotente).
 * Devuelve unsubscribe; también se puede llamar stopAppwriteProductRealtime().
 */
export function startAppwriteProductRealtime(): AppwriteProductRealtimeUnsubscribe {
    if (activeUnsub) {
        console.info(`${LOG} ya activo — skip re-subscribe`);
        return activeUnsub;
    }

    if (!ENV.appwriteEndpoint || !ENV.appwriteProjectId) {
        console.warn(`${LOG} omitido: Appwrite client no configurado`);
        return () => {};
    }

    const channel = buildProductDocumentsChannel();
    if (!channel) {
        return () => {};
    }

    console.info(`${LOG} subscribe channel=${channel}`);

    try {
        // appwrite SDK (v16+): Client#subscribe(channels, callback) → () => void
        const unsubscribe = client.subscribe(channel, (response) => {
            try {
                onRealtimeMessage(response as RealtimeResponseEvent<Record<string, unknown>>);
            } catch (err) {
                console.error(
                    `${LOG} handler error: ${err instanceof Error ? err.message : String(err)}`,
                    err
                );
            }
        });

        activeUnsub = () => {
            try {
                unsubscribe();
                console.info(`${LOG} unsubscribed channel=${channel}`);
            } catch (err) {
                console.warn(
                    `${LOG} unsubscribe error: ${err instanceof Error ? err.message : String(err)}`
                );
            } finally {
                activeUnsub = null;
            }
        };

        console.info(`${LOG} listener activo (probe-only, sin dominio)`);
        return activeUnsub;
    } catch (err) {
        console.error(
            `${LOG} subscribe FAILED: ${err instanceof Error ? err.message : String(err)}`,
            err
        );
        activeUnsub = null;
        return () => {};
    }
}

export function stopAppwriteProductRealtime(): void {
    if (activeUnsub) {
        activeUnsub();
        activeUnsub = null;
    }
}

export function isAppwriteProductRealtimeActive(): boolean {
    return activeUnsub != null;
}
