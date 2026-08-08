import type { RealtimeResponseEvent } from "appwrite";
import { client } from "../../di/appwrite.config";
import { ENV } from "../../env";
import { APPWRITE_COLLECTIONS } from "./public-data-contract";

/**
 * Canal primario de stock vía Appwrite Realtime.
 *
 * El payload del evento ES el documento post-cambio (fuente de verdad).
 * Señal al dominio:
 *   { productIds, action, snapshots[] }
 * Los clientes aplican snapshots al offline-first local — sin re-fetch.
 */

export type AppwriteProductChangeAction = "create" | "update" | "delete" | "unknown";

export interface AppwriteProductChangeSignal {
    productIds: string[];
    action: AppwriteProductChangeAction;
    /** Documentos Appwrite post-mutación (uno por productId, último gana). */
    snapshots: Record<string, unknown>[];
    timestamp: string;
}

export type AppwriteProductRealtimeHandler = (signal: AppwriteProductChangeSignal) => void;

export type AppwriteProductRealtimeUnsubscribe = () => void;

const LOG = "[appwrite-rt]";

let activeUnsub: AppwriteProductRealtimeUnsubscribe | null = null;
let activeHandler: AppwriteProductRealtimeHandler | null = null;

const DEBOUNCE_MS = 280;
/** id → último snapshot recibido en la ventana de debounce */
let pendingById = new Map<string, Record<string, unknown>>();
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
    const snapshots = [...pendingById.values()];
    const productIds = [...pendingById.keys()];
    const action = pendingAction;
    pendingById = new Map();
    pendingAction = "update";

    if (productIds.length === 0 || !activeHandler) return;

    const signal: AppwriteProductChangeSignal = {
        productIds,
        action,
        snapshots,
        timestamp: new Date().toISOString()
    };

    console.info(
        `${LOG} signal → dominio action=${action} count=${productIds.length} ids=${productIds.join(",")} (snapshots locales, sin re-fetch)`
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

function queueSnapshot(
    productId: string,
    action: AppwriteProductChangeAction,
    snapshot: Record<string, unknown>
): void {
    pendingById.set(productId, snapshot);
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
        console.warn(`${LOG} evento sin $id — ignorado`);
        return;
    }

    const existence = payload.existence ?? "?";
    const reserved = payload.reserved ?? "?";
    console.info(
        `${LOG} NOTIFICATION RECEIVED action=${action} productId=${productId} ` +
            `existence=${existence} reserved=${reserved} (aplicará snapshot local)`
    );

    queueSnapshot(productId, action, payload);
}

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

    console.info(`${LOG} subscribe channel=${channel} (snapshot → offline-first local)`);

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
            pendingById = new Map();
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

        console.info(`${LOG} listener activo → ApplyProductRealtimeSnapshots`);
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
