import { logStore } from "../viewmodel/log.store";

let sessionCounter = 0;

export function logNavSessionStart(
    url: string,
    type: "deeplink" | "normal",
    details: Record<string, any>
) {
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
    action: "continue" | "auto-guest" | "redirect-login" | "redirect-welcome"
) {
    const icon =
        action === "continue"      ? "✅" :
        action === "auto-guest"    ? "👤" :
        action === "redirect-login"? "🔐" : "🏠";

    logStore.add(
        `  [AuthCheck] ${icon} hasSession=${hasSession} isGuest=${isGuest} → ${action.toUpperCase()}`,
        "info"
    );
}

export function logNavRoute(target: string, args?: Record<string, any>) {
    const argsStr = args ? JSON.stringify(args, null, 2) : "void";
    logStore.add(`  [Route] → "${target}"  args=${argsStr}`, "info");
}

export function logProductFlow(productId: string, step: "resolve-start" | "resolve-success" | "resolve-fail") {
    const icon = step === "resolve-success" ? "✅" : step === "resolve-fail" ? "❌" : "🔍";
    logStore.add(`  [ProductFlow] ${icon} step=${step} id=${productId}`, step === "resolve-fail" ? "warn" : "info");
}

export function logNavError(msg: string, err?: any) {
    const stack = err instanceof Error ? err.stack : undefined;
    logStore.add(`  [Error] ${msg}${err ? " :: " + String(err) : ""}`, "error", stack);
}