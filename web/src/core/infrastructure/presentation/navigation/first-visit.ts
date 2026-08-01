/**
 * Persists whether the user has already completed (or skipped) the Welcome flow.
 * Used so returning visitors go straight to products, while first-time visitors
 * still see the short onboarding screen.
 *
 * Deeplinks always bypass Welcome regardless of this flag (handled in Splash).
 */

const STORAGE_KEY = "alejo_has_visited";

export function hasCompletedWelcome(): boolean {
    if (typeof window === "undefined") return false;
    try {
        return window.localStorage.getItem(STORAGE_KEY) === "1";
    } catch {
        // private mode / blocked storage → treat as first visit
        return false;
    }
}

export function markWelcomeCompleted(): void {
    if (typeof window === "undefined") return;
    try {
        window.localStorage.setItem(STORAGE_KEY, "1");
    } catch {
        // ignore quota / private mode errors
    }
}

/** Clears the flag (useful for tests or "reset onboarding"). */
export function clearWelcomeCompleted(): void {
    if (typeof window === "undefined") return;
    try {
        window.localStorage.removeItem(STORAGE_KEY);
    } catch {
        // ignore
    }
}
