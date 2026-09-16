import type { AuthPort } from "../domain/AuthPort";
import { Auth0AuthAdapter } from "../data/Auth0AuthAdapter";
import { ENV } from "../../../infrastructure/env";

export type AuthProviderId = "auth0" | "appwrite";

/**
 * Auth0 es el proveedor de autenticación de Core6.
 * Prioridad:
 * 1) VITE_AUTH_PROVIDER=auth0|appwrite
 * 2) Si hay domain+clientId Auth0 → auth0
 * 3) Si dataProvider=turso → auth0
 * 4) appwrite (legacy)
 */
export function resolveAuthProvider(): AuthProviderId {
    const p = (ENV.authProvider ?? "").toLowerCase().trim();
    if (p === "auth0") return "auth0";
    if (p === "appwrite") return "appwrite";
    const hasAuth0 =
        Boolean((ENV.auth0Domain ?? "").trim()) && Boolean((ENV.auth0ClientId ?? "").trim());
    if (hasAuth0) return "auth0";
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
