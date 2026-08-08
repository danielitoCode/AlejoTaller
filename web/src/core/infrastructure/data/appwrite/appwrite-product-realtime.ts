import type { RealtimeResponseEvent } from "appwrite";
import { client } from "../../di/appwrite.config";
import { ENV } from "../../env";
import { APPWRITE_COLLECTIONS } from "./public-data-contract";

/**
 * Canal primario de stock (sustituye Pusher stock-updates en Core 1).
 *
 * Contrato de señal (invalidación), no mutación:
 *   { productIds: string[], action: create|update|delete }
 *
 * La UI / casos de uso deben re-leer Appwrite (refreshByIds).
 * El documento completo en el payload de Appwrite se usa solo para logs;
 * no se aplica a cache desde aquí (fuente de verdad = getById remoto).
 *
 * Canal: databases.{DATABASE_ID}.collections.product.documents
 */

export type AppwriteProductChangeAction = "create" | "update" | "delete" | "unknown";

export interface AppwriteProductChangeSignal {
    productIds: string[];
    action: AppwriteProductChangeAction;
    /** ISO timestamp local de recepción */
    timestamp: string;
}

export type AppwriteProductRealtimeHandler = (signal: AppwriteProductChangeSignal) => void;

export type AppwriteProductRealtimeUnsubscribe = () => void;

const LOG = "[appwrite-rt]";

let activeUnsub: AppwriteProductRealtimeUnsubscribe | null = null;
let activeHandler: AppwriteProductRealtimeHandler | null = null;

/** Agrupa ids que llegan en ráfaga (p.ej. multi-línea soft-hold). */
const DEBOUNCE_MS = 280;
let pendingIds = new Set<string>();
let pendingAction: AppwriteProductChangeAction = "update";
let debounceTimer: ReturnType<typeof setTimeout> | null = null;

function buildProductDocumentsChannel(): string | null {
    const databaseId = ENV.databaseId?.trim();
    if (!databaseId) {
        console.warn(`${LOG} omitido: falta VITE_APPWRITE_DATABASE_ID`);
        return null;
    }
    return `databases.${databaseId}.collections.${APPWRITE_COLLECTIONS.product}.documents`;
}

function classifyAction(events: string[]): AppwriteProductChangeAction {
    const joined = events.join(" ").toLowerCase();
    if (joined.includes(".create")) return "create";
    if (joined.includes(".delete")) return "delete";
    if (joined.includes(".update")) return "update";
    return "unknown";
}

function extractProductId(payload: Record<string, unknown>): string | null {
    const id = String(payload.$id ?? payload.id ?? "").trim();
    return id || null;
}

function flushPending(): void {
    debounceTimer = null;
    const ids = [...pendingIds];
    const action = pendingAction;
    pendingIds = new Set();
    pendingAction = "update";

    if (ids.length === 0 || !activeHandler) return;

    const signal: AppwriteProductChangeSignal = {
        productIds: ids,
        action,
        timestamp: new Date().toISOString()
    };

    console.info(
        `${LOG} signal → dominio action=${action} count=${ids.length} ids=${ids.join(",")}`
    );
    try {
        activeHandler(signal);
    } catch (err) {
        console.error(
            `${LOG} handler dominio falló: ${err instanceof Error ? err.message : String(err)}`,
            err
        );
    }
}

function queueSignal(productId: string, action: AppwriteProductChangeAction): void {
    pendingIds.add(productId);
    // prioriza delete si aparece en el batch
    if (action === "delete" || pendingAction === "unknown") {
        pendingAction = action;
    } else if (pendingAction !== "delete") {
        pendingAction = action;
    }

    if (debounceTimer) clearTimeout(debounceTimer);
    debounceTimer = setTimeout(flushPending, DEBOUNCE_MS);
}

function onRealtimeMessage(response: RealtimeResponseEvent<Record<string, unknown>>): void {
    const events = Array.isArray(response.events) ? response.events : [];
    const action = classifyAction(events);
    const payload = (response.payload ?? {}) as Record<string, unknown>;
    const productId = extractProductId(payload);

    if (!productId) {
        console.warn(`${LOG} evento sin $id — ignorado events=${events.slice(0, 3).join(",")}`);
        return;
    }

    // Log compacto: no volcamos el documento completo ni la lista enorme de events
    const existence = payload.existence ?? payload.status ?? "?";
    const reserved = payload.reserved ?? "?";
    console.info(
        `${LOG} NOTIFICATION RECEIVED action=${action} productId=${productId} ` +
            `existence=${existence} reserved=${reserved} (payload solo para log; refresh irá a Appwrite)`
    );

    queueSignal(productId, action);
}

/**
 * Suscribe al canal de productos.
 * @param onChange callback de dominio (p.ej. refreshByIds). Idempotente: reasigna handler.
 */
export function startAppwriteProductRealtime(
    onChange?: AppwriteProductRealtimeHandler
): AppwriteProductRealtimeUnsubscribe {
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

    const channel = buildProductDocumentsChannel();
    if (!channel) return () => {};

    console.info(`${LOG} subscribe channel=${channel} (canal primario stock)`);

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
            if (debounceTimer) {
                clearTimeout(debounceTimer);
                debounceTimer = null;
            }
            pendingIds = new Set();
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

        console.info(`${LOG} listener activo → dominio vía refreshByIds`);
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
    activeHandler = null;
}

export function isAppwriteProductRealtimeActive(): boolean {
    return activeUnsub != null;
}
