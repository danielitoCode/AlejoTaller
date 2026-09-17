<script lang="ts">
    import { onMount } from "svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import Screen from "../../../../infrastructure/presentation/components/Screen.svelte";
    import { sessionStore } from "../viewmodel/session.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { getAuthPort, isExternalAuthProvider } from "../../di/authPort.factory";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { LogIn, UserPlus, UserRound } from "lucide-svelte";

    export let navController: NavController;

    const useExternalAuth = isExternalAuthProvider();

    let loading = false;
    let redirecting = false;
    let error: string | null = null;
    let entered = false;

    onMount(() => {
        requestAnimationFrame(() => {
            entered = true;
        });
    });

    function applyPendingDeepLink(): void {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    async function continueAsGuest() {
        if (loading || redirecting) return;
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

    async function goClerk(mode: "login" | "signup") {
        if (!useExternalAuth) {
            error = "Clerk no configurado (VITE_CLERK_PUBLISHABLE_KEY)";
            toastStore.error(error);
            return;
        }
        const auth = getAuthPort();
        if (!auth) {
            error = "Clerk no configurado";
            toastStore.error(error);
            return;
        }
        redirecting = true;
        error = null;
        try {
            await auth.init();
            await auth.loginWithRedirect({
                returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                screenHint: mode === "signup" ? "signup" : "login",
            });
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo abrir el acceso";
            toastStore.error(error);
            redirecting = false;
        }
    }
</script>

<AuthBusyOverlay
    open={loading || redirecting}
    title={redirecting ? "Abriendo acceso…" : "Validando acceso…"}
    subtitle={redirecting
        ? "Te llevamos a la pantalla segura de inicio de sesión"
        : "Comprobando tus credenciales. Esto suele tardar solo un momento."}
/>

<Screen ariaLabel="Login" scrollable={false}>
    <main class="login-root" class:is-in={entered}>
        <div class="login-bg" aria-hidden="true">
            <span class="orb orb-a"></span>
            <span class="orb orb-b"></span>
            <span class="grid-fade"></span>
        </div>

        <section class="login-brand anim" style="--d: 0ms">
            <div class="logo-stage">
                <div class="logo-ring"></div>
                <div class="logo-ring logo-ring-delayed"></div>
                <div class="logo-plate">
                    <img class="login-logo" src="/alejoicon_clean.svg" alt="Logo Alejo Taller" />
                </div>
            </div>
            <p class="eyebrow">Taller Alejo</p>
            <h1>Electrónica & tecnología</h1>
            <p class="lead">Inicia sesión, crea tu cuenta o explora el catálogo como visitante.</p>
        </section>

        <section class="login-panel anim" style="--d: 120ms" aria-label="Acciones de acceso">
            {#if useExternalAuth}
                <button
                    type="button"
                    class="btn btn-primary"
                    disabled={loading || redirecting}
                    on:click={() => goClerk("login")}
                >
                    <span class="btn-ico"><LogIn size={20} strokeWidth={2.25} /></span>
                    <span class="btn-copy">
                        <strong>Iniciar sesión</strong>
                        <small>Email o Google</small>
                    </span>
                </button>

                <button
                    type="button"
                    class="btn btn-secondary"
                    disabled={loading || redirecting}
                    on:click={() => goClerk("signup")}
                >
                    <span class="btn-ico"><UserPlus size={20} strokeWidth={2.25} /></span>
                    <span class="btn-copy">
                        <strong>Crear cuenta</strong>
                        <small>Registro rápido</small>
                    </span>
                </button>
            {:else}
                <p class="error-copy">Falta VITE_CLERK_PUBLISHABLE_KEY.</p>
            {/if}

            <div class="divider anim" style="--d: 200ms" aria-hidden="true">
                <span>o</span>
            </div>

            <button
                type="button"
                class="btn btn-ghost anim"
                style="--d: 240ms"
                disabled={loading || redirecting}
                on:click={continueAsGuest}
            >
                <span class="btn-ico"><UserRound size={20} strokeWidth={2.25} /></span>
                <span class="btn-copy">
                    <strong>Entrar como visitante</strong>
                    <small>Sin cuenta · solo explorar</small>
                </span>
            </button>

            {#if useExternalAuth}
                <p class="hint anim" style="--d: 300ms">
                    Credenciales en pantalla segura. No pedimos contraseña aquí.
                </p>
            {/if}
            {#if error}<p class="error-copy">{error}</p>{/if}
        </section>
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
        overflow: hidden;
    }

    .login-root {
        position: relative;
        isolation: isolate;
        width: 100%;
        height: 100%;
        padding:
            max(20px, calc(env(safe-area-inset-top) + 16px))
            20px
            max(24px, calc(env(safe-area-inset-bottom) + 16px));
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: clamp(20px, 5vh, 36px);
        color: var(--md-sys-color-on-background);
        background: var(--md-sys-color-background);
    }

    .login-bg {
        position: absolute;
        inset: 0;
        z-index: -1;
        overflow: hidden;
        pointer-events: none;
    }

    .orb {
        position: absolute;
        border-radius: 50%;
        filter: blur(48px);
        opacity: 0.55;
    }

    .orb-a {
        width: min(70vw, 320px);
        height: min(70vw, 320px);
        top: -12%;
        left: 50%;
        transform: translateX(-50%);
        background: color-mix(in srgb, var(--md-sys-color-primary) 42%, transparent);
    }

    .orb-b {
        width: min(55vw, 240px);
        height: min(55vw, 240px);
        bottom: 8%;
        right: -10%;
        background: color-mix(in srgb, var(--md-sys-color-tertiary, var(--md-sys-color-primary)) 28%, transparent);
    }

    .grid-fade {
        position: absolute;
        inset: 0;
        background:
            radial-gradient(ellipse 80% 50% at 50% 0%, transparent 30%, var(--md-sys-color-background) 75%);
        opacity: 0.9;
    }

    .anim {
        opacity: 0;
        transform: translateY(18px);
        transition:
            opacity 0.55s cubic-bezier(0.22, 1, 0.36, 1),
            transform 0.55s cubic-bezier(0.22, 1, 0.36, 1);
        transition-delay: var(--d, 0ms);
    }

    .login-root.is-in .anim {
        opacity: 1;
        transform: translateY(0);
    }

    .login-brand {
        display: grid;
        justify-items: center;
        text-align: center;
        gap: 10px;
    }

    .logo-stage {
        position: relative;
        width: 120px;
        height: 120px;
        display: grid;
        place-items: center;
        margin-bottom: 4px;
    }

    .logo-ring {
        position: absolute;
        inset: 0;
        border-radius: 32%;
        border: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent);
        animation: ring-pulse 2.4s ease-in-out infinite;
    }

    .logo-ring-delayed {
        inset: -8px;
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
        animation-delay: 0.45s;
    }

    .logo-plate {
        width: 88px;
        height: 88px;
        border-radius: 28%;
        display: grid;
        place-items: center;
        background:
            linear-gradient(
                145deg,
                color-mix(in srgb, var(--md-sys-color-surface) 92%, white),
                color-mix(in srgb, var(--md-sys-color-primary-container) 35%, var(--md-sys-color-surface))
            );
        box-shadow:
            0 12px 32px color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-outline-variant) 50%, transparent);
    }

    .login-logo {
        width: 52px;
        height: 52px;
        object-fit: contain;
        filter: drop-shadow(0 4px 10px color-mix(in srgb, var(--md-sys-color-primary) 30%, transparent));
    }

    .eyebrow {
        margin: 0;
        font-size: 0.72rem;
        font-weight: 700;
        letter-spacing: 0.14em;
        text-transform: uppercase;
        color: var(--md-sys-color-primary);
    }

    .login-brand h1 {
        margin: 0;
        font-size: clamp(1.45rem, 4.2vw, 1.85rem);
        font-weight: 800;
        letter-spacing: -0.03em;
        line-height: 1.15;
    }

    .lead {
        margin: 0;
        max-width: 28ch;
        font-size: 0.92rem;
        line-height: 1.45;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 72%, transparent);
    }

    .login-panel {
        width: 100%;
        max-width: 400px;
        margin-inline: auto;
        display: grid;
        gap: 10px;
        padding: 16px;
        border-radius: 22px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 88%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow:
            0 18px 40px color-mix(in srgb, black 12%, transparent),
            0 1px 0 color-mix(in srgb, white 8%, transparent) inset;
        backdrop-filter: blur(12px);
    }

    .btn {
        display: flex;
        align-items: center;
        gap: 14px;
        width: 100%;
        min-height: 56px;
        padding: 10px 14px;
        border-radius: 16px;
        border: 0;
        cursor: pointer;
        text-align: left;
        font: inherit;
        transition:
            transform 0.18s ease,
            box-shadow 0.18s ease,
            background 0.18s ease,
            opacity 0.18s ease;
    }

    .btn:disabled {
        opacity: 0.55;
        cursor: not-allowed;
    }

    .btn:not(:disabled):active {
        transform: scale(0.985);
    }

    .btn-ico {
        flex-shrink: 0;
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: grid;
        place-items: center;
    }

    .btn-copy {
        display: grid;
        gap: 1px;
        min-width: 0;
    }

    .btn-copy strong {
        font-size: 0.98rem;
        font-weight: 700;
        line-height: 1.2;
    }

    .btn-copy small {
        font-size: 0.75rem;
        opacity: 0.78;
    }

    .btn-primary {
        color: var(--md-sys-color-on-primary);
        background: var(--md-sys-color-primary);
        box-shadow: 0 10px 24px color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
    }

    .btn-primary .btn-ico {
        background: color-mix(in srgb, white 18%, transparent);
    }

    .btn-primary:not(:disabled):hover {
        box-shadow: 0 12px 28px color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent);
    }

    .btn-secondary {
        color: var(--md-sys-color-on-surface);
        background: color-mix(in srgb, var(--md-sys-color-surface) 70%, var(--md-sys-color-surface-variant));
        border: 1px solid var(--md-sys-color-outline-variant);
    }

    .btn-secondary .btn-ico {
        background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
        color: var(--md-sys-color-primary);
    }

    .btn-ghost {
        color: var(--md-sys-color-on-surface-variant);
        background: transparent;
        border: 1px dashed color-mix(in srgb, var(--md-sys-color-outline) 45%, transparent);
    }

    .btn-ghost .btn-ico {
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 55%, transparent);
        color: var(--md-sys-color-on-surface-variant);
    }

    .divider {
        display: grid;
        place-items: center;
        margin: 2px 0;
        position: relative;
        height: 20px;
    }

    .divider::before {
        content: "";
        position: absolute;
        left: 0;
        right: 0;
        top: 50%;
        height: 1px;
        background: color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
    }

    .divider span {
        position: relative;
        z-index: 1;
        padding: 0 10px;
        font-size: 0.72rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--md-sys-color-on-surface-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
    }

    .hint {
        margin: 4px 0 0;
        text-align: center;
        font-size: 0.76rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
    }

    .error-copy {
        color: var(--md-sys-color-error);
        margin: 0;
        font-size: 0.85rem;
        text-align: center;
    }

    @keyframes ring-pulse {
        0%,
        100% {
            opacity: 0.35;
            transform: scale(1);
        }
        50% {
            opacity: 0.85;
            transform: scale(1.04);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .anim {
            opacity: 1;
            transform: none;
            transition: none;
        }
        .logo-ring,
        .logo-ring-delayed {
            animation: none;
        }
    }
</style>
