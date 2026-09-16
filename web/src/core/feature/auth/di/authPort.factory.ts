import type { AuthPort } from "../domain/AuthPort";
import { Auth0AuthAdapter } from "../data/Auth0AuthAdapter";
import { ENV } from "../../../infrastructure/env";

export type AuthProviderId = "auth0" | "appwrite";

/**
 * Core6: Auth0 es el único proveedor de autenticación operativo.
 * - Si hay VITE_AUTH0_DOMAIN + VITE_AUTH0_CLIENT_ID → siempre auth0
 *   (aunque alguien ponga VITE_AUTH_PROVIDER=appwrite por error).
 * - Solo queda "appwrite" si no hay credenciales Auth0 (rollback legacy).
 */
export function resolveAuthProvider(): AuthProviderId {
    const hasAuth0 =
        Boolean((ENV.auth0Domain ?? "").trim()) && Boolean((ENV.auth0ClientId ?? "").trim());
    if (hasAuth0) return "auth0";

    const p = (ENV.authProvider ?? "").toLowerCase().trim();
    if (p === "auth0") return "auth0";

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

/** Solo tests: limpia el singleton del port. */
export function __resetAuthPortForTests(): void {
    cached = undefined;
}
