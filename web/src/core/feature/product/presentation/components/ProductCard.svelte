<script lang="ts">
    import { Icon } from "m3-svelte";
    import FavoriteBrokenRounded from "@ktibow/iconset-material-symbols/favorite-outline-rounded";
    import Inventory2Rounded from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import WarningRounded from "@ktibow/iconset-material-symbols/warning-rounded";
    import BlockRounded from "@ktibow/iconset-material-symbols/block";
    import SyncRounded from "@ktibow/iconset-material-symbols/sync-rounded";
    import type { Product } from "../../domain/entity/Product";
    import { availableStock } from "../../domain/entity/Product";
    import { exchangeStore, formatMoney } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import { getPrimaryProductImageUrl } from "../utils/product.images";

    export let product: Product;
    /** true mientras se sincroniza stock desde la nube — no mostrar Agotado falso */
    export let stockPending: boolean = false;
    export let onClick: () => void = () => {};
    export let onFavoriteClick: (event: Event) => void = () => {};

    $: available = availableStock(product);
    $: stockTone = stockPending
        ? "pending"
        : available === 0
          ? "out"
          : available <= 5
            ? "low"
            : "ok";
    $: stockLabel = stockPending
        ? "Sincronizando…"
        : available === 0
          ? "Agotado"
          : available <= 5
            ? `Ultimas ${available}`
            : `Disponibles: ${available}`;
    $: stockIcon =
        stockTone === "pending"
            ? SyncRounded
            : stockTone === "out"
              ? BlockRounded
              : stockTone === "low"
                ? WarningRounded
                : Inventory2Rounded;
</script>

<div
        class="product-item"
        class:is-out={!stockPending && available === 0}
        on:click={onClick}
        role="button"
        tabindex="0"
        on:keydown={(e) => e.key === "Enter" && onClick()}
>
    <div class="card-image">
        <img
                src={
                getPrimaryProductImageUrl(product.photoUrl) ??
                `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='300'%3E%3Crect fill='%23f5f5f5' width='300' height='300'/%3E%3C/svg%3E`
            }
                alt={product.name}
                loading="lazy"
        />

        <span class="stock-badge stock-badge--{stockTone}" aria-label={stockLabel} aria-busy={stockPending}>
            <span class="stock-badge-icon" class:spin={stockTone === "pending"} aria-hidden="true">
                <Icon icon={stockIcon} />
            </span>
            <span class="stock-badge-text">{stockLabel}</span>
        </span>

        <div class="card-overlay">
            <button
                    class="favorite-btn"
                    type="button"
                    aria-label="Agregar a favoritos"
                    title="Favoritos"
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
        <h3 class="product-name">
            {product.name}
        </h3>

        {#if product.price}
            <p class="product-price">
                {formatMoney(product.price, $exchangeStore)}
            </p>
        {/if}
    </div>
</div>

<style>
    .product-item {
        display: flex;
        flex-direction: column;
        gap: 10px;

        min-width: 0;

        cursor: pointer;

        user-select: none;

        transition:
                transform 0.22s ease,
                opacity 0.22s ease;
    }

    .product-item.is-out {
        opacity: 0.78;
    }

    .product-item:active {
        transform: scale(0.985);
    }

    .card-image {
        position: relative;

        width: 100%;
        aspect-ratio: 1 / 1;

        overflow: hidden;

        border-radius: 24px;

        background: var(--md-sys-color-surface-container-high);

        box-shadow:
                0 2px 6px rgba(0, 0, 0, 0.06),
                0 10px 24px rgba(0, 0, 0, 0.10);

        transition:
                transform 0.25s ease,
                box-shadow 0.25s ease;
    }

    .card-image img {
        width: 100%;
        height: 100%;

        object-fit: cover;
        object-position: center;

        transition:
                transform 0.45s ease,
                filter 0.3s ease;
    }

    .product-item.is-out .card-image img {
        filter: grayscale(0.35);
    }

    .product-item:hover .card-image {
        transform: translateY(-3px);

        box-shadow:
                0 8px 18px rgba(0, 0, 0, 0.12),
                0 18px 38px rgba(0, 0, 0, 0.16);
    }

    .product-item:hover .card-image img {
        transform: scale(1.05);
    }

    .stock-badge {
        position: absolute;
        top: 10px;
        left: 10px;
        z-index: 2;

        max-width: calc(100% - 20px);

        display: inline-flex;
        align-items: center;
        gap: 5px;

        padding: 5px 10px 5px 7px;

        border-radius: 999px;

        font-size: 0.72rem;
        font-weight: 700;
        line-height: 1.2;
        letter-spacing: 0.01em;

        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;

        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
    }

    .stock-badge-icon {
        display: inline-flex;
        flex-shrink: 0;
        width: 14px;
        height: 14px;
        align-items: center;
        justify-content: center;
    }

    .stock-badge-icon :global(svg) {
        width: 14px;
        height: 14px;
    }

    .stock-badge-icon.spin :global(svg) {
        animation: stock-spin 0.9s linear infinite;
    }

    .stock-badge-text {
        min-width: 0;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .stock-badge--ok {
        color: var(--md-sys-color-on-secondary-container, #1a1c19);
        background: var(--md-sys-color-secondary-container, #c8efc8);
    }

    .stock-badge--low {
        color: var(--md-sys-color-on-tertiary-container, #3b2f00);
        background: var(--md-sys-color-tertiary-container, #ffe08a);
    }

    .stock-badge--out {
        color: var(--md-sys-color-on-error-container, #410002);
        background: var(--md-sys-color-error-container, #ffdad6);
    }

    .stock-badge--pending {
        color: var(--md-sys-color-on-surface-variant, #444);
        background: var(--md-sys-color-surface-container-highest, #e8e8e8);
        animation: stock-pulse 1.2s ease-in-out infinite;
    }

    @keyframes stock-pulse {
        0%, 100% { opacity: 0.75; }
        50% { opacity: 1; }
    }

    @keyframes stock-spin {
        to { transform: rotate(360deg); }
    }

    .card-overlay {
        position: absolute;

        left: 0;
        right: 0;
        bottom: 0;

        display: flex;
        justify-content: flex-end;
        align-items: center;

        padding: 12px;

        background:
                linear-gradient(
                        to top,
                        rgba(0, 0, 0, 0.55) 0%,
                        rgba(0, 0, 0, 0.20) 35%,
                        transparent 100%
                );
    }

    .favorite-btn {
        width: 42px;
        height: 42px;

        display: flex;
        align-items: center;
        justify-content: center;

        border: none;
        border-radius: 50%;

        cursor: pointer;

        color: var(--md-sys-color-on-surface);

        background:
                radial-gradient(
                    circle at center,
                    #242724 0%,
                    var(--m3c-outline-variant) 100%
                );

        box-shadow:
                0 2px 8px rgba(0, 0, 0, 0.15);

        transition:
                transform 0.2s ease,
                background 0.2s ease;
    }

    .favorite-btn:hover {
        transform: scale(1.08);
    }

    .favorite-btn:active {
        transform: scale(0.95);
    }

    .card-footer {
        display: flex;
        flex-direction: column;

        gap: 6px;

        padding: 2px;
    }

    .product-name {
        margin: 0;

        color: var(--md-sys-color-on-surface);

        font-size: 0.96rem;
        font-weight: 600;

        line-height: 1.35;

        overflow: hidden;

        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;

        min-height: calc(1.35em * 2);
    }

    .product-price {
        margin: 0;

        color: var(--md-sys-color-primary);

        font-size: 1.1rem;
        font-weight: 800;

        letter-spacing: -0.02em;
    }

    @media (max-width: 768px) {
        .card-image {
            border-radius: 22px;
        }

        .favorite-btn {
            width: 40px;
            height: 40px;
        }

        .product-price {
            font-size: 1.02rem;
        }

        .stock-badge {
            font-size: 0.68rem;
            padding: 4px 9px 4px 6px;
        }
    }

    @media (max-width: 480px) {
        .product-item {
            gap: 8px;
        }

        .card-image {
            border-radius: 18px;
        }

        .product-name {
            font-size: 0.88rem;
        }

        .product-price {
            font-size: 0.98rem;
        }

        .favorite-btn {
            width: 38px;
            height: 38px;
        }
    }

    @media (hover: none) {
        .product-item:hover .card-image {
            transform: none;
        }

        .product-item:hover .card-image img {
            transform: none;
        }
    }

    .product-item:focus-visible {
        outline: none;
    }

    .product-item:focus-visible .card-image {
        box-shadow:
                0 0 0 3px var(--md-sys-color-primary),
                0 10px 24px rgba(0, 0, 0, 0.14);
    }
</style>
