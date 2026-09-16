<script lang="ts">
    import { getAuthPort } from "../../di/authPort.factory";
    import { Button } from "m3-svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logAuth0 } from "../../../../infrastructure/presentation/navigation/debug-logger";

    export let disabled = false;

    let loading = false;

    async function login(opts?: {
        connection?: string;
        screenHint?: "signup" | "login";
        loginHint?: string;
    }) {
        const auth = getAuthPort();
        if (!auth) {
            logAuth0("warn", "Login: Auth0 inactivo");
            toastStore.error("Auth0 no está activo. Revisa VITE_AUTH0_DOMAIN / CLIENT_ID.");
            return;
        }
        loading = true;
        try {
            logAuth0(
                "info",
                `Login UI connection=${opts?.connection ?? "universal"} screen=${opts?.screenHint ?? "login"}`,
            );
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                connection: opts?.connection,
                screenHint: opts?.screenHint,
                loginHint: opts?.loginHint,
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
    <Button
        variant="filled"
        size="m"
        disabled={disabled || loading}
        onclick={() => login({ screenHint: "login" })}
    >
        {#if loading}Redirigiendo…{:else}Entrar con email / contraseña{/if}
    </Button>
    <Button
        variant="outlined"
        size="m"
        disabled={disabled || loading}
        onclick={() => login({ connection: "google-oauth2" })}
    >
        Continuar con Google
    </Button>
    <Button
        variant="text"
        size="m"
        disabled={disabled || loading}
        onclick={() => login({ screenHint: "signup" })}
    >
        Crear cuenta (Auth0)
    </Button>
    <p class="hint">
        Email y Google vía Auth0 Universal Login. Appwrite auth desconectado.
    </p>
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
