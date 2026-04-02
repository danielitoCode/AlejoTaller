<script lang="ts">
    import {onMount} from "svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import {authContainer} from "../../di/auth.container";
    import alejoIcon from "/alejoicon_clean.svg";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
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

    function continueAsClient(user: any) {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
        navController.resetTo("home", { id: user.id ?? user.$id });
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

    onMount(async () => {
        try {
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
            continueAsClient(user);
        } catch {
            navController.resetTo("welcome");
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
