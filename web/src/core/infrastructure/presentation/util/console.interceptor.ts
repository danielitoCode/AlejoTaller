import { logStore} from "../viewmodel/log.store";

export function initGlobalLogger() {

    // Console interception
    ["log", "info", "warn", "error"].forEach(level => {
        const original = console[level as keyof Console] as Function;

        console[level as keyof Console] = (...args: any[]) => {
            original(...args);

            const stack =
                level === "error"
                    ? new Error().stack
                    : undefined;

            logStore.add(args.join(" "), level as any, stack);
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