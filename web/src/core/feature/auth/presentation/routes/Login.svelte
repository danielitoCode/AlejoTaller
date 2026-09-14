<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import type { GoogleIdTokenProfile } from "../util/google-id-token";
    import { ENV } from "../../../../infrastructure/env";
    import { registerStore } from "../viewmodel/register.store";
    import { Button, Card, LoadingIndicator, TextFieldOutlined } from "m3-svelte";
    import { ArrowRightToLine } from "lucide-svelte";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import LockOutline from "@ktibow/iconset-material-symbols/lock-outline";
    import VisibilityRounded from "@ktibow/iconset-material-symbols/visibility-rounded";
    import VisibilityOffRounded from "@ktibow/iconset-material-symbols/visibility-off-rounded";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { parseGoogleIdToken } from "../util/google-id-token";
    import FrameModal from "../components/FrameModal.svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../util/admin-redirect";
    import { sessionStore } from "../viewmodel/session.store";
    import { getAuthPort, resolveAuthProvider } from "../../di/authPort.factory";
    import LoginAuth0Actions from "../components/LoginAuth0Actions.svelte";

    export let navController: NavController;

    const useAuth0 = resolveAuthProvider() === "auth0";

    let email = "";
    let password = "";
    let showPassword = false;
    let loading = false;
    let error: string | null = null;
    let pendingAdminUser: any = null;
    let pendingAuthContext: { userId: string; email: string; provider: "password" | "google" } | null = null;

    $: canSubmit = email.trim().length > 3 && password.trim().length > 3 && !loading;
    $: normalizedEmail = email.trim().toLowerCase();

    function restorePendingHashIfNeeded() {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    function completeClientLogin(context: { userId: string; email: string; provider: "password" | "google" }) {
        sessionStore.setAuthenticatedSession();
        authFlowStore.setSuccess(context);
        restorePendingHashIfNeeded();
        navController.resetTo("home", context);
    }

    async function maybeHandleAdminChoice(
        user: any,
        context: { userId: string; email: string; provider: "password" | "google" }
    ): Promise<boolean> {
        if (!shouldOfferAdminChoice(user)) return false;
        const choice = getStoredAdminChoice();
        if (choice === "admin") {
            await goToAdminDashboard(async () => await authContainer.useCases.sessions.closeSession.execute());
            return true;
        }
        if (choice === "client") return false;
        pendingAdminUser = user;
        pendingAuthContext = context;
        return true;
    }

    function continueAsClient() {
        if (!pendingAuthContext) return;
        rememberAdminChoice("client");
        const context = pendingAuthContext;
        pendingAdminUser = null;
        pendingAuthContext = null;
        completeClientLogin(context);
    }

    async function continueToAdmin() {
        rememberAdminChoice("admin");
        pendingAdminUser = null;
        pendingAuthContext = null;
        loading = true;
        await goToAdminDashboard(async () => await authContainer.useCases.sessions.closeSession.execute());
        loading = false;
    }

    async function signIn() {
        if (!canSubmit || useAuth0) return;
        loading = true;
        error = null;
        try {
            try { await authContainer.useCases.sessions.closeSession.execute(); } catch {}
            const userId = await authContainer.useCases.sessions.openSession.openCustomSession(normalizedEmail, password);
            const currentUser = await authContainer.useCases.accounts.getCurrentUser();
            const authContext = { userId, email: normalizedEmail, provider: "password" as const };
            if (await maybeHandleAdminChoice(currentUser, authContext)) return;
            completeClientLogin(authContext);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion";
            authFlowStore.setError(error, { email: normalizedEmail, provider: "password" });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    let googleFrameOpen = false;
    let registerFrameOpen = false;
    let googleProfile: GoogleIdTokenProfile | null = null;
    let googleAuthSrc = "";
    let googleRegisterSrc = "";
    let linkOpen = false;
    let linkPassword = "";
    let linkError: string | null = null;

    function getGoogleAuthSrc(): string {
        const clientId = ENV.googleClientId;
        if (!clientId) throw new Error("Falta configurar VITE_GOOGLE_CLIENT_ID");
        const params = new URLSearchParams({ client_id: clientId, parent_origin: window.location.origin });
        return `/google-auth.html#${params.toString()}`;
    }

    async function continueAsGuest() {
        if (loading) return;
        loading = true;
        error = null;
        try {
            if (useAuth0) {
                sessionStore.setGuestSession();
                const guestContext = { userId: "guest-local", email: null, provider: "guest" as const };
                authFlowStore.setSuccess(guestContext);
                restorePendingHashIfNeeded();
                navController.resetTo("home", guestContext);
                return;
            }
            try { await authContainer.useCases.sessions.closeSession.execute(); } catch {}
            const userId = await authContainer.useCases.sessions.openSession.openGuestSession();
            sessionStore.setGuestSession();
            const guestContext = { userId, email: null, provider: "guest" as const };
            authFlowStore.setSuccess(guestContext);
            restorePendingHashIfNeeded();
            navController.resetTo("home", guestContext);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo entrar como visitante";
            authFlowStore.setError(error, { provider: "guest" });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function continueWithGoogle() {
        if (loading || useAuth0) return;
        error = null;
        try {
            googleAuthSrc = getGoogleAuthSrc();
            googleFrameOpen = true;
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion con Google";
        }
    }

    function goToRegister() {
        if (useAuth0) {
            const auth = getAuthPort();
            if (auth) {
                void auth.init().then(() => auth.loginWithRedirect({ returnTo: window.location.origin }));
                return;
            }
        }
        navController.navigate("register");
    }

    function closeGoogleFrame() {
        googleFrameOpen = false;
        googleAuthSrc = "";
    }
</script>

<AuthBusyOverlay open={loading} title="Validando acceso…" subtitle="Comprobando tus credenciales." />

{#if pendingAdminUser}
    <AdminRoleChoiceCard busy={loading} on:stayClient={continueAsClient} on:goAdmin={continueToAdmin} />
{:else}
<Screen ariaLabel="Login" scrollable={false}>
    <main class="login-screen-mobile">
        <section class="login-brand">
            <div class="login-indicator-wrap">
                <LoadingIndicator size={132} aria-label="Cargando" />
                <img class="login-logo" src="/alejoicon_clean.svg" alt="Logo" />
            </div>
            <h2>Alejo Taller</h2>
            <p>{useAuth0 ? "Accede con Auth0 (Google o email)" : "Accede con tu cuenta para continuar"}</p>
        </section>

        <div class="login-card">
            <Card variant="filled">
                <div class="login-card-content">
                    {#if useAuth0}
                        <LoginAuth0Actions disabled={loading} />
                        <p class="auth0-or">Auth0 · Google Social · sin Appwrite</p>
                    {:else}
                        <div class="field-wrap">
                            <TextFieldOutlined label="Correo" bind:value={email} leadingIcon={MailOutlineRounded} type="email" enter={signIn} />
                        </div>
                        <div class="field-wrap">
                            <TextFieldOutlined
                                label="Contrasena"
                                bind:value={password}
                                type={showPassword ? "text" : "password"}
                                leadingIcon={LockOutline}
                                trailing={{
                                    icon: showPassword ? VisibilityOffRounded : VisibilityRounded,
                                    onclick: () => { showPassword = !showPassword; },
                                    "aria-label": showPassword ? "Ocultar contrasena" : "Mostrar contrasena",
                                    title: showPassword ? "Ocultar contrasena" : "Mostrar contrasena"
                                }}
                                enter={signIn}
                            />
                        </div>
                        <div class="action-row">
                            <Button variant="filled" size="m" disabled={!canSubmit} onclick={signIn}>
                                <span class="btn-content"><span>Entrar</span><ArrowRightToLine size={18} /></span>
                            </Button>
                        </div>
                        <div class="action-row">
                            <Button variant="filled" size="m" disabled={loading} onclick={continueWithGoogle}>
                                <span class="btn-content btn-google">
                                    <span>Google</span>
                                    <img class="google-icon" src="/icon/googleIcon.png" alt="" aria-hidden="true" />
                                </span>
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
                            {useAuth0 ? "Crear cuenta (Auth0)" : "No tienes cuenta? Registrate"}
                        </Button>
                    </div>
                    {#if error}<p class="error-copy">{error}</p>{/if}
                </div>
            </Card>
        </div>
    </main>
</Screen>
{/if}

{#if googleFrameOpen && !useAuth0}
    <FrameModal src={googleAuthSrc} on:close={closeGoogleFrame} />
{/if}

<style>
    .login-screen-mobile {
        min-height: 100dvh;
        display: grid;
        place-items: center;
        gap: 24px;
        padding: 24px;
        box-sizing: border-box;
    }
    .login-brand { text-align: center; }
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
    .login-card { width: min(100%, 400px); }
    .login-card-content { display: grid; gap: 14px; padding: 8px; }
    .action-row { display: grid; }
    .btn-content { display: inline-flex; align-items: center; gap: 8px; }
    .btn-google { justify-content: center; }
    .google-icon { width: 18px; height: 18px; }
    .error-copy { color: var(--md-sys-color-error); font-size: 0.88rem; margin: 0; }
    .auth0-or {
        margin: 0;
        text-align: center;
        font-size: 0.78rem;
        color: var(--md-sys-color-on-surface-variant);
    }
</style>
