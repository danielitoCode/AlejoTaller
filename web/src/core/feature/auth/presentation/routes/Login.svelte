<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import { Button, Card, TextFieldOutlined } from "m3-svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import LoadingSpinner from "../../../../infrastructure/presentation/components/LoadingSpinner.svelte";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import LockOutline from "@ktibow/iconset-material-symbols/lock-outline";
    import VisibilityRounded from "@ktibow/iconset-material-symbols/visibility-rounded";
    import VisibilityOffRounded from "@ktibow/iconset-material-symbols/visibility-off-rounded";
    import { ArrowRightToLine } from "lucide-svelte";
    import { sessionStore } from "../viewmodel/session.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { getAuthPort, resolveAuthProvider } from "../../di/authPort.factory";
    import LoginAuth0Actions from "../components/LoginAuth0Actions.svelte";
    import { restorePendingHashIfNeeded } from "../../../../infrastructure/presentation/navigation/deep-link";

    export let navController: NavController;

    const useAuth0 = resolveAuthProvider() === "auth0";

    let email = "";
    let password = "";
    let showPassword = false;
    let loading = false;
    let error: string | null = null;

    $: canSubmit = email.trim().length > 3 && password.trim().length > 3 && !loading;
    $: normalizedEmail = email.trim().toLowerCase();

    function completeClientLogin(context: { userId: string; email: string; provider: "password" | "google" }) {
        sessionStore.setAuthenticatedSession();
        authFlowStore.setSuccess(context);
        restorePendingHashIfNeeded();
        navController.resetTo("home", context);
    }

    async function continueAsGuest() {
        if (loading) return;
        loading = true;
        error = null;
        try {
            // Auth0 / Core6: guest 100% local — nunca Appwrite openGuestSession
            sessionStore.setGuestSession();
            const guestContext = { userId: "guest-local", email: null, provider: "guest" as const };
            authFlowStore.setSuccess(guestContext);
            restorePendingHashIfNeeded();
            navController.resetTo("home", guestContext);
        } catch (e) {
            authFlowStore.setError(e, { provider: "guest" });
            toastStore.error(e instanceof Error ? e.message : "No se pudo continuar como visitante");
        } finally {
            loading = false;
        }
    }

    function goToRegister() {
        if (useAuth0) {
            const auth = getAuthPort();
            if (auth) {
                void auth
                    .init()
                    .then(() =>
                        auth.loginWithRedirect({
                            returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                            screenHint: "signup",
                        }),
                    )
                    .catch((e) => {
                        error = e instanceof Error ? e.message : "No se pudo abrir registro Auth0";
                        toastStore.error(error);
                    });
                return;
            }
        }
        navController.navigate("register");
    }

    // Legacy Appwrite — solo si Auth0 está apagado (UI oculta con useAuth0)
    async function signIn() {
        if (!canSubmit || useAuth0) return;
        loading = true;
        error = null;
        try {
            try {
                await authContainer.useCases.sessions.closeSession.execute();
            } catch {}
            const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                normalizedEmail,
                password,
            );
            const authContext = { userId, email: normalizedEmail, provider: "password" as const };
            completeClientLogin(authContext);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion";
            authFlowStore.setError(error, { email: normalizedEmail, provider: "password" });
            toastStore.error(error);
        } finally {
            loading = false;
        }
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
            <p>Accede con tu cuenta para continuar</p>
        </section>

        <div class="login-card">
            <Card variant="filled">
                <div class="login-card-content">
                    {#if useAuth0}
                        <LoginAuth0Actions disabled={loading} />
                        <p class="auth0-hint">Acceso con Auth0 (email o Google). Sin Appwrite.</p>
                    {:else}
                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Correo"
                                bind:value={email}
                                leadingIcon={MailOutlineRounded}
                                type="email"
                                enter={signIn}
                            />
                        </div>
                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Contrasena"
                                bind:value={password}
                                type={showPassword ? "text" : "password"}
                                leadingIcon={LockOutline}
                                trailing={{
                                    icon: showPassword ? VisibilityOffRounded : VisibilityRounded,
                                    onclick: () => {
                                        showPassword = !showPassword;
                                    },
                                    "aria-label": showPassword ? "Ocultar contrasena" : "Mostrar contrasena",
                                    title: showPassword ? "Ocultar contrasena" : "Mostrar contrasena",
                                }}
                                enter={signIn}
                            />
                        </div>
                        <div class="action-row">
                            <Button variant="filled" size="m" disabled={!canSubmit} onclick={signIn}>
                                <span class="btn-content"><span>Entrar</span><ArrowRightToLine size={18} /></span>
                            </Button>
                        </div>
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
    .auth0-hint {
        margin: 0;
        text-align: center;
        font-size: 0.8rem;
        color: var(--md-sys-color-on-surface-variant);
    }
    .error-copy {
        color: var(--md-sys-color-error);
        margin: 0;
        font-size: 0.85rem;
    }
    .btn-content {
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
</style>
