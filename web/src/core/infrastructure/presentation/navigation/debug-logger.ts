import { logStore } from "../viewmodel/log.store";

let sessionCounter = 0;

function isDev(): boolean {
    return import.meta.env.DEV === true;
}

export function logNavSessionStart(
    url: string,
    type: "deeplink" | "normal",
    details: Record<string, any>
) {
    if (!isDev()) return;
    sessionCounter++;
    const banner = type === "deeplink"
        ? "╔══════════════════════════════════════╗\n║       🔗 DEEPLINK DETECTADO          ║\n╚══════════════════════════════════════╝"
        : "╔══════════════════════════════════════╗\n║      🚀 NAVEGACION NORMAL            ║\n╚══════════════════════════════════════╝";

    logStore.add(
        `${banner}\n` +
        `  Sess #${sessionCounter}\n` +
        `  URL : ${url}\n` +
        `  Tipo: ${type.toUpperCase()}\n` +
        `  Det : ${JSON.stringify(details, null, 2)}\n` +
        `──────────────────────────────────────`,
        "info"
    );
}

export function logNavAuthCheck(
    hasSession: boolean,
    isGuest: boolean,
    action: string
) {
    if (!isDev()) return;
    const icon =
        action === "continue" || action.startsWith("auth-") ? "✅" :
        action.includes("guest") ? "👤" :
        action.includes("login") ? "🔐" : "🏠";

    logStore.add(
        `  [AuthCheck] ${icon} hasSession=${hasSession} isGuest=${isGuest} → ${action.toUpperCase()}`,
        "info"
    );
}

export function logNavRoute(target: string, args?: Record<string, any>) {
    if (!isDev()) return;
    const argsStr = args ? JSON.stringify(args, null, 2) : "void";
    logStore.add(`  [Route] → "${target}"  args=${argsStr}`, "info");
}

export function logProductFlow(productId: string, step: "resolve-start" | "resolve-success" | "resolve-fail") {
    if (!isDev()) return;
    const icon = step === "resolve-success" ? "✅" : step === "resolve-fail" ? "❌" : "🔍";
    logStore.add(`  [ProductFlow] ${icon} step=${step} id=${productId}`, step === "resolve-fail" ? "warn" : "info");
}

export function logNavError(msg: string, err?: any) {
    if (!isDev()) return;
    const stack = err instanceof Error ? err.stack : undefined;
    logStore.add(`  [Error] ${msg}${err ? " :: " + String(err) : ""}`, "error", stack);
}

/** Logs Auth0 (solo local / import.meta.env.DEV). */
export function logAuth0(
    level: "log" | "info" | "warn" | "error",
    message: string,
    err?: unknown
) {
    if (!isDev()) return;
    const stack = err instanceof Error ? err.stack : undefined;
    logStore.add(`  [Auth0] ${message}`, level, stack);
}
