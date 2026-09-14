<script lang="ts">
    import { getAuthPort } from "../../di/authPort.factory";
    import { Button } from "m3-svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logAuth0 } from "../../../../infrastructure/presentation/navigation/debug-logger";

    export let disabled = false;

    let loading = false;

    async function login(opts?: { connection?: string }) {
        const auth = getAuthPort();
        if (!auth) {
            logAuth0("warn", "Login: Auth0 inactivo (VITE_AUTH_PROVIDER≠auth0)");
            toastStore.error("Auth0 no está activo (VITE_AUTH_PROVIDER=auth0)");
            return;
        }
        loading = true;
        try {
            logAuth0(
                "info",
                `Login UI click connection=${opts?.connection ?? "universal"}`,
            );
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                connection: opts?.connection,
            });
        } catch (e) {
            const msg = e instanceof Error ? e.message : "No se pudo iniciar Auth0";
            logAuth0("error", `Login UI falló: ${msg}`, e);
            toastStore.error(msg);
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
    <p class="hint">Google vía Auth0 Social · logs solo en local (panel Logs)</p>
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
