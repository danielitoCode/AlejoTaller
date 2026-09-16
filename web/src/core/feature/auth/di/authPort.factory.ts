import type { AuthPort } from "../domain/AuthPort";
import { Auth0AuthAdapter } from "../data/Auth0AuthAdapter";
import { ClerkAuthAdapter } from "../data/ClerkAuthAdapter";
import { ENV } from "../../../infrastructure/env";

export type AuthProviderId = "clerk" | "auth0" | "appwrite";

/**
 * Core6 IdP:
 * 1) VITE_AUTH_PROVIDER=clerk|auth0|appwrite
 * 2) Si hay VITE_CLERK_PUBLISHABLE_KEY → clerk (Auth0 bloquea Cuba)
 * 3) Si hay Auth0 domain+clientId → auth0
 * 4) appwrite legacy
 */
export function resolveAuthProvider(): AuthProviderId {
    const p = (ENV.authProvider ?? "").toLowerCase().trim();
    if (p === "clerk") return "clerk";
    if (p === "auth0") return "auth0";
    if (p === "appwrite") return "appwrite";

    const hasClerk = Boolean((ENV.clerkPublishableKey ?? "").trim());
    if (hasClerk) return "clerk";

    const hasAuth0 =
        Boolean((ENV.auth0Domain ?? "").trim()) && Boolean((ENV.auth0ClientId ?? "").trim());
    if (hasAuth0) return "auth0";

    const data = String(ENV.dataProvider ?? "").toLowerCase().trim();
    if (data === "turso") return "clerk";

    return "appwrite";
}

/** Auth externo (Clerk o Auth0) — no Appwrite Account. */
export function isExternalAuthProvider(): boolean {
    const id = resolveAuthProvider();
    return id === "clerk" || id === "auth0";
}

export function createAuthPort(): AuthPort | null {
    const id = resolveAuthProvider();
    if (id === "clerk") return new ClerkAuthAdapter();
    if (id === "auth0") return new Auth0AuthAdapter();
    return null;
}

let cached: AuthPort | null | undefined;

export function getAuthPort(): AuthPort | null {
    if (cached === undefined) cached = createAuthPort();
    return cached;
}

export function __resetAuthPortForTests(): void {
    cached = undefined;
}
