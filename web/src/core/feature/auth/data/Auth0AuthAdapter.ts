import {
    createAuth0Client,
    type Auth0Client,
    type RedirectLoginOptions,
} from "@auth0/auth0-spa-js";
import type { AuthPort } from "../domain/AuthPort";
import { AUTH_ROLES_CLAIM, type AuthSession } from "../domain/entity/AuthSession";
import { ENV } from "../../../infrastructure/env";

function parseRoles(claims: Record<string, unknown> | undefined): string[] {
    if (!claims) return [];
    const raw = claims[AUTH_ROLES_CLAIM] ?? claims["roles"];
    if (Array.isArray(raw)) return raw.map(String);
    if (typeof raw === "string") return raw.split(/[\s,]+/).filter(Boolean);
    return [];
}

function requireConfig() {
    const domain = (ENV.auth0Domain ?? "").trim();
    const clientId = (ENV.auth0ClientId ?? "").trim();
    if (!domain || !clientId) {
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
        if (this.client) return;
        if (this.initPromise) return this.initPromise;
        this.initPromise = (async () => {
            const cfg = requireConfig();
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
        })();
        try {
            await this.initPromise;
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
        const options: RedirectLoginOptions = {
            authorizationParams: {
                redirect_uri: cfg.redirectUri,
                ...(cfg.audience ? { audience: cfg.audience } : {}),
                ...(appState?.connection ? { connection: appState.connection } : {}),
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
            window.history.replaceState({}, document.title, window.location.pathname);
            throw new Error(`Auth0: ${params.get("error")} — ${params.get("error_description") ?? ""}`);
        }
        if (!q.includes("code=") || !q.includes("state=")) return;
        const client = await this.ensure();
        await client.handleRedirectCallback();
        window.history.replaceState({}, document.title, window.location.pathname);
    }

    async logout(): Promise<void> {
        const client = await this.ensure();
        const cfg = requireConfig();
        await client.logout({ logoutParams: { returnTo: cfg.logoutReturnTo } });
    }

    async isAuthenticated(): Promise<boolean> {
        return (await this.ensure()).isAuthenticated();
    }

    async getAccessToken(): Promise<string | null> {
        const client = await this.ensure();
        if (!(await client.isAuthenticated())) return null;
        try {
            return (await client.getTokenSilently()) ?? null;
        } catch {
            return null;
        }
    }

    async getSubject(): Promise<string | null> {
        return (await this.getSession())?.subject ?? null;
    }

    async getSession(): Promise<AuthSession | null> {
        const client = await this.ensure();
        if (!(await client.isAuthenticated())) return null;
        const user = await client.getUser();
        if (!user?.sub) return null;
        let accessToken = "";
        try {
            const token = await client.getTokenSilently();
            if (!token) return null;
            accessToken = token;
        } catch {
            return null;
        }
        return {
            subject: user.sub,
            accessToken,
            email: user.email ?? null,
            displayName: user.name ?? user.nickname ?? null,
            roles: parseRoles(user as Record<string, unknown>),
            pictureUrl: user.picture ?? null,
        };
    }
}
