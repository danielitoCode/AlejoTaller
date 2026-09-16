<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { Button, Card } from "m3-svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import LoadingSpinner from "../../../../infrastructure/presentation/components/LoadingSpinner.svelte";
    import { sessionStore } from "../viewmodel/session.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { getAuthPort, isExternalAuthProvider } from "../../di/authPort.factory";
    import LoginAuth0Actions from "../components/LoginAuth0Actions.svelte";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";

    export let navController: NavController;

    const useExternalAuth = isExternalAuthProvider();

    let loading = false;
    let error: string | null = null;

    function applyPendingDeepLink(): void {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    async function continueAsGuest() {
        if (loading) return;
        loading = true;
        error = null;
        try {
            sessionStore.setGuestSession();
            const guestContext = { userId: "guest-local", email: null, provider: "guest" as const };
            authFlowStore.setSuccess(guestContext);
            applyPendingDeepLink();
            navController.resetTo("home", guestContext);
        } catch (e) {
            authFlowStore.setError(e, { provider: "guest" });
            toastStore.error(e instanceof Error ? e.message : "No se pudo continuar como visitante");
        } finally {
            loading = false;
        }
    }

    function goToRegister() {
        const auth = getAuthPort();
        if (!auth) {
            error = "Clerk/Auth0 no configurado (falta VITE_CLERK_PUBLISHABLE_KEY)";
            toastStore.error(error);
            return;
        }
        void auth
            .init()
            .then(() =>
                auth.loginWithRedirect({
                    returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                    screenHint: "signup",
                }),
            )
            .catch((e) => {
                error = e instanceof Error ? e.message : "No se pudo abrir registro";
                toastStore.error(error);
            });
    }
</script>

<AuthBusyOverlay
    open={loading}
    title="Validando acceso…"
    subtitle="Comprobando tus credenciales. Esto suele tardar solo un momento."
/>

<Screen ariaLabel="Login" scrollable={false}>
    <main class="login-screen-mobile">
        <section class="login-brand">
            <div class="login-indicator-wrap">
                <LoadingSpinner size={132} label="Cargando" />
                <img class="login-logo" src="/alejoicon_clean.svg" alt="Logo de la aplicacion" />
            </div>
            <h2>Alejo Taller</h2>
            <p>Accede con Clerk (email o Google)</p>
        </section>

        <div class="login-card">
            <Card variant="filled">
                <div class="login-card-content">
                    {#if useExternalAuth}
                        <LoginAuth0Actions disabled={loading} />
                    {:else}
                        <p class="error-copy">
                            Falta VITE_CLERK_PUBLISHABLE_KEY. No hay login Appwrite en Core6.
                        </p>
                    {/if}

                    <div class="action-row">
                        <Button variant="tonal" size="m" disabled={loading} onclick={continueAsGuest}>
                            Entrar como visitante
                        </Button>
                    </div>
                    <div class="action-row">
                        <Button variant="text" size="m" onclick={goToRegister}>
                            No tienes cuenta? Registrate
                        </Button>
                    </div>
                    {#if error}<p class="error-copy">{error}</p>{/if}
                </div>
            </Card>
        </div>
    </main>
</Screen>

<style>
    :global(.screen[aria-label="Login"]) {
        width: 100%;
        max-width: none;
        height: 100dvh;
        min-height: 100dvh;
        padding: 0;
        margin: 0;
    }
    .login-screen-mobile {
        width: 100%;
        height: 100%;
        padding: max(14px, calc(env(safe-area-inset-top) + 8px)) 16px max(16px, calc(env(safe-area-inset-bottom) + 10px));
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        gap: 12px;
        color: var(--md-sys-color-on-background);
    }
    .login-brand {
        display: grid;
        justify-items: center;
        text-align: center;
        gap: 8px;
    }
    .login-indicator-wrap {
        position: relative;
        width: 132px;
        height: 132px;
        display: grid;
        place-items: center;
    }
    .login-logo {
        position: absolute;
        width: 56px;
        height: 56px;
        object-fit: contain;
    }
    .login-card {
        width: 100%;
        max-width: 420px;
        margin-inline: auto;
    }
    .login-card-content {
        padding: 16px;
        display: grid;
        gap: 12px;
    }
    .action-row {
        display: grid;
    }
    .error-copy {
        color: var(--md-sys-color-error);
        margin: 0;
        font-size: 0.85rem;
    }
</style>
