<script lang="ts">
    import {Button, Card, Icon} from "m3-svelte";
    import deleteIcon from "@ktibow/iconset-material-symbols/delete-rounded";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {cartStore} from "../../../feature/sale/presentation/viewmodel/cart.store";
    import {buyConfirm, dashboard} from "../navigation/nested.router";
    import CurrencySwitch from "../../../feature/exchange/presentation/components/CurrencySwitch.svelte";
    import { exchangeStore, formatMoney } from "../../../feature/exchange/presentation/viewmodels/exchanges.store";
    import { getPrimaryProductImageUrl } from "../../../feature/product/presentation/utils/product.images";
    import { availableStock } from "../../../feature/product/domain/entity/Product";
    import { toastStore } from "../viewmodel/toast.store";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navBackStackEntry;

    $: items = $cartStore.items;
    $: totalAmount = $cartStore.items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
    $: displayTotalAmount = formatMoney(totalAmount, $exchangeStore);
    $: lineCount = items.length;
    $: unitCount = items.reduce((sum, item) => sum + item.quantity, 0);

    function increase(productId: string, current: number, max: number) {
        if (current >= max) {
            toastStore.info(`Máximo disponible: ${max}`);
            return;
        }
        const res = cartStore.setQuantity(productId, current + 1);
        if (res.ok && res.clamped) {
            toastStore.info(`Solo hay ${res.max} disponibles`);
        } else if (!res.ok && res.reason === "out_of_stock") {
            toastStore.error("Sin stock disponible");
        }
    }

    function decrease(productId: string, current: number) {
        cartStore.setQuantity(productId, current - 1);
    }

    function lineTotal(price: number, qty: number): string {
        return formatMoney(price * qty, $exchangeStore);
    }
</script>

<section class="screen">
    <header class="hero header">
        <div class="hero-copy">
            <p class="eyebrow">Compra</p>
            <h1>Tu carrito</h1>
            <p class="support">Cantidades limitadas al stock disponible. Elige moneda y prepara tu reserva.</p>
        </div>
        {#if items.length}
            <div class="hero-side">
                <div class="summary-chips">
                    <span class="chip">{lineCount} {lineCount === 1 ? "línea" : "líneas"}</span>
                    <span class="chip">{unitCount} {unitCount === 1 ? "ud." : "uds."}</span>
                </div>
                <div class="total-pill">
                    <span class="total-label">Total</span>
                    <strong>{displayTotalAmount}</strong>
                </div>
            </div>
        {/if}
    </header>

    {#if !items.length}
        <div class="empty-state">
            <Card variant="outlined">
                <div class="empty-state-content">
                    <div class="empty-icon">
                        <Icon icon={shoppingCartIcon} />
                    </div>
                    <h2>Carrito vacío</h2>
                    <p>Agrega piezas desde el catálogo para continuar con la reserva.</p>
                    <Button variant="filled" size="m" onclick={() => navController.navigate(dashboard.path)}>
                        Ver productos
                    </Button>
                </div>
            </Card>
        </div>
    {:else}
        <div class="toolbar">
            <CurrencySwitch />
        </div>

        <div class="list">
            {#each items as item (item.product.id)}
                {@const max = availableStock(item.product)}
                {@const atMax = item.quantity >= max}
                {@const img = getPrimaryProductImageUrl(item.product.photoUrl) ?? "/alejoicon_clean.svg"}
                <article class="cart-card">
                    <div class="cart-card-body">
                        <div class="item-main">
                            <div class="item-thumb">
                                <img src={img} alt="" loading="lazy" />
                            </div>
                            <div class="item-copy">
                                <strong class="item-name">{item.product.name}</strong>
                                <div class="price-row">
                                    <span class="unit-price">{formatMoney(item.product.price, $exchangeStore)} c/u</span>
                                    <span class="line-total">{lineTotal(item.product.price, item.quantity)}</span>
                                </div>
                                <span class="stock-hint" class:at-max={atMax} class:out={max <= 0}>
                                    {#if max <= 0}
                                        Sin stock
                                    {:else if atMax}
                                        Máximo alcanzado · {max}
                                    {:else}
                                        Disponibles: {max}
                                    {/if}
                                </span>
                            </div>
                        </div>

                        <div class="controls">
                            <div class="qty" role="group" aria-label="Cantidad">
                                <button
                                    type="button"
                                    class="qty-btn"
                                    aria-label="Quitar una unidad"
                                    on:click={() => decrease(item.product.id, item.quantity)}
                                >−</button>
                                <span class="qty-value">{item.quantity}</span>
                                <button
                                    type="button"
                                    class="qty-btn"
                                    aria-label="Añadir una unidad"
                                    disabled={atMax}
                                    on:click={() => increase(item.product.id, item.quantity, max)}
                                >+</button>
                            </div>
                            <button
                                type="button"
                                class="remove-btn"
                                on:click={() => cartStore.remove(item.product.id)}
                            >
                                <Icon icon={deleteIcon} />
                                <span>Quitar</span>
                            </button>
                        </div>
                    </div>
                </article>
            {/each}
        </div>

        <div class="checkout-bar">
            <div class="checkout-total">
                <span class="checkout-label">Total a reservar</span>
                <strong class="checkout-amount">{displayTotalAmount}</strong>
            </div>
            <div class="footer-actions">
                <Button variant="outlined" size="m" onclick={() => navController.navigate(dashboard.path)}>
                    Seguir comprando
                </Button>
                <Button variant="filled" size="m" onclick={() => navController.navigate(buyConfirm.path)}>
                    Continuar
                </Button>
            </div>
        </div>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 1.15rem;
        align-content: start;
        padding-bottom: 6.5rem;
        max-width: 880px;
        margin-inline: auto;
        width: 100%;
    }

    .header {
        display: flex;
        flex-wrap: wrap;
        justify-content: space-between;
        gap: 1rem;
        align-items: end;
    }

    .hero {
        padding: 1.25rem 1.35rem;
        border-radius: 1.5rem;
        background:
            radial-gradient(120% 80% at 100% 0%, color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent), transparent 55%),
            linear-gradient(165deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
    }

    .hero-copy {
        flex: 1 1 14rem;
        min-width: 0;
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
        max-width: 34rem;
    }

    .hero-side {
        display: grid;
        gap: 0.55rem;
        justify-items: end;
    }

    .summary-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 0.4rem;
        justify-content: flex-end;
    }

    .chip {
        padding: 0.3rem 0.65rem;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 700;
        background: color-mix(in srgb, var(--md-sys-color-surface) 45%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 40%, transparent);
        color: var(--md-sys-color-on-surface-variant);
    }

    .total-pill {
        display: grid;
        gap: 0.1rem;
        padding: 0.55rem 0.9rem;
        border-radius: 1rem;
        background: color-mix(in srgb, var(--md-sys-color-primary) 16%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent);
        color: var(--md-sys-color-primary);
        text-align: right;
    }

    .total-label {
        font-size: 0.68rem;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        opacity: 0.85;
    }

    .total-pill strong {
        font-size: 1.05rem;
        font-variant-numeric: tabular-nums;
    }

    .toolbar {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
        align-items: center;
    }

    .list {
        display: grid;
        gap: 0.85rem;
    }

    .cart-card {
        border-radius: 1.25rem;
        background:
            radial-gradient(circle at 0% 0%, color-mix(in srgb, var(--md-sys-color-primary) 8%, transparent), transparent 40%),
            linear-gradient(180deg, var(--md-sys-color-surface-container-high), var(--md-sys-color-surface-container));
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 58%, transparent);
        box-shadow: 0 12px 26px rgb(0 0 0 / 0.12);
        transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
        overflow: hidden;
    }

    .cart-card:hover {
        transform: translateY(-2px);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 28%, var(--md-sys-color-outline-variant));
        box-shadow: 0 16px 32px rgb(0 0 0 / 0.16);
    }

    .cart-card-body {
        display: grid;
        gap: 0.85rem;
        padding: 1rem;
    }

    .item-main {
        display: grid;
        grid-template-columns: 5rem minmax(0, 1fr);
        gap: 0.9rem;
        align-items: center;
    }

    .item-thumb {
        width: 5rem;
        height: 5rem;
        border-radius: 1rem;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-highest, var(--md-sys-color-surface-container-high));
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 55%, transparent);
    }

    .item-thumb img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .item-copy {
        display: grid;
        gap: 0.3rem;
        min-width: 0;
    }

    .item-name {
        font-size: 0.98rem;
        line-height: 1.3;
        letter-spacing: -0.01em;
    }

    .price-row {
        display: flex;
        flex-wrap: wrap;
        gap: 0.5rem 1rem;
        align-items: baseline;
        justify-content: space-between;
    }

    .unit-price {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.86rem;
    }

    .line-total {
        font-weight: 800;
        font-variant-numeric: tabular-nums;
        font-size: 0.95rem;
        color: var(--md-sys-color-primary);
    }

    .stock-hint {
        font-size: 0.76rem;
        font-weight: 700;
        color: var(--md-sys-color-secondary);
    }

    .stock-hint.at-max {
        color: var(--md-sys-color-tertiary);
    }

    .stock-hint.out {
        color: var(--md-sys-color-error);
    }

    .controls {
        display: flex;
        flex-wrap: wrap;
        gap: 0.65rem;
        align-items: center;
        justify-content: space-between;
        padding-top: 0.75rem;
        border-top: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 45%, transparent);
    }

    .qty {
        display: inline-flex;
        align-items: center;
        gap: 0.15rem;
        padding: 0.2rem;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 55%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
    }

    .qty-btn {
        width: 2.1rem;
        height: 2.1rem;
        border: 0;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        color: inherit;
        font-size: 1.15rem;
        font-weight: 700;
        line-height: 1;
        cursor: pointer;
        display: grid;
        place-items: center;
        transition: background 0.15s ease, opacity 0.15s ease;
    }

    .qty-btn:hover:not(:disabled) {
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
    }

    .qty-btn:disabled {
        opacity: 0.4;
        cursor: not-allowed;
    }

    .qty-value {
        min-width: 1.75rem;
        text-align: center;
        font-weight: 800;
        font-variant-numeric: tabular-nums;
    }

    .remove-btn {
        display: inline-flex;
        align-items: center;
        gap: 0.35rem;
        border: 0;
        background: transparent;
        color: var(--md-sys-color-error);
        font-weight: 700;
        font-size: 0.85rem;
        cursor: pointer;
        padding: 0.35rem 0.5rem;
        border-radius: 0.5rem;
    }

    .remove-btn:hover {
        background: color-mix(in srgb, var(--md-sys-color-error) 12%, transparent);
    }

    .remove-btn :global(svg) {
        width: 1rem;
        height: 1rem;
    }

    .checkout-bar {
        position: sticky;
        bottom: 0.75rem;
        z-index: 5;
        display: flex;
        flex-wrap: wrap;
        gap: 0.85rem;
        align-items: center;
        justify-content: space-between;
        padding: 0.9rem 1rem;
        border-radius: 1.25rem;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 92%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 55%, transparent);
        box-shadow: 0 12px 40px rgb(0 0 0 / 0.28);
        backdrop-filter: blur(12px);
    }

    .checkout-total {
        display: grid;
        gap: 0.1rem;
        min-width: 0;
    }

    .checkout-label {
        font-size: 0.7rem;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        color: var(--md-sys-color-on-surface-variant);
    }

    .checkout-amount {
        font-size: 1.2rem;
        letter-spacing: -0.02em;
        font-variant-numeric: tabular-nums;
    }

    .footer-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 0.55rem;
        align-items: center;
    }

    .empty-state {
        border-radius: 1.5rem;
    }

    .empty-state-content {
        text-align: center;
        display: grid;
        justify-items: center;
        gap: 0.65rem;
        padding: 1.5rem 1.1rem;
    }

    .empty-icon {
        width: 3.5rem;
        height: 3.5rem;
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

    .empty-state-content p {
        color: var(--md-sys-color-on-surface-variant);
        max-width: 22rem;
    }

    @media (max-width: 640px) {
        .screen {
            padding-bottom: 8rem;
        }

        .header,
        .hero-side {
            justify-items: stretch;
            width: 100%;
        }

        .hero-side {
            justify-items: stretch;
        }

        .summary-chips {
            justify-content: flex-start;
        }

        .total-pill {
            text-align: left;
            width: 100%;
        }

        .item-main {
            grid-template-columns: 4.25rem minmax(0, 1fr);
            gap: 0.75rem;
        }

        .item-thumb {
            width: 4.25rem;
            height: 4.25rem;
        }

        .checkout-bar {
            bottom: 0.5rem;
            flex-direction: column;
            align-items: stretch;
        }

        .footer-actions {
            display: grid;
            grid-template-columns: 1fr;
        }

        .footer-actions :global(button) {
            width: 100%;
        }
    }

    @media (min-width: 720px) {
        .cart-card-body {
            grid-template-columns: minmax(0, 1fr) auto;
            align-items: center;
            gap: 1rem;
        }

        .controls {
            border-top: 0;
            padding-top: 0;
            flex-direction: column;
            align-items: flex-end;
            gap: 0.5rem;
        }
    }
</style>
