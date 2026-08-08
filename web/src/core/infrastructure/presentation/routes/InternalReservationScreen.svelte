<script lang="ts">
    import { onMount } from "svelte";
    import {Button, Card, Icon} from "m3-svelte";
    import shoppingBagIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import arrowOutwardIcon from "@ktibow/iconset-material-symbols/arrow-outward-rounded";
    import storefrontIcon from "@ktibow/iconset-material-symbols/storefront-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {BuyState, DeliveryType, type Currency} from "../../../feature/sale/domain/entity/enums";
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

    function formatSaleMoney(amount: number, currency?: Currency | string | null): string {
        const code = (currency && String(currency)) || "USD";
        const value = Number.isFinite(amount) ? amount : 0;
        return `${value.toLocaleString("es-CU", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${code}`;
    }

    function formatSaleDate(raw: string): string {
        try {
            return new Date(raw).toLocaleString("es-CU", {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return String(raw);
        }
    }

    $: items = $saleStore.items
        .filter((sale) => !currentUserId || sale.userId === currentUserId)
        .slice()
        .sort((a, b) => String(b.date).localeCompare(String(a.date)));

    $: pendingCount = items.filter((s) => s.verified === BuyState.UNVERIFIED).length;
    $: readyCount = items.filter((s) => s.verified === BuyState.VERIFIED).length;
</script>

<section class="screen">
    <header class="hero anim-in">
        <div class="hero-copy">
            <p class="eyebrow">Reservas</p>
            <h1>Mis reservas</h1>
            <p class="support">Estado de tus pedidos, importes en la moneda elegida y acceso rápido al detalle.</p>
        </div>
        {#if items.length}
            <div class="hero-stats" aria-label="Resumen de reservas">
                <div class="stat">
                    <span class="stat-value">{items.length}</span>
                    <span class="stat-label">Total</span>
                </div>
                <div class="stat pending">
                    <span class="stat-value">{pendingCount}</span>
                    <span class="stat-label">Pendientes</span>
                </div>
                <div class="stat ready">
                    <span class="stat-value">{readyCount}</span>
                    <span class="stat-label">Listos</span>
                </div>
            </div>
        {/if}
    </header>

    {#if bootstrapping}
        <Card variant="filled" class="empty-state anim-in">
            <div class="skeleton-pulse"></div>
            <h2>Cargando reservas…</h2>
            <p>Sincronizando tus pedidos.</p>
        </Card>
    {:else if !items.length}
        <Card variant="outlined" class="empty-state anim-in">
            <div class="empty-icon bounce-soft">
                <Icon icon={shoppingBagIcon} />
            </div>
            <h2>Aún no tienes compras</h2>
            <p>Aquí aparecerán tus pedidos cuando realices una reserva.</p>
            <Button
                class="action-btn primary-action"
                variant="filled"
                size="m"
                iconType="left"
                onclick={() => navController.navigate(dashboard.path)}
            >
                <Icon icon={storefrontIcon} />
                Ver productos
            </Button>
        </Card>
    {:else}
        <div class="list">
            {#each items as sale, i (sale.id)}
                {@const meta = saleStatusMeta(sale.verified)}
                {@const delivery = deliveryMeta(sale.deliveryType)}
                {@const summary = itemsSummary(sale.products)}
                <button
                    class="sale-card {meta.tone} anim-card"
                    type="button"
                    style={`--i: ${i}`}
                    on:click={() => navController.navigate(reservationDetail.path, { id: sale.id })}
                >
                    <div class="sale-copy">
                        <div class="card-head">
                            <div class="title-block">
                                <span class="card-kicker">Pedido</span>
                                <strong class="order-id">#{sale.id.slice(0, 8)}</strong>
                            </div>
                            <div class="badge {meta.tone}">
                                <Icon icon={meta.icon} />
                                <span>{meta.label}</span>
                            </div>
                        </div>

                        <div class="card-main">
                            <div class="amount-block">
                                <span class="amount-label">Total reservado</span>
                                <strong class="amount-value">{formatSaleMoney(sale.amount, sale.currency)}</strong>
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
                                <span>{summary.totalUnits} {summary.totalUnits === 1 ? "artículo" : "artículos"}</span>
                            </span>
                            {#if sale.currency}
                                <span class="info-pill muted">
                                    <span>{String(sale.currency)}</span>
                                </span>
                            {/if}
                        </div>

                        <div class="meta-grid">
                            <div class="meta-block">
                                <span class="meta-label">Fecha</span>
                                <span class="meta-value">{formatSaleDate(sale.date)}</span>
                            </div>
                            <div class="meta-block">
                                <span class="meta-label">Productos</span>
                                <span class="meta-value">{summary.preview}</span>
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
        gap: 1.25rem;
        align-content: start;
        padding-bottom: 1rem;
        max-width: 960px;
        margin-inline: auto;
        width: 100%;
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
        font-size: 0.72rem;
        font-weight: 800;
        letter-spacing: 0.1em;
    }

    .hero {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        gap: 1.25rem;
        align-items: end;
        padding: 1.25rem 1.35rem;
        border-radius: 1.5rem;
        background:
            radial-gradient(120% 80% at 100% 0%, color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent), transparent 55%),
            linear-gradient(165deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
    }

    .hero-copy {
        flex: 1 1 16rem;
        min-width: 0;
    }

    .hero h1 {
        font-size: clamp(1.45rem, 3.5vw, 1.85rem);
        letter-spacing: -0.03em;
        margin-top: 0.2rem;
    }

    .support {
        margin-top: 0.4rem;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.92rem;
        line-height: 1.45;
        max-width: 36rem;
    }

    .hero-stats {
        display: flex;
        gap: 0.5rem;
        flex-wrap: wrap;
    }

    .stat {
        display: grid;
        gap: 0.15rem;
        min-width: 4.5rem;
        padding: 0.65rem 0.85rem;
        border-radius: 1rem;
        background: color-mix(in srgb, var(--md-sys-color-surface) 40%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 40%, transparent);
        text-align: center;
        transition: transform 0.2s ease;
    }

    .stat:hover {
        transform: translateY(-2px);
    }

    .stat-value {
        font-size: 1.15rem;
        font-weight: 800;
        letter-spacing: -0.02em;
    }

    .stat-label {
        font-size: 0.68rem;
        font-weight: 700;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        color: var(--md-sys-color-on-surface-variant);
    }

    .stat.pending .stat-value { color: #f0b429; }
    .stat.ready .stat-value { color: #7fd98f; }

    .list {
        display: grid;
        gap: 0.85rem;
    }

    .sale-card {
        width: 100%;
        border: 0;
        border-radius: 1.35rem;
        padding: 0;
        background: transparent;
        text-align: left;
        cursor: pointer;
        color: inherit;
        transition: transform 200ms cubic-bezier(0.22, 1, 0.36, 1), filter 200ms ease;
    }

    .sale-card.anim-card {
        animation: card-in 0.45s cubic-bezier(0.22, 1, 0.36, 1) both;
        animation-delay: calc(var(--i, 0) * 55ms);
    }

    .sale-card:hover {
        transform: translateY(-3px);
        filter: saturate(1.05);
    }

    .sale-card:active {
        transform: translateY(-1px) scale(0.995);
    }

    .sale-card:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 80%, white);
        outline-offset: 3px;
    }

    .sale-copy {
        display: grid;
        gap: 1rem;
        padding: 1.1rem 1.2rem 1.15rem;
        border-radius: 1.35rem;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 55%, transparent);
        background:
            radial-gradient(circle at 100% 0%, rgb(255 255 255 / 0.05), transparent 32%),
            linear-gradient(180deg, color-mix(in srgb, var(--md-sys-color-surface-container-high) 96%, transparent), var(--md-sys-color-surface-container));
        box-shadow: 0 14px 28px rgb(0 0 0 / 0.14);
        position: relative;
        overflow: hidden;
        transition: box-shadow 0.22s ease, border-color 0.22s ease;
    }

    .sale-card:hover .sale-copy {
        box-shadow: 0 20px 40px rgb(0 0 0 / 0.2);
    }

    .sale-copy::before {
        content: "";
        position: absolute;
        inset: 0 auto 0 0;
        width: 4px;
        background: color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);
    }

    .sale-card.pending .sale-copy {
        background:
            radial-gradient(circle at 100% 0%, rgb(245 158 11 / 0.16), transparent 34%),
            linear-gradient(145deg, color-mix(in srgb, #f59e0b 12%, var(--md-sys-color-surface-container-high)), var(--md-sys-color-surface-container));
        border-color: color-mix(in srgb, #f59e0b 22%, var(--md-sys-color-outline-variant));
    }
    .sale-card.pending .sale-copy::before {
        background: linear-gradient(180deg, #f6ad2e, #d97706);
    }

    .sale-card.ready .sale-copy {
        background:
            radial-gradient(circle at 100% 0%, color-mix(in srgb, #7fd98f 18%, transparent), transparent 34%),
            linear-gradient(145deg, color-mix(in srgb, #7fd98f 12%, var(--md-sys-color-surface-container-high)), var(--md-sys-color-surface-container));
        border-color: color-mix(in srgb, #7fd98f 20%, var(--md-sys-color-outline-variant));
    }
    .sale-card.ready .sale-copy::before {
        background: linear-gradient(180deg, #7fd98f, var(--md-sys-color-primary));
    }

    .sale-card.cancelled .sale-copy {
        background:
            radial-gradient(circle at 100% 0%, color-mix(in srgb, #ff8f8f 16%, transparent), transparent 34%),
            linear-gradient(145deg, color-mix(in srgb, #ff8f8f 10%, var(--md-sys-color-surface-container-high)), var(--md-sys-color-surface-container));
        border-color: color-mix(in srgb, #ff8f8f 20%, var(--md-sys-color-outline-variant));
        opacity: 0.92;
    }
    .sale-card.cancelled .sale-copy::before {
        background: linear-gradient(180deg, #ff8f8f, var(--md-sys-color-error));
    }

    .card-head,
    .card-main {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 0.75rem;
    }

    .title-block,
    .amount-block,
    .meta-block {
        display: grid;
        gap: 0.2rem;
        min-width: 0;
    }

    .card-kicker,
    .amount-label,
    .meta-label {
        font-size: 0.7rem;
        font-weight: 800;
        letter-spacing: 0.08em;
        text-transform: uppercase;
        color: var(--md-sys-color-on-surface-variant);
    }

    .order-id {
        font-size: 1.12rem;
        letter-spacing: -0.02em;
        font-variant-numeric: tabular-nums;
    }

    .amount-value {
        font-size: clamp(1.25rem, 2.4vw, 1.55rem);
        letter-spacing: -0.03em;
        font-variant-numeric: tabular-nums;
        font-weight: 800;
    }

    .pill-row {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem;
    }

    .info-pill,
    .arrow-chip,
    .badge {
        display: inline-flex;
        align-items: center;
        gap: 0.4rem;
        border-radius: 999px;
    }

    .info-pill {
        padding: 0.4rem 0.75rem;
        background: color-mix(in srgb, var(--md-sys-color-surface) 42%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 42%, transparent);
        font-size: 0.8rem;
        font-weight: 700;
        transition: transform 0.18s ease, background 0.18s ease;
    }

    .sale-card:hover .info-pill {
        background: color-mix(in srgb, var(--md-sys-color-surface) 58%, transparent);
    }

    .info-pill.muted {
        opacity: 0.85;
        font-weight: 800;
        letter-spacing: 0.04em;
    }

    .info-pill :global(svg) {
        width: 15px;
        height: 15px;
        flex-shrink: 0;
    }

    .arrow-chip {
        justify-content: center;
        width: 2.5rem;
        height: 2.5rem;
        flex: 0 0 auto;
        background: color-mix(in srgb, var(--md-sys-color-surface) 50%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 40%, transparent);
        transition:
            transform 0.25s cubic-bezier(0.22, 1, 0.36, 1),
            background 0.2s ease,
            border-color 0.2s ease,
            box-shadow 0.2s ease;
    }

    .arrow-chip :global(svg) {
        transition: transform 0.25s cubic-bezier(0.22, 1, 0.36, 1);
    }

    .sale-card:hover .arrow-chip {
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
        box-shadow: 0 6px 16px color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
        transform: scale(1.06);
    }

    .sale-card:hover .arrow-chip :global(svg) {
        transform: translate(2px, -2px);
    }

    .meta-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 0.65rem;
    }

    .meta-block {
        padding: 0.7rem 0.85rem;
        border-radius: 0.9rem;
        background: color-mix(in srgb, var(--md-sys-color-surface) 32%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 32%, transparent);
    }

    .meta-value {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.88rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .badge {
        width: fit-content;
        padding: 0.35rem 0.7rem;
        font-size: 0.75rem;
        font-weight: 800;
        flex-shrink: 0;
        transition: transform 0.18s ease;
    }

    .sale-card:hover .badge {
        transform: scale(1.04);
    }

    .badge :global(svg) {
        width: 15px;
        height: 15px;
    }

    .badge.pending {
        background: color-mix(in srgb, #f59e0b 18%, transparent);
        color: #f0b429;
        border: 1px solid color-mix(in srgb, #f59e0b 28%, transparent);
    }
    .badge.ready {
        background: color-mix(in srgb, #7fd98f 18%, transparent);
        color: #84d99a;
        border: 1px solid color-mix(in srgb, #7fd98f 24%, transparent);
    }
    .badge.cancelled {
        background: color-mix(in srgb, #ff8f8f 16%, transparent);
        color: #ff9d9d;
        border: 1px solid color-mix(in srgb, #ff8f8f 24%, transparent);
    }

    .empty-state {
        text-align: center;
        align-items: center;
        gap: 0.65rem;
        border-radius: 1.5rem;
        padding: 1.5rem 1rem;
    }

    .empty-icon {
        width: 3.5rem;
        height: 3.5rem;
        margin-inline: auto;
        display: grid;
        place-items: center;
        border-radius: 1rem;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 80%, transparent);
        color: var(--md-sys-color-on-surface-variant);
    }

    .empty-icon :global(svg) {
        width: 1.75rem;
        height: 1.75rem;
    }

    .empty-state :global(.action-btn) {
        transition: transform 0.18s cubic-bezier(0.22, 1, 0.36, 1), box-shadow 0.18s ease;
    }

    .empty-state :global(.action-btn:hover) {
        transform: translateY(-2px);
        box-shadow: 0 8px 20px color-mix(in srgb, var(--md-sys-color-primary) 30%, transparent);
    }

    .empty-state :global(.action-btn:active) {
        transform: scale(0.97);
    }

    .empty-state :global(.primary-action svg) {
        transition: transform 0.2s ease;
    }

    .empty-state :global(.primary-action:hover svg) {
        transform: scale(1.1);
    }

    .skeleton-pulse {
        width: 2.5rem;
        height: 2.5rem;
        margin-inline: auto;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary) 25%, transparent);
        animation: pulse 1.2s ease-in-out infinite;
    }

    .anim-in {
        animation: fade-up 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
    }

    .bounce-soft {
        animation: bounce-soft 1.8s ease-in-out infinite;
    }

    @keyframes pulse {
        0%, 100% { opacity: 0.45; transform: scale(0.92); }
        50% { opacity: 1; transform: scale(1); }
    }

    @keyframes fade-up {
        from { opacity: 0; transform: translateY(10px); }
        to { opacity: 1; transform: translateY(0); }
    }

    @keyframes card-in {
        from { opacity: 0; transform: translateY(12px) scale(0.98); }
        to { opacity: 1; transform: translateY(0) scale(1); }
    }

    @keyframes bounce-soft {
        0%, 100% { transform: translateY(0); }
        50% { transform: translateY(-4px); }
    }

    @media (prefers-reduced-motion: reduce) {
        .anim-in,
        .anim-card,
        .bounce-soft,
        .skeleton-pulse {
            animation: none !important;
        }

        .sale-card,
        .arrow-chip,
        .stat,
        .empty-state :global(.action-btn) {
            transition: none !important;
        }
    }

    @media (max-width: 640px) {
        .hero {
            padding: 1.1rem 1rem;
        }

        .card-head {
            align-items: flex-start;
        }

        .meta-grid {
            grid-template-columns: 1fr;
        }

        .meta-value {
            white-space: normal;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
        }

        .amount-value {
            font-size: 1.2rem;
        }
    }
</style>
