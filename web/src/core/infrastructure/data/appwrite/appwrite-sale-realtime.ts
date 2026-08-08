import type { RealtimeResponseEvent } from "appwrite";
import { client } from "../../di/appwrite.config";
import { ENV } from "../../env";

/**
 * Canal de verificación de ventas vía Appwrite Realtime.
 * Sustituye Pusher `sale-verification-{userId}` + publish con secret.
 *
 * El operador solo hace updateDocument (buy_state). Appwrite notifica a
 * suscriptores con permiso de lectura sobre ese documento — sin Pulse ni secret.
 *
 * Canal: databases.{DATABASE_ID}.collections.sale.documents
 */

export type AppwriteSaleChangeAction = "create" | "update" | "delete" | "unknown";

export interface AppwriteSaleChangeSignal {
    saleId: string;
    action: AppwriteSaleChangeAction;
    /** Documento Appwrite post-mutación */
    snapshot: Record<string, unknown>;
    timestamp: string;
}

export type AppwriteSaleRealtimeHandler = (signal: AppwriteSaleChangeSignal) => void;

export type AppwriteSaleRealtimeUnsubscribe = () => void;

const LOG = "[appwrite-sale-rt]";
const COLLECTION_ID = "sale";

let activeUnsub: AppwriteSaleRealtimeUnsubscribe | null = null;
let activeHandler: AppwriteSaleRealtimeHandler | null = null;

function buildSaleDocumentsChannel(): string | null {
    const databaseId = ENV.databaseId?.trim();
    if (!databaseId) {
        console.warn(`${LOG} omitido: falta VITE_APPWRITE_DATABASE_ID`);
        return null;
    }
    return `databases.${databaseId}.collections.${COLLECTION_ID}.documents`;
}

function classifyAction(events: string[]): AppwriteSaleChangeAction {
    const joined = events.join(" ").toLowerCase();
    if (joined.includes(".create")) return "create";
    if (joined.includes(".delete")) return "delete";
    if (joined.includes(".update")) return "update";
    return "unknown";
}

function onRealtimeMessage(response: RealtimeResponseEvent<Record<string, unknown>>): void {
    const events = Array.isArray(response.events) ? response.events : [];
    const action = classifyAction(events);
    const payload = (response.payload ?? {}) as Record<string, unknown>;
    const saleId = String(payload.$id ?? payload.id ?? "").trim();

    if (!saleId) {
        console.warn(`${LOG} evento sin $id — ignorado`);
        return;
    }

    const buyState = payload.buy_state ?? payload.verified ?? "?";
    const userId = payload.user_id ?? "?";
    console.info(
        `${LOG} NOTIFICATION RECEIVED action=${action} saleId=${saleId} ` +
            `buy_state=${buyState} user_id=${userId}`
    );

    if (!activeHandler) return;

    const signal: AppwriteSaleChangeSignal = {
        saleId,
        action,
        snapshot: payload,
        timestamp: new Date().toISOString()
    };

    try {
        activeHandler(signal);
    } catch (err) {
        console.error(
            `${LOG} handler falló: ${err instanceof Error ? err.message : String(err)}`,
            err
        );
    }
}

/**
 * Suscribe a documentos de la colección sale.
 * El filtrado por userId / UNVERIFIED se hace en el store (dominio de presentación).
 */
export function startAppwriteSaleRealtime(
    onChange?: AppwriteSaleRealtimeHandler
): AppwriteSaleRealtimeUnsubscribe {
    if (onChange) {
        activeHandler = onChange;
    }

    if (activeUnsub) {
        console.info(`${LOG} ya activo — handler actualizado, skip re-subscribe`);
        return activeUnsub;
    }

    if (!ENV.appwriteEndpoint || !ENV.appwriteProjectId) {
        console.warn(`${LOG} omitido: Appwrite client no configurado`);
        return () => {};
    }

    const channel = buildSaleDocumentsChannel();
    if (!channel) return () => {};

    console.info(`${LOG} subscribe channel=${channel} (reemplazo sale-verification Pusher)`);

    try {
        const unsubscribe = client.subscribe(channel, (response) => {
            try {
                onRealtimeMessage(response as RealtimeResponseEvent<Record<string, unknown>>);
            } catch (err) {
                console.error(
                    `${LOG} parse error: ${err instanceof Error ? err.message : String(err)}`,
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
                activeHandler = null;
            }
        };

        console.info(`${LOG} listener activo (sin secret Pusher)`);
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

export function stopAppwriteSaleRealtime(): void {
    if (activeUnsub) {
        activeUnsub();
        activeUnsub = null;
    }
    activeHandler = null;
}

export function isAppwriteSaleRealtimeActive(): boolean {
    return activeUnsub != null;
}
