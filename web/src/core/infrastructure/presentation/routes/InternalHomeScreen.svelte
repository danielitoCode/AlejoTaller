<script lang="ts">
    import {Button, Card} from "m3-svelte";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {authFlowStore} from "../../../feature/auth/presentation/viewmodel/auth-flow.store";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {onMount} from "svelte";
    import {toastStore} from "../viewmodel/toast.store";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    const routeTitles: Record<string, string> = {
        dashboard: "Productos",
        product: "Productos",
        buy: "Su compra",
        reservation: "Reservas",
        profile: "Perfil",
        settings: "Ajustes"
    };

    const currentUser = sessionStore.getCurrentUser();
    const routeKey = navBackStackEntry?.route ?? "dashboard";
    const title = routeTitles[routeKey] ?? "Modulo temporal";
    const isHome = routeKey === "dashboard";

    onMount(() => {
        if (isHome && $authFlowStore.userId) {
            toastStore.success(`Sesion iniciada para ${$authFlowStore.email ?? $authFlowStore.userId}`, 2200);
        }
        if ($authFlowStore.error) {
            toastStore.error($authFlowStore.error, 2800);
        }
    });
</script>

<section class="home-shell">
    <header class="hero-card">
        <div class="hero-copy">
            <span class="eyebrow">Navegacion interna</span>
            <h1>{title}</h1>
            <p>
                {#if isHome}
                    Estructura temporal conectada. Esta pantalla confirma que el login web ya entra al flujo general
                    del usuario, igual que en Android.
                {:else}
                    Ruta temporal activa mientras se cablean los modulos definitivos del flujo principal.
                {/if}
            </p>
        </div>
    </header>

    <div class="content-grid">
        <Card variant="filled">
            <div class="info-card">
                <h2>Sesion</h2>
                <dl class="meta-grid">
                    <div>
                        <dt>User ID</dt>
                        <dd>{$authFlowStore.userId ?? navBackStackEntry?.args?.id ?? "No disponible"}</dd>
                    </div>
                    <div>
                        <dt>Email</dt>
                        <dd>{$authFlowStore.email ?? "No disponible"}</dd>
                    </div>
                    <div>
                        <dt>Proveedor</dt>
                        <dd>{$authFlowStore.provider}</dd>
                    </div>
                    <div>
                        <dt>Ruta</dt>
                        <dd>{routeKey}</dd>
                    </div>
                </dl>
            </div>
        </Card>

        <Card variant="filled">
            <div class="info-card">
                <h2>Usuario actual</h2>
                {#await currentUser}
                    <p class="muted">Cargando datos de sesion...</p>
                {:then user}
                    <dl class="meta-grid">
                        <div>
                            <dt>Nombre</dt>
                            <dd>{user.name ?? "Sin nombre"}</dd>
                        </div>
                        <div>
                            <dt>Email</dt>
                            <dd>{user.email ?? "Sin correo"}</dd>
                        </div>
                        <div>
                            <dt>ID</dt>
                            <dd>{user.$id ?? "Sin ID"}</dd>
                        </div>
                    </dl>
                {:catch error}
                    <p class="muted">{error.message}</p>
                {/await}
            </div>
        </Card>
    </div>

    <Card variant="filled">
        <div class="actions-card">
            <div>
                <h2>ToastHost</h2>
                <p class="muted">Validacion temporal del flujo de feedback y de la navegacion interna.</p>
            </div>
            <div class="actions-row">
                <Button variant="filled" size="m" onclick={() => toastStore.success("Home temporal lista")}>
                    Mostrar exito
                </Button>
                <Button variant="tonal" size="m" onclick={() => toastStore.info("Navegacion interna cableada")}>
                    Mostrar info
                </Button>
                <Button variant="text" size="m" onclick={() => toastStore.error("Toast de error temporal")}>
                    Mostrar error
                </Button>
            </div>
        </div>
    </Card>
</section>

<style>
    .home-shell {
        display: grid;
        gap: 16px;
        min-height: 100%;
        align-content: start;
    }

    .hero-card {
        border-radius: 28px;
        padding: 24px;
        background:
            radial-gradient(circle at top left, color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent), transparent 34%),
            linear-gradient(
                180deg,
                color-mix(in srgb, var(--md-sys-color-primary-container) 32%, var(--md-sys-color-surface-container-high)) 0%,
                var(--md-sys-color-surface-container) 100%
            );
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 85%, transparent);
    }

    .hero-copy {
        display: grid;
        gap: 8px;
        max-width: 48rem;
    }

    .eyebrow {
        font-size: 0.78rem;
        font-weight: 700;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--md-sys-color-primary);
    }

    h1,
    h2,
    p,
    dl,
    dd,
    dt {
        margin: 0;
    }

    h1 {
        font-size: clamp(1.8rem, 4vw, 2.6rem);
        line-height: 1.05;
    }

    h2 {
        font-size: 1.05rem;
    }

    .content-grid {
        display: grid;
        gap: 16px;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
    }

    .info-card,
    .actions-card {
        display: grid;
        gap: 14px;
        padding: 18px;
    }

    .meta-grid {
        display: grid;
        gap: 12px;
        grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
    }

    .meta-grid div {
        display: grid;
        gap: 4px;
    }

    dt {
        font-size: 0.78rem;
        font-weight: 700;
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    dd,
    .muted {
        color: var(--md-sys-color-on-surface-variant);
        line-height: 1.45;
        overflow-wrap: anywhere;
    }

    .actions-row {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
    }

    .actions-row :global(.m3-button) {
        min-width: 0;
    }
    
    @media (max-width: 720px) {
        .hero-card {
            padding: 18px;
        }

        .actions-row {
            display: grid;
        }

        .actions-row :global(.m3-button) {
            width: 100%;
        }
    }
</style>
