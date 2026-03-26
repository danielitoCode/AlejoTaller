import {derived, writable} from "svelte/store";
import {ENV} from "../../env";

export type InfraProviderKey = "appwrite" | "render" | "cloudflare";

export type InfraProviderStatus = {
    ok: boolean;
    label: string;
    details?: string;
    latencyMs?: number;
    lastUpdatedAtIso: string;
};

export type InfraStatusSnapshot = {
    appwrite?: InfraProviderStatus;
    render?: InfraProviderStatus;
    cloudflare?: InfraProviderStatus;
    links?: {
        appwrite?: string;
        render?: string;
        cloudflare?: string;
    };
};

type InfraStatusState = {
    loading: boolean;
    error: string | null;
    data: InfraStatusSnapshot | null;
    history: Record<InfraProviderKey, number[]>;
};

const initialState: InfraStatusState = {
    loading: false,
    error: null,
    data: null,
    history: { appwrite: [], render: [], cloudflare: [] }
};

function normalizeError(error: unknown): string {
    return error instanceof Error ? error.message : "Unexpected error";
}

function clampLatency(value: unknown): number | null {
    const n = typeof value === "number" ? value : Number(value);
    if (!Number.isFinite(n) || n <= 0) return null;
    return Math.min(10_000, Math.max(1, Math.round(n)));
}

function pushHistory(list: number[], value: number, max = 28): number[] {
    const next = [...list, value];
    if (next.length <= max) return next;
    return next.slice(next.length - max);
}

export function createInfraStatusStore() {
    const { subscribe, update } = writable<InfraStatusState>(initialState);

    async function sync(): Promise<void> {
        const url = (ENV.infraStatusUrl || "").trim();
        if (!url) return;

        update((s) => ({ ...s, loading: true, error: null }));
        const started = performance.now();

        try {
            const res = await fetch(url, { method: "GET" });
            const text = await res.text();
            let payload: any = {};
            try {
                payload = JSON.parse(text || "{}");
            } catch {
                throw new Error("Infra status: respuesta inválida.");
            }

            if (!res.ok) {
                const msg = typeof payload?.error === "string" ? payload.error : "No se pudo cargar el estado.";
                throw new Error(msg);
            }

            const nowIso = new Date().toISOString();
            const data: InfraStatusSnapshot = {
                appwrite: payload?.appwrite
                    ? {
                          ok: Boolean(payload.appwrite.ok),
                          label: String(payload.appwrite.label ?? "Appwrite"),
                          details: typeof payload.appwrite.details === "string" ? payload.appwrite.details : undefined,
                          latencyMs: clampLatency(payload.appwrite.latencyMs) ?? undefined,
                          lastUpdatedAtIso: String(payload.appwrite.lastUpdatedAtIso ?? nowIso)
                      }
                    : undefined,
                render: payload?.render
                    ? {
                          ok: Boolean(payload.render.ok),
                          label: String(payload.render.label ?? "Render"),
                          details: typeof payload.render.details === "string" ? payload.render.details : undefined,
                          latencyMs: clampLatency(payload.render.latencyMs) ?? undefined,
                          lastUpdatedAtIso: String(payload.render.lastUpdatedAtIso ?? nowIso)
                      }
                    : undefined,
                cloudflare: payload?.cloudflare
                    ? {
                          ok: Boolean(payload.cloudflare.ok),
                          label: String(payload.cloudflare.label ?? "Cloudflare"),
                          details: typeof payload.cloudflare.details === "string" ? payload.cloudflare.details : undefined,
                          latencyMs: clampLatency(payload.cloudflare.latencyMs) ?? undefined,
                          lastUpdatedAtIso: String(payload.cloudflare.lastUpdatedAtIso ?? nowIso)
                      }
                    : undefined,
                links: {
                    appwrite:
                        String(payload?.links?.appwrite || ENV.appwriteConsoleUrl || "").trim() || undefined,
                    render: String(payload?.links?.render || ENV.renderConsoleUrl || "").trim() || undefined,
                    cloudflare:
                        String(payload?.links?.cloudflare || ENV.cloudflareConsoleUrl || "").trim() || undefined
                }
            };

            const totalLatency = Math.round(performance.now() - started);

            update((s) => {
                const nextHistory = { ...s.history };
                const a = clampLatency(data.appwrite?.latencyMs) ?? totalLatency;
                const r = clampLatency(data.render?.latencyMs) ?? totalLatency;
                const c = clampLatency(data.cloudflare?.latencyMs) ?? totalLatency;
                nextHistory.appwrite = pushHistory(nextHistory.appwrite, a);
                nextHistory.render = pushHistory(nextHistory.render, r);
                nextHistory.cloudflare = pushHistory(nextHistory.cloudflare, c);
                return { ...s, data, history: nextHistory };
            });
        } catch (e) {
            update((s) => ({ ...s, error: normalizeError(e) }));
        } finally {
            update((s) => ({ ...s, loading: false }));
        }
    }

    function clearError(): void {
        update((s) => ({ ...s, error: null }));
    }

    function reset(): void {
        update(() => initialState);
    }

    const isConfigured = derived({ subscribe }, ($s) => Boolean((ENV.infraStatusUrl || "").trim().length));

    return { subscribe, sync, clearError, reset, isConfigured };
}

export const infraStatusStore = createInfraStatusStore();

