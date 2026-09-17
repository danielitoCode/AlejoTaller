<script lang="ts">
    /**
     * Core6: bienvenida + acceso.
     * UX mínima: Iniciar sesión → Clerk | Crear cuenta → Clerk | Visitante local.
     * Sin pantallas intermedias ni LoginAuth0Actions.
     */
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { Button, Card } from "m3-svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { authFlowStore } from "../viewmodel/auth-flow.store";
    import { sessionStore } from "../viewmodel/session.store";
    import { consumePendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";
    import { markWelcomeCompleted } from "../../../../infrastructure/presentation/navigation/first-visit";
    import { getAuthPort, isExternalAuthProvider } from "../../di/authPort.factory";
    import { Package, Truck, Headphones, ShieldCheck } from "lucide-svelte";

    export let navController: NavController;

    const useExternalAuth = isExternalAuthProvider();

    let loading = false;
    let error: string | null = null;
    let redirecting = false;

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

    /** Un solo toque → Universal Login de Clerk (email + Google en la misma UI). */
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
            // redirect: no vuelve aquí
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

                <h1 class="wu-title">Componentes para <em>tus ideas</em>.</h1>
                <p class="wu-lead">
                    Inicia sesión o crea tu cuenta. También puedes explorar como visitante.
                </p>

                <ul class="wu-trust" role="list">
                    {#each trustItems as t}
                        <li>
                            <span class="wu-trust-icon"><svelte:component this={t.icon} size={18} /></span>
                            <span>{t.label}</span>
                        </li>
                    {/each}
                </ul>
            </section>

            <section class="wu-actions">
                <Card variant="filled">
                    <div class="wu-card">
                        {#if useExternalAuth}
                            <Button
                                variant="filled"
                                size="m"
                                disabled={loading || redirecting}
                                onclick={() => goClerk("login")}
                            >
                                Iniciar sesión
                            </Button>
                            <Button
                                variant="outlined"
                                size="m"
                                disabled={loading || redirecting}
                                onclick={() => goClerk("signup")}
                            >
                                Crear cuenta
                            </Button>
                            <Button
                                variant="tonal"
                                size="m"
                                disabled={loading || redirecting}
                                onclick={continueAsGuest}
                            >
                                Continuar como visitante
                            </Button>
                            <p class="wu-hint">
                                Email o Google se eligen en la pantalla de acceso segura.
                            </p>
                        {:else}
                            <p class="wu-error">Falta VITE_CLERK_PUBLISHABLE_KEY.</p>
                            <Button variant="tonal" size="m" disabled={loading} onclick={continueAsGuest}>
                                Continuar como visitante
                            </Button>
                        {/if}
                        {#if error}<p class="wu-error">{error}</p>{/if}
                    </div>
                </Card>
            </section>
        </div>
    </div>
</div>

<style>
    .wu-root {
        min-height: 100dvh;
        display: grid;
        place-items: center;
        padding: 16px;
        box-sizing: border-box;
        background: var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
    }
    .wu-frame {
        width: min(100%, 440px);
    }
    .wu-shell {
        display: grid;
        gap: 20px;
    }
    .wu-brand {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    .wu-logo {
        width: 48px;
        height: 48px;
        object-fit: contain;
    }
    .wu-brand-text {
        display: grid;
        gap: 2px;
    }
    .wu-brand-text strong {
        font-size: 1.05rem;
    }
    .wu-brand-text span {
        font-size: 0.8rem;
        color: var(--md-sys-color-on-surface-variant);
    }
    .wu-title {
        margin: 12px 0 8px;
        font-size: 1.45rem;
        line-height: 1.25;
    }
    .wu-title em {
        font-style: normal;
        color: var(--md-sys-color-primary);
    }
    .wu-lead {
        margin: 0 0 12px;
        font-size: 0.9rem;
        color: var(--md-sys-color-on-surface-variant);
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
        font-size: 0.8rem;
        padding: 8px;
        border-radius: 12px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container) 80%, transparent);
    }
    .wu-trust-icon {
        display: grid;
        place-items: center;
        color: var(--md-sys-color-primary);
    }
    .wu-card {
        padding: 16px;
        display: grid;
        gap: 10px;
    }
    .wu-hint {
        margin: 4px 0 0;
        text-align: center;
        font-size: 0.78rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
    }
    .wu-error {
        margin: 0;
        color: var(--md-sys-color-error);
        font-size: 0.85rem;
    }
</style>
