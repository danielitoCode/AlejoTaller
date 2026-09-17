<script lang="ts">
    /**
     * Core6: bienvenida + acceso.
     * Iniciar sesión / Crear cuenta → Clerk | Visitante local.
     */
    import { onMount } from "svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { sessionStore } from "../viewmodel/session.store";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { markWelcomeCompleted } from "../../../../infrastructure/presentation/navigation/first-visit";
    import { getAuthPort, isExternalAuthProvider } from "../../di/authPort.factory";
    import {
        Package,
        Truck,
        Headphones,
        ShieldCheck,
        LogIn,
        UserPlus,
        UserRound,
    } from "lucide-svelte";

    export let navController: NavController;

    const useExternalAuth = isExternalAuthProvider();

    let loading = false;
    let error: string | null = null;
    let redirecting = false;
    let entered = false;

    onMount(() => {
        requestAnimationFrame(() => {
            entered = true;
        });
    });

    function restorePendingHashIfNeeded() {
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
            authFlowStore.setSuccess({
                userId: "guest-local",
                email: null,
                provider: "guest",
            });
            markWelcomeCompleted();
            restorePendingHashIfNeeded();
            navController.resetTo("home", {
                userId: "guest-local",
                email: null,
                provider: "guest",
            });
        } catch (e) {
            error = e instanceof Error ? e.message : "No se pudo entrar como visitante";
            authFlowStore.setError(error, { provider: "guest" });
            toastStore.error(error);
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
            error = e instanceof Error ? e.message : "No se pudo abrir Clerk";
            toastStore.error(error);
            redirecting = false;
        }
    }

    const trustItems = [
        { icon: Package, label: "Miles de productos" },
        { icon: Truck, label: "Envíos rápidos" },
        { icon: Headphones, label: "Soporte" },
        { icon: ShieldCheck, label: "Pagos seguros" },
    ];
</script>

<AuthBusyOverlay
    open={loading || redirecting}
    title={redirecting ? "Abriendo acceso…" : "Preparando visita…"}
    subtitle={redirecting ? "Te llevamos a la pantalla segura de inicio de sesión" : "Un momento"}
/>

<div class="wu-root" class:is-in={entered} aria-label="Bienvenida y acceso">
    <div class="wu-bg" aria-hidden="true">
        <span class="orb orb-a"></span>
        <span class="orb orb-b"></span>
        <span class="orb orb-c"></span>
        <span class="vignette"></span>
    </div>

    <div class="wu-frame">
        <header class="wu-brand anim" style="--d: 0ms">
            <div class="logo-stage">
                <div class="logo-ring"></div>
                <div class="logo-ring logo-ring-2"></div>
                <div class="logo-plate">
                    <img src="/alejoicon_clean.svg" alt="" class="wu-logo" />
                </div>
            </div>
            <div class="wu-brand-text">
                <p class="eyebrow">Taller Alejo</p>
                <strong>Electrónica & Tecnología</strong>
            </div>
        </header>

        <section class="wu-hero anim" style="--d: 80ms">
            <h1 class="wu-title">Componentes para <em>tus ideas</em>.</h1>
            <p class="wu-lead">
                Inicia sesión o crea tu cuenta. También puedes explorar el catálogo como visitante.
            </p>

            <ul class="wu-trust" role="list">
                {#each trustItems as t, i}
                    <li class="anim" style="--d: {140 + i * 40}ms">
                        <span class="wu-trust-icon">
                            <svelte:component this={t.icon} size={16} strokeWidth={2.25} />
                        </span>
                        <span>{t.label}</span>
                    </li>
                {/each}
            </ul>
        </section>

        <section class="wu-panel anim" style="--d: 220ms" aria-label="Acciones de acceso">
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

                <div class="divider" aria-hidden="true"><span>o</span></div>

                <button
                    type="button"
                    class="btn btn-ghost"
                    disabled={loading || redirecting}
                    on:click={continueAsGuest}
                >
                    <span class="btn-ico"><UserRound size={20} strokeWidth={2.25} /></span>
                    <span class="btn-copy">
                        <strong>Continuar como visitante</strong>
                        <small>Sin cuenta · solo explorar</small>
                    </span>
                </button>

                <p class="wu-hint">Credenciales en pantalla segura. Aquí no pedimos contraseña.</p>
            {:else}
                <p class="wu-error">Falta VITE_CLERK_PUBLISHABLE_KEY.</p>
                <button type="button" class="btn btn-ghost" disabled={loading} on:click={continueAsGuest}>
                    <span class="btn-ico"><UserRound size={20} strokeWidth={2.25} /></span>
                    <span class="btn-copy">
                        <strong>Continuar como visitante</strong>
                        <small>Sin cuenta · solo explorar</small>
                    </span>
                </button>
            {/if}
            {#if error}<p class="wu-error">{error}</p>{/if}
        </section>
    </div>
</div>

<style>
    .wu-root {
        position: relative;
        isolation: isolate;
        min-height: 100dvh;
        display: grid;
        place-items: center;
        padding:
            max(20px, calc(env(safe-area-inset-top) + 12px))
            18px
            max(24px, calc(env(safe-area-inset-bottom) + 12px));
        box-sizing: border-box;
        background: var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
        overflow: hidden;
    }

    .wu-bg {
        position: absolute;
        inset: 0;
        z-index: -1;
        pointer-events: none;
        overflow: hidden;
    }

    .orb {
        position: absolute;
        border-radius: 50%;
        filter: blur(56px);
    }

    .orb-a {
        width: min(78vw, 380px);
        height: min(78vw, 380px);
        top: -18%;
        left: 50%;
        transform: translateX(-55%);
        background: color-mix(in srgb, var(--md-sys-color-primary) 38%, transparent);
        opacity: 0.7;
    }

    .orb-b {
        width: min(50vw, 220px);
        height: min(50vw, 220px);
        bottom: 12%;
        right: -8%;
        background: color-mix(
            in srgb,
            var(--md-sys-color-tertiary, var(--md-sys-color-primary)) 26%,
            transparent
        );
        opacity: 0.5;
    }

    .orb-c {
        width: min(40vw, 160px);
        height: min(40vw, 160px);
        bottom: 28%;
        left: -10%;
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 40%, transparent);
        opacity: 0.45;
    }

    .vignette {
        position: absolute;
        inset: 0;
        background:
            radial-gradient(
                ellipse 90% 70% at 50% 35%,
                transparent 20%,
                color-mix(in srgb, var(--md-sys-color-background) 88%, transparent) 80%
            );
    }

    .wu-frame {
        width: min(100%, 420px);
        display: grid;
        gap: clamp(18px, 4vh, 28px);
    }

    .anim {
        opacity: 0;
        transform: translateY(20px);
        transition:
            opacity 0.6s cubic-bezier(0.22, 1, 0.36, 1),
            transform 0.6s cubic-bezier(0.22, 1, 0.36, 1);
        transition-delay: var(--d, 0ms);
    }

    .wu-root.is-in .anim {
        opacity: 1;
        transform: translateY(0);
    }

    .wu-brand {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: 12px;
    }

    .logo-stage {
        position: relative;
        width: 112px;
        height: 112px;
        display: grid;
        place-items: center;
    }

    .logo-ring {
        position: absolute;
        inset: 0;
        border-radius: 32%;
        border: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 50%, transparent);
        animation: ring-pulse 2.6s ease-in-out infinite;
    }

    .logo-ring-2 {
        inset: -10px;
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
        animation-delay: 0.5s;
    }

    .logo-plate {
        width: 80px;
        height: 80px;
        border-radius: 28%;
        display: grid;
        place-items: center;
        background:
            linear-gradient(
                150deg,
                color-mix(in srgb, var(--md-sys-color-surface) 90%, white),
                color-mix(in srgb, var(--md-sys-color-primary-container) 40%, var(--md-sys-color-surface))
            );
        box-shadow:
            0 14px 36px color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-outline-variant) 45%, transparent);
    }

    .wu-logo {
        width: 46px;
        height: 46px;
        object-fit: contain;
        filter: drop-shadow(0 4px 12px color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent));
    }

    .wu-brand-text {
        display: grid;
        gap: 4px;
        justify-items: center;
    }

    .eyebrow {
        margin: 0;
        font-size: 0.7rem;
        font-weight: 700;
        letter-spacing: 0.16em;
        text-transform: uppercase;
        color: var(--md-sys-color-primary);
    }

    .wu-brand-text strong {
        font-size: 1.05rem;
        font-weight: 700;
        letter-spacing: -0.01em;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 88%, transparent);
    }

    .wu-hero {
        text-align: center;
    }

    .wu-title {
        margin: 0 0 10px;
        font-size: clamp(1.55rem, 5vw, 1.95rem);
        font-weight: 800;
        letter-spacing: -0.035em;
        line-height: 1.15;
    }

    .wu-title em {
        font-style: normal;
        color: var(--md-sys-color-primary);
        text-shadow: 0 0 28px color-mix(in srgb, var(--md-sys-color-primary) 40%, transparent);
    }

    .wu-lead {
        margin: 0 auto 16px;
        max-width: 32ch;
        font-size: 0.92rem;
        line-height: 1.5;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 68%, transparent);
    }

    .wu-trust {
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 8px;
    }

    .wu-trust li {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 0.78rem;
        font-weight: 500;
        padding: 10px 12px;
        border-radius: 14px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 55%, transparent);
        backdrop-filter: blur(8px);
    }

    .wu-trust-icon {
        flex-shrink: 0;
        width: 28px;
        height: 28px;
        border-radius: 9px;
        display: grid;
        place-items: center;
        color: var(--md-sys-color-primary);
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
    }

    .wu-panel {
        display: grid;
        gap: 10px;
        padding: 16px;
        border-radius: 22px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 78%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 65%, transparent);
        box-shadow:
            0 20px 48px color-mix(in srgb, black 28%, transparent),
            0 1px 0 color-mix(in srgb, white 6%, transparent) inset;
        backdrop-filter: blur(16px);
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
        font-size: 0.74rem;
        opacity: 0.78;
    }

    .btn-primary {
        color: var(--md-sys-color-on-primary);
        background: var(--md-sys-color-primary);
        box-shadow: 0 10px 28px color-mix(in srgb, var(--md-sys-color-primary) 40%, transparent);
    }

    .btn-primary .btn-ico {
        background: color-mix(in srgb, white 18%, transparent);
    }

    .btn-primary:not(:disabled):hover {
        box-shadow: 0 14px 32px color-mix(in srgb, var(--md-sys-color-primary) 50%, transparent);
    }

    .btn-secondary {
        color: var(--md-sys-color-on-surface);
        background: color-mix(in srgb, var(--md-sys-color-surface) 65%, var(--md-sys-color-surface-variant));
        border: 1px solid var(--md-sys-color-outline-variant);
    }

    .btn-secondary .btn-ico {
        color: var(--md-sys-color-primary);
        background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
    }

    .btn-ghost {
        color: var(--md-sys-color-on-surface-variant);
        background: transparent;
        border: 1px dashed color-mix(in srgb, var(--md-sys-color-outline) 40%, transparent);
    }

    .btn-ghost .btn-ico {
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 50%, transparent);
        color: var(--md-sys-color-on-surface-variant);
    }

    .divider {
        display: grid;
        place-items: center;
        position: relative;
        height: 18px;
        margin: 2px 0;
    }

    .divider::before {
        content: "";
        position: absolute;
        left: 0;
        right: 0;
        top: 50%;
        height: 1px;
        background: color-mix(in srgb, var(--md-sys-color-outline-variant) 75%, transparent);
    }

    .divider span {
        position: relative;
        z-index: 1;
        padding: 0 10px;
        font-size: 0.7rem;
        font-weight: 700;
        letter-spacing: 0.1em;
        text-transform: uppercase;
        color: var(--md-sys-color-on-surface-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 90%, transparent);
    }

    .wu-hint {
        margin: 4px 0 0;
        text-align: center;
        font-size: 0.76rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
    }

    .wu-error {
        margin: 0;
        color: var(--md-sys-color-error);
        font-size: 0.85rem;
        text-align: center;
    }

    @keyframes ring-pulse {
        0%,
        100% {
            opacity: 0.3;
            transform: scale(1);
        }
        50% {
            opacity: 0.9;
            transform: scale(1.045);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .anim {
            opacity: 1;
            transform: none;
            transition: none;
        }
        .logo-ring,
        .logo-ring-2 {
            animation: none;
        }
    }
</style>
