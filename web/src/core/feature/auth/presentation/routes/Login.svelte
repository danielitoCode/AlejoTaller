<script lang="ts">
    /**
     * Login web. Con VITE_AUTH_PROVIDER=auth0:
     * - Solo Auth0 / Google vía Auth0 (no One Tap, no Appwrite).
     * - Guest = flag local (sin openGuestSession Appwrite).
     */
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { getAuthPort, resolveAuthProvider } from "../../di/authPort.factory";
    import { Button, Card, LoadingIndicator } from "m3-svelte";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { sessionStore } from "../viewmodel/session.store";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import LoginAuth0Actions from "../components/LoginAuth0Actions.svelte";

    export let navController: NavController;

    const useAuth0 = resolveAuthProvider() === "auth0";
    let loading = false;

    function restorePendingHashIfNeeded() {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    function continueAsLocalGuest() {
        if (loading) return;
        loading = true;
        sessionStore.setGuestSession();
        authFlowStore.setSuccess({
            userId: "guest-local",
            email: null,
            provider: "guest",
        });
        restorePendingHashIfNeeded();
        navController.resetTo("home");
        loading = false;
    }

    function goToRegister() {
        if (useAuth0) {
            // Signup vía Universal Login Auth0
            const auth = getAuthPort();
            if (auth) {
                void auth.init().then(() =>
                    auth.loginWithRedirect({
                        returnTo: window.location.origin,
                    }),
                );
                return;
            }
        }
        navController.navigate("register");
    }
</script>

<Screen ariaLabel="Login" scrollable={false}>
    <main class="login-screen-mobile">
        <section class="login-brand">
            <div class="login-indicator-wrap">
                <LoadingIndicator size={132} aria-label="Cargando" />
                <img class="login-logo" src="/alejoicon_clean.svg" alt="Logo" />
            </div>
            <h2>Alejo Taller</h2>
            <p>
                {#if useAuth0}
                    Accede con Auth0 (Google o email)
                {:else}
                    Accede con tu cuenta para continuar
                {/if}
            </p>
        </section>

        <div class="login-card">
            <Card variant="filled">
                <div class="login-card-content">
                    {#if useAuth0}
                        <LoginAuth0Actions disabled={loading} />
                        <p class="hint">
                            Google y registro pasan por Auth0 — no por Appwrite.
                        </p>
                    {:else}
                        <p class="hint error-hint">
                            VITE_AUTH_PROVIDER no es auth0. Activa Auth0 en .env
                            para dejar de usar Appwrite.
                        </p>
                        <Button
                            variant="filled"
                            size="m"
                            disabled={loading}
                            onclick={() =>
                                toastStore.error(
                                    "Configura VITE_AUTH_PROVIDER=auth0 en web/.env",
                                )}
                        >
                            Auth0 no activo
                        </Button>
                    {/if}

                    <div class="action-row">
                        <Button
                            variant="tonal"
                            size="m"
                            disabled={loading}
                            onclick={continueAsLocalGuest}
                        >
                            Entrar como visitante
                        </Button>
                    </div>

                    <div class="action-row">
                        <Button variant="text" size="m" onclick={goToRegister}>
                            {#if useAuth0}Crear cuenta (Auth0){:else}Regístrate{/if}
                        </Button>
                    </div>
                </div>
            </Card>
        </div>
    </main>
</Screen>

<style>
    .login-screen-mobile {
        min-height: 100dvh;
        display: grid;
        place-items: center;
        gap: 24px;
        padding: 24px;
        box-sizing: border-box;
    }
    .login-brand {
        text-align: center;
    }
    .login-indicator-wrap {
        position: relative;
        width: 132px;
        height: 132px;
        margin: 0 auto 12px;
    }
    .login-logo {
        position: absolute;
        inset: 18px;
        width: calc(100% - 36px);
        height: calc(100% - 36px);
        object-fit: contain;
    }
    .login-card {
        width: min(100%, 400px);
    }
    .login-card-content {
        display: grid;
        gap: 14px;
        padding: 8px;
    }
    .action-row {
        display: grid;
    }
    .hint {
        margin: 0;
        font-size: 0.8rem;
        text-align: center;
        color: var(--md-sys-color-on-surface-variant);
    }
    .error-hint {
        color: var(--md-sys-color-error);
    }
</style>
