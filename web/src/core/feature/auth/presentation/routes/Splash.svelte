<script lang="ts">
    import {onMount} from "svelte";
    import {authContainer} from "../../di/auth.container";
    import alejoIcon from "/alejoicon_clean.svg";

    export let navController;

    onMount(async () => {
        try {
            const user = await authContainer.useCases.accounts.getCurrentUser();

            if (user.role !== "admin") {
                navController.navigate("unauthorized", { message: "Tu cuenta existe, pero no tiene permisos de administrador." });
                return;
            }

            navController.navigate("home", { id: user.id });
        } catch {
            navController.navigate("welcome");
        }
    })
</script>
<div class="splash-screen" role="status" aria-label="Loading app">
    <img src={alejoIcon} class="app-icon" alt="App icon" />
</div>


<style>
    .splash-screen {
        width: 100%;
        height: 100dvh;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
    }

    .app-icon {
        width: 180px;
        height: 180px;
        object-fit: contain;
        color: var(--md-sys-color-on-background);
    }
</style>
