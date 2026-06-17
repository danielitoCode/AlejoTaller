import { logStore} from "../viewmodel/log.store";

export function initGlobalLogger() {

    // Console interception
    ["log", "info", "warn", "error"].forEach((level) => {
        const original = (console[level as keyof Console] as Function).bind(console);

        // Asignamos mediante any para no romper el tipo global Console, manteniendo el comportamiento.
        (console as any)[level] = (...args: any[]) => {
            try {
                original(...args);
            } catch (err) {
                // Si el original falla, aún intentamos seguir.
            }

            const stack = level === "error" ? new Error().stack : undefined;

            logStore.add(args.map((a) => String(a)).join(" "), level as any, stack);
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
            `Unhandled Promise: ${event.reason}`,
            "error",
            event.reason?.stack
        );
    });
}