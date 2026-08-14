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
    export let stockPending: boolean = false;
    export let salePrice: number | null = null;
    export let listPrice: number | null = null;
    export let promoBadge: boolean = false;
    export let onClick: () => void = () => {};
    export let onFavoriteClick: (event: Event) => void = () => {};

    $: unitPrice = salePrice != null && Number.isFinite(salePrice) ? salePrice : product.price;
    $: showListStrike =
        listPrice != null &&
        Number.isFinite(listPrice) &&
        Number(listPrice) > Number(unitPrice);
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
            ? `Últimas ${available}`
            : `${available} disp.`;
    $: stockIcon =
        stockTone === "pending"
            ? SyncRounded
            : stockTone === "out"
              ? BlockRounded
              : stockTone === "low"
                ? WarningRounded
                : Inventory2Rounded;
    $: imageUrl = getPrimaryProductImageUrl(product?.photoUrl);
</script>

<!-- svelte-ignore a11y_no_static_element_interactions a11y_click_events_have_key_events -->
<div
    class="product-item"
    role="button"
    tabindex="0"
    on:click={onClick}
    on:keydown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            onClick();
        }
    }}
>
    <div class="card-image">
        {#if imageUrl}
            <img src={imageUrl} alt={product.name} loading="lazy" />
        {:else}
            <div class="img-fallback" aria-hidden="true">📦</div>
        {/if}
        <div class="image-overlay">
            <span class="stock-badge stock-{stockTone}">
                <Icon icon={stockIcon} />
                {stockLabel}
            </span>
            <button
                type="button"
                class="favorite-btn"
                aria-label="Favorito"
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

        {#if unitPrice || unitPrice === 0}
            <p class="product-price" class:has-promo={showListStrike}>
                {#if showListStrike}
                    <span class="list-strike">{formatMoney(listPrice, $exchangeStore)}</span>
                {/if}
                <span class="sale-price">{formatMoney(unitPrice, $exchangeStore)}</span>
                {#if promoBadge}
                    <span class="promo-pill">Promo</span>
                {/if}
            </p>
        {/if}
    </div>
</div>

<style>
    .product-item {
        display: flex;
        flex-direction: column;
        gap: 10px;
        width: 100%;
        padding: 0;
        border: none;
        background: transparent;
        color: inherit;
        font: inherit;
        text-align: left;
        cursor: pointer;
    }

    .card-image {
        position: relative;
        aspect-ratio: 1;
        border-radius: 24px;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-high);
    }

    .card-image img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
        transition: transform 0.25s ease;
    }

    .img-fallback {
        width: 100%;
        height: 100%;
        display: grid;
        place-items: center;
        font-size: 2rem;
        opacity: 0.45;
    }

    .product-item:hover .card-image img {
        transform: scale(1.04);
    }

    .image-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        padding: 10px;
        pointer-events: none;
    }

    .image-overlay > * {
        pointer-events: auto;
    }

    .stock-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 4px 10px 4px 6px;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 700;
        line-height: 1.2;
        box-shadow: 0 1px 3px color-mix(in srgb, #000 22%, transparent);
    }

    /* Fondo = color de estado; texto/icono con contraste */
    .stock-ok {
        background: #2e7d32;
        color: #fff;
    }
    .stock-low {
        background: #c47a00;
        color: #fff;
    }
    .stock-out {
        background: #ef9a9a;
        color: #b71c1c;
    }
    .stock-pending {
        background: var(--md-sys-color-surface-container-highest);
        color: var(--md-sys-color-on-surface-variant);
    }

    .favorite-btn {
        width: 42px;
        height: 42px;
        border: none;
        border-radius: 999px;
        display: grid;
        place-items: center;
        cursor: pointer;
        background: color-mix(in srgb, var(--md-sys-color-surface) 88%, transparent);
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
        display: flex;
        flex-wrap: wrap;
        align-items: baseline;
        gap: 6px;
        margin: 0;
        color: var(--md-sys-color-primary);
        font-size: 1.1rem;
        font-weight: 800;
        letter-spacing: -0.02em;
    }

    .list-strike {
        text-decoration: line-through;
        opacity: 0.65;
        font-size: 0.85em;
        font-weight: 600;
    }

    .sale-price {
        font-weight: 800;
    }

    .product-price.has-promo .sale-price {
        color: var(--md-sys-color-primary);
    }

    .promo-pill {
        font-size: 0.68rem;
        font-weight: 800;
        padding: 2px 6px;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
        color: var(--md-sys-color-primary);
        letter-spacing: 0.02em;
        text-transform: uppercase;
    }

    @media (max-width: 768px) {
        .card-image {
            border-radius: 22px;
        }
        .favorite-btn {
            width: 40px;
            height: 40px;
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
        .favorite-btn {
            width: 38px;
            height: 38px;
        }
    }

    @media (hover: none) {
        .product-item:hover .card-image img {
            transform: none;
        }
    }
</style>
