<script lang="ts">
    import { Button, Icon } from "m3-svelte";
    import ArrowBackRounded from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import FavoriteBorderRounded from "@ktibow/iconset-material-symbols/favorite-outline-rounded";
    import ShareRounded from "@ktibow/iconset-material-symbols/share-eta-rounded";
    import ShoppingCartRounded from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import type { Product } from "../../domain/entity/Product";

    export let product: Product;
    export let showTopBar: boolean = true;
    export let onBackClick: () => void = () => {};
    export let onFavoriteClick: () => void = () => {};
    export let onShareClick: () => void = () => {};
    export let onAddToCartClick: () => void = () => {};
</script>

<div class="product-detail-screen">
    {#if showTopBar}
        <header class="header-section">
            <button
                class="icon-button back-button"
                type="button"
                aria-label="Volver"
                on:click={onBackClick}
            >
                <Icon icon={ArrowBackRounded} />
            </button>

            <div class="header-actions">
                <button
                    class="icon-button action-button"
                    type="button"
                    aria-label="Agregar a favoritos"
                    on:click={onFavoriteClick}
                >
                    <Icon icon={FavoriteBorderRounded} />
                </button>
                <button
                    class="icon-button action-button"
                    type="button"
                    aria-label="Compartir"
                    on:click={onShareClick}
                >
                    <Icon icon={ShareRounded} />
                </button>
            </div>
        </header>
    {/if}

    <div class="product-image-section">
        <img
            src={product.photoUrl || `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400'%3E%3Crect fill='%23f5f5f5' width='400' height='400'/%3E%3C/svg%3E`}
            alt={product.name}
            class="product-image"
        />
    </div>

    <div class="detail-copy-card">
        <section class="product-info-section">
            <h1 class="product-name">{product.name}</h1>

            {#if product.price}
                <div class="price-section">
                    <span class="price-value">${product.price.toFixed(2)}</span>
                </div>
            {/if}
        </section>

        <section class="description-section">
            <h2 class="description-title">Descripcion</h2>
            <div class="description-scroll">
                <p class="product-description">
                    {product.description}
                </p>
            </div>
        </section>
    </div>

    <footer class="bottom-bar">
        <div class="cart-action">
            <Button variant="filled" size="m" onclick={onAddToCartClick}>
                <Icon icon={ShoppingCartRounded} />
                <span>Agregar al carrito</span>
            </Button>
        </div>
    </footer>
</div>

<style>
    .product-detail-screen {
        width: 100%;
        height: 100%;
        min-height: 0;
        display: grid;
        grid-template-rows: auto minmax(170px, 230px) minmax(0, 1fr) auto;
        background: var(--md-sys-color-background);
        overflow: hidden;
    }

    .header-section {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 16px 16px 8px;
        gap: 8px;
    }

    .icon-button {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: transparent;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--md-sys-color-on-surface);
        transition: all 0.2s ease;
    }

    .icon-button:hover {
        background: var(--md-sys-color-surface-container);
    }

    .icon-button:active {
        transform: scale(0.95);
    }

    .action-button {
        background: var(--md-sys-color-surface);
    }

    .header-actions {
        display: flex;
        gap: 8px;
    }

    .product-image-section {
        min-height: 0;
        background: var(--md-sys-color-surface-container-high);
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 16px;
        border-radius: 28px;
        overflow: hidden;
    }

    .product-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center;
    }

    .detail-copy-card {
        min-height: 0;
        margin: 10px 16px 0;
        border-radius: 28px;
        background: linear-gradient(
            180deg,
            var(--md-sys-color-surface-container-high) 0%,
            var(--md-sys-color-surface-container) 100%
        );
        display: grid;
        grid-template-rows: auto minmax(0, 1fr);
        overflow: hidden;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }

    .product-info-section {
        padding: 18px 18px 8px;
        display: flex;
        flex-direction: column;
        gap: 6px;
        min-height: 0;
    }

    .product-name {
        font-size: 1.5rem;
        font-weight: 600;
        color: var(--md-sys-color-on-background);
        margin: 0;
        line-height: 1.2;
    }

    .price-section {
        display: flex;
        align-items: center;
    }

    .price-value {
        font-size: 1.08rem;
        font-weight: 500;
        color: var(--md-sys-color-on-surface-variant);
    }

    .description-section {
        min-height: 0;
        padding: 8px 18px 18px;
        display: grid;
        grid-template-rows: auto minmax(0, 1fr);
        gap: 8px;
        overflow: hidden;
    }

    .description-title {
        margin: 0;
        font-size: 0.82rem;
        font-weight: 700;
        color: var(--md-sys-color-on-surface-variant);
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .description-scroll {
        min-height: 0;
        height: 100%;
        overflow-y: auto;
        overscroll-behavior-y: contain;
        -webkit-overflow-scrolling: touch;
        padding-right: 4px;
    }

    .product-description {
        font-size: 0.95rem;
        line-height: 1.6;
        color: var(--md-sys-color-on-surface);
        margin: 0;
        white-space: pre-wrap;
    }

    .bottom-bar {
        padding: 14px 16px 24px;
        background: linear-gradient(
            180deg,
            color-mix(in srgb, var(--md-sys-color-surface) 72%, transparent) 0%,
            var(--md-sys-color-surface) 36%
        );
    }

    .cart-action {
        display: grid;
    }

    .cart-action :global(.m3-container) {
        width: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        min-height: 54px;
        border-radius: 22px;
    }

    @media (max-width: 768px) {
        .product-detail-screen {
            grid-template-rows: auto minmax(148px, 192px) minmax(0, 1fr) auto;
        }

        .product-image-section {
            margin: 0 12px;
            border-radius: 20px;
        }

        .detail-copy-card {
            margin: 10px 12px 0;
            border-radius: 22px;
        }

        .product-info-section {
            padding: 16px 14px 8px;
        }

        .description-section {
            padding: 6px 14px 14px;
        }

        .bottom-bar {
            padding: 14px 12px 20px;
        }
    }
</style>
