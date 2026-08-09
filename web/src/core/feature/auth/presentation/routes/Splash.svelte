<script lang="ts">
    import {onMount} from "svelte";
    import { get } from "svelte/store";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import {authContainer} from "../../di/auth.container";
    import alejoIcon from "/alejoicon_clean.svg";
    import { consumePendingDeepLink, rememberPendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { parseDeepLinkHash } from "../../../../infrastructure/presentation/navigation/deeplink";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
    import { exchangeStore } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import { sessionStore } from "../viewmodel/session.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { getCapturedHash, getCapturedParsedDeeplink } from "../../../../infrastructure/presentation/navigation/initial-deep-link";
    import { logNavAuthCheck, logNavRoute, logNavError } from "../../../../infrastructure/presentation/navigation/debug-logger";
    import { hasCompletedWelcome, markWelcomeCompleted } from "../../../../infrastructure/presentation/navigation/first-visit";
    import {
        classifySessionMode,
        hasClearAuthenticatedProfile,
        resolveUserId
    } from "../util/profile-classification";

    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../util/admin-redirect";

    export let navController: NavController;

    let adminUser: any = null;
    let loading = true;
    let redirecting = false;
    /** The hash captured before Svelte could touch it, used on cold boot */
    const capturedHash = getCapturedHash();

    /** UX status under the logo (same language as AuthBusyOverlay orbit) */
    type SplashStatus = "loading" | "authenticated" | "visitor" | "first-visit";
    let status: SplashStatus = "loading";
    let displayName = "";

    const STATUS_HOLD_MS = 1100;

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

    /** Any actionable deeplink into the shell (home/*). */
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

    /** Clear authenticated client → home with full privileges */
    async function continueAsAuthenticatedClient(user: any) {
        await holdStatus("authenticated", resolveDisplayName(user));
        sessionStore.setAuthenticatedSession();
        const userId = resolveUserId(user);
        authFlowStore.setSuccess({
            userId,
            email: typeof user?.email === "string" ? user.email : null,
            provider: "password"
        });
        markWelcomeCompleted();
        const pendingHash = applyPendingDeepLink();
        if (import.meta.env.DEV) {
            logNavAuthCheck(true, false, "continue");
            const parsed = parseDeepLinkHash(pendingHash || window.location.hash);
            logNavRoute("home", { id: userId, productId: parsed?.args?.productId, mode: "authenticated" });
        }
        navController.resetTo("home", { id: userId ?? undefined });
    }

    /**
     * Visitor path: mark local guest flag + guest provider.
     * Reuses existing anonymous Appwrite session when possible (caller already has user).
     */
    async function continueAsVisitor(user?: any, firstVisit = false) {
        await holdStatus(firstVisit ? "first-visit" : "visitor");
        sessionStore.setGuestSession();
        const userId = resolveUserId(user);
        authFlowStore.setSuccess({
            userId,
            email: null,
            provider: "guest"
        });
        markWelcomeCompleted();
        const pendingHash = applyPendingDeepLink();
        if (import.meta.env.DEV) {
            logNavAuthCheck(false, true, "continue");
            logNavRoute("home", {
                id: userId,
                productId: parseDeepLinkHash(pendingHash || window.location.hash)?.args?.productId,
                mode: "visitor"
            });
        }
        navController.resetTo("home", userId ? { id: userId } : undefined);
    }

    async function autoCreateGuestSession(firstVisit = false) {
        if (import.meta.env.DEV) {
            logNavAuthCheck(false, false, "auto-guest");
        }
        try {
            status = firstVisit ? "first-visit" : "visitor";
            const userId = await authContainer.useCases.sessions.openSession.openGuestSession();
            await holdStatus(firstVisit ? "first-visit" : "visitor");
            sessionStore.setGuestSession();
            authFlowStore.setSuccess({
                userId,
                email: null,
                provider: "guest"
            });
            markWelcomeCompleted();
            const pendingHash = applyPendingDeepLink();
            if (pendingHash && import.meta.env.DEV) {
                logNavRoute("home", { productId: parseDeepLinkHash(pendingHash)?.args?.productId, mode: "visitor" });
            }
            navController.resetTo("home");
        } catch (e) {
            if (import.meta.env.DEV) logNavError("autoCreateGuestSession failed", e);
            await holdStatus("first-visit");
            navController.resetTo("welcome");
        }
    }

    async function chooseClient() {
        if (!adminUser) return;
        rememberAdminChoice("client");
        await continueAsAuthenticatedClient(adminUser);
    }

    async function chooseAdmin() {
        redirecting = true;
        rememberAdminChoice("admin");
        const redirected = await goToAdminDashboard(
            async () => await authContainer.useCases.sessions.closeSession.execute()
        );
        if (!redirected) {
            redirecting = false;
        }
    }

    function saveHomeDeepLinkIfPresent() {
        if (typeof window === "undefined") return;
        const raw = capturedHash ?? window.location.hash;
        if (isHomeDeepLink(raw)) {
            rememberPendingDeepLink(raw);
        }
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
                hasDeeplink ? "deeplink" : returningVisitor ? "returning-direct-home" : "first-visit-welcome"
            );
        }
        try {
            await exchangeStore.refreshForSplash();
            const user = await authContainer.useCases.accounts.getCurrentUser();
            const mode = classifySessionMode(user);

            // Only offer admin choice for CLEAR authenticated admin profiles
            if (mode === "authenticated" && shouldOfferAdminChoice(user)) {
                const choice = getStoredAdminChoice();
                if (choice === "admin") {
                    if (import.meta.env.DEV) logNavRoute("admin");
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

            if (hasDeeplink) {
                saveHomeDeepLinkIfPresent();
            }

            // POLICY: unclear / anonymous / empty-email profile → visitor
            if (mode === "visitor" || !hasClearAuthenticatedProfile(user)) {
                if (import.meta.env.DEV) {
                    logNavRoute("home", { reason: "unclear-profile-as-visitor", email: user?.email ?? null });
                }
                await continueAsVisitor(user, !returningVisitor && !hasDeeplink);
                return;
            }

            await continueAsAuthenticatedClient(user);
        } catch {
            // No Appwrite session at all
            if (hasDeeplink) {
                saveHomeDeepLinkIfPresent();
                await autoCreateGuestSession(false);
            } else if (returningVisitor) {
                if (import.meta.env.DEV) logNavRoute("home", { reason: "returning-visitor-auto-guest" });
                await autoCreateGuestSession(false);
            } else {
                if (import.meta.env.DEV) logNavRoute("welcome", { reason: "first-visit-no-deeplink" });
                await holdStatus("first-visit");
                navController.resetTo("welcome");
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
            ? "Preparando tu experiencia en la tienda"
            : status === "authenticated"
              ? "Entrando a tu espacio de compras"
              : "Para una mejor experiencia te recomendamos registrarte";

    $: showOrbit = loading || status === "loading" || (!adminUser && status !== "authenticated");
    // Keep soft orbit while showing welcome lines; stop hard spin feel only when admin card is up
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
        position: relative;
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
        color: var(--md-sys-color-on-background);
        position: relative;
        z-index: 1;
        filter: drop-shadow(0 12px 28px color-mix(in srgb, black 22%, transparent));
    }

    .ring {
        position: absolute;
        border-radius: 50%;
        border: 2px solid transparent;
        pointer-events: none;
        opacity: 0.95;
    }

    .ring-a {
        inset: 0;
        border-top-color: var(--md-sys-color-primary);
        border-right-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
    }

    .ring-b {
        inset: 14px;
        border-bottom-color: var(--md-sys-color-tertiary, #c9a227);
        border-left-color: color-mix(in srgb, var(--md-sys-color-tertiary, #c9a227) 40%, transparent);
    }

    .ring-c {
        inset: 28px;
        border-top-color: color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent);
        border-left-color: color-mix(in srgb, var(--md-sys-color-outline-variant) 55%, transparent);
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
        animation: fade-up 0.35s ease both;
    }

    .status-title {
        margin: 0;
        font-size: clamp(1.05rem, 2.6vw, 1.25rem);
        font-weight: 800;
        letter-spacing: -0.02em;
        color: var(--md-sys-color-on-surface);
        line-height: 1.3;
    }

    .status-lead {
        margin: 0;
        font-size: 0.98rem;
        font-weight: 650;
        color: var(--md-sys-color-on-surface);
        line-height: 1.35;
    }

    .status-subtitle {
        margin: 0;
        font-size: 0.84rem;
        line-height: 1.45;
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
            transform: translateY(0);
        }
        50% {
            opacity: 1;
            transform: translateY(-3px);
        }
    }

    @keyframes fade-up {
        from {
            opacity: 0;
            transform: translateY(8px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .logo-orbit.active .ring-a,
        .logo-orbit.active .ring-b,
        .logo-orbit.active .ring-c,
        .dots span {
            animation: none !important;
        }

        .status-block {
            animation: none !important;
        }
    }
</style>
