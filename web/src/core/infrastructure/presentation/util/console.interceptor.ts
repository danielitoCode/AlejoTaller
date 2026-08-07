import { logStore} from "../viewmodel/log.store";

/** Evita TypeError: Cannot convert object to primitive value (Appwrite docs, etc.). */
function safeArgToString(value: unknown): string {
    if (value == null) return String(value)
    const t = typeof value
    if (t === "string") return value as string
    if (t === "number" || t === "boolean" || t === "bigint") return String(value)
    if (t === "symbol") return (value as symbol).toString()
    if (value instanceof Error) {
        const anyErr = value as Error & { code?: unknown; type?: unknown }
        const extra = [
            anyErr.code != null ? `code=${anyErr.code}` : null,
            typeof anyErr.type === "string" ? `type=${anyErr.type}` : null
        ].filter(Boolean).join(" ")
        return extra ? `${value.message} ${extra}` : value.message || value.name
    }
    try {
        return JSON.stringify(value)
    } catch {
        try {
            return Object.prototype.toString.call(value)
        } catch {
            return "[unprintable]"
        }
    }
}

export function initGlobalLogger() {

    // Console interception
    ["log", "info", "warn", "error"].forEach((level) => {
        const original = (console[level as keyof Console] as Function).bind(console);

        // Asignamos mediante any para no romper el tipo global Console, manteniendo el comportamiento.
        (console as any)[level] = (...args: any[]) => {
            try {
                original(...args);
            } catch {
                // Si el original falla, aún intentamos seguir.
            }

            try {
                const stack = level === "error" ? new Error().stack : undefined;
                const message = args.map(safeArgToString).join(" ");
                logStore.add(message, level as any, stack);
            } catch {
                // Nunca romper el flujo de la app por el interceptor de logs.
            }
        };
    });

    // Runtime errors
    window.addEventListener("error", (event) => {
        logStore.add(
            event.message,
            "error",
            event.error?.stack
        );
    });

    // Unhandled promises
    window.addEventListener("unhandledrejection", (event) => {
        logStore.add(
            `Unhandled Promise: ${safeArgToString(event.reason)}`,
            "error",
            event.reason?.stack
        );
    });
}
