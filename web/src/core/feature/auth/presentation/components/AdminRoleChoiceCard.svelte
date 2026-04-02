<script lang="ts">
    import { createEventDispatcher } from "svelte";
    import { Button, Card } from "m3-svelte";
    import { LayoutDashboard, ShoppingBag, ShieldUser } from "lucide-svelte";

    export let busy = false;
    export let surface = "overlay";

    const dispatch = createEventDispatcher<{
        stayClient: void;
        goAdmin: void;
    }>();
</script>

<div class:overlay-shell={surface === "overlay"} class:inline-shell={surface !== "overlay"}>
    <div class="panel">
        <Card variant="filled">
            <div class="content">
                <div class="badge">
                    <ShieldUser size={22} />
                </div>

                <div class="copy">
                    <p class="eyebrow">Cuenta administrativa</p>
                    <h3>Elige como quieres continuar</h3>
                    <p>
                        Tu cuenta tiene acceso de administrador. Puedes seguir en la experiencia de cliente
                        o abrir el panel administrativo en una ventana dedicada.
                    </p>
                </div>

                <div class="actions">
                    <Button variant="outlined" size="m" disabled={busy} onclick={() => dispatch("stayClient")}>
                        <span class="btn-content">
                            <ShoppingBag size={18} />
                            <span>Seguir en cliente</span>
                        </span>
                    </Button>

                    <Button variant="filled" size="m" disabled={busy} onclick={() => dispatch("goAdmin")}>
                        <span class="btn-content">
                            <LayoutDashboard size={18} />
                            <span>Ir al administrador</span>
                        </span>
                    </Button>
                </div>
            </div>
        </Card>
    </div>
</div>

<style>
    .overlay-shell {
        position: fixed;
        inset: 0;
        z-index: 1200;
        display: grid;
        place-items: center;
        padding: 20px;
        background:
            linear-gradient(
                180deg,
                color-mix(in srgb, var(--md-sys-color-scrim) 40%, transparent),
                color-mix(in srgb, var(--md-sys-color-scrim) 55%, transparent)
            );
        backdrop-filter: blur(10px);
    }

    .inline-shell {
        width: 100%;
        display: grid;
        justify-items: center;
    }

    .panel {
        width: min(520px, calc(100vw - 24px));
    }

    .panel :global(.m3-container.filled) {
        border-radius: 28px;
        background: var(--md-sys-color-surface-container-high);
        --m3v-background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 24px 60px rgb(0 0 0 / 0.22);
    }

    .content {
        display: grid;
        gap: 18px;
        padding: 22px;
    }

    .badge {
        width: 52px;
        height: 52px;
        border-radius: 18px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, var(--md-sys-color-surface));
        color: var(--md-sys-color-primary);
    }

    .copy {
        display: grid;
        gap: 8px;
    }

    .eyebrow,
    .copy h3,
    .copy p {
        margin: 0;
    }

    .eyebrow {
        font-size: 0.82rem;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--md-sys-color-primary);
    }

    .copy h3 {
        font-size: clamp(1.35rem, 4vw, 1.8rem);
        line-height: 1.15;
    }

    .copy p:last-child {
        color: var(--md-sys-color-on-surface-variant);
        line-height: 1.5;
    }

    .actions {
        display: grid;
        gap: 12px;
    }

    .actions :global(.m3-container) {
        width: 100%;
    }

    .btn-content {
        width: 100%;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        padding: 8px 0;
    }

    @media (min-width: 640px) {
        .actions {
            grid-template-columns: 1fr 1fr;
        }
    }
</style>
