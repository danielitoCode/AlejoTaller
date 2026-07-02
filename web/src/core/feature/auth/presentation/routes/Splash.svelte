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
    import { getCapturedHash, getCapturedParsedDeeplink, isProductDeeplinkCaptured } from "../../../../infrastructure/presentation/navigation/initial-deep-link";
    import { logNavAuthCheck, logNavRoute, logProductFlow, logNavError } from "../../../../infrastructure/presentation/navigation/debug-logger";

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
    const capturedParsed = getCapturedParsedDeeplink();

    function isProductDeepLink(hash: string): boolean {
        const parsed = parseDeepLinkHash(hash);
        return parsed?.top === "home" && (parsed.nested === "product-detail" || !!parsed.args?.productId);
    }

    function continueAsClient(user: any) {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
        if (import.meta.env.DEV) {
            logNavAuthCheck(true, false, "continue");
            const parsed = parseDeepLinkHash(pendingHash || window.location.hash);
            logNavRoute("home", { id: user.id ?? user.$id, productId: parsed?.args?.productId });
        }
        navController.resetTo("home", { id: user.id ?? user.$id });
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
            const pendingHash = consumePendingDeepLink();
            if (pendingHash && typeof window !== "undefined") {
                if (import.meta.env.DEV) {
                    logNavRoute("home", { productId: parseDeepLinkHash(pendingHash)?.args?.productId });
                }
                window.history.replaceState({}, "", pendingHash);
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
        continueAsClient(adminUser);
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

    /** Store the captured (or current) product deeplink so it survives navigation */
    function saveProductDeepLinkIfPresent() {
        if (typeof window === "undefined") return;
        // Prefer the captured hash if it exists, otherwise use the current hash
        const raw = capturedHash ?? window.location.hash;
        const parsed = parseDeepLinkHash(raw);
        if (parsed?.top === "home" && (parsed.nested === "product-detail" || !!parsed.args?.productId)) {
            rememberPendingDeepLink(raw);
        }
    }

    onMount(async () => {
        // Source of truth for the deep-link we need to process on cold-boot
        const hashToCheck = capturedHash ?? window.location.hash;
        if (import.meta.env.DEV) {
            const hasDeeplink = isProductDeepLink(hashToCheck);
            logNavAuthCheck(
                false,
                get(sessionStore).isGuest,
                hasDeeplink ? "auto-guest" : "redirect-welcome"
            );
        }
        try {
            await exchangeStore.refreshForSplash();
            const user = await authContainer.useCases.accounts.getCurrentUser();
            if (shouldOfferAdminChoice(user)) {
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
            // Guest without a product deep-link → go to WelcomeScreen so they can log in properly
            if (get(sessionStore).isGuest && !isProductDeepLink(hashToCheck)) {
                if (import.meta.env.DEV) logNavRoute("welcome", { reason: "guest-no-deeplink" });
                navController.resetTo("welcome");
                return;
            }
            continueAsClient(user);
        } catch {
            if (isProductDeepLink(hashToCheck)) {
                saveProductDeepLinkIfPresent();
                await autoCreateGuestSession();
            } else {
                if (import.meta.env.DEV) logNavRoute("welcome", { reason: "catch-no-deeplink" });
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
