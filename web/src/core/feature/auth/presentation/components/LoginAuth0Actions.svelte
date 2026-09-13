<script lang="ts">
    import { getAuthPort } from "../../di/authPort.factory";
    import { Button } from "m3-svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";

    export let disabled = false;

    let loading = false;

    async function login(opts?: { connection?: string }) {
        const auth = getAuthPort();
        if (!auth) {
            toastStore.error("Auth0 no está activo (VITE_AUTH_PROVIDER=auth0)");
            return;
        }
        loading = true;
        try {
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                connection: opts?.connection,
            });
        } catch (e) {
            toastStore.error(e instanceof Error ? e.message : "No se pudo iniciar Auth0");
            loading = false;
        }
    }
</script>

<div class="auth0-block">
    <Button variant="filled" size="m" disabled={disabled || loading} onclick={() => login()}>
        {#if loading}Redirigiendo…{:else}Continuar con Auth0{/if}
    </Button>
    <Button
        variant="outlined"
        size="m"
        disabled={disabled || loading}
        onclick={() => login({ connection: "google-oauth2" })}
    >
        Continuar con Google
    </Button>
    <p class="hint">Google vía Auth0 Social · puerto 5174</p>
</div>

<style>
    .auth0-block {
        display: grid;
        gap: 8px;
        width: 100%;
    }
    .hint {
        margin: 0;
        text-align: center;
        font-size: 0.78rem;
        color: var(--md-sys-color-on-surface-variant);
    }
</style>
