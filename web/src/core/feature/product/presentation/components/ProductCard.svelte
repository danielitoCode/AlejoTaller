<script lang="ts">
    import { Icon } from "m3-svelte";
    import FavoriteBrokenRounded from "@ktibow/iconset-material-symbols/favorite-outline-rounded";
    import type { Product } from "../../domain/entity/Product";

    export let product: Product;
    export let onClick: () => void = () => {};
    export let onFavoriteClick: (event: Event) => void = () => {};
</script>

<div class="product-item" on:click={onClick} role="button" tabindex="0" on:keydown={(e) => e.key === 'Enter' && onClick()}>
    <div class="card-image">
        <img
            src={product.photoUrl || `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='200' height='200'%3E%3Crect fill='%23f5f5f5' width='200' height='200'/%3E%3C/svg%3E`}
            alt={product.name}
        />
        <div class="card-overlay">
            <button
                class="favorite-btn"
                type="button"
                aria-label="Agregar a favoritos"
                on:click={(e) => {
                    e.stopPropagation();
                    onFavoriteClick(e);
                }}
            >
                <Icon icon={FavoriteBrokenRounded} />
            </button>
        </div>
    </div>

    <div class="card-footer">
        <h3 class="product-name">{product.name}</h3>
        {#if product.price}
            <p class="product-price">${product.price.toFixed(2)}</p>
        {/if}
    </div>
</div>

<style>
    .product-item {
        display: flex;
        flex-direction: column;
        cursor: pointer;
        gap: 8px;
        min-width: 0;
    }

    .card-image {
        position: relative;
        width: 100%;
        aspect-ratio: 1 / 1.08;
        max-height: 220px;
        background: var(--md-sys-color-surface-variant);
        overflow: hidden;
        border-radius: 20px;
    }

    .card-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center;
    }

    .card-overlay {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        width: 100%;
        background: linear-gradient(
            to top,
            var(--md-sys-color-background),
            var(--md-sys-color-background)
        );
        opacity: 0.5;
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        padding: 5px 10px;
        border-radius: 0 0 20px 20px;
    }

    .favorite-btn {
        background: none;
        border: none;
        cursor: pointer;
        color: var(--md-sys-color-on-background);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0;
        width: 24px;
        height: 24px;
    }

    .card-footer {
        padding: 0;
        min-height: 2.5rem;
        display: grid;
        gap: 4px;
    }

    .product-name {
        font-weight: 500;
        font-size: 0.95rem;
        line-height: 1.2;
        color: var(--md-sys-color-on-surface-variant);
        margin: 0;
        overflow: hidden;
        display: -webkit-box;
        line-clamp: 2;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
        word-break: break-word;
    }

    .product-price {
        margin: 0;
        font-size: 0.98rem;
        font-weight: 700;
        color: var(--md-sys-color-primary);
    }

    @media (max-width: 768px) {
        .card-image {
            max-height: 200px;
        }
    }

    @media (max-width: 480px) {
        .card-image {
            max-height: 170px;
            border-radius: 18px;
        }
    }
</style>
