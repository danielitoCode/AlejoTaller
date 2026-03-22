import { ENV } from "../../env";

type PulseFetchOptions = {
    method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
    path: string;
    body?: unknown;
};

function joinUrl(baseUrl: string, path: string): string {
    const base = baseUrl.endsWith("/") ? baseUrl.slice(0, -1) : baseUrl;
    const p = path.startsWith("/") ? path : `/${path}`;
    return `${base}${p}`;
}

function getBaseUrl(): string {
    const url = ENV.pulseBaseUrl;
    if (!url) throw new Error("Falta configurar VITE_ALSET_PULSE_BASE_URL");
    return url;
}

export async function pulseFetchJson<T>(opts: PulseFetchOptions): Promise<T> {
    const url = joinUrl(getBaseUrl(), opts.path);

    const headers: Record<string, string> = {
        Accept: "application/json"
    };

    if (ENV.pulseApiKey) headers["Authorization"] = `Bearer ${ENV.pulseApiKey}`;

    const method = opts.method ?? "GET";
    const init: RequestInit = { method, headers };

    if (opts.body !== undefined) {
        headers["Content-Type"] = "application/json";
        init.body = JSON.stringify(opts.body);
    }

    const res = await fetch(url, init);
    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`Alset Pulse error ${res.status}: ${text || res.statusText}`);
    }

    return (await res.json()) as T;
}

