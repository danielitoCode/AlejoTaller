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

    function isProductDeepLink(hash: string): boolean {
        const parsed = parseDeepLinkHash(hash);
        return parsed?.top === "home" && (parsed.nested === "product-detail" || !!parsed.args?.productId);
    }

    function continueAsClient(user: any) {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
        navController.resetTo("home", { id: user.id ?? user.$id });
    }

    async function autoCreateGuestSession() {
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
                window.history.replaceState({}, "", pendingHash);
            }
            navController.resetTo("home");
        } catch {
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

    function rememberProductDeepLinkIfPresent() {
        if (typeof window === "undefined") return;

        const parsed = parseDeepLinkHash(window.location.hash);
        if (parsed?.top === "home" && (parsed.nested === "product-detail" || !!parsed.args?.productId)) {
            rememberPendingDeepLink(window.location.hash);
        }
    }

    onMount(async () => {
        try {
            await exchangeStore.refreshForSplash();
            const user = await authContainer.useCases.accounts.getCurrentUser();
            if (shouldOfferAdminChoice(user)) {
                const choice = getStoredAdminChoice();
                if (choice === "admin") {
                    await chooseAdmin();
                    return;
                }
                if (choice !== "client") {
                    adminUser = user;
                    loading = false;
                    return;
                }
            }
            // Si la sesión activa es de visitante (anónima) y no hay deeplink de producto,
            // redirigir a WelcomeScreen para que el usuario inicie sesión correctamente.
            if (get(sessionStore).isGuest && !isProductDeepLink(window.location.hash)) {
                navController.resetTo("welcome");
                return;
            }
            continueAsClient(user);
        } catch {
            if (isProductDeepLink(window.location.hash)) {
                rememberProductDeepLinkIfPresent();
                await autoCreateGuestSession();
            } else {
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
