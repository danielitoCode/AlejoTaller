import type { AuthPort } from "../domain/AuthPort";
import { Auth0AuthAdapter } from "../data/Auth0AuthAdapter";
import { ENV } from "../../../infrastructure/env";

export type AuthProviderId = "auth0" | "appwrite";

export function resolveAuthProvider(): AuthProviderId {
    const p = (ENV.authProvider ?? "appwrite").toLowerCase().trim();
    return p === "auth0" ? "auth0" : "appwrite";
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
