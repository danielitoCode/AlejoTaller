<script lang="ts">
    import {Button, Card, Icon} from "m3-svelte";
    import deleteIcon from "@ktibow/iconset-material-symbols/delete-rounded";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {cartStore} from "../../../feature/sale/presentation/viewmodel/cart.store";
    import {buyConfirm, dashboard} from "../navigation/nested.router";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navBackStackEntry;

    $: items = $cartStore.items;
    $: totalAmount = $cartStore.items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
</script>

<section class="screen">
    <div class="hero header">
        <div>
            <p class="eyebrow">Compra</p>
            <h1>Tu carrito</h1>
            <p class="support">Revisa cantidades y prepara tu reserva con una visual mas consistente.</p>
        </div>
        {#if items.length}
            <div class="total-pill">Total ${totalAmount.toFixed(2)}</div>
        {/if}
    </div>

    {#if !items.length}
        <div class="empty-state">
            <Card variant="outlined">
                <div class="empty-state-content">
                    <Icon icon={shoppingCartIcon} />
                    <h2>No hay productos en el carrito</h2>
                    <p>Agrega piezas desde productos para continuar con la reserva o el pago.</p>
                    <Button variant="filled" size="m" onclick={() => navController.navigate(dashboard.path)}>Ver productos</Button>
                </div>
            </Card>
        </div>
    {:else}
        <div class="list">
            {#each items as item}
                <div class="cart-card">
                    <Card variant="elevated">
                        <div class="cart-card-body">
                            <div class="item-top">
                                <div class="item-thumb">
                                    <img src={item.product.photoUrl || "/alejoicon_clean.svg"} alt="" />
                                </div>
                                <div class="item-copy">
                                    <strong>{item.product.name}</strong>
                                    <span>${item.product.price.toFixed(2)} c/u</span>
                                </div>
                            </div>
                            <div class="controls">
                                <div class="qty">
                                    <Button variant="outlined" size="s" onclick={() => cartStore.setQuantity(item.product.id, item.quantity - 1)}>-1</Button>
                                    <span>{item.quantity}</span>
                                    <Button variant="tonal" size="s" onclick={() => cartStore.setQuantity(item.product.id, item.quantity + 1)}>+1</Button>
                                </div>
                                <Button variant="text" size="s" iconType="left" onclick={() => cartStore.remove(item.product.id)}>
                                    <Icon icon={deleteIcon} />
                                    Quitar
                                </Button>
                            </div>
                        </div>
                    </Card>
                </div>
            {/each}
        </div>

        <div class="footer-actions">
            <Button
                    variant="outlined"
                    size="m"
                    onclick={() =>
                           navController.navigate(dashboard.path)
                    }
            >
                <span style="padding: 12px 0">
                    Seguir comprando
                </span>
            </Button>
            <Button
                    variant="filled"
                    size="m"
                    onclick={() =>
                        navController.navigate(buyConfirm.path)
                    }
            >
                <span style="padding: 12px 0">
                    Continuar
                </span>
            </Button>
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

    .header {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: end;
    }

    .hero {
        padding: 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
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

    .support {
        margin-top: 6px;
        color: var(--md-sys-color-on-surface-variant);
    }

    .total-pill {
        padding: 8px 12px;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
        color: var(--md-sys-color-primary);
        font-weight: 800;
    }

    .list {
        display: grid;
        gap: 14px;
        padding: 4px 0;
    }

    .cart-card {
        border-radius: 24px;
        background: linear-gradient(
            180deg,
            var(--md-sys-color-surface-container-high) 0%,
            var(--md-sys-color-surface-container) 100%
        );
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow: 0 12px 28px color-mix(in srgb, black 8%, transparent);
        transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
    }

    .cart-card:hover {
        transform: translateY(-2px);
        box-shadow: 0 16px 30px color-mix(in srgb, black 11%, transparent);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 32%, var(--md-sys-color-outline-variant));
    }

    .cart-card-body {
        display: grid;
        gap: 12px;
        padding: 4px;
    }

    .item-top {
        display: grid;
        grid-template-columns: 72px minmax(0, 1fr);
        gap: 14px;
        align-items: center;
        padding: 14px 14px 0;
    }

    .item-thumb {
        width: 72px;
        height: 72px;
        border-radius: 18px;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-high);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
    }

    .item-thumb img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .item-copy {
        display: grid;
        gap: 4px;
    }

    .item-copy span,
    .empty-state-content p {
        color: var(--md-sys-color-on-surface-variant);
    }

    .controls,
    .qty,
    .footer-actions {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        align-items: center;
    }

    .qty span {
        min-width: 30px;
        text-align: center;
        font-weight: 800;
    }

    .controls {
        justify-content: space-between;
        padding: 0 14px 14px;
        border-top: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 65%, transparent);
        padding-top: 12px;
    }

    .qty {
        padding: 6px;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 52%, transparent);
    }

    .footer-actions {
        justify-content: flex-end;
    }

    .empty-state {
        border-radius: 28px;
    }

    .empty-state-content {
        text-align: center;
        display: grid;
        align-items: center;
        gap: 10px;
        padding: 18px;
    }

    .empty-state-content :global(svg) {
        width: 42px;
        height: 42px;
        color: var(--md-sys-color-on-surface-variant);
    }

    @media (max-width: 640px) {
        .header,
        .footer-actions {
            display: grid;
        }

        .controls {
            display: grid;
            justify-content: stretch;
        }

        .item-top {
            grid-template-columns: 60px minmax(0, 1fr);
            gap: 12px;
        }

        .item-thumb {
            width: 60px;
            height: 60px;
        }

        .footer-actions :global(button) {
            width: 100%;
        }
    }
</style>
