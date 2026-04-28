<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import {Button, Card, Icon} from "m3-svelte";
    import shoppingBagIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import arrowOutwardIcon from "@ktibow/iconset-material-symbols/arrow-outward-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {BuyState, DeliveryType} from "../../../feature/sale/domain/entity/enums";
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

    function deliveryMeta(deliveryType?: DeliveryType | null) {
        if (deliveryType === DeliveryType.DELIVERY) {
            return { label: "A domicilio", icon: localShippingIcon };
        }
        if (deliveryType === DeliveryType.PICKUP) {
            return { label: "Recogida", icon: storeIcon };
        }
        return { label: "Entrega pendiente", icon: scheduleIcon };
    }

    function itemsSummary(
        products: Array<{ productName?: string | null; productId: string; quantity: number }>
    ) {
        const totalUnits = products.reduce((sum, item) => sum + item.quantity, 0);
        const names = products
            .slice(0, 2)
            .map((item) => item.productName?.trim() || item.productId)
            .join(" · ");
        const extra = products.length > 2 ? ` +${products.length - 2}` : "";

        return {
            totalUnits,
            preview: names ? `${names}${extra}` : "Sin productos"
        };
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
                {@const delivery = deliveryMeta(sale.deliveryType)}
                {@const summary = itemsSummary(sale.products)}
                <button
                    class="sale-card {meta.tone}"
                    type="button"
                    on:click={() => navController.navigate(reservationDetail.path, { id: sale.id })}
                >
                    <div class="sale-copy">
                        <div class="card-head">
                            <div class="title-block">
                                <span class="card-kicker">Pedido</span>
                                <strong>#{sale.id.slice(0, 8)}</strong>
                            </div>
                            <div class="badge {meta.tone}">
                                <Icon icon={meta.icon} />
                                <span>{meta.label}</span>
                            </div>
                        </div>

                        <div class="card-main">
                            <div class="amount-block">
                                <span class="amount-label">Total reservado</span>
                                <strong>${sale.amount.toFixed(2)} CUP</strong>
                            </div>
                            <div class="arrow-chip" aria-hidden="true">
                                <Icon icon={arrowOutwardIcon} />
                            </div>
                        </div>

                        <div class="pill-row">
                            <span class="info-pill">
                                <Icon icon={delivery.icon} />
                                <span>{delivery.label}</span>
                            </span>
                            <span class="info-pill">
                                <Icon icon={shoppingBagIcon} />
                                <span>{summary.totalUnits} {summary.totalUnits === 1 ? "articulo" : "articulos"}</span>
                            </span>
                        </div>

                        <div class="meta-grid">
                            <div class="meta-block">
                                <span class="meta-label">Fecha</span>
                                <span>{new Date(sale.date).toLocaleString()}</span>
                            </div>
                            <div class="meta-block">
                                <span class="meta-label">Productos</span>
                                <span>{summary.preview}</span>
                            </div>
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
        border-radius: 28px;
        padding: 0;
        background: transparent;
        text-align: left;
        cursor: pointer;
        transition:
            transform 180ms ease,
            filter 180ms ease;
    }
    .sale-card:hover {
        transform: translateY(-2px);
        filter: saturate(1.04);
    }
    .sale-card:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 76%, white);
        outline-offset: 4px;
    }
    .sale-copy {
        display: grid;
        gap: 16px;
        padding: 18px;
        border-radius: 28px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 66%, transparent);
        background:
            radial-gradient(circle at top right, rgb(255 255 255 / 0.06), transparent 26%),
            linear-gradient(180deg, color-mix(in srgb, var(--md-sys-color-surface-container-high) 95%, transparent) 0%, var(--md-sys-color-surface-container) 100%);
        box-shadow: 0 18px 34px rgb(0 0 0 / 0.12);
        position: relative;
        overflow: hidden;
    }
    .sale-copy::before {
        content: "";
        position: absolute;
        inset: 0 auto 0 0;
        width: 5px;
        background: color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
    }
    .sale-card.pending .sale-copy {
        background:
            radial-gradient(circle at top right, rgb(255 193 92 / 0.18), transparent 28%),
            linear-gradient(
                135deg,
                color-mix(in srgb, #f59e0b 14%, var(--md-sys-color-surface-container-high)) 0%,
                color-mix(in srgb, #f59e0b 7%, var(--md-sys-color-surface-container)) 42%,
                var(--md-sys-color-surface-container) 100%
            );
        border-color: color-mix(in srgb, #f59e0b 24%, var(--md-sys-color-outline-variant));
        box-shadow:
            0 18px 34px rgb(0 0 0 / 0.12),
            inset 0 1px 0 rgb(255 196 118 / 0.08);
    }
    .sale-card.pending .sale-copy::before {
        background: linear-gradient(180deg, #f6ad2e 0%, #d97706 100%);
    }
    .sale-card.ready .sale-copy {
        background:
            radial-gradient(circle at top right, color-mix(in srgb, #7fd98f 20%, transparent) 0%, transparent 28%),
            linear-gradient(
                135deg,
                color-mix(in srgb, #7fd98f 14%, var(--md-sys-color-surface-container-high)) 0%,
                color-mix(in srgb, #7fd98f 8%, var(--md-sys-color-surface-container)) 40%,
                var(--md-sys-color-surface-container) 100%
            );
        border-color: color-mix(in srgb, #7fd98f 22%, var(--md-sys-color-outline-variant));
        box-shadow:
            0 18px 34px rgb(0 0 0 / 0.12),
            inset 0 1px 0 rgb(180 255 196 / 0.08);
    }
    .sale-card.ready .sale-copy::before {
        background: linear-gradient(180deg, #7fd98f 0%, var(--md-sys-color-primary) 100%);
    }
    .sale-card.cancelled .sale-copy {
        background:
            radial-gradient(circle at top right, color-mix(in srgb, #ff8f8f 20%, transparent) 0%, transparent 28%),
            linear-gradient(
                135deg,
                color-mix(in srgb, #ff8f8f 13%, var(--md-sys-color-surface-container-high)) 0%,
                color-mix(in srgb, #ff8f8f 7%, var(--md-sys-color-surface-container)) 40%,
                var(--md-sys-color-surface-container) 100%
            );
        border-color: color-mix(in srgb, #ff8f8f 22%, var(--md-sys-color-outline-variant));
        box-shadow:
            0 18px 34px rgb(0 0 0 / 0.12),
            inset 0 1px 0 rgb(255 205 205 / 0.07);
    }
    .sale-card.cancelled .sale-copy::before {
        background: linear-gradient(180deg, #ff8f8f 0%, var(--md-sys-color-error) 100%);
    }
    .card-head,
    .card-main,
    .badge {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
    }
    .title-block,
    .amount-block,
    .meta-block {
        display: grid;
        gap: 4px;
    }
    .card-kicker,
    .amount-label,
    .meta-label {
        font-size: 0.76rem;
        font-weight: 800;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--md-sys-color-on-surface-variant);
    }
    .title-block strong,
    .amount-block strong {
        display: block;
    }
    .title-block strong {
        font-size: 1.18rem;
        letter-spacing: -0.02em;
    }
    .amount-block strong {
        font-size: clamp(1.2rem, 2vw, 1.55rem);
        letter-spacing: -0.03em;
    }
    .empty-state p,
    .meta-block span:last-child {
        color: var(--md-sys-color-on-surface-variant);
    }
    .pill-row {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
    }
    .info-pill,
    .arrow-chip {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        border-radius: 999px;
    }
    .info-pill {
        padding: 8px 12px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 34%, transparent);
        color: var(--md-sys-color-on-surface);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 48%, transparent);
        font-size: 0.86rem;
        font-weight: 700;
    }
    .arrow-chip {
        justify-content: center;
        width: 42px;
        height: 42px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 48%, transparent);
        color: var(--md-sys-color-on-surface);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 42%, transparent);
        flex: 0 0 auto;
    }
    .meta-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 12px;
    }
    .meta-block {
        padding: 12px 14px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 28%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 36%, transparent);
        min-width: 0;
    }
    .meta-block span:last-child {
        overflow: hidden;
        text-overflow: ellipsis;
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
        background: color-mix(in srgb, #f59e0b 16%, transparent);
        color: #e59a17;
        border: 1px solid color-mix(in srgb, #f59e0b 26%, transparent);
    }
    .badge.ready {
        background: color-mix(in srgb, #7fd98f 16%, transparent);
        color: #84d99a;
        border: 1px solid color-mix(in srgb, #7fd98f 22%, transparent);
    }
    .badge.cancelled {
        background: color-mix(in srgb, #ff8f8f 15%, transparent);
        color: #ff9d9d;
        border: 1px solid color-mix(in srgb, #ff8f8f 24%, transparent);
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
    @media (max-width: 680px) {
        .card-head,
        .card-main {
            align-items: start;
        }
        .card-head,
        .meta-grid {
            grid-template-columns: 1fr;
        }
        .card-head {
            display: grid;
        }
        .card-main {
            gap: 14px;
        }
        .arrow-chip {
            width: 38px;
            height: 38px;
        }
    }
</style>
