const STORAGE_KEY = "alejo-taller-web-pending-deeplink";

let memoryPendingHash = "";

function canUseStorage(): boolean {
    return typeof window !== "undefined" && !!window.sessionStorage;
}

export function rememberPendingDeepLink(hash: string): void {
    if (!hash.trim()) return;
    memoryPendingHash = hash;
    if (canUseStorage()) {
        window.sessionStorage.setItem(STORAGE_KEY, hash);
    }
}

export function peekPendingDeepLink(): string {
    if (canUseStorage()) {
        return window.sessionStorage.getItem(STORAGE_KEY) ?? memoryPendingHash;
    }
    return memoryPendingHash;
}

export function consumePendingDeepLink(): string {
    const value = peekPendingDeepLink();
    clearPendingDeepLink();
    return value;
}

export function clearPendingDeepLink(): void {
    memoryPendingHash = "";
    if (canUseStorage()) {
        window.sessionStorage.removeItem(STORAGE_KEY);
    }
}
