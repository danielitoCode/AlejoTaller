<script lang="ts">
    /**
     * Core6: bienvenida + acceso Clerk (AuthPort).
     * Guest 100% local. Sin Appwrite.
     */
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { Button, Card } from "m3-svelte";
    import AuthBusyOverlay from "../components/AuthBusyOverlay.svelte";
    import LoginAuth0Actions from "../components/LoginAuth0Actions.svelte";
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
    let showAuth = false;

    function restorePendingHashIfNeeded() {
        const pendingHash = consumePendingDeepLink();
        if (pendingHash && typeof window !== "undefined") {
            window.history.replaceState({}, "", pendingHash);
        }
    }

    async function continueAsGuest() {
        if (loading) return;
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

    function openAuth() {
        if (!useExternalAuth) {
            error = "Clerk no configurado (VITE_CLERK_PUBLISHABLE_KEY)";
            toastStore.error(error);
            return;
        }
        showAuth = true;
        error = null;
    }

    function goRegister() {
        const auth = getAuthPort();
        if (!auth) {
            error = "Clerk no configurado";
            toastStore.error(error);
            return;
        }
        void auth
            .init()
            .then(() =>
                auth.loginWithRedirect({
                    returnTo: typeof window !== "undefined" ? window.location.origin : undefined,
                    screenHint: "signup",
                }),
            )
            .catch((e) => {
                error = e instanceof Error ? e.message : "No se pudo abrir Clerk";
                toastStore.error(error);
            });
    }

    const trustItems = [
        { icon: Package, label: "Miles de productos" },
        { icon: Truck, label: "Envíos rápidos" },
        { icon: Headphones, label: "Soporte" },
        { icon: ShieldCheck, label: "Pagos seguros" },
    ];
</script>

<AuthBusyOverlay open={loading} title="Preparando visita…" subtitle="Clerk · sin Appwrite" />

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
                    Acceso con Clerk (email o Google). Visitante 100% local — sin Appwrite.
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
                        {#if showAuth && useExternalAuth}
                            <LoginAuth0Actions disabled={loading} />
                            <Button variant="text" size="m" onclick={() => (showAuth = false)}>Volver</Button>
                        {:else}
                            <Button variant="filled" size="m" disabled={loading} onclick={openAuth}>
                                Entrar / Registrarse (Clerk)
                            </Button>
                            <Button variant="tonal" size="m" disabled={loading} onclick={continueAsGuest}>
                                Continuar como visitante
                            </Button>
                            <Button variant="text" size="m" disabled={loading} onclick={goRegister}>
                                Crear cuenta nueva (Clerk)
                            </Button>
                        {/if}
                        {#if error}<p class="wu-error">{error}</p>{/if}
                        {#if !useExternalAuth}
                            <p class="wu-error">Falta VITE_CLERK_PUBLISHABLE_KEY. No hay login Appwrite.</p>
                        {/if}
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
    .wu-error {
        margin: 0;
        color: var(--md-sys-color-error);
        font-size: 0.85rem;
    }
</style>
