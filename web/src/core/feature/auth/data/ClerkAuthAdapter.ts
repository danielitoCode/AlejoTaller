import { Clerk } from "@clerk/clerk-js";
import type { AuthPort, AuthLoginOptions } from "../domain/AuthPort";
import type { AuthSession } from "../domain/entity/AuthSession";
import { ENV } from "../../../infrastructure/env";
import { logAuth0 as logAuth } from "../../../infrastructure/presentation/navigation/debug-logger";

function mask(value: string | null | undefined, keep = 6): string {
    if (!value) return "—";
    if (value.length <= keep) return "***";
    return `${value.slice(0, keep)}…`;
}

function parseRole(raw: unknown): string[] {
    if (typeof raw === "string" && raw.trim()) return [raw.trim().toLowerCase()];
    if (Array.isArray(raw) && raw.length > 0) {
        const first = String(raw[0] ?? "").trim().toLowerCase();
        return first ? [first] : [];
    }
    return [];
}

/** Lee role desde publicMetadata o claims del session token. */
function resolveRoles(
    user: {
        publicMetadata?: Record<string, unknown> | null;
    },
    sessionClaims?: Record<string, unknown> | null,
): string[] {
    const fromMeta = parseRole(user.publicMetadata?.role);
    if (fromMeta.length) return fromMeta;
    if (sessionClaims) {
        const fromClaim = parseRole(sessionClaims.role ?? sessionClaims.metadata);
        if (fromClaim.length) return fromClaim;
        const meta = sessionClaims.metadata;
        if (meta && typeof meta === "object" && !Array.isArray(meta)) {
            const nested = parseRole((meta as Record<string, unknown>).role);
            if (nested.length) return nested;
        }
    }
    // Cliente B2C por defecto (registro Google/email sin metadata)
    return ["user"];
}

export class ClerkAuthAdapter implements AuthPort {
    private clerk: Clerk | null = null;
    private initPromise: Promise<void> | null = null;

    async init(): Promise<void> {
        if (this.clerk) {
            logAuth("log", "Clerk init: client ya listo");
            return;
        }
        if (this.initPromise) return this.initPromise;

        this.initPromise = (async () => {
            const key = (ENV.clerkPublishableKey ?? "").trim();
            if (!key) {
                logAuth("error", "falta VITE_CLERK_PUBLISHABLE_KEY");
                throw new Error("Clerk: falta VITE_CLERK_PUBLISHABLE_KEY");
            }
            logAuth("info", `Clerk init pk=${mask(key, 10)}`);
            const clerk = new Clerk(key);
            const origin =
                typeof window !== "undefined" ? window.location.origin : "http://localhost:5174";
            await clerk.load({
                signInForceRedirectUrl: origin,
                signUpForceRedirectUrl: origin,
                afterSignOutUrl: origin,
            });
            this.clerk = clerk;
            logAuth("info", "Clerk client listo");
        })();

        try {
            await this.initPromise;
        } catch (e) {
            logAuth("error", `Clerk init falló: ${e instanceof Error ? e.message : String(e)}`, e);
            throw e;
        } finally {
            this.initPromise = null;
        }
    }

    private async ensure(): Promise<Clerk> {
        await this.init();
        if (!this.clerk) throw new Error("Clerk no inicializado");
        return this.clerk;
    }

    async loginWithRedirect(appState?: AuthLoginOptions): Promise<void> {
        const clerk = await this.ensure();
        const returnTo =
            appState?.returnTo ||
            (typeof window !== "undefined" ? window.location.origin : "http://localhost:5174");

        const isSignup = appState?.screenHint === "signup";
        const isGoogle = (appState?.connection ?? "").toLowerCase().includes("google");

        logAuth(
            "info",
            `Clerk login redirect mode=${isSignup ? "signup" : "signin"} google=${isGoogle} returnTo=${returnTo}`,
        );

        if (isSignup) {
            await clerk.redirectToSignUp({
                signUpForceRedirectUrl: returnTo,
                signInForceRedirectUrl: returnTo,
            });
            return;
        }

        await clerk.redirectToSignIn({
            signInForceRedirectUrl: returnTo,
            signUpForceRedirectUrl: returnTo,
        });
    }

    async handleRedirectCallback(): Promise<void> {
        const clerk = await this.ensure();
        const user = await this.waitForUser(clerk);
        if (user) {
            logAuth("info", `Clerk callback: sesión activa user=${mask(user.id, 12)}`);
        } else {
            logAuth("log", "Clerk callback: sin sesión (no-op)");
        }
    }

    async logout(): Promise<void> {
        const clerk = await this.ensure();
        const origin =
            typeof window !== "undefined" ? window.location.origin : "http://localhost:5174";
        logAuth("info", `Clerk logout → ${origin}`);
        await clerk.signOut({ redirectUrl: origin });
    }

    async isAuthenticated(): Promise<boolean> {
        const clerk = await this.ensure();
        const user = await this.waitForUser(clerk);
        const ok = Boolean(user);
        logAuth("log", `Clerk isAuthenticated=${ok}`);
        return ok;
    }

    async getAccessToken(): Promise<string | null> {
        const clerk = await this.ensure();
        if (!clerk.session) return null;
        try {
            const token = await clerk.session.getToken();
            logAuth("log", `Clerk getToken len=${token?.length ?? 0}`);
            return token ?? null;
        } catch (e) {
            logAuth("warn", `Clerk getToken: ${e instanceof Error ? e.message : String(e)}`, e);
            return null;
        }
    }

    async getSubject(): Promise<string | null> {
        return (await this.getSession())?.subject ?? null;
    }

    /** Tras redirect OAuth, a veces user llega unos ms después de load(). */
    private async waitForUser(clerk: Clerk, attempts = 10): Promise<typeof clerk.user> {
        for (let i = 0; i < attempts; i++) {
            if (clerk.user) return clerk.user;
            await new Promise((r) => setTimeout(r, 120));
        }
        return clerk.user;
    }

    async getSession(): Promise<AuthSession | null> {
        const clerk = await this.ensure();
        const user = await this.waitForUser(clerk);
        if (!user) {
            logAuth("log", "Clerk getSession: no autenticado");
            return null;
        }

        let accessToken = "";
        let claims: Record<string, unknown> | null = null;
        try {
            const token = await clerk.session?.getToken();
            if (!token) {
                logAuth("warn", "Clerk getSession: token vacío");
                return null;
            }
            accessToken = token;
            try {
                const parts = token.split(".");
                if (parts.length >= 2) {
                    const b64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
                    const pad = b64.length % 4 === 0 ? "" : "=".repeat(4 - (b64.length % 4));
                    claims = JSON.parse(atob(b64 + pad)) as Record<string, unknown>;
                }
            } catch {
                claims = null;
            }
        } catch (e) {
            logAuth("warn", `Clerk getSession token: ${e instanceof Error ? e.message : String(e)}`, e);
            return null;
        }

        const roles = resolveRoles(
            { publicMetadata: (user.publicMetadata ?? {}) as Record<string, unknown> },
            claims,
        );
        const email =
            user.primaryEmailAddress?.emailAddress ??
            user.emailAddresses?.[0]?.emailAddress ??
            null;
        const displayName =
            [user.firstName, user.lastName].filter(Boolean).join(" ").trim() ||
            user.username ||
            email;

        logAuth(
            "info",
            `Clerk session sub=${mask(user.id, 12)} email=${email ?? "—"} role=${roles[0] ?? "user"}`,
        );

        return {
            subject: user.id,
            accessToken,
            email,
            displayName: displayName || null,
            roles,
            pictureUrl: user.imageUrl ?? null,
        };
    }
}
