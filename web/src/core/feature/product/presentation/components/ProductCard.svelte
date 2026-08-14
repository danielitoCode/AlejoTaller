<script lang="ts">
    import { Icon } from "m3-svelte";
    import FavoriteOutlineRounded from "@ktibow/iconset-material-symbols/favorite-outline-rounded";
    import FavoriteFilledRounded from "@ktibow/iconset-material-symbols/favorite-rounded";
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
    export let isLiked: boolean = false;
    export let onClick: () => void = () => {};
    export let onFavoriteClick: (event: Event) => void = () => {};

    let burst = false;

    function handleFavorite(e: Event) {
        e.stopPropagation();
        const wasLiked = isLiked;
        onFavoriteClick(e);
        if (!wasLiked) {
            burst = false;
            requestAnimationFrame(() => {
                burst = true;
                setTimeout(() => {
                    burst = false;
                }, 700);
            });
        }
    }

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
                class:liked={isLiked}
                class:burst={burst}
                aria-label={isLiked ? "Quitar de me gusta" : "Me gusta"}
                aria-pressed={isLiked}
                on:click={handleFavorite}
            >
                <span class="heart-icon" aria-hidden="true">
                    <Icon icon={isLiked ? FavoriteFilledRounded : FavoriteOutlineRounded} />
                </span>
                {#if burst}
                    <span class="heart-burst" aria-hidden="true">
                        {#each [0, 1, 2, 3, 4, 5] as i}
                            <span class="burst-particle" style="--i: {i}"></span>
                        {/each}
                    </span>
                {/if}
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
        position: relative;
        width: 42px;
        height: 42px;
        border: none;
        border-radius: 999px;
        display: grid;
        place-items: center;
        cursor: pointer;
        background: color-mix(in srgb, var(--md-sys-color-surface) 88%, transparent);
        color: var(--md-sys-color-on-surface);
        transition: background 0.15s ease, color 0.15s ease;
    }

    .favorite-btn.liked {
        color: #ff2d55;
        background: color-mix(in srgb, #ff2d55 16%, var(--md-sys-color-surface));
    }

    .heart-icon {
        display: grid;
        place-items: center;
        transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    .favorite-btn.burst .heart-icon {
        animation: ig-heart-pop 0.55s cubic-bezier(0.34, 1.56, 0.64, 1);
    }

    @keyframes ig-heart-pop {
        0% { transform: scale(1); }
        25% { transform: scale(0.75); }
        55% { transform: scale(1.35); }
        75% { transform: scale(0.95); }
        100% { transform: scale(1); }
    }

    .heart-burst {
        position: absolute;
        inset: 0;
        pointer-events: none;
    }

    .burst-particle {
        position: absolute;
        left: 50%;
        top: 50%;
        width: 6px;
        height: 6px;
        margin: -3px 0 0 -3px;
        border-radius: 50%;
        background: #ff2d55;
        opacity: 0;
        animation: ig-burst 0.65s ease-out forwards;
        animation-delay: calc(var(--i) * 0.02s);
        --angle: calc(var(--i) * 60deg);
        transform: rotate(var(--angle)) translateY(0) scale(0.4);
    }

    .burst-particle:nth-child(odd) {
        background: #ff7a9a;
        width: 5px;
        height: 5px;
    }

    .burst-particle:nth-child(3n) {
        background: #ffb3c6;
    }

    @keyframes ig-burst {
        0% {
            opacity: 1;
            transform: rotate(var(--angle)) translateY(0) scale(0.5);
        }
        70% {
            opacity: 0.85;
        }
        100% {
            opacity: 0;
            transform: rotate(var(--angle)) translateY(-22px) scale(0.2);
        }
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
