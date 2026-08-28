<script lang="ts">
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { authContainer } from "../../di/auth.container";
    import type { GoogleIdTokenProfile } from "../util/google-id-token";
    import { ENV } from "../../../../infrastructure/env";
    import { registerStore } from "../viewmodel/register.store";
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
    import HeroDisassembly from "../components/HeroDisassembly.svelte";

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

    const sampleAvatars = [
        "https://i.pravatar.cc/64?img=12",
        "https://i.pravatar.cc/64?img=32",
        "https://i.pravatar.cc/64?img=47",
        "https://i.pravatar.cc/64?img=5",
        "https://i.pravatar.cc/64?img=68"
    ];
</script>

<!-- SEE artifacts/WelcomeUpdateScreen.svelte FOR FULL MARKUP+STYLES — restore locally -->
