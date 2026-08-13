<script lang="ts">
    import { LoadingIndicator, Icon } from "m3-svelte";
    import { fly } from "svelte/transition";
    import SearchBar from "../components/SearchBar.svelte";
    import { exchangeStore, formatMoney } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import CategoryFilter from "../components/CategoryFilter.svelte";
    import ProductCard from "../components/ProductCard.svelte";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";
    import promoImage from "../../../../../assets/hero.png";
    import type { Product } from "../../domain/entity/Product";
    import type { Promotion } from "../../../notification/domain/entity/Promotion";
    import type { Category } from "../../../category/domain/entity/Category";
    import CurrencySwitch from "../../../exchange/presentation/components/CurrencySwitch.svelte";

    export let products: Product[] = [];
    export let promotions: Promotion[] = [];
    export let categories: Category[] = [];
    export let searchQuery: string = "";
    export let selectedCategoryId: string | null = null;
    export let loading: boolean = false;
    export let stockSyncing: boolean = false;
    export let realtimeUpdating: boolean = false;
    export let syncMessage: string | null = null;
    export let onSearchQueryChanged: (query: string) => void = () => {};
    export let onCategorySelected: (categoryId: string | null) => void = () => {};
    export let onProductClick: (productId: string) => void = () => {};
    export let onPromotionClick: (promotionId: string) => void = () => {};
    export let onFavoriteClick: (productId: string) => void = () => {};
    $: exchangeState = $exchangeStore;

    const DISMISS_KEY = "alejo-web-dismissed-promos";

    function readDismissed(): Set<string> {
        try {
            const raw = sessionStorage.getItem(DISMISS_KEY);
            if (!raw) return new Set();
            const arr = JSON.parse(raw);
            return new Set(Array.isArray(arr) ? arr.map(String) : []);
        } catch {
            return new Set();
        }
    }

    function writeDismissed(ids: Set<string>): void {
        try {
            sessionStorage.setItem(DISMISS_KEY, JSON.stringify([...ids]));
        } catch {
            /* ignore */
        }
    }

    let dismissedIds = readDismissed();
    let isPromoVisible = true;

    function dismissPromo(id: string): void {
        dismissedIds = new Set(dismissedIds);
        dismissedIds.add(id);
        writeDismissed(dismissedIds);
        isPromoVisible = false;
    }

    $: filteredProducts = products
        .filter((product) => {
            const matchesSearch =
                product.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                product.description?.toLowerCase().includes(searchQuery.toLowerCase());
            const matchesCategory =
                !selectedCategoryId || product.categoryId === selectedCategoryId;
            return matchesSearch && matchesCategory;
        })
        .map((product) => ({
            ...product,
            displayPrice: formatMoney(product.price, exchangeState),
        }));

    $: activePromotion = (() => {
        const now = Date.now();
        const list = promotions.filter((p) => {
            if (dismissedIds.has(p.id)) return false;
            const from = Number(p.validFromEpochMillis) || 0;
            const until = Number(p.validUntilEpochMillis) || Number.MAX_SAFE_INTEGER;
            return now >= from && now <= until;
        });
        const banners = list.filter(
            (p) => !p.productId || String(p.productId).trim() === "" || p.kind === "banner"
        );
        const discounts = list.filter(
            (p) => p.productId && String(p.productId).trim() !== "" && p.kind !== "banner"
        );
        return banners[0] ?? discounts[0] ?? null;
    })();

    $: if (activePromotion) {
        isPromoVisible = true;
    }
</script>

<div class="product-screen">
    <div class="screen-content">
        <div class="top-row">
            <div class="search-section">
                <SearchBar
                    query={searchQuery}
                    placeholder="Buscar productos..."
                    onQueryChanged={onSearchQueryChanged}
                    onClearQuery={() => onSearchQueryChanged("")}
                />
            </div>
        </div>

        {#if isPromoVisible && activePromotion}
            <div
                class="promo-banner"
                role="region"
                aria-label="Promoción"
                transition:fly={{ y: -12, duration: 220 }}
            >
                <button
                    class="promo-banner-main"
                    type="button"
                    aria-label="Ver promoción"
                    on:click={() => onPromotionClick(activePromotion.id)}
                >
                    <img
                        class="promo-banner-img"
                        src={activePromotion.imageUrl || promoImage}
                        alt=""
                    />
                    <span class="promo-banner-copy">
                        <strong>{activePromotion.title || "Promo activa"}</strong>
                        <small>{activePromotion.message || "Oferta disponible por tiempo limitado"}</small>
                    </span>
                </button>
                <button
                    class="promo-banner-close"
                    type="button"
                    aria-label="Cerrar promoción"
                    on:click={() => dismissPromo(activePromotion.id)}
                >
                    <Icon icon={closeIcon} />
                </button>
            </div>
        {/if}

        {#if stockSyncing || realtimeUpdating}
            <div
                class="stock-sync-banner"
                class:realtime={realtimeUpdating}
                role="status"
                aria-live="polite"
                transition:fly={{ y: -12, duration: 200 }}
            >
                <span class="stock-sync-spinner" aria-hidden="true"></span>
                <div class="stock-sync-copy">
                    <strong>
                        {#if realtimeUpdating}
                            Hemos recibido actualizaciones de productos
                        {:else}
                            Sincronizando catálogo
                        {/if}
                    </strong>
                    <small>{syncMessage || "Actualizando disponibilidad…"}</small>
                </div>
            </div>
        {/if}

        <div class="exchange-section">
            <CurrencySwitch />
        </div>

        {#if categories.length > 0}
            <div class="category-section">
                <CategoryFilter
                    {categories}
                    {selectedCategoryId}
                    onCategorySelected={onCategorySelected}
                />
            </div>
        {/if}

        <div class="products-region">
            {#if loading && filteredProducts.length === 0}
                <div class="loading-container">
                    <LoadingIndicator size={80} aria-label="Cargando" />
                    <p>Cargando productos...</p>
                </div>
            {:else if filteredProducts.length === 0}
                <div class="empty-state">
                    <div class="empty-icon">🔍</div>
                    <h3>Sin resultados</h3>
                    <p>
                        {searchQuery || selectedCategoryId
                            ? "Prueba con otro termino o categoria"
                            : "No hay productos disponibles"}
                    </p>
                </div>
            {:else}
                {#if filteredProducts.length > 0}
                    <div class="featured-strip">
                        <span>🔥 Más vendidos</span>
                        {#if stockSyncing}
                            <span class="sync-hint">{realtimeUpdating ? "En vivo · actualizando…" : "Actualizando stock…"}</span>
                        {/if}
                    </div>
                {/if}
                <div class="products-grid">
                    {#each filteredProducts as product, index (product.id)}
                        <div transition:fly={{ y: 16, duration: 250, delay: index * 25 }}>
                            <ProductCard
                                {product}
                                stockPending={stockSyncing}
                                onClick={() => onProductClick(product.id)}
                                onFavoriteClick={() => onFavoriteClick(product.id)}
                            />
                        </div>
                    {/each}
                </div>
            {/if}
        </div>
    </div>
</div>

<style>
    .product-screen {
        width: 100%;
        max-width: 100%;
        box-sizing: border-box;
        height: 100%;
        min-height: 0;
        background: linear-gradient(
            180deg,
            var(--md-sys-color-surface-container-low) 0%,
            var(--md-sys-color-background) 25%
        );
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .screen-content {
        flex: 1;
        max-width: 100%;
        box-sizing: border-box;
        min-height: 0;
        overflow: hidden;
        display: grid;
        grid-template-rows: auto auto auto auto auto minmax(0, 1fr);
        gap: 16px;
        padding: 0;
    }

    .exchange-section {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        padding: 0 16px;
    }

    .top-row {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        position: sticky;
        top: 0;
        z-index: 20;
        padding-top: 16px;
        backdrop-filter: blur(20px);
        background: color-mix(in srgb, var(--md-sys-color-background) 85%, transparent);
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
    }

    .search-section {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
    }

    .promo-banner {
        margin: 0 16px;
        flex-shrink: 0;
        height: 64px;
        max-height: 64px;
        box-sizing: border-box;
        display: flex;
        align-items: stretch;
        border-radius: 16px;
        overflow: hidden;
        position: relative;
        isolation: isolate;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 28%, var(--md-sys-color-outline-variant));
        background: linear-gradient(
            105deg,
            color-mix(in srgb, var(--md-sys-color-primary-container) 88%, transparent) 0%,
            color-mix(in srgb, var(--md-sys-color-tertiary-container) 55%, transparent) 100%
        );
        box-shadow:
            0 8px 22px color-mix(in srgb, black 10%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
        animation:
            promo-banner-in 420ms cubic-bezier(0.22, 1, 0.36, 1) both,
            promo-banner-glow 3.2s ease-in-out 0.45s infinite;
    }

    .promo-banner::after {
        content: "";
        position: absolute;
        inset: 0;
        pointer-events: none;
        background: linear-gradient(
            110deg,
            transparent 0%,
            transparent 38%,
            color-mix(in srgb, white 28%, transparent) 50%,
            transparent 62%,
            transparent 100%
        );
        background-size: 220% 100%;
        animation: promo-shimmer 4.5s ease-in-out 0.6s infinite;
        opacity: 0.55;
        z-index: 0;
    }

    .promo-banner-main,
    .promo-banner-close {
        position: relative;
        z-index: 1;
    }

    .promo-banner-main {
        flex: 1;
        min-width: 0;
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px 12px;
        border: none;
        background: transparent;
        color: inherit;
        font: inherit;
        text-align: left;
        cursor: pointer;
        transition: transform 180ms ease, filter 180ms ease;
    }

    .promo-banner-main:hover {
        filter: brightness(1.04);
    }

    .promo-banner-main:active {
        transform: scale(0.992);
    }

    .promo-banner-img {
        width: 44px;
        height: 44px;
        border-radius: 12px;
        object-fit: cover;
        flex-shrink: 0;
        background: color-mix(in srgb, var(--md-sys-color-surface) 40%, transparent);
        box-shadow: 0 4px 12px color-mix(in srgb, black 12%, transparent);
        animation: promo-img-pop 520ms cubic-bezier(0.22, 1, 0.36, 1) both;
    }

    .promo-banner-copy {
        min-width: 0;
        display: grid;
        gap: 2px;
        animation: promo-copy-in 480ms cubic-bezier(0.22, 1, 0.36, 1) 80ms both;
    }

    .promo-banner-copy strong,
    .promo-banner-copy small {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .promo-banner-copy strong {
        font-size: 0.9rem;
        font-weight: 800;
        line-height: 1.15;
    }

    .promo-banner-copy small {
        font-size: 0.76rem;
        opacity: 0.88;
        color: var(--md-sys-color-on-surface-variant, inherit);
    }

    .promo-banner-close {
        width: 44px;
        flex-shrink: 0;
        border: none;
        border-left: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-surface) 35%, transparent);
        color: inherit;
        cursor: pointer;
        display: grid;
        place-items: center;
        transition: background 180ms ease, transform 180ms ease, color 180ms ease;
    }

    .promo-banner-close:hover {
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        transform: scale(1.06);
    }

    .promo-banner-close:active {
        transform: scale(0.94);
    }

    @keyframes promo-banner-in {
        from {
            opacity: 0;
            transform: translateY(-10px) scale(0.98);
        }
        to {
            opacity: 1;
            transform: translateY(0) scale(1);
        }
    }

    @keyframes promo-banner-glow {
        0%,
        100% {
            box-shadow:
                0 8px 22px color-mix(in srgb, black 10%, transparent),
                0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
        }
        50% {
            box-shadow:
                0 10px 26px color-mix(in srgb, black 12%, transparent),
                0 0 18px color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
        }
    }

    @keyframes promo-shimmer {
        0% {
            background-position: 120% 0;
        }
        100% {
            background-position: -120% 0;
        }
    }

    @keyframes promo-img-pop {
        from {
            opacity: 0;
            transform: scale(0.86);
        }
        to {
            opacity: 1;
            transform: scale(1);
        }
    }

    @keyframes promo-copy-in {
        from {
            opacity: 0;
            transform: translateX(8px);
        }
        to {
            opacity: 1;
            transform: translateX(0);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .promo-banner,
        .promo-banner::after,
        .promo-banner-img,
        .promo-banner-copy {
            animation: none !important;
        }
        .promo-banner-main,
        .promo-banner-close {
            transition: none !important;
        }
    }

    .stock-sync-banner {
        margin: 0 16px;
        padding: 12px 14px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        gap: 12px;
        background: color-mix(in srgb, var(--md-sys-color-secondary-container) 78%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow: 0 8px 20px color-mix(in srgb, black 8%, transparent);
    }

    .stock-sync-banner.realtime {
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 82%, transparent);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
    }

    .stock-sync-spinner {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        border: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 25%, transparent);
        border-top-color: var(--md-sys-color-primary);
        animation: stock-spin 0.75s linear infinite;
        flex-shrink: 0;
    }

    .stock-sync-copy {
        display: grid;
        gap: 2px;
        min-width: 0;
    }

    .stock-sync-copy strong {
        font-size: 0.88rem;
        line-height: 1.2;
    }

    .stock-sync-copy small {
        font-size: 0.78rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    @keyframes stock-spin {
        to {
            transform: rotate(360deg);
        }
    }

    .featured-strip {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 18px;
        font-size: 0.9rem;
        font-weight: 700;
        color: var(--md-sys-color-primary);
    }

    .sync-hint {
        font-size: 0.75rem;
        font-weight: 600;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.85;
    }

    .products-region {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        overflow-x: hidden;
        min-height: 0;
        height: 100%;
        overflow-y: auto;
        overscroll-behavior-y: contain;
        -webkit-overflow-scrolling: touch;
        padding: 0 16px calc(96px + env(safe-area-inset-bottom, 0px));
    }

    .category-section {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        position: sticky;
        top: 84px;
        z-index: 15;
        backdrop-filter: blur(20px);
        background: color-mix(in srgb, var(--md-sys-color-background) 92%, transparent);
        padding-top: 8px;
        padding-bottom: 8px;
    }

    .loading-container,
    .empty-state {
        min-height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
        padding: 32px;
        text-align: center;
    }

    .empty-icon {
        font-size: 48px;
        opacity: 0.4;
    }

    .products-grid {
        display: grid;
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
        grid-auto-rows: max-content;
        gap: 16px;
        align-content: start;
    }

    .products-grid > div {
        min-width: 0;
        max-width: 100%;
        box-sizing: border-box;
    }

    @media (max-width: 768px) {
        .screen-content {
            gap: 12px;
        }
        .exchange-section {
            padding: 0 12px;
        }
        .promo-banner,
        .stock-sync-banner {
            margin: 0 12px;
        }
        .products-region {
            padding: 0 12px calc(128px + env(safe-area-inset-bottom, 0px));
        }
        .products-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
        }
    }

    @media (max-width: 480px) {
        .screen-content {
            gap: 6px;
        }
        .promo-banner,
        .stock-sync-banner {
            margin: 0 8px;
        }
        .products-region {
            padding: 0 8px calc(128px + env(safe-area-inset-bottom, 0px));
        }
        .products-grid {
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 12px;
        }
    }
</style>
