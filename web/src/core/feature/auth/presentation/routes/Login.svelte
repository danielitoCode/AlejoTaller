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
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../util/admin-redirect";

    export let navController: NavController;

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
            await goToAdminDashboard(
                async () => await authContainer.useCases.sessions.closeSession.execute()
            );
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
        await goToAdminDashboard(
            async () => await authContainer.useCases.sessions.closeSession.execute()
        );
        loading = false;
    }

    async function signIn() {
        if (!canSubmit) return;

        loading = true;
        error = null;

        try {
            const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                normalizedEmail,
                password
            );
            const currentUser = await authContainer.useCases.accounts.getCurrentUser();
            const authContext = {
                userId,
                email: normalizedEmail,
                provider: "password"
            } as const;
            if (await maybeHandleAdminChoice(currentUser, authContext)) return;
            completeClientLogin(authContext);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion";
            authFlowStore.setError(error, {
                email: normalizedEmail,
                provider: "password"
            });
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
        const params = new URLSearchParams({
            client_id: clientId,
            parent_origin: window.location.origin
        });
        return `/google-auth.html#${params.toString()}`;
    }

    function getGoogleRegisterSrc(profile: GoogleIdTokenProfile): string {
        const params = new URLSearchParams({
            email: profile.email,
            name: profile.name,
            picture: profile.picture,
            parent_origin: window.location.origin
        });
        return `/google-register.html#${params.toString()}`;
    }

    async function handleGoogleProfile(profile: GoogleIdTokenProfile) {
        const sanitizedProfile = {
            ...profile,
            email: profile.email.trim().toLowerCase()
        };
        googleProfile = sanitizedProfile;

        loading = true;
        error = null;
        linkError = null;

        try {
            try {
                const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                    sanitizedProfile.email,
                    sanitizedProfile.sub
                );
                const current = await authContainer.useCases.accounts.getCurrentUser();
                const currentPhoto =
                    typeof current?.photo_url === "string"
                        ? current.photo_url.trim()
                        : "";
                if (!currentPhoto && sanitizedProfile.picture?.trim()) {
                    await authContainer.useCases.accounts.updatePhotoUrl(sanitizedProfile.picture.trim());
                }
                const authContext = {
                    userId,
                    email: sanitizedProfile.email,
                    provider: "google"
                } as const;
                if (await maybeHandleAdminChoice(current, authContext)) return;
                completeClientLogin(authContext);
                return;
            } catch {
                googleRegisterSrc = getGoogleRegisterSrc(sanitizedProfile);
                registerFrameOpen = true;
                return;
            }
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion con Google";
            authFlowStore.setError(error, {
                email: sanitizedProfile.email,
                provider: "google"
            });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function linkGoogleAccount() {
        if (!googleProfile) return;
        if (!linkPassword.trim()) {
            linkError = "Ingresa tu contrasena actual.";
            return;
        }

        loading = true;
        linkError = null;
        error = null;

        try {
            const userId = await authContainer.useCases.accounts.linkGoogleAccount({
                email: googleProfile.email,
                currentPassword: linkPassword,
                googleSub: googleProfile.sub,
                name: googleProfile.name || googleProfile.email.split("@")[0] || "Usuario",
                photoUrl: googleProfile.picture || ""
            });
            const currentUser = await authContainer.useCases.accounts.getCurrentUser();

            linkOpen = false;
            linkPassword = "";
            const authContext = {
                userId,
                email: googleProfile.email,
                provider: "google"
            } as const;
            if (await maybeHandleAdminChoice(currentUser, authContext)) return;
            completeClientLogin(authContext);
        } catch (e: any) {
            const code = typeof e?.code === "number" ? e.code : null;
            linkError = code === 401
                ? "Contrasena incorrecta."
                : (e instanceof Error ? e.message : "No se pudo vincular la cuenta.");
            authFlowStore.setError(linkError, {
                email: googleProfile.email,
                provider: "google"
            });
            toastStore.error(linkError);
        } finally {
            loading = false;
        }
    }

    async function continueWithGoogle() {
        if (loading) return;
        error = null;
        try {
            googleAuthSrc = getGoogleAuthSrc();
            googleFrameOpen = true;
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesion con Google";
        }
    }

    function goToRegister() {
        navController.navigate("register");
    }

    async function registerStoreFromGoogle(profile: GoogleIdTokenProfile) {
        const sanitizedProfile = {
            ...profile,
            email: profile.email.trim().toLowerCase()
        };
        loading = true;
        error = null;

        try {
            await registerStore.createAccount({
                name: sanitizedProfile.name || sanitizedProfile.email.split("@")[0] || "Usuario",
                email: sanitizedProfile.email,
                password: sanitizedProfile.sub,
                phone: "",
                photo_url: sanitizedProfile.picture,
                role: "viewer",
                sub: sanitizedProfile.sub,
                verification: true
            });
            const current = await authContainer.useCases.accounts.getCurrentUser();
            const authContext = {
                userId: current.id ?? "",
                email: sanitizedProfile.email ?? "",
                provider: "google"
            } as const;
            if (await maybeHandleAdminChoice(current, authContext)) return;
            completeClientLogin(authContext);
        } catch (e: any) {
            const code = typeof e?.code === "number" ? e.code : null;
            if (code === 409) {
                registerFrameOpen = false;
                linkPassword = "";
                linkError = null;
                linkOpen = true;
                return;
            }
            error = e instanceof Error ? e.message : "No se pudo crear la cuenta";
            authFlowStore.setError(error, {
                email: sanitizedProfile.email,
                provider: "google"
            });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    function closeGoogleFrame() {
        googleFrameOpen = false;
        googleAuthSrc = "";
    }

    function closeRegisterFrame() {
        registerFrameOpen = false;
        googleRegisterSrc = "";
    }

    async function handleGoogleFrameMessage(data: any) {
        if (!data || typeof data !== "object") return;

        if (data.type === "google-cancel") {
            closeGoogleFrame();
            return;
        }

        if (data.type !== "google-credential" || typeof data.credential !== "string") return;

        closeGoogleFrame();

        try {
            const profile = parseGoogleIdToken(data.credential);
            await handleGoogleProfile(profile);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo leer la credencial de Google";
            authFlowStore.setError(error, { provider: "google" });
            toastStore.error(error);
        }
    }

    async function handleRegisterFrameMessage(data: any) {
        if (!data || typeof data !== "object") return;

        if (data.type === "google-register-cancel") {
            closeRegisterFrame();
            return;
        }

        if (data.type === "google-register-accept" && googleProfile) {
            closeRegisterFrame();
            await registerStoreFromGoogle(googleProfile);
        }
    }

    function closeLinkDialog() {
        linkOpen = false;
        linkPassword = "";
        linkError = null;
    }

</script>

<Screen ariaLabel="Login" scrollable={false}>
    <main class="login-screen-mobile">
        <section class="login-brand">
            <div class="login-indicator-wrap">
                <LoadingIndicator size={132} aria-label="Cargando" />
                <img class="login-logo" src="/alejoicon_clean.svg" alt="Logo de la aplicacion" />
            </div>

            <h2>Alejo Taller</h2>
            <p>Accede con tu cuenta para continuar</p>

            <footer class="signature signature-desktop">
                <p>Disenado por ELITEC X</p>
                <p>+5356628793</p>
            </footer>
        </section>

        <div class="login-card">
            <Card variant="filled">
                <div class="login-card-content">
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
                                title: showPassword ? "Ocultar contrasena" : "Mostrar contrasena"
                            }}
                            enter={signIn}
                        />
                    </div>

                    <div class="action-row">
                        <Button variant="filled" size="m" disabled={!canSubmit} onclick={signIn}>
                            <span class="btn-content">
                                <span>Entrar</span>
                                <ArrowRightToLine size={18} />
                            </span>
                        </Button>
                    </div>

                    <div class="action-row">
                        <Button variant="filled" size="m" disabled={loading} onclick={continueWithGoogle}>
                            <span class="btn-content btn-google">
                                <span>Google</span>
                                <img
                                    class="google-icon"
                                    src="/icon/googleIcon.png"
                                    alt=""
                                    aria-hidden="true"
                                />
                            </span>
                        </Button>
                    </div>

                    <div class="action-row">
                        <Button variant="text" size="m" onclick={goToRegister}>
                            No tienes cuenta? Registrate
                        </Button>
                    </div>

                    {#if error}
                        <p class="error-copy">{error}</p>
                    {/if}
                </div>
            </Card>
        </div>

        <footer class="signature">
            <p>Disenado por ELITEC X</p>
            <p>+5356628793</p>
        </footer>
    </main>
</Screen>

{#if pendingAdminUser}
    <AdminRoleChoiceCard
        busy={loading}
        on:stayClient={continueAsClient}
        on:goAdmin={continueToAdmin}
    />
{/if}

<FrameModal
    open={googleFrameOpen}
    title="Continuar con Google"
    ariaLabel="Autenticacion con Google"
    src={googleFrameOpen ? googleAuthSrc : ""}
    on:close={closeGoogleFrame}
    on:frameMessage={(event) => handleGoogleFrameMessage(event.detail.data)}
/>

<FrameModal
    open={registerFrameOpen}
    title="Crear cuenta con Google"
    ariaLabel="Registro con Google"
    src={registerFrameOpen ? googleRegisterSrc : ""}
    on:close={closeRegisterFrame}
    on:frameMessage={(event) => handleRegisterFrameMessage(event.detail.data)}
/>

{#if linkOpen}
    <div class="link-modal">
        <button class="link-scrim" type="button" aria-label="Cerrar dialogo" on:click={closeLinkDialog}></button>
        <div class="link-panel">
            <Card variant="filled">
                <div class="link-card-content">
                    <div class="link-copy">
                        <h3>Confirma tu contrasena</h3>
                        <p>
                            Esta cuenta ya existe con una contrasena personalizada. Usa tu password actual para
                            vincular Google y continuar.
                        </p>
                    </div>

                    <div class="field-wrap">
                        <TextFieldOutlined
                            label="Contrasena actual"
                            bind:value={linkPassword}
                            type="password"
                            leadingIcon={LockOutline}
                            enter={linkGoogleAccount}
                        />
                    </div>

                    {#if linkError}
                        <p class="error-copy">{linkError}</p>
                    {/if}

                    <div class="link-actions">
                        <Button variant="text" size="m" onclick={closeLinkDialog}>Cancelar</Button>
                        <Button variant="filled" size="m" disabled={loading || !linkPassword.trim()} onclick={linkGoogleAccount}>
                            Vincular y entrar
                        </Button>
                    </div>
                </div>
            </Card>
        </div>
    </div>
{/if}

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
        max-height: 100%;
        padding:
            max(14px, calc(env(safe-area-inset-top) + 8px))
            16px
            max(16px, calc(env(safe-area-inset-bottom) + 10px));
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        gap: clamp(10px, 1.8vh, 18px);
        overflow-x: hidden;
        overflow-y: hidden;
        color: var(--md-sys-color-on-background);
        background:
            radial-gradient(
                circle at 50% 12%,
                color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent),
                transparent 32%
            ),
            linear-gradient(
                180deg,
                color-mix(in srgb, var(--md-sys-color-primary-container) 18%, var(--md-sys-color-background)) 0%,
                color-mix(in srgb, var(--md-sys-color-surface-container-low) 74%, var(--md-sys-color-background)) 58%,
                var(--md-sys-color-background) 100%
            );
    }

    .login-brand {
        display: grid;
        justify-items: center;
        text-align: center;
        gap: 8px;
        align-content: center;
        min-width: 0;
    }

    .login-brand h2 {
        margin: 0;
        font-size: clamp(1.95rem, 8vw, 2.5rem);
        line-height: 1.1;
    }

    .login-brand p {
        margin: 0;
        color: var(--md-sys-color-on-surface-variant);
        font-size: clamp(0.95rem, 3.8vw, 1.08rem);
    }

    .login-indicator-wrap {
        position: relative;
        width: clamp(108px, 28vw, 136px);
        height: clamp(108px, 28vw, 136px);
        display: grid;
        place-items: center;
        color: var(--md-sys-color-primary);
    }

    .login-logo {
        position: absolute;
        width: clamp(50px, 13vw, 64px);
        height: clamp(50px, 13vw, 64px);
        object-fit: contain;
        filter: drop-shadow(0 8px 20px rgb(0 0 0 / 0.25));
        pointer-events: none;
    }

    .login-card {
        width: 100%;
        max-width: 420px;
        margin-inline: auto;
        min-width: 0;
    }

    .login-card-content {
        padding: 16px;
        display: grid;
        gap: 12px;
        min-width: 0;
    }

    .login-card :global(.m3-container.filled) {
        border-radius: 20px;
        background: var(--md-sys-color-surface-container-high);
        --m3v-background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 16px 36px rgb(0 0 0 / 0.22);
    }

    .field-wrap {
        display: grid;
    }

    .field-wrap :global(.m3-container) {
        width: 100%;
        min-width: 0;
        height: 56px;
        --m3-field-outlined-shape: 0.85rem;
        --m3v-background: var(--md-sys-color-surface-container);
    }

    .field-wrap :global(input) {
        min-height: 56px;
        color: var(--md-sys-color-on-surface);
    }

    .field-wrap :global(label) {
        color: var(--md-sys-color-on-surface-variant);
    }

    .field-wrap :global(.layer) {
        border-color: var(--md-sys-color-outline);
        border-width: 1.5px;
    }

    .field-wrap :global(.leading),
    .field-wrap :global(.trailing) {
        color: var(--md-sys-color-on-surface-variant);
    }

    .field-wrap :global(input:focus ~ .layer) {
        border-color: var(--md-sys-color-primary);
    }

    .field-wrap :global(input:focus ~ label) {
        color: var(--md-sys-color-primary);
    }

    .action-row :global(.m3-container) {
        width: 100%;
        min-width: 0;
        max-width: 100%;
    }

    .action-row :global(.m3-container.m) {
        padding-inline: clamp(1.35rem, 4vw, 1.85rem);
    }

    .btn-content {
        width: 100%;
        min-width: 0;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        font-weight: 500;
        padding: 12px 0;
    }

    .google-icon {
        width: 18px;
        height: 18px;
        object-fit: contain;
        flex: 0 0 auto;
    }

    .error-copy {
        margin: 4px 0 0;
        color: var(--md-sys-color-error);
        font-size: 0.9rem;
        text-align: center;
    }

    .signature {
        display: grid;
        justify-items: center;
        gap: 2px;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.78rem;
        padding-bottom: 2px;
    }

    .signature p {
        margin: 0;
    }

    .signature-desktop {
        display: none;
    }

    .link-modal {
        position: fixed;
        inset: 0;
        z-index: 1200;
        display: grid;
        place-items: center;
        padding: 16px;
    }

    .link-scrim {
        position: absolute;
        inset: 0;
        border: 0;
        background: color-mix(in srgb, black 44%, transparent);
        backdrop-filter: blur(6px);
    }

    .link-panel {
        position: relative;
        z-index: 1;
        width: min(460px, calc(100vw - 24px));
    }

    .link-panel :global(.m3-container.filled) {
        border-radius: 24px;
        background: var(--md-sys-color-surface-container-high);
        --m3v-background: var(--md-sys-color-surface-container-high);
    }

    .link-card-content {
        display: grid;
        gap: 14px;
        padding: 18px;
    }

    .link-copy {
        display: grid;
        gap: 6px;
    }

    .link-copy h3,
    .link-copy p {
        margin: 0;
    }

    .link-copy p {
        color: var(--md-sys-color-on-surface-variant);
        line-height: 1.45;
    }

    .link-actions {
        display: flex;
        flex-wrap: wrap;
        justify-content: flex-end;
        gap: 10px;
    }

    @media (max-height: 760px) {
        .login-screen-mobile {
            gap: 8px;
            padding-top: max(10px, calc(env(safe-area-inset-top) + 4px));
            padding-bottom: max(10px, calc(env(safe-area-inset-bottom) + 6px));
        }

        .login-brand h2 {
            font-size: clamp(1.7rem, 7vw, 2.2rem);
        }

        .login-card-content {
            gap: 10px;
            padding: 12px;
        }

        .field-wrap :global(.m3-container) {
            height: 54px;
        }

        .field-wrap :global(input) {
            min-height: 54px;
        }
    }

    @media (min-width: 800px) {
        .login-screen-mobile {
            max-width: 1180px;
            margin: 0 auto;
            padding:
                max(18px, calc(env(safe-area-inset-top) + 10px))
                28px
                max(18px, calc(env(safe-area-inset-bottom) + 10px));
            display: grid;
            grid-template-columns: minmax(320px, 0.95fr) minmax(380px, 0.9fr);
            align-items: center;
            justify-content: center;
            justify-items: center;
            gap: clamp(24px, 4vw, 56px);
        }

        .login-card {
            max-width: 548px;
            width: 100%;
        }

        .login-brand {
            width: 100%;
            max-width: 420px;
            justify-items: center;
            text-align: center;
            align-content: center;
            min-height: 0;
            gap: 12px;
        }

        .login-brand h2 {
            font-size: clamp(2.6rem, 4.5vw, 4rem);
        }

        .login-brand p {
            font-size: clamp(1.05rem, 1.6vw, 1.3rem);
        }

        .login-indicator-wrap {
            width: 148px;
            height: 148px;
        }

        .login-logo {
            width: 68px;
            height: 68px;
        }

        .signature {
            display: none;
        }

        .signature-desktop {
            display: grid;
            justify-items: center;
            font-size: 0.68rem;
            gap: 1px;
            opacity: 0.82;
        }

        .signature-desktop p {
            margin: 0;
        }
    }

    @media (max-width: 360px) {
        .login-indicator-wrap {
            width: 100px;
            height: 100px;
        }
    }
</style>
