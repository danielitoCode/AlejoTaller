<script lang="ts">
    import { onMount } from "svelte";
    import { get } from "svelte/store";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import { getAuthPort, resolveAuthProvider } from "../../di/authPort.factory";
    import { userLikeFromAuthSession } from "../../domain/util/authSessionBridge";
    import alejoIcon from "/alejoicon_clean.svg";
    import {
        consumePendingDeepLink,
        rememberPendingDeepLink,
    } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { parseDeepLinkHash } from "../../../../infrastructure/presentation/navigation/deeplink";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
    import { exchangeStore } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import { sessionStore } from "../viewmodel/session.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { getCapturedHash } from "../../../../infrastructure/presentation/navigation/initial-deep-link";
    import {
        logNavAuthCheck,
        logNavRoute,
        logNavError,
    } from "../../../../infrastructure/presentation/navigation/debug-logger";
    import {
        hasCompletedWelcome,
        markWelcomeCompleted,
    } from "../../../../infrastructure/presentation/navigation/first-visit";
    import {
        classifySessionMode,
        hasClearAuthenticatedProfile,
        resolveUserId,
    } from "../util/profile-classification";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice,
    } from "../util/admin-redirect";

    export let navController: NavController;

    const useAuth0 = resolveAuthProvider() === "auth0";

    let adminUser: any = null;
    let loading = true;
    let redirecting = false;
    const capturedHash = getCapturedHash();

    type SplashStatus = "loading" | "authenticated" | "visitor" | "first-visit";
    let status: SplashStatus = "loading";
    let displayName = "";

    const STATUS_HOLD_MS = 900;

    function sleep(ms: number) {
        return new Promise<void>((resolve) => setTimeout(resolve, ms));
    }

    function resolveDisplayName(user: any): string {
        const name = typeof user?.name === "string" ? user.name.trim() : "";
        if (name) return name;
        const email = typeof user?.email === "string" ? user.email.trim() : "";
        if (email.includes("@")) return email.split("@")[0] || email;
        return email || "cliente";
    }

    async function holdStatus(next: SplashStatus, name = "") {
        status = next;
        displayName = name;
        await sleep(STATUS_HOLD_MS);
    }

    function isHomeDeepLink(hash: string): boolean {
        const parsed = parseDeepLinkHash(hash);
        return parsed?.top === "home";
    }

    function applyPendingDeepLink() {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
        return pendingHash;
    }

    async function continueAsAuthenticatedClient(user: any, provider = "auth0") {
        await holdStatus("authenticated", resolveDisplayName(user));
        sessionStore.setAuthenticatedSession();
        const userId = resolveUserId(user);
        authFlowStore.setSuccess({
            userId,
            email: typeof user?.email === "string" ? user.email : null,
            provider,
        });
        markWelcomeCompleted();
        applyPendingDeepLink();
        if (import.meta.env.DEV) {
            logNavAuthCheck(true, false, `auth-${provider}`);
            logNavRoute("home", { id: userId, mode: "authenticated", provider });
        }
        navController.resetTo("home", { id: userId ?? undefined });
    }

    /** Guest 100% local — no Appwrite (billing bloqueado / Auth0 mode). */
    async function continueAsLocalGuest(firstVisit = false) {
        if (import.meta.env.DEV) {
            logNavAuthCheck(false, true, useAuth0 ? "auth0-local-guest" : "local-guest");
        }
        await holdStatus(firstVisit ? "first-visit" : "visitor");
        sessionStore.setGuestSession();
        authFlowStore.setSuccess({
            userId: "guest-local",
            email: null,
            provider: "guest",
        });
        markWelcomeCompleted();
        applyPendingDeepLink();
        navController.resetTo("home");
    }

    async function chooseClient() {
        if (!adminUser) return;
        rememberAdminChoice("client");
        await continueAsAuthenticatedClient(adminUser);
    }

    async function chooseAdmin() {
        redirecting = true;
        rememberAdminChoice("admin");
        const redirected = await goToAdminDashboard(async () => {
            const auth = getAuthPort();
            if (auth) await auth.logout();
            else await authContainer.useCases.sessions.closeSession.execute();
        });
        if (!redirected) redirecting = false;
    }

    function saveHomeDeepLinkIfPresent() {
        if (typeof window === "undefined") return;
        const raw = capturedHash ?? window.location.hash;
        if (isHomeDeepLink(raw)) rememberPendingDeepLink(raw);
    }

    onMount(async () => {
        const hashToCheck = capturedHash ?? window.location.hash;
        const hasDeeplink = isHomeDeepLink(hashToCheck);
        const returningVisitor = hasCompletedWelcome();
        status = "loading";

        if (import.meta.env.DEV) {
            logNavAuthCheck(
                false,
                get(sessionStore).isGuest,
                hasDeeplink
                    ? "deeplink"
                    : returningVisitor
                      ? "returning-direct-home"
                      : "first-visit-welcome",
            );
            logNavRoute("splash", { provider: resolveAuthProvider() });
        }

        try {
            await exchangeStore.refreshForSplash().catch(() => {});

            // ── Auth0 path (no Appwrite) ──────────────────────────
            if (useAuth0) {
                const authPort = getAuthPort();
                if (authPort) {
                    try {
                        await authPort.init();
                        await authPort.handleRedirectCallback();
                        const session = await authPort.getSession();
                        if (session) {
                            const user = userLikeFromAuthSession(session);
                            if (shouldOfferAdminChoice(user)) {
                                const choice = getStoredAdminChoice();
                                if (choice === "admin") {
                                    await chooseAdmin();
                                    return;
                                }
                                if (choice !== "client") {
                                    adminUser = user;
                                    displayName = resolveDisplayName(user);
                                    status = "authenticated";
                                    loading = false;
                                    return;
                                }
                            }
                            await continueAsAuthenticatedClient(user, "auth0");
                            return;
                        }
                    } catch (e) {
                        if (import.meta.env.DEV) logNavError("Auth0 splash", e);
                    }
                }
                // Sin sesión Auth0: guest local o welcome — NUNCA Appwrite
                if (hasDeeplink) saveHomeDeepLinkIfPresent();
                if (returningVisitor || hasDeeplink) {
                    await continueAsLocalGuest(false);
                } else {
                    await holdStatus("first-visit");
                    navController.resetTo("welcome-update");
                }
                return;
            }

            // ── Legacy Appwrite ───────────────────────────────────
            const user = await authContainer.useCases.accounts.getCurrentUser();
            const mode = classifySessionMode(user);

            if (mode === "authenticated" && shouldOfferAdminChoice(user)) {
                const choice = getStoredAdminChoice();
                if (choice === "admin") {
                    await chooseAdmin();
                    return;
                }
                if (choice !== "client") {
                    adminUser = user;
                    displayName = resolveDisplayName(user);
                    status = "authenticated";
                    loading = false;
                    return;
                }
            }

            if (hasDeeplink) saveHomeDeepLinkIfPresent();

            if (mode === "visitor" || !hasClearAuthenticatedProfile(user)) {
                await continueAsLocalGuest(!returningVisitor && !hasDeeplink);
                return;
            }

            await continueAsAuthenticatedClient(user, "appwrite");
        } catch (e) {
            if (import.meta.env.DEV) logNavError("Splash catch", e);
            if (hasDeeplink) saveHomeDeepLinkIfPresent();
            // Auth0 o Appwrite caído: guest local, sin openGuestSession Appwrite
            if (returningVisitor || hasDeeplink || useAuth0) {
                await continueAsLocalGuest(false);
            } else {
                await holdStatus("first-visit");
                navController.resetTo("welcome-update");
            }
        } finally {
            loading = false;
        }
    });

    $: statusTitle =
        status === "loading"
            ? "Cargando sesión de usuario"
            : status === "authenticated"
              ? `Bienvenido${displayName ? `, ${displayName}` : ""}`
              : status === "first-visit"
                ? "Has encontrado la tienda virtual AlejoTaller"
                : "Le damos la bienvenida a nuestra tienda";

    $: statusSubtitle =
        status === "loading"
            ? useAuth0
                ? "Auth0 · sin Appwrite"
                : "Preparando tu experiencia en la tienda"
            : status === "authenticated"
              ? "Entrando a tu espacio de compras"
              : "Para una mejor experiencia te recomendamos registrarte";

    $: orbitActive = !adminUser;
</script>

<div class="splash-screen" role="status" aria-live="polite" aria-busy={loading}>
    <div class="splash-stage">
        <div class="logo-orbit" class:active={orbitActive} aria-hidden="true">
            <span class="ring ring-a"></span>
            <span class="ring ring-b"></span>
            <span class="ring ring-c"></span>
            <img src={alejoIcon} class="app-icon" alt="AlejoTaller" />
        </div>

        {#if !adminUser}
            <div class="status-block">
                <p class="status-title">{statusTitle}</p>
                {#if status === "first-visit"}
                    <p class="status-lead">Le damos la bienvenida a nuestra tienda</p>
                {/if}
                <p class="status-subtitle">{statusSubtitle}</p>
                {#if status === "loading"}
                    <div class="dots" aria-hidden="true">
                        <span></span><span></span><span></span>
                    </div>
                {/if}
            </div>
        {/if}

        {#if !loading && adminUser}
            <AdminRoleChoiceCard
                busy={redirecting}
                on:stayClient={chooseClient}
                on:goAdmin={chooseAdmin}
            />
        {/if}
    </div>
</div>

<style>
    .splash-screen {
        width: 100%;
        height: 100dvh;
        display: grid;
        place-items: center;
        background:
            radial-gradient(
                circle at 50% 38%,
                color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent),
                transparent 52%
            ),
            var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
        padding: 24px;
        box-sizing: border-box;
    }
    .splash-stage {
        display: grid;
        justify-items: center;
        gap: 28px;
        width: min(100%, 420px);
    }
    .logo-orbit {
        position: relative;
        width: 220px;
        height: 220px;
        display: grid;
        place-items: center;
    }
    .app-icon {
        width: 180px;
        height: 180px;
        object-fit: contain;
        z-index: 1;
    }
    .ring {
        position: absolute;
        border-radius: 50%;
        border: 2px solid transparent;
        pointer-events: none;
    }
    .ring-a {
        inset: 0;
        border-top-color: var(--md-sys-color-primary);
    }
    .ring-b {
        inset: 14px;
        border-bottom-color: var(--md-sys-color-tertiary, #c9a227);
    }
    .ring-c {
        inset: 28px;
        border-top-color: color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent);
        opacity: 0.7;
    }
    .logo-orbit.active .ring-a {
        animation: spin 1.15s linear infinite;
    }
    .logo-orbit.active .ring-b {
        animation: spin 1.7s linear infinite reverse;
    }
    .logo-orbit.active .ring-c {
        animation: spin 2.4s linear infinite;
    }
    .status-block {
        display: grid;
        gap: 8px;
        text-align: center;
        max-width: 22rem;
    }
    .status-title {
        margin: 0;
        font-size: 1.15rem;
        font-weight: 800;
    }
    .status-lead {
        margin: 0;
        font-weight: 650;
    }
    .status-subtitle {
        margin: 0;
        font-size: 0.84rem;
        color: var(--md-sys-color-on-surface-variant);
    }
    .dots {
        display: flex;
        justify-content: center;
        gap: 6px;
        margin-top: 6px;
    }
    .dots span {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: var(--md-sys-color-primary);
        opacity: 0.35;
        animation: pulse 1.2s ease-in-out infinite;
    }
    .dots span:nth-child(2) {
        animation-delay: 0.18s;
    }
    .dots span:nth-child(3) {
        animation-delay: 0.36s;
    }
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
    @keyframes pulse {
        0%,
        100% {
            opacity: 0.3;
        }
        50% {
            opacity: 1;
        }
    }
</style>
