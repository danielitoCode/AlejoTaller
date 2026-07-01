import { parseDeepLinkHash } from "./deeplink";

let _capturedHash: string | null = null;
let _capturedParsed: ReturnType<typeof parseDeepLinkHash> = null;

/**
 * Captures the current URL hash immediately, before any Svelte component
 * or router can touch or clear it. This MUST be called before Svelte mounts.
 */
export function captureInitialDeepLink(): void {
    if (typeof window === "undefined") return;
    const rawHash = window.location.hash;
    if (rawHash && rawHash.length > 1) {
        _capturedHash = rawHash;
        _capturedParsed = parseDeepLinkHash(rawHash);
    }
}

/** Returns the raw captured hash (e.g. "#/home/product-detail?productId=abc") */
export function getCapturedHash(): string | null {
    return _capturedHash ? _capturedHash : null;
}

/** Returns the parsed form of the captured hash, or null if none */
export function getCapturedParsedDeeplink() {
    return _capturedParsed;
}

/** True if we captured a deeplink on cold boot */
export function hasCapturedDeeplink(): boolean {
    return _capturedHash !== null;
}

/** True if the captured deeplink is a product detail link */
export function isProductDeeplinkCaptured(): boolean {
    return !!_capturedParsed && _capturedParsed.top === "home" &&
        (_capturedParsed.nested === "product-detail" || !!_capturedParsed.args?.productId);
}