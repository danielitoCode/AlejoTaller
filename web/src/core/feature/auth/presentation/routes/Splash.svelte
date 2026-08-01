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
    function continueAsAuthenticatedClient(user: any) {
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
    function continueAsVisitor(user?: any) {
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

    async function autoCreateGuestSession() {
        if (import.meta.env.DEV) {
            logNavAuthCheck(false, false, "auto-guest");
        }
        try {
            const userId = await authContainer.useCases.sessions.openSession.openGuestSession();
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
            navController.resetTo("welcome");
        }
    }

    async function chooseClient() {
        if (!adminUser) return;
        rememberAdminChoice("client");
        continueAsAuthenticatedClient(adminUser);
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
                continueAsVisitor(user);
                return;
            }

            continueAsAuthenticatedClient(user);
        } catch {
            // No Appwrite session at all
            if (hasDeeplink) {
                saveHomeDeepLinkIfPresent();
                await autoCreateGuestSession();
            } else if (returningVisitor) {
                if (import.meta.env.DEV) logNavRoute("home", { reason: "returning-visitor-auto-guest" });
                await autoCreateGuestSession();
            } else {
                if (import.meta.env.DEV) logNavRoute("welcome", { reason: "first-visit-no-deeplink" });
                navController.resetTo("welcome");
            }
        } finally {
            loading = false;
        }
    })
</script>
<div class="splash-screen" role="status" aria-label="Loading app">
    <img src={alejoIcon} class="app-icon" alt="App icon" />

    {#if !loading && adminUser}
        <AdminRoleChoiceCard
            busy={redirecting}
            on:stayClient={chooseClient}
            on:goAdmin={chooseAdmin}
        />
    {/if}
</div>


<style>
    .splash-screen {
        width: 100%;
        height: 100dvh;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
        position: relative;
    }

    .app-icon {
        width: 180px;
        height: 180px;
        object-fit: contain;
        color: var(--md-sys-color-on-background);
    }
</style>
