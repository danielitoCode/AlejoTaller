<script lang="ts">
    import type {NavController} from "../../../../../lib/navigation/NavController";
    import {authContainer} from "../../di/auth.container";
    import type {GoogleIdTokenProfile} from "../util/google-id-token";
    import {ENV} from "../../../../infrastructure/env";
    import {registerStore} from "../viewmodel/register.store";
    import { Card, LoadingIndicator } from "m3-svelte"
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";

    export let navController: NavController;

    let email = "";
    let password = "";
    let loading = false;
    let error: string | null = null;
    let contentVisible = false;

    const glowAlpha = 0.45;

    $: canSubmit = email.trim().length > 3 && password.trim().length > 3 && !loading;

    setTimeout(() => {
        contentVisible = true;
    }, 20);

    async function signIn() {
        if (!canSubmit) return;

        loading = true;
        error = null;

        try {
            const userId = await authContainer.useCases.sessions.openSession.openCustomSession(
                email.trim(),
                password
            );
            const current = await authContainer.useCases.accounts.getCurrentUser();
            if (current.role !== "admin") {
                navController.navigate("unauthorized", { message: "Tu cuenta existe, pero no tiene permisos de administrador." });
                return;
            }
            navController.navigate("home", { id: userId });
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión";
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
        googleProfile = profile;

        loading = true;
        error = null;
        linkError = null;

        try {
            try {
                const userId = await authContainer.useCases.sessions.openSession.openCustomSession(profile.email, profile.sub);
                const current = await authContainer.useCases.accounts.getCurrentUser();
                if (current.role !== "admin") {
                    navController.navigate("unauthorized", { message: "Tu cuenta existe, pero no tiene permisos de administrador." });
                    return;
                }
                navController.navigate("home", { id: userId });
                return;
            } catch {
                googleRegisterSrc = getGoogleRegisterSrc(profile);
                registerFrameOpen = true;
                return;
            }
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión con Google";
        } finally {
            loading = false;
        }
    }

    async function linkGoogleAccount() {
        if (!googleProfile) return;
        if (!linkPassword.trim()) {
            linkError = "Ingresa tu contraseña actual.";
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

            const current = await authContainer.useCases.accounts.getCurrentUser();
            if (current.role !== "admin") {
                navController.navigate("unauthorized", { message: "Tu cuenta existe, pero no tiene permisos de administrador." });
                return;
            }

            linkOpen = false;
            linkPassword = "";
            navController.navigate("home", { id: userId });
        } catch (e: any) {
            const code = typeof e?.code === "number" ? e.code : null;
            linkError = code === 401 ? "Contraseña incorrecta." : (e instanceof Error ? e.message : "No se pudo vincular la cuenta.");
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
            error = e instanceof Error ? e.message : "No se pudo iniciar sesión con Google";
        }
    }

    function goToRegister() {
        navController.navigate("register");
    }

    async function registerStoreFromGoogle(profile: GoogleIdTokenProfile) {
        loading = true;
        error = null;

        try {
            await registerStore.createAccount({
                name: profile.name || profile.email.split("@")[0] || "Usuario",
                email: profile.email,
                password: profile.sub,
                phone: "",
                photo_url: profile.picture,
                role: "viewer",
                sub: profile.sub,
                verification: true
            });

            const current = await authContainer.useCases.accounts.getCurrentUser();
            if (current.role !== "admin") {
                navController.navigate("unauthorized", { message: "Cuenta creada, pero sin permisos para el panel de gestión." });
                return;
            }
            navController.navigate("home", { id: current.id });
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
        } finally {
            loading = false;
        }
    }
</script>

<Screen  ariaLabel="Login" scrollable={false}>
    <main class="login-screen">
        <section class="header-content">
            <LoadingIndicator
                    container={false}
                    size={120}
                    center={true}
            />
            <img class="app-icon" src="/public/alejoicon_clean.svg" alt="app-icon">

        </section>
        <section class="tittle-container">

            <h2>Iniciar sesión</h2>
            <h4>Accede a tu cuenta para continuar</h4>
        </section>
        <section class="card-content">
            <Card variant="elevated">
                <h1>Text</h1>
            </Card>
            <Card variant="elevated">
                <h1>Text</h1>
            </Card>
            <Card variant="elevated">
                <h1>Text</h1>
            </Card>
        </section>
        <section>
            <Card variant="elevated">
                <h1>Text</h1>
            </Card>
        </section>
    </main>
</Screen>

<style>
    .login-screen {
        min-height: 100dvh;
        width: 100%;
        display: grid;
        grid-template-rows: auto auto auto auto;
    }

    .tittle-container {
        text-align: center;
    }

    .app-icon {
        max-block-size: 80px;
    }

    .header-content {
        text-align: center;
        align-content: center;
    }

    .card-content {

    }

</style>