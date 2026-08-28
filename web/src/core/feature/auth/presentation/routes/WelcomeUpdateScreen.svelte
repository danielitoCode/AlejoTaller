<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import type { GoogleIdTokenProfile } from "../util/google-id-token";
    import { ENV } from "../../../../infrastructure/env";
    import { registerStore } from "../viewmodel/register.store";
    import { Button } from "m3-svelte";
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
    import {
        Package,
        Truck,
        Headphones,
        ShieldCheck,
        ArrowRight,
        Eye,
        EyeOff,
        X,
        Star,
        Mail,
        Lock,
        User
    } from "lucide-svelte";

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

    function onFieldKey(e: KeyboardEvent, action: () => void) {
        if (e.key === "Enter") {
            e.preventDefault();
            action();
        }
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
        { icon: Truck, label: "Envíos rápidos\ny seguros" },
        { icon: Headphones, label: "Soporte\nespecializado" },
        { icon: ShieldCheck, label: "Pagos 100%\nseguros" }
    ];

    const heroImageUrl =
        "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1400&q=80";

    const sampleAvatars = [
        "https://i.pravatar.cc/64?img=12",
        "https://i.pravatar.cc/64?img=32",
        "https://i.pravatar.cc/64?img=47",
        "https://i.pravatar.cc/64?img=5",
        "https://i.pravatar.cc/64?img=68"
    ];
</script>

<AuthBusyOverlay
    open={loading}
    title={authMode === "register" ? "Creando tu cuenta…" : "Validando acceso…"}
    subtitle="Esto suele tardar solo un momento."
/>

<div class="wu-root" aria-label="Bienvenida y acceso">
    <div class="wu-frame">
        <div class="wu-shell">
            <section class="wu-hero">
                <header class="wu-brand">
                    <div class="wu-logo-wrap">
                        <img src="/alejoicon_clean.svg" alt="" class="wu-logo" />
                    </div>
                    <div class="wu-brand-text">
                        <strong>Taller Alejo</strong>
                        <span>Electrónica & Tecnología</span>
                    </div>
                </header>

                <p class="wu-badge"><span class="wu-badge-dot"></span> Tu tienda de confianza</p>

                <h1 class="wu-title">
                    Componentes electrónicos<br />
                    para <em>tus ideas</em>.
                </h1>

                <p class="wu-lead">
                    Descubre miles de productos de electrónica, baterías, BMS, herramientas y más.
                    Calidad, precio y soporte en un solo lugar.
                </p>

                <ul class="wu-trust" role="list">
                    {#each trustItems as t}
                        <li>
                            <span class="wu-trust-icon"><svelte:component this={t.icon} size={20} /></span>
                            <span class="wu-trust-label">{t.label}</span>
                        </li>
                    {/each}
                </ul>

                <div class="wu-stage" aria-hidden="true">
                    <div class="wu-stage-glow"></div>
                    <div class="wu-stage-plate">
                        <img
                            class="wu-shot"
                            src={heroImageUrl}
                            alt="Componentes y circuitos electrónicos"
                            loading="eager"
                            decoding="async"
                        />
                    </div>
                </div>

                <div class="wu-social-proof">
                    <div class="wu-avatars" aria-hidden="true">
                        {#each sampleAvatars as src, i}
                            <img class="av" src={src} alt="" style="z-index: {sampleAvatars.length - i}" />
                        {/each}
                    </div>
                    <div class="wu-social-meta">
                        <span class="wu-stars" aria-hidden="true">
                            <Star size={12} /><Star size={12} /><Star size={12} /><Star size={12} /><Star size={12} />
                        </span>
                        <span>Más de 2,500 clientes satisfechos</span>
                    </div>
                </div>
            </section>

            <aside class="wu-auth-panel desktop-auth">
                {#if authMode === "login"}
                    <h2>Bienvenido de nuevo 👋</h2>
                    <p class="wu-auth-sub">Inicia sesión para continuar</p>
                {:else}
                    <h2>Crea tu cuenta</h2>
                    <p class="wu-auth-sub">Regístrate para comprar y reservar</p>
                {/if}

                <form
                    class="wu-fields"
                    on:submit|preventDefault={authMode === "login" ? signIn : submitRegister}
                >
                    {#if authMode === "register"}
                        <label class="wu-field">
                            <span class="wu-field-label">Nombre</span>
                            <div class="wu-field-box">
                                <span class="wu-field-icon"><User size={18} /></span>
                                <input
                                    type="text"
                                    bind:value={name}
                                    placeholder="Tu nombre"
                                    autocomplete="name"
                                    on:keydown={(e) => onFieldKey(e, submitRegister)}
                                />
                            </div>
                        </label>
                    {/if}

                    <label class="wu-field">
                        <span class="wu-field-label">Correo electrónico</span>
                        <div class="wu-field-box">
                            <span class="wu-field-icon"><Mail size={18} /></span>
                            <input
                                type="email"
                                bind:value={email}
                                placeholder="tu@correo.com"
                                autocomplete="email"
                                on:keydown={(e) =>
                                    onFieldKey(e, authMode === "login" ? signIn : submitRegister)}
                            />
                        </div>
                    </label>

                    <label class="wu-field">
                        <span class="wu-field-label">Contraseña</span>
                        <div class="wu-field-box">
                            <span class="wu-field-icon"><Lock size={18} /></span>
                            <input
                                type={showPassword ? "text" : "password"}
                                bind:value={password}
                                placeholder="••••••••"
                                autocomplete={authMode === "login" ? "current-password" : "new-password"}
                                on:keydown={(e) =>
                                    onFieldKey(e, authMode === "login" ? signIn : submitRegister)}
                            />
                            <button
                                type="button"
                                class="wu-field-toggle"
                                aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                                on:click={() => (showPassword = !showPassword)}
                            >
                                {#if showPassword}<EyeOff size={18} />{:else}<Eye size={18} />{/if}
                            </button>
                        </div>
                    </label>

                    {#if authMode === "register"}
                        <label class="wu-field">
                            <span class="wu-field-label">Confirmar contraseña</span>
                            <div class="wu-field-box">
                                <span class="wu-field-icon"><Lock size={18} /></span>
                                <input
                                    type={showConfirmPassword ? "text" : "password"}
                                    bind:value={confirmPassword}
                                    placeholder="••••••••"
                                    autocomplete="new-password"
                                    on:keydown={(e) => onFieldKey(e, submitRegister)}
                                />
                                <button
                                    type="button"
                                    class="wu-field-toggle"
                                    aria-label={showConfirmPassword
                                        ? "Ocultar contraseña"
                                        : "Mostrar contraseña"}
                                    on:click={() => (showConfirmPassword = !showConfirmPassword)}
                                >
                                    {#if showConfirmPassword}<EyeOff size={18} />{:else}<Eye size={18} />{/if}
                                </button>
                            </div>
                        </label>
                    {/if}

                    {#if authMode === "login"}
                        <div class="wu-row-between">
                            <label class="wu-remember">
                                <input type="checkbox" bind:checked={rememberMe} />
                                Recordarme
                            </label>
                        </div>
                    {/if}

                    {#if error}<p class="wu-error">{error}</p>{/if}

                    {#if authMode === "login"}
                        <button type="submit" class="wu-cta" disabled={!canLogin}>
                            Iniciar sesión <ArrowRight size={18} />
                        </button>
                    {:else}
                        <button type="submit" class="wu-cta" disabled={!canRegister}>Registrarse</button>
                    {/if}
                </form>

                {#if authMode === "login"}
                    <div class="wu-or"><span>o continúa con</span></div>
                    <button type="button" class="wu-google" disabled={loading} on:click={continueWithGoogle}>
                        <img class="g-icon" src="/icon/googleIcon.png" alt="" /> Google
                    </button>
                    <p class="wu-switch">
                        ¿No tienes cuenta?
                        <button type="button" class="wu-link" on:click={() => switchMode("register")}
                            >Crear cuenta</button
                        >
                    </p>
                {:else}
                    <p class="wu-switch">
                        ¿Ya tienes cuenta?
                        <button type="button" class="wu-link" on:click={() => switchMode("login")}
                            >Inicia sesión</button
                        >
                    </p>
                {/if}

                <button type="button" class="wu-guest" disabled={loading} on:click={continueAsGuest}>
                    <Eye size={16} /> Explorar como visitante
                </button>
            </aside>

            <div class="mobile-cta">
                <button type="button" class="wu-cta" on:click={() => openMobileAuth("login")}>
                    Iniciar sesión <ArrowRight size={18} />
                </button>
                <p class="wu-switch">
                    ¿No tienes cuenta?
                    <button type="button" class="wu-link" on:click={() => openMobileAuth("register")}
                        >Crear cuenta</button
                    >
                </p>
                <button type="button" class="wu-guest" disabled={loading} on:click={continueAsGuest}>
                    <Eye size={16} /> Explorar como visitante
                </button>
            </div>
        </div>
    </div>
</div>

{#if mobileDialogOpen}
    <div class="wu-dialog" role="dialog" aria-modal="true" aria-labelledby="wu-dialog-title">
        <button class="wu-dialog-scrim" type="button" aria-label="Cerrar" on:click={closeMobileDialog}></button>
        <div class="wu-dialog-panel">
            <header class="wu-dialog-head">
                <h2 id="wu-dialog-title">{authMode === "login" ? "Bienvenido de nuevo 👋" : "Crear cuenta"}</h2>
                <button type="button" class="wu-dialog-close" aria-label="Cerrar" on:click={closeMobileDialog}
                    ><X size={20} /></button
                >
            </header>
            <p class="wu-auth-sub">
                {authMode === "login" ? "Inicia sesión para continuar" : "Regístrate para comprar y reservar"}
            </p>

            <form
                class="wu-fields"
                on:submit|preventDefault={authMode === "login" ? signIn : submitRegister}
            >
                {#if authMode === "register"}
                    <label class="wu-field">
                        <span class="wu-field-label">Nombre</span>
                        <div class="wu-field-box">
                            <span class="wu-field-icon"><User size={18} /></span>
                            <input type="text" bind:value={name} placeholder="Tu nombre" autocomplete="name" />
                        </div>
                    </label>
                {/if}
                <label class="wu-field">
                    <span class="wu-field-label">Correo electrónico</span>
                    <div class="wu-field-box">
                        <span class="wu-field-icon"><Mail size={18} /></span>
                        <input type="email" bind:value={email} placeholder="tu@correo.com" autocomplete="email" />
                    </div>
                </label>
                <label class="wu-field">
                    <span class="wu-field-label">Contraseña</span>
                    <div class="wu-field-box">
                        <span class="wu-field-icon"><Lock size={18} /></span>
                        <input
                            type={showPassword ? "text" : "password"}
                            bind:value={password}
                            placeholder="••••••••"
                            autocomplete={authMode === "login" ? "current-password" : "new-password"}
                        />
                        <button
                            type="button"
                            class="wu-field-toggle"
                            aria-label="Toggle password"
                            on:click={() => (showPassword = !showPassword)}
                        >
                            {#if showPassword}<EyeOff size={18} />{:else}<Eye size={18} />{/if}
                        </button>
                    </div>
                </label>
                {#if authMode === "register"}
                    <label class="wu-field">
                        <span class="wu-field-label">Confirmar contraseña</span>
                        <div class="wu-field-box">
                            <span class="wu-field-icon"><Lock size={18} /></span>
                            <input
                                type={showConfirmPassword ? "text" : "password"}
                                bind:value={confirmPassword}
                                placeholder="••••••••"
                                autocomplete="new-password"
                            />
                            <button
                                type="button"
                                class="wu-field-toggle"
                                aria-label="Toggle"
                                on:click={() => (showConfirmPassword = !showConfirmPassword)}
                            >
                                {#if showConfirmPassword}<EyeOff size={18} />{:else}<Eye size={18} />{/if}
                            </button>
                        </div>
                    </label>
                {/if}

                {#if error}<p class="wu-error">{error}</p>{/if}

                {#if authMode === "login"}
                    <button type="submit" class="wu-cta" disabled={!canLogin}>
                        Iniciar sesión <ArrowRight size={18} />
                    </button>
                {:else}
                    <button type="submit" class="wu-cta" disabled={!canRegister}>Registrarse</button>
                {/if}
            </form>

            {#if authMode === "login"}
                <div class="wu-or"><span>o continúa con</span></div>
                <button type="button" class="wu-google" disabled={loading} on:click={continueWithGoogle}>
                    <img class="g-icon" src="/icon/googleIcon.png" alt="" /> Google
                </button>
                <p class="wu-switch">
                    ¿No tienes cuenta?
                    <button type="button" class="wu-link" on:click={() => switchMode("register")}>Crear cuenta</button>
                </p>
            {:else}
                <p class="wu-switch">
                    ¿Ya tienes cuenta?
                    <button type="button" class="wu-link" on:click={() => switchMode("login")}>Inicia sesión</button>
                </p>
            {/if}
        </div>
    </div>
{/if}

{#if pendingAdminUser}
    <AdminRoleChoiceCard busy={loading} on:stayClient={continueAsClient} on:goAdmin={continueToAdmin} />
{/if}

<FrameModal
    open={googleFrameOpen}
    title="Continuar con Google"
    ariaLabel="Autenticación con Google"
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
    <div class="wu-dialog">
        <button class="wu-dialog-scrim" type="button" aria-label="Cerrar" on:click={() => (linkOpen = false)}></button>
        <div class="wu-dialog-panel">
            <h3>Confirma tu contraseña</h3>
            <p class="wu-auth-sub">Esta cuenta ya existe. Usa tu password actual para vincular Google.</p>
            <label class="wu-field">
                <span class="wu-field-label">Contraseña actual</span>
                <div class="wu-field-box">
                    <span class="wu-field-icon"><Lock size={18} /></span>
                    <input
                        type="password"
                        bind:value={linkPassword}
                        placeholder="••••••••"
                        autocomplete="current-password"
                        on:keydown={(e) => onFieldKey(e, linkGoogleAccount)}
                    />
                </div>
            </label>
            {#if linkError}<p class="wu-error">{linkError}</p>{/if}
            <div class="wu-link-actions">
                <button type="button" class="wu-btn-text" on:click={() => (linkOpen = false)}>Cancelar</button>
                <button
                    type="button"
                    class="wu-cta wu-cta-sm"
                    disabled={loading || !linkPassword.trim()}
                    on:click={linkGoogleAccount}>Vincular y entrar</button
                >
            </div>
        </div>
    </div>
{/if}

<style>
    .wu-root {
        min-height: 100dvh;
        width: 100%;
        box-sizing: border-box;
        display: grid;
        place-items: center;
        padding: max(8px, env(safe-area-inset-top)) 10px max(10px, env(safe-area-inset-bottom));
        background:
            radial-gradient(ellipse 80% 60% at 18% 28%, rgba(34, 197, 94, 0.16), transparent 55%),
            radial-gradient(ellipse 70% 50% at 88% 78%, rgba(16, 185, 129, 0.1), transparent 50%),
            linear-gradient(165deg, #0a0f0c 0%, #0d1210 40%, #0a0e0c 100%);
        color: #e8eee9;
    }

    .wu-frame {
        width: min(1480px, 98vw);
        border-radius: 28px;
        border: 1px solid rgba(255, 255, 255, 0.06);
        background:
            radial-gradient(ellipse 55% 45% at 22% 35%, rgba(34, 197, 94, 0.12), transparent 60%),
            linear-gradient(145deg, rgba(18, 24, 20, 0.95) 0%, rgba(12, 16, 14, 0.98) 100%);
        box-shadow:
            0 0 0 1px rgba(34, 197, 94, 0.04),
            0 32px 80px rgba(0, 0, 0, 0.55),
            inset 0 1px 0 rgba(255, 255, 255, 0.04);
        overflow: hidden;
    }

    .wu-shell {
        display: grid;
        gap: 16px;
        padding: 18px 16px 20px;
    }

    .wu-hero {
        display: grid;
        gap: 12px;
        align-content: start;
    }

    .wu-brand {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .wu-logo-wrap {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        background: linear-gradient(145deg, rgba(34, 197, 94, 0.25), rgba(34, 197, 94, 0.08));
        border: 1px solid rgba(34, 197, 94, 0.3);
        box-shadow: 0 8px 24px rgba(34, 197, 94, 0.15);
    }
    .wu-logo {
        width: 32px;
        height: 32px;
        object-fit: contain;
    }
    .wu-brand-text strong {
        display: block;
        font-size: 1.05rem;
        font-weight: 800;
        letter-spacing: -0.02em;
    }
    .wu-brand-text span {
        font-size: 0.72rem;
        font-weight: 600;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        color: rgba(134, 239, 172, 0.75);
    }

    .wu-badge {
        margin: 2px 0 0;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        width: fit-content;
        padding: 5px 12px 5px 10px;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 700;
        letter-spacing: 0.02em;
        color: #86efac;
        background: rgba(34, 197, 94, 0.12);
        border: 1px solid rgba(34, 197, 94, 0.28);
    }
    .wu-badge-dot {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: #22c55e;
        box-shadow: 0 0 10px #22c55e;
    }

    .wu-title {
        margin: 0;
        font-size: clamp(1.85rem, 4.8vw, 2.85rem);
        font-weight: 850;
        letter-spacing: -0.035em;
        line-height: 1.08;
        color: #f4f7f5;
    }
    .wu-title em {
        font-style: normal;
        background: linear-gradient(90deg, #4ade80, #22c55e 60%, #86efac);
        -webkit-background-clip: text;
        background-clip: text;
        color: transparent;
    }

    .wu-lead {
        margin: 0;
        max-width: 36rem;
        font-size: 0.95rem;
        line-height: 1.55;
        color: rgba(200, 214, 205, 0.78);
    }

    .wu-trust {
        list-style: none;
        margin: 4px 0 0;
        padding: 0;
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 12px 18px;
    }
    .wu-trust li {
        display: flex;
        align-items: center;
        gap: 10px;
    }
    .wu-trust-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: grid;
        place-items: center;
        flex-shrink: 0;
        color: #4ade80;
        background: rgba(34, 197, 94, 0.1);
        border: 1px solid rgba(34, 197, 94, 0.22);
    }
    .wu-trust-label {
        font-size: 0.78rem;
        font-weight: 650;
        line-height: 1.25;
        white-space: pre-line;
        color: rgba(210, 222, 214, 0.88);
    }

    .wu-stage {
        position: relative;
        margin-top: 6px;
    }
    .wu-stage-glow {
        position: absolute;
        inset: 15% 8% -8%;
        background: radial-gradient(ellipse at 50% 70%, rgba(34, 197, 94, 0.32), transparent 65%);
        filter: blur(32px);
        pointer-events: none;
    }
    .wu-stage-plate {
        position: relative;
        border-radius: 20px;
        overflow: hidden;
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow:
            0 28px 56px rgba(0, 0, 0, 0.5),
            inset 0 1px 0 rgba(255, 255, 255, 0.06);
        background: #0a0e0c;
    }
    .wu-shot {
        width: 100%;
        height: 200px;
        object-fit: cover;
        object-position: center;
        display: block;
    }

    .wu-social-proof {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-top: 2px;
    }
    .wu-avatars {
        display: flex;
        align-items: center;
    }
    .av {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        border: 2px solid #0d1210;
        margin-left: -10px;
        object-fit: cover;
        background: #1a2e22;
    }
    .av:first-child {
        margin-left: 0;
    }
    .wu-social-meta {
        display: grid;
        gap: 2px;
        font-size: 0.78rem;
        font-weight: 600;
        color: rgba(200, 214, 205, 0.75);
    }
    .wu-stars {
        display: inline-flex;
        gap: 2px;
        color: #facc15;
    }

    .wu-auth-panel,
    .wu-dialog-panel {
        display: grid;
        gap: 14px;
        padding: 26px 24px;
        border-radius: 22px;
        background: linear-gradient(165deg, rgba(28, 36, 31, 0.92), rgba(18, 24, 20, 0.96));
        border: 1px solid rgba(255, 255, 255, 0.08);
        box-shadow:
            0 20px 50px rgba(0, 0, 0, 0.4),
            inset 0 1px 0 rgba(255, 255, 255, 0.05);
        backdrop-filter: blur(16px);
    }
    .wu-auth-panel h2,
    .wu-dialog-head h2,
    .wu-dialog-panel h3 {
        margin: 0;
        font-size: 1.3rem;
        font-weight: 800;
        letter-spacing: -0.02em;
        color: #f4f7f5;
    }
    .wu-auth-sub {
        margin: -6px 0 2px;
        font-size: 0.88rem;
        color: rgba(180, 198, 188, 0.75);
    }

    /* ——— Modern form fields ——— */
    .wu-fields {
        display: grid;
        gap: 14px;
        margin: 0;
    }
    .wu-field {
        display: grid;
        gap: 6px;
    }
    .wu-field-label {
        font-size: 0.78rem;
        font-weight: 650;
        letter-spacing: 0.01em;
        color: rgba(200, 214, 205, 0.7);
        padding-left: 2px;
    }
    .wu-field-box {
        display: flex;
        align-items: center;
        gap: 10px;
        height: 48px;
        padding: 0 14px;
        border-radius: 12px;
        background: rgba(8, 12, 10, 0.65);
        border: 1px solid rgba(255, 255, 255, 0.08);
        transition:
            border-color 0.15s ease,
            box-shadow 0.15s ease,
            background 0.15s ease;
    }
    .wu-field-box:focus-within {
        border-color: rgba(74, 222, 128, 0.55);
        box-shadow: 0 0 0 3px rgba(34, 197, 94, 0.15);
        background: rgba(10, 16, 12, 0.85);
    }
    .wu-field-icon {
        display: grid;
        place-items: center;
        flex-shrink: 0;
        color: rgba(160, 180, 168, 0.65);
    }
    .wu-field-box:focus-within .wu-field-icon {
        color: #4ade80;
    }
    .wu-field-box input {
        flex: 1;
        min-width: 0;
        height: 100%;
        border: 0;
        outline: none;
        background: transparent;
        color: #f0f4f1;
        font: inherit;
        font-size: 0.95rem;
        font-weight: 500;
    }
    .wu-field-box input::placeholder {
        color: rgba(160, 180, 168, 0.4);
        font-weight: 450;
    }
    .wu-field-toggle {
        display: grid;
        place-items: center;
        flex-shrink: 0;
        width: 36px;
        height: 36px;
        margin-right: -6px;
        border: 0;
        border-radius: 8px;
        background: transparent;
        color: rgba(160, 180, 168, 0.65);
        cursor: pointer;
        transition: color 0.15s, background 0.15s;
    }
    .wu-field-toggle:hover {
        color: #e8eee9;
        background: rgba(255, 255, 255, 0.06);
    }

    .wu-row-between {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .wu-remember {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.84rem;
        font-weight: 600;
        cursor: pointer;
        color: rgba(200, 214, 205, 0.85);
        user-select: none;
    }
    .wu-remember input {
        width: 15px;
        height: 15px;
        accent-color: #22c55e;
        cursor: pointer;
    }

    .wu-error {
        margin: 0;
        color: #f87171;
        font-size: 0.88rem;
        text-align: center;
    }

    .wu-cta {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        width: 100%;
        height: 48px;
        border: 0;
        border-radius: 12px;
        font: inherit;
        font-size: 0.95rem;
        font-weight: 750;
        cursor: pointer;
        color: #052e16;
        background: linear-gradient(180deg, #4ade80 0%, #22c55e 100%);
        box-shadow: 0 8px 24px rgba(34, 197, 94, 0.35);
        transition: filter 0.15s, transform 0.1s, opacity 0.15s;
    }
    .wu-cta:hover:not(:disabled) {
        filter: brightness(1.06);
    }
    .wu-cta:active:not(:disabled) {
        transform: scale(0.985);
    }
    .wu-cta:disabled {
        opacity: 0.45;
        cursor: not-allowed;
        box-shadow: none;
    }
    .wu-cta-sm {
        width: auto;
        height: 42px;
        padding: 0 18px;
    }

    .wu-google {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        width: 100%;
        height: 48px;
        border-radius: 12px;
        border: 1px solid rgba(255, 255, 255, 0.12);
        background: rgba(255, 255, 255, 0.03);
        color: #e8eee9;
        font: inherit;
        font-size: 0.92rem;
        font-weight: 650;
        cursor: pointer;
        transition: background 0.15s, border-color 0.15s;
    }
    .wu-google:hover:not(:disabled) {
        background: rgba(255, 255, 255, 0.06);
        border-color: rgba(255, 255, 255, 0.18);
    }
    .wu-google:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }

    .wu-or {
        display: flex;
        align-items: center;
        gap: 10px;
        color: rgba(160, 180, 168, 0.65);
        font-size: 0.75rem;
    }
    .wu-or::before,
    .wu-or::after {
        content: "";
        flex: 1;
        height: 1px;
        background: rgba(255, 255, 255, 0.1);
    }

    .g-icon {
        width: 18px;
        height: 18px;
        object-fit: contain;
    }

    .wu-switch {
        margin: 0;
        text-align: center;
        font-size: 0.88rem;
        color: rgba(180, 198, 188, 0.75);
    }
    .wu-link {
        border: 0;
        background: none;
        color: #4ade80;
        font: inherit;
        font-weight: 750;
        cursor: pointer;
        padding: 0;
    }

    .wu-guest {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        width: 100%;
        border: 0;
        background: transparent;
        color: rgba(180, 198, 188, 0.7);
        font: inherit;
        font-size: 0.88rem;
        font-weight: 650;
        cursor: pointer;
        padding: 8px;
        border-radius: 10px;
        transition: color 0.15s, background 0.15s;
    }
    .wu-guest:hover {
        color: #4ade80;
        background: rgba(34, 197, 94, 0.08);
    }

    .wu-btn-text {
        border: 0;
        background: transparent;
        color: rgba(180, 198, 188, 0.85);
        font: inherit;
        font-weight: 650;
        cursor: pointer;
        padding: 8px 12px;
        border-radius: 10px;
    }
    .wu-btn-text:hover {
        background: rgba(255, 255, 255, 0.05);
    }

    .mobile-cta {
        display: grid;
        gap: 12px;
        padding: 4px 0 0;
    }
    .desktop-auth {
        display: none;
    }

    .wu-dialog {
        position: fixed;
        inset: 0;
        z-index: 1100;
        display: grid;
        place-items: end center;
    }
    .wu-dialog-scrim {
        position: absolute;
        inset: 0;
        border: 0;
        background: rgba(0, 0, 0, 0.55);
        backdrop-filter: blur(8px);
    }
    .wu-dialog-panel {
        position: relative;
        z-index: 1;
        width: 100%;
        max-height: 92dvh;
        overflow: auto;
        border-radius: 24px 24px 0 0;
        padding-bottom: max(20px, env(safe-area-inset-bottom));
    }
    .wu-dialog-head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
    }
    .wu-dialog-close {
        width: 40px;
        height: 40px;
        border: 0;
        border-radius: 12px;
        background: rgba(255, 255, 255, 0.06);
        color: inherit;
        display: grid;
        place-items: center;
        cursor: pointer;
    }
    .wu-link-actions {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;
    }

    @media (min-width: 900px) {
        .wu-root {
            padding: 16px 20px;
        }
        .wu-frame {
            width: min(1520px, 96vw);
            min-height: min(860px, 92dvh);
        }
        .wu-shell {
            grid-template-columns: minmax(0, 1.35fr) minmax(380px, 440px);
            gap: 28px 56px;
            padding: 32px 40px 36px;
            align-items: center;
            min-height: min(860px, 92dvh);
        }
        .wu-title {
            font-size: clamp(2.35rem, 3.4vw, 3.15rem);
        }
        .wu-lead {
            font-size: 1rem;
            max-width: 40rem;
        }
        .wu-shot {
            height: min(280px, 32vh);
        }
        .desktop-auth {
            display: grid;
            align-self: center;
        }
        .mobile-cta {
            display: none;
        }
        .wu-dialog {
            place-items: center;
            padding: 16px;
        }
        .wu-dialog-panel {
            width: min(420px, 100%);
            border-radius: 22px;
            max-height: 90dvh;
        }
    }

    @media (min-width: 1280px) {
        .wu-shell {
            grid-template-columns: minmax(0, 1.45fr) minmax(400px, 460px);
            gap: 36px 64px;
            padding: 40px 48px 44px;
        }
        .wu-shot {
            height: min(320px, 36vh);
        }
        .wu-title {
            font-size: clamp(2.6rem, 3.2vw, 3.35rem);
        }
    }

    @media (max-width: 899px) {
        .wu-frame {
            border-radius: 20px;
        }
        .wu-shot {
            height: 160px;
        }
    }
</style>
