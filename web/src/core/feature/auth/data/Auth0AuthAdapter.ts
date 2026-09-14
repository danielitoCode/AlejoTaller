import {
    createAuth0Client,
    type Auth0Client,
    type RedirectLoginOptions,
} from "@auth0/auth0-spa-js";
import type { AuthPort } from "../domain/AuthPort";
import { AUTH_ROLES_CLAIM, type AuthSession } from "../domain/entity/AuthSession";
import { ENV } from "../../../infrastructure/env";
import { logAuth0 } from "../../../infrastructure/presentation/navigation/debug-logger";

/**
 * Un solo rol (app_metadata.role → claim string o array[0]).
 * Nunca app_metadata en el cliente.
 * @see .roadmap/Core6/AUTH0_ROLES_ACTION.md
 */
function parseRoleClaim(raw: unknown): string[] {
    if (typeof raw === "string" && raw.trim()) return [raw.trim()];
    if (Array.isArray(raw) && raw.length > 0) {
        const first = String(raw[0] ?? "").trim();
        return first ? [first] : [];
    }
    return [];
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
    try {
        const parts = token.split(".");
        if (parts.length < 2) return null;
        const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
        const pad = b64.length % 4 === 0 ? "" : "=".repeat(4 - (b64.length % 4));
        const json = atob(b64 + pad);
        const payload = JSON.parse(json) as Record<string, unknown>;
        return payload && typeof payload === "object" ? payload : null;
    } catch {
        return null;
    }
}

function resolveRolesFromTokens(
    accessToken: string,
    idTokenUser: Record<string, unknown> | undefined,
): string[] {
    const fromAccess = decodeJwtPayload(accessToken);
    if (fromAccess && AUTH_ROLES_CLAIM in fromAccess) {
        const roles = parseRoleClaim(fromAccess[AUTH_ROLES_CLAIM]);
        if (roles.length > 0) return roles;
    }
    if (idTokenUser && AUTH_ROLES_CLAIM in idTokenUser) {
        const roles = parseRoleClaim(idTokenUser[AUTH_ROLES_CLAIM]);
        if (roles.length > 0) return roles;
    }
    return [];
}

function mask(value: string | null | undefined, keep = 6): string {
    if (!value) return "—";
    if (value.length <= keep) return "***";
    return `${value.slice(0, keep)}…`;
}

function requireConfig() {
    const domain = (ENV.auth0Domain ?? "").trim();
    const clientId = (ENV.auth0ClientId ?? "").trim();
    if (!domain || !clientId) {
        logAuth0("error", "faltan VITE_AUTH0_DOMAIN / VITE_AUTH0_CLIENT_ID");
        throw new Error("Auth0: faltan VITE_AUTH0_DOMAIN / VITE_AUTH0_CLIENT_ID");
    }
    const origin =
        typeof window !== "undefined" ? window.location.origin : "http://localhost:5174";
    return {
        domain,
        clientId,
        audience: (ENV.auth0Audience ?? "").trim() || undefined,
        redirectUri: (ENV.auth0RedirectUri ?? "").trim() || origin,
        logoutReturnTo: (ENV.auth0LogoutReturnTo ?? "").trim() || origin,
    };
}

export class Auth0AuthAdapter implements AuthPort {
    private client: Auth0Client | null = null;
    private initPromise: Promise<void> | null = null;

    async init(): Promise<void> {
        if (this.client) {
            logAuth0("log", "init: client ya listo (cache)");
            return;
        }
        if (this.initPromise) return this.initPromise;
        this.initPromise = (async () => {
            const cfg = requireConfig();
            logAuth0(
                "info",
                `init domain=${cfg.domain} clientId=${mask(cfg.clientId)} redirect=${cfg.redirectUri}`,
            );
            this.client = await createAuth0Client({
                domain: cfg.domain,
                clientId: cfg.clientId,
                authorizationParams: {
                    redirect_uri: cfg.redirectUri,
                    ...(cfg.audience ? { audience: cfg.audience } : {}),
                },
                cacheLocation: "localstorage",
                useRefreshTokens: true,
            });
            logAuth0("info", "client listo (localstorage + refreshTokens)");
        })();
        try {
            await this.initPromise;
        } catch (e) {
            logAuth0("error", `init falló: ${e instanceof Error ? e.message : String(e)}`, e);
            throw e;
        } finally {
            this.initPromise = null;
        }
    }

    private async ensure(): Promise<Auth0Client> {
        await this.init();
        if (!this.client) throw new Error("Auth0 client no inicializado");
        return this.client;
    }

    async loginWithRedirect(appState?: { returnTo?: string; connection?: string }): Promise<void> {
        const client = await this.ensure();
        const cfg = requireConfig();
        const connection = appState?.connection;
        logAuth0(
            "info",
            `loginWithRedirect connection=${connection ?? "universal"} returnTo=${appState?.returnTo ?? cfg.redirectUri}`,
        );
        const options: RedirectLoginOptions = {
            authorizationParams: {
                redirect_uri: cfg.redirectUri,
                ...(cfg.audience ? { audience: cfg.audience } : {}),
                ...(connection ? { connection } : {}),
            },
            appState: appState?.returnTo ? { returnTo: appState.returnTo } : undefined,
        };
        await client.loginWithRedirect(options);
    }

    async handleRedirectCallback(): Promise<void> {
        if (typeof window === "undefined") return;
        const q = window.location.search;
        if (q.includes("error=")) {
            const params = new URLSearchParams(q);
            const msg = `${params.get("error")} — ${params.get("error_description") ?? ""}`;
            logAuth0("error", `callback error: ${msg}`);
            window.history.replaceState({}, document.title, window.location.pathname);
            throw new Error(`Auth0: ${msg}`);
        }
        if (!q.includes("code=") || !q.includes("state=")) {
            logAuth0("log", "callback: sin code/state (no-op)");
            return;
        }
        logAuth0("info", "handleRedirectCallback: procesando code/state…");
        const client = await this.ensure();
        await client.handleRedirectCallback();
        window.history.replaceState({}, document.title, window.location.pathname);
        logAuth0("info", "callback OK — query limpia");
    }

    async logout(): Promise<void> {
        const client = await this.ensure();
        const cfg = requireConfig();
        logAuth0("info", `logout → returnTo=${cfg.logoutReturnTo}`);
        await client.logout({ logoutParams: { returnTo: cfg.logoutReturnTo } });
    }

    async isAuthenticated(): Promise<boolean> {
        const ok = await (await this.ensure()).isAuthenticated();
        logAuth0("log", `isAuthenticated=${ok}`);
        return ok;
    }

    async getAccessToken(): Promise<string | null> {
        const client = await this.ensure();
        if (!(await client.isAuthenticated())) return null;
        try {
            const token = await client.getTokenSilently();
            logAuth0("log", `getAccessToken ok len=${token?.length ?? 0}`);
            return token ?? null;
        } catch (e) {
            logAuth0("warn", `getAccessToken falló: ${e instanceof Error ? e.message : String(e)}`, e);
            return null;
        }
    }

    async getSubject(): Promise<string | null> {
        return (await this.getSession())?.subject ?? null;
    }

    async getSession(): Promise<AuthSession | null> {
        const client = await this.ensure();
        if (!(await client.isAuthenticated())) {
            logAuth0("log", "getSession: no autenticado");
            return null;
        }
        const user = await client.getUser();
        if (!user?.sub) {
            logAuth0("warn", "getSession: user sin sub");
            return null;
        }
        let accessToken = "";
        try {
            const token = await client.getTokenSilently();
            if (!token) {
                logAuth0("warn", "getSession: token vacío");
                return null;
            }
            accessToken = token;
        } catch (e) {
            logAuth0("warn", `getSession token: ${e instanceof Error ? e.message : String(e)}`, e);
            return null;
        }

        const roles = resolveRolesFromTokens(accessToken, user as Record<string, unknown>);
        logAuth0(
            "info",
            `session sub=${mask(user.sub, 12)} email=${user.email ?? "—"} role=${roles[0] ?? "(none)"} claim=${AUTH_ROLES_CLAIM}`,
        );
        return {
            subject: user.sub,
            accessToken,
            email: user.email ?? null,
            displayName: user.name ?? user.nickname ?? null,
            roles,
            pictureUrl: user.picture ?? null,
        };
    }
}
