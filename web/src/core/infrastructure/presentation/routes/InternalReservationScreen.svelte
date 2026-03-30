<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import {Button, Card, Icon} from "m3-svelte";
    import shoppingBagIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {BuyState} from "../../../feature/sale/domain/entity/enums";
    import {saleStore} from "../../../feature/sale/presentation/viewmodel/sale.store";
    import {dashboard, reservationDetail} from "../navigation/nested.router";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navBackStackEntry;

    let currentUserId = "";
    let bootstrapping = true;

    async function hydrateReservations() {
        bootstrapping = true;
        try {
            const user = await sessionStore.getCurrentUser().catch(() => null);
            currentUserId = user?.$id ?? "";
            await saleStore.syncAll().catch(() => null);
        } finally {
            bootstrapping = false;
        }
    }

    onMount(() => {
        void hydrateReservations();
        const handleOnline = () => {
            void hydrateReservations();
        };
        window.addEventListener("online", handleOnline);
        return () => {
            window.removeEventListener("online", handleOnline);
        };
    });

    function saleStatusMeta(state: BuyState) {
        if (state === BuyState.VERIFIED) return { label: "Listo", icon: checkCircleIcon, tone: "ready" };
        if (state === BuyState.DELETED) return { label: "Cancelada", icon: cancelIcon, tone: "cancelled" };
        return { label: "Pendiente", icon: scheduleIcon, tone: "pending" };
    }

    $: items = $saleStore.items
        .filter((sale) => !currentUserId || sale.userId === currentUserId)
        .slice()
        .sort((a, b) => String(b.date).localeCompare(String(a.date)));
</script>

<section class="screen">
    <div class="hero">
        <p class="eyebrow">Reservas</p>
        <h1>Mis reservas</h1>
        <p class="support">Consulta el estado de tus pedidos y entra al detalle con el mismo lenguaje visual del modulo.</p>
    </div>

    {#if bootstrapping}
        <Card variant="filled" class="empty-state">
            <h2>Cargando reservas...</h2>
            <p>Estamos sincronizando tus pedidos.</p>
        </Card>
    {:else if !items.length}
        <Card variant="outlined" class="empty-state">
            <Icon icon={shoppingBagIcon} />
            <h2>Aun no tienes compras</h2>
            <p>Aqui apareceran tus pedidos una vez que realices una reserva.</p>
            <Button variant="filled" size="m" onclick={() => navController.navigate(dashboard.path)}>
                Ver productos
            </Button>
        </Card>
    {:else}
        <div class="list">
            {#each items as sale}
                {@const meta = saleStatusMeta(sale.verified)}
                <button class="sale-card" type="button" on:click={() => navController.navigate(reservationDetail.path, { id: sale.id })}>
                    <div class="sale-copy">
                        <div class="top">
                            <div>
                                <strong>Pedido</strong>
                                <span>#{sale.id.slice(0, 8)}</span>
                            </div>
                            <div class="badge {meta.tone}">
                                <Icon icon={meta.icon} />
                                <span>{meta.label}</span>
                            </div>
                        </div>
                        <div class="meta-row">
                            <span>{new Date(sale.date).toLocaleString()}</span>
                            <span>${sale.amount.toFixed(2)} CUP</span>
                        </div>
                    </div>
                </button>
            {/each}
        </div>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 18px;
        align-content: start;
        padding-bottom: 8px;
    }
    .eyebrow,
    h1,
    h2,
    p {
        margin: 0;
    }
    .eyebrow {
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        font-size: 0.78rem;
        font-weight: 800;
        letter-spacing: 0.08em;
    }
    .hero {
        padding: 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }
    .support {
        margin-top: 6px;
        color: var(--md-sys-color-on-surface-variant);
    }
    .list {
        display: grid;
        gap: 12px;
    }
    .sale-card {
        width: 100%;
        border: 0;
        border-radius: 20px;
        padding: 0;
        background: transparent;
        text-align: left;
        cursor: pointer;
    }
    .sale-copy {
        display: grid;
        gap: 12px;
        padding: 16px;
        border-radius: 24px;
        background: linear-gradient(180deg, color-mix(in srgb, var(--md-sys-color-primary-container) 76%, transparent) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 66%, transparent);
    }
    .top,
    .meta-row,
    .badge {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
    }
    .top strong,
    .top span {
        display: block;
    }
    .top span,
    .meta-row,
    .empty-state p {
        color: var(--md-sys-color-on-surface-variant);
    }
    .badge {
        width: fit-content;
        padding: 6px 10px;
        border-radius: 999px;
        font-size: 0.8rem;
        font-weight: 800;
    }
    .badge :global(svg) {
        width: 16px;
        height: 16px;
    }
    .badge.pending {
        background: rgba(255, 151, 60, 0.14);
        color: #d97706;
    }
    .badge.ready {
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
        color: var(--md-sys-color-primary);
    }
    .badge.cancelled {
        background: color-mix(in srgb, var(--md-sys-color-error) 14%, transparent);
        color: var(--md-sys-color-error);
    }
    .empty-state {
        text-align: center;
        align-items: center;
        gap: 10px;
        border-radius: 28px;
    }
    .empty-state :global(svg) {
        width: 42px;
        height: 42px;
        color: var(--md-sys-color-on-surface-variant);
    }
</style>
