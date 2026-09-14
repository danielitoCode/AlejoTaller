import type { AuthPort } from "../domain/AuthPort";
import { Auth0AuthAdapter } from "../data/Auth0AuthAdapter";
import { ENV } from "../../../infrastructure/env";

export type AuthProviderId = "auth0" | "appwrite";

/**
 * Auth0 si VITE_AUTH_PROVIDER=auth0.
 * Si solo hay Turso y no se fijó provider, preferir auth0 (migración Core6).
 */
export function resolveAuthProvider(): AuthProviderId {
    const p = (ENV.authProvider ?? "").toLowerCase().trim();
    if (p === "auth0") return "auth0";
    if (p === "appwrite") return "appwrite";
    const data = String(ENV.dataProvider ?? "").toLowerCase().trim();
    if (data === "turso") return "auth0";
    return "appwrite";
}

export function createAuthPort(): AuthPort | null {
    if (resolveAuthProvider() !== "auth0") return null;
    return new Auth0AuthAdapter();
}

let cached: AuthPort | null | undefined;

export function getAuthPort(): AuthPort | null {
    if (cached === undefined) cached = createAuthPort();
    return cached;
}
