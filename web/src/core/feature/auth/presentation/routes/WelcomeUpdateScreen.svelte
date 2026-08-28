<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import type { GoogleIdTokenProfile } from "../util/google-id-token";
    import { ENV } from "../../../../infrastructure/env";
    import { registerStore } from "../viewmodel/register.store";
    import { Button, TextFieldOutlined } from "m3-svelte";
    import MailOutlineRounded from "@ktibow/iconset-material-symbols/mail-outline-rounded";
    import LockOutline from "@ktibow/iconset-material-symbols/lock-outline";
    import PersonRounded from "@ktibow/iconset-material-symbols/person-rounded";
    import VisibilityRounded from "@ktibow/iconset-material-symbols/visibility-rounded";
    import VisibilityOffRounded from "@ktibow/iconset-material-symbols/visibility-off-rounded";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { parseGoogleIdToken } from "../util/google-id-token";
    import FrameModal from "../components/FrameModal.svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { markWelcomeCompleted } from "../../../../infrastructure/presentation/navigation/first-visit";
    import AdminRoleChoiceCard from "../components/AdminRoleChoiceCard.svelte";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../util/admin-redirect";
    import { sessionStore } from "../viewmodel/session.store";
    import { Package, Truck, Headphones, ShieldCheck, ArrowRight, Eye, X } from "lucide-svelte";

    export let navController: NavController;

    type AuthMode = "login" | "register";
    let authMode: AuthMode = "login";
    let mobileDialogOpen = false;

    let email = "";
    let password = "";
    let name = "";
    let confirmPassword = "";
    let showPassword = false;
    let showConfirmPassword = false;
    let rememberMe = false;
    let loading = false;
    let error: string | null = null;

    let pendingAdminUser: any = null;
    let pendingAuthContext: { userId: string; email: string; provider: "password" | "google" } | null = null;

    let googleFrameOpen = false;
    let registerFrameOpen = false;
    let googleProfile: GoogleIdTokenProfile | null = null;
    let googleAuthSrc = "";
    let googleRegisterSrc = "";
    let linkOpen = false;
    let linkPassword = "";
    let linkError: string | null = null;

    $: canLogin = email.trim().length > 3 && password.trim().length > 3 && !loading;
    $: canRegister =
        name.trim().length > 1 &&
        email.trim().length > 3 &&
        password.trim().length > 3 &&
        confirmPassword === password &&
        !$registerStore.loading &&
        !loading;
    $: normalizedEmail = email.trim().toLowerCase();

    function openMobileAuth(mode: AuthMode) {
        authMode = mode;
        error = null;
        mobileDialogOpen = true;
    }
    function closeMobileDialog() {
        if (loading) return;
        mobileDialogOpen = false;
    }
    function switchMode(mode: AuthMode) {
        authMode = mode;
        error = null;
    }

    function restorePendingHashIfNeeded() {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    function completeClientLogin(context: { userId: string; email: string; provider: "password" | "google" }) {
        sessionStore.setAuthenticatedSession();
        authFlowStore.setSuccess(context);
        markWelcomeCompleted();
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
        if (!canLogin) return;
        loading = true;
        error = null;
        try {
            try {
                await authContainer.useCases.sessions.closeSession.execute();
            } catch {
                /* ignore */
            }
            const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                normalizedEmail,
                password
            );
            const currentUser = await authContainer.useCases.accounts.getCurrentUser();
            const authContext = { userId, email: normalizedEmail, provider: "password" as const };
            if (await maybeHandleAdminChoice(currentUser, authContext)) return;
            completeClientLogin(authContext);
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión";
            authFlowStore.setError(error, { email: normalizedEmail, provider: "password" });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function submitRegister() {
        if (!canRegister) return;
        if (password !== confirmPassword) {
            error = "Las contraseñas no coinciden";
            return;
        }
        loading = true;
        error = null;
        try {
            await registerStore.createAccount({
                name: name.trim(),
                email: normalizedEmail,
                password,
                phone: "",
                photo_url: "",
                role: "viewer",
                verification: false
            });
            toastStore.success("Cuenta creada. Ya puedes iniciar sesión.");
            authMode = "login";
            password = "";
            confirmPassword = "";
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo registrar";
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function continueAsGuest() {
        if (loading) return;
        loading = true;
        error = null;
        try {
            try {
                await authContainer.useCases.sessions.closeSession.execute();
            } catch {
                /* ignore */
            }
            const userId = await authContainer.useCases.sessions.openSession.openGuestSession();
            sessionStore.setGuestSession();
            authFlowStore.setSuccess({ userId, email: null, provider: "guest" });
            markWelcomeCompleted();
            restorePendingHashIfNeeded();
            navController.resetTo("home", { userId, email: null, provider: "guest" });
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo entrar como visitante";
            authFlowStore.setError(error, { provider: "guest" });
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

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

    async function continueWithGoogle() {
        if (loading) return;
        error = null;
        try {
            googleAuthSrc = getGoogleAuthSrc();
            googleFrameOpen = true;
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión con Google";
            toastStore.error(error);
        }
    }

    async function handleGoogleProfile(profile: GoogleIdTokenProfile) {
        const sanitizedProfile = { ...profile, email: profile.email.trim().toLowerCase() };
        googleProfile = sanitizedProfile;
        loading = true;
        error = null;
        try {
            try {
                await authContainer.useCases.sessions.closeSession.execute();
            } catch {
                /* ignore */
            }
            try {
                const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                    sanitizedProfile.email,
                    sanitizedProfile.sub
                );
                const current = await authContainer.useCases.accounts.getCurrentUser();
                const currentPhoto =
                    typeof current?.photo_url === "string" ? current.photo_url.trim() : "";
                if (!currentPhoto && sanitizedProfile.picture?.trim()) {
                    await authContainer.useCases.accounts.updatePhotoUrl(sanitizedProfile.picture.trim());
                }
                const authContext = {
                    userId,
                    email: sanitizedProfile.email,
                    provider: "google" as const
                };
                if (await maybeHandleAdminChoice(current, authContext)) return;
                completeClientLogin(authContext);
            } catch {
                googleRegisterSrc = getGoogleRegisterSrc(sanitizedProfile);
                registerFrameOpen = true;
            }
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión con Google";
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function registerStoreFromGoogle(profile: GoogleIdTokenProfile) {
        const sanitizedProfile = { ...profile, email: profile.email.trim().toLowerCase() };
        loading = true;
        error = null;
        try {
            try {
                await authContainer.useCases.sessions.closeSession.execute();
            } catch {
                /* ignore */
            }
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
                provider: "google" as const
            };
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
            toastStore.error(error);
        } finally {
            loading = false;
        }
    }

    async function linkGoogleAccount() {
        if (!googleProfile || !linkPassword.trim()) {
            linkError = "Ingresa tu contraseña actual.";
            return;
        }
        loading = true;
        linkError = null;
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
                provider: "google" as const
            };
            if (await maybeHandleAdminChoice(currentUser, authContext)) return;
            completeClientLogin(authContext);
        } catch (e: any) {
            const code = typeof e?.code === "number" ? e.code : null;
            linkError =
                code === 401
                    ? "Contraseña incorrecta."
                    : e instanceof Error
                      ? e.message
                      : "No se pudo vincular la cuenta.";
            toastStore.error(linkError);
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
            await handleGoogleProfile(parseGoogleIdToken(data.credential));
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo leer la credencial de Google";
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

    const trustItems = [
        { icon: Package, label: "Miles de productos" },
        { icon: Truck, label: "Envíos rápidos y seguros" },
        { icon: Headphones, label: "Soporte especializado" },
        { icon: ShieldCheck, label: "Pagos 100% seguros" }
    ];
</script>

<AuthBusyOverlay
    open={loading}
    title={authMode === "register" ? "Creando tu cuenta…" : "Validando acceso…"}
    subtitle="Esto suele tardar solo un momento."
/>

<div class="wu-root" aria-label="Bienvenida y acceso">
    <div class="wu-shell">
        <section class="wu-hero">
            <header class="wu-brand">
                <img src="/alejoicon_clean.svg" alt="" class="wu-logo" />
                <div>
                    <strong>Taller Alejo</strong>
                    <span>Electrónica & Tecnología</span>
                </div>
            </header>
            <p class="wu-badge">Tu tienda de confianza</p>
            <h1 class="wu-title">Componentes electrónicos<br />para <em>tus ideas</em>.</h1>
            <p class="wu-lead">
                Descubre miles de productos de electrónica, baterías, BMS, herramientas y más. Calidad,
                precio y soporte en un solo lugar.
            </p>
            <ul class="wu-trust" role="list">
                {#each trustItems as t}
                    <li>
                        <span class="wu-trust-icon"><svelte:component this={t.icon} size={18} /></span>
                        <span>{t.label}</span>
                    </li>
                {/each}
            </ul>
            <div class="wu-showcase" aria-hidden="true">
                <img
                    class="wu-shot"
                    src="https://commons.wikimedia.org/wiki/Special:FilePath/Electronic_components_(8370189100).jpg"
                    alt=""
                />
            </div>
            <p class="wu-social-proof">Más de 2,500 clientes satisfechos</p>
        </section>

        <aside class="wu-auth-panel desktop-auth">
            {#if authMode === "login"}
                <h2>Bienvenido de nuevo 👋</h2>
                <p class="wu-auth-sub">Inicia sesión para continuar</p>
            {:else}
                <h2>Crea tu cuenta</h2>
                <p class="wu-auth-sub">Regístrate para comprar y reservar</p>
            {/if}
            <div class="wu-fields">
                {#if authMode === "register"}
                    <TextFieldOutlined label="Nombre" bind:value={name} leadingIcon={PersonRounded} enter={submitRegister} />
                {/if}
                <TextFieldOutlined label="Correo electrónico" bind:value={email} type="email" leadingIcon={MailOutlineRounded} enter={authMode === "login" ? signIn : submitRegister} />
                <TextFieldOutlined label="Contraseña" bind:value={password} type={showPassword ? "text" : "password"} leadingIcon={LockOutline} trailing={{ icon: showPassword ? VisibilityOffRounded : VisibilityRounded, onclick: () => (showPassword = !showPassword), "aria-label": "Toggle password", title: "Toggle" }} enter={authMode === "login" ? signIn : submitRegister} />
                {#if authMode === "register"}
                    <TextFieldOutlined label="Confirmar contraseña" bind:value={confirmPassword} type={showConfirmPassword ? "text" : "password"} leadingIcon={LockOutline} trailing={{ icon: showConfirmPassword ? VisibilityOffRounded : VisibilityRounded, onclick: () => (showConfirmPassword = !showConfirmPassword), "aria-label": "Toggle", title: "Toggle" }} enter={submitRegister} />
                {/if}
            </div>
            {#if authMode === "login"}
                <label class="wu-remember"><input type="checkbox" bind:checked={rememberMe} /> Recordarme</label>
            {/if}
            {#if error}<p class="wu-error">{error}</p>{/if}
            {#if authMode === "login"}
                <Button variant="filled" size="m" disabled={!canLogin} onclick={signIn}><span class="wu-btn-inner">Iniciar sesión <ArrowRight size={18} /></span></Button>
                <div class="wu-or"><span>o continúa con</span></div>
                <Button variant="outlined" size="m" disabled={loading} onclick={continueWithGoogle}><span class="wu-btn-inner"><img class="g-icon" src="/icon/googleIcon.png" alt="" /> Google</span></Button>
                <p class="wu-switch">¿No tienes cuenta? <button type="button" class="wu-link" on:click={() => switchMode("register")}>Crear cuenta</button></p>
            {:else}
                <Button variant="filled" size="m" disabled={!canRegister} onclick={submitRegister}><span class="wu-btn-inner">Registrarse</span></Button>
                <p class="wu-switch">¿Ya tienes cuenta? <button type="button" class="wu-link" on:click={() => switchMode("login")}>Inicia sesión</button></p>
            {/if}
            <button type="button" class="wu-guest" disabled={loading} on:click={continueAsGuest}><Eye size={16} /> Explorar como visitante</button>
        </aside>

        <div class="mobile-cta">
            <Button variant="filled" size="m" onclick={() => openMobileAuth("login")}><span class="wu-btn-inner">Iniciar sesión <ArrowRight size={18} /></span></Button>
            <p class="wu-switch">¿No tienes cuenta? <button type="button" class="wu-link" on:click={() => openMobileAuth("register")}>Crear cuenta</button></p>
            <button type="button" class="wu-guest" disabled={loading} on:click={continueAsGuest}><Eye size={16} /> Explorar como visitante</button>
        </div>
    </div>
</div>

{#if mobileDialogOpen}
    <div class="wu-dialog" role="dialog" aria-modal="true" aria-labelledby="wu-dialog-title">
        <button class="wu-dialog-scrim" type="button" aria-label="Cerrar" on:click={closeMobileDialog}></button>
        <div class="wu-dialog-panel">
            <header class="wu-dialog-head">
                <h2 id="wu-dialog-title">{authMode === "login" ? "Bienvenido de nuevo 👋" : "Crear cuenta"}</h2>
                <button type="button" class="wu-dialog-close" aria-label="Cerrar" on:click={closeMobileDialog}><X size={20} /></button>
            </header>
            <p class="wu-auth-sub">{authMode === "login" ? "Inicia sesión para continuar" : "Regístrate para comprar y reservar"}</p>
            <div class="wu-fields">
                {#if authMode === "register"}
                    <TextFieldOutlined label="Nombre" bind:value={name} leadingIcon={PersonRounded} enter={submitRegister} />
                {/if}
                <TextFieldOutlined label="Correo electrónico" bind:value={email} type="email" leadingIcon={MailOutlineRounded} enter={authMode === "login" ? signIn : submitRegister} />
                <TextFieldOutlined label="Contraseña" bind:value={password} type={showPassword ? "text" : "password"} leadingIcon={LockOutline} trailing={{ icon: showPassword ? VisibilityOffRounded : VisibilityRounded, onclick: () => (showPassword = !showPassword), "aria-label": "Toggle", title: "Toggle" }} enter={authMode === "login" ? signIn : submitRegister} />
                {#if authMode === "register"}
                    <TextFieldOutlined label="Confirmar contraseña" bind:value={confirmPassword} type={showConfirmPassword ? "text" : "password"} leadingIcon={LockOutline} trailing={{ icon: showConfirmPassword ? VisibilityOffRounded : VisibilityRounded, onclick: () => (showConfirmPassword = !showConfirmPassword), "aria-label": "Toggle", title: "Toggle" }} enter={submitRegister} />
                {/if}
            </div>
            {#if error}<p class="wu-error">{error}</p>{/if}
            {#if authMode === "login"}
                <Button variant="filled" size="m" disabled={!canLogin} onclick={signIn}><span class="wu-btn-inner">Iniciar sesión <ArrowRight size={18} /></span></Button>
                <div class="wu-or"><span>o continúa con</span></div>
                <Button variant="outlined" size="m" disabled={loading} onclick={continueWithGoogle}><span class="wu-btn-inner"><img class="g-icon" src="/icon/googleIcon.png" alt="" /> Google</span></Button>
                <p class="wu-switch">¿No tienes cuenta? <button type="button" class="wu-link" on:click={() => switchMode("register")}>Crear cuenta</button></p>
            {:else}
                <Button variant="filled" size="m" disabled={!canRegister} onclick={submitRegister}><span class="wu-btn-inner">Registrarse</span></Button>
                <p class="wu-switch">¿Ya tienes cuenta? <button type="button" class="wu-link" on:click={() => switchMode("login")}>Inicia sesión</button></p>
            {/if}
        </div>
    </div>
{/if}

{#if pendingAdminUser}
    <AdminRoleChoiceCard busy={loading} on:stayClient={continueAsClient} on:goAdmin={continueToAdmin} />
{/if}

<FrameModal open={googleFrameOpen} title="Continuar con Google" ariaLabel="Autenticación con Google" src={googleFrameOpen ? googleAuthSrc : ""} on:close={closeGoogleFrame} on:frameMessage={(event) => handleGoogleFrameMessage(event.detail.data)} />
<FrameModal open={registerFrameOpen} title="Crear cuenta con Google" ariaLabel="Registro con Google" src={registerFrameOpen ? googleRegisterSrc : ""} on:close={closeRegisterFrame} on:frameMessage={(event) => handleRegisterFrameMessage(event.detail.data)} />

{#if linkOpen}
    <div class="wu-dialog">
        <button class="wu-dialog-scrim" type="button" aria-label="Cerrar" on:click={() => (linkOpen = false)}></button>
        <div class="wu-dialog-panel">
            <h3>Confirma tu contraseña</h3>
            <p class="wu-auth-sub">Esta cuenta ya existe. Usa tu password actual para vincular Google.</p>
            <TextFieldOutlined label="Contraseña actual" bind:value={linkPassword} type="password" leadingIcon={LockOutline} enter={linkGoogleAccount} />
            {#if linkError}<p class="wu-error">{linkError}</p>{/if}
            <div class="wu-link-actions">
                <Button variant="text" size="m" onclick={() => (linkOpen = false)}>Cancelar</Button>
                <Button variant="filled" size="m" disabled={loading || !linkPassword.trim()} onclick={linkGoogleAccount}>Vincular y entrar</Button>
            </div>
        </div>
    </div>
{/if}

<style>
    .wu-root {
        min-height: 100dvh; width: 100%; box-sizing: border-box;
        padding: max(16px, env(safe-area-inset-top)) 16px max(20px, env(safe-area-inset-bottom));
        background:
            radial-gradient(ellipse at 20% 30%, color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent), transparent 50%),
            radial-gradient(ellipse at 80% 70%, color-mix(in srgb, #0d3d2a 40%, transparent), transparent 45%),
            var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
    }
    .wu-shell { width: min(1120px, 100%); margin: 0 auto; display: grid; gap: 24px; align-items: stretch; }
    .wu-hero { display: grid; gap: 14px; align-content: start; }
    .wu-brand { display: flex; align-items: center; gap: 12px; }
    .wu-logo { width: 44px; height: 44px; object-fit: contain; }
    .wu-brand strong { display: block; font-size: 1.05rem; font-weight: 800; }
    .wu-brand span { font-size: 0.78rem; color: var(--md-sys-color-on-surface-variant); }
    .wu-badge {
        margin: 8px 0 0; display: inline-flex; width: fit-content; padding: 4px 12px; border-radius: 999px;
        font-size: 0.75rem; font-weight: 700;
        background: color-mix(in srgb, var(--md-sys-color-primary) 16%, transparent);
        color: var(--md-sys-color-primary);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent);
    }
    .wu-title { margin: 0; font-size: clamp(1.75rem, 5vw, 2.75rem); font-weight: 800; letter-spacing: -0.03em; line-height: 1.12; }
    .wu-title em { font-style: normal; color: var(--md-sys-color-primary); }
    .wu-lead { margin: 0; max-width: 34rem; font-size: 0.95rem; line-height: 1.5; color: var(--md-sys-color-on-surface-variant); }
    .wu-trust { list-style: none; margin: 8px 0 0; padding: 0; display: grid; grid-template-columns: 1fr 1fr; gap: 10px 14px; }
    .wu-trust li { display: flex; align-items: center; gap: 8px; font-size: 0.8rem; font-weight: 600; color: var(--md-sys-color-on-surface-variant); }
    .wu-trust-icon {
        width: 32px; height: 32px; border-radius: 10px; display: grid; place-items: center; flex-shrink: 0;
        color: var(--md-sys-color-primary); background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
    }
    .wu-showcase { margin-top: 8px; border-radius: 20px; overflow: hidden; max-height: 180px; box-shadow: 0 20px 48px rgba(0,0,0,.35); }
    .wu-shot { width: 100%; height: 180px; object-fit: cover; display: block; }
    .wu-social-proof { margin: 4px 0 0; font-size: 0.8rem; color: var(--md-sys-color-on-surface-variant); }
    .wu-auth-panel, .wu-dialog-panel {
        display: grid; gap: 12px; padding: 22px 20px; border-radius: 20px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 92%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow: 0 24px 48px rgba(0,0,0,.28);
    }
    .wu-auth-panel h2, .wu-dialog-head h2 { margin: 0; font-size: 1.25rem; font-weight: 800; }
    .wu-auth-sub { margin: 0; font-size: 0.88rem; color: var(--md-sys-color-on-surface-variant); }
    .wu-fields { display: grid; gap: 10px; }
    .wu-fields :global(.m3-container) { width: 100%; height: 52px; }
    .wu-remember { display: flex; align-items: center; gap: 8px; font-size: 0.85rem; font-weight: 600; cursor: pointer; }
    .wu-error { margin: 0; color: var(--md-sys-color-error); font-size: 0.88rem; text-align: center; }
    .wu-btn-inner { display: inline-flex; align-items: center; justify-content: center; gap: 8px; width: 100%; padding: 12px 0; font-weight: 700; }
    .wu-auth-panel :global(.m3-container), .wu-dialog-panel :global(.m3-container), .mobile-cta :global(.m3-container) { width: 100%; }
    .wu-or { display: flex; align-items: center; gap: 10px; color: var(--md-sys-color-on-surface-variant); font-size: 0.78rem; }
    .wu-or::before, .wu-or::after { content: ""; flex: 1; height: 1px; background: var(--md-sys-color-outline-variant); }
    .g-icon { width: 18px; height: 18px; object-fit: contain; }
    .wu-switch { margin: 0; text-align: center; font-size: 0.88rem; color: var(--md-sys-color-on-surface-variant); }
    .wu-link { border: 0; background: none; color: var(--md-sys-color-primary); font: inherit; font-weight: 750; cursor: pointer; padding: 0; }
    .wu-guest { display: inline-flex; align-items: center; justify-content: center; gap: 8px; width: 100%; border: 0; background: transparent; color: var(--md-sys-color-on-surface-variant); font: inherit; font-size: 0.88rem; font-weight: 650; cursor: pointer; padding: 8px; }
    .wu-guest:hover { color: var(--md-sys-color-primary); }
    .mobile-cta { display: grid; gap: 12px; padding: 8px 0 0; }
    .desktop-auth { display: none; }
    .wu-dialog { position: fixed; inset: 0; z-index: 1100; display: grid; place-items: end center; }
    .wu-dialog-scrim { position: absolute; inset: 0; border: 0; background: color-mix(in srgb, black 50%, transparent); backdrop-filter: blur(6px); }
    .wu-dialog-panel { position: relative; z-index: 1; width: 100%; max-height: 92dvh; overflow: auto; border-radius: 24px 24px 0 0; padding-bottom: max(20px, env(safe-area-inset-bottom)); }
    .wu-dialog-head { display: flex; justify-content: space-between; align-items: center; gap: 12px; }
    .wu-dialog-close { width: 40px; height: 40px; border: 0; border-radius: 12px; background: color-mix(in srgb, var(--md-sys-color-on-surface) 8%, transparent); color: inherit; display: grid; place-items: center; cursor: pointer; }
    .wu-link-actions { display: flex; justify-content: flex-end; gap: 8px; flex-wrap: wrap; }
    @media (min-width: 900px) {
        .wu-root { display: grid; place-items: center; padding: 28px; }
        .wu-shell { grid-template-columns: minmax(0, 1.15fr) minmax(340px, 420px); gap: 40px; align-items: center; }
        .wu-showcase { max-height: 260px; }
        .wu-shot { height: 260px; }
        .desktop-auth { display: grid; }
        .mobile-cta { display: none; }
        .wu-dialog { place-items: center; padding: 16px; }
        .wu-dialog-panel { width: min(440px, 100%); border-radius: 20px; max-height: 90dvh; }
    }
</style>
