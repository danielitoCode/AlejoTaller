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
    /** true mientras se refresca stock desde Appwrite — badges en estado neutro */
    export let stockSyncing: boolean = false;
    /** true si el sync viene de señal realtime stock:changed */
    export let realtimeUpdating: boolean = false;
    /** texto del banner de sincronización */
    export let syncMessage: string | null = null;
    export let onSearchQueryChanged: (query: string) => void = () => {};
    export let onCategorySelected: (categoryId: string | null) => void = () => {};
    export let onProductClick: (productId: string) => void = () => {};
    export let onPromotionClick: (promotionId: string) => void = () => {};
    export let onFavoriteClick: (productId: string) => void = () => {};
    $: exchangeState = $exchangeStore;

    let isPromoVisible = false;

    $: filteredProducts = products
        .filter((product) => {
            const matchesSearch =
                product.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                product.description?.toLowerCase().includes(searchQuery.toLowerCase());

            const matchesCategory =
                !selectedCategoryId || product.categoryId === selectedCategoryId;

            return matchesSearch && matchesCategory;
        })
        .map((product) => {
            return {
                ...product,
                displayPrice: formatMoney(product.price, exchangeState)
            };
        });

    const testPromotion = {
        id: "promo-local-test",
        title: "Diagnostico y montaje",
        message: "Promocion visual de prueba con recursos locales del proyecto"
    };

    $: activePromotion = promotions.length > 0 ? promotions[0] : testPromotion;
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

            {#if isPromoVisible && activePromotion}
                <button
                    class="promo-inline"
                    type="button"
                    aria-label="Ver promocion"
                    in:fly={{ y: -16, duration: 220, opacity: 0.2 }}
                    on:click={() => onPromotionClick(activePromotion.id)}
                >
                    <img class="promo-image" src={promoImage} alt="" />
                    <span class="promo-copy">
                        <strong>{activePromotion.title || "Promo activa"}</strong>
                        <small>{activePromotion.message || "Oferta disponible por tiempo limitado"}</small>
                    </span>
                    <span
                        class="promo-close"
                        role="button"
                        tabindex="0"
                        aria-label="Cerrar promocion"
                        on:click={(e) => {
                            e.stopPropagation();
                            isPromoVisible = false;
                        }}
                        on:keydown={(e) => {
                            if (e.key === "Enter") {
                                e.stopPropagation();
                                isPromoVisible = false;
                            }
                        }}
                    >
                        <Icon icon={closeIcon} />
                    </span>
                </button>
            {/if}
        </div>

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
                        <div
                                transition:fly={{
                                    y: 16,
                                    duration: 250,
                                    delay: index * 25
                                }}
                        >
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
        background:
                linear-gradient(
                        180deg,
                        var(--md-sys-color-surface-container-low)
                        0%,
                        var(--md-sys-color-background)
                        25%
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
        grid-template-rows: auto auto auto auto minmax(0, 1fr);
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
        background:
                color-mix(
                        in srgb,
                        var(--md-sys-color-background)
                        85%,
                        transparent
                );
        border-bottom:
                1px solid
                var(--md-sys-color-outline-variant);
    }

    .search-section {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
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
        to { transform: rotate(360deg); }
    }

    .featured-strip {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 18px;
        font-size: .9rem;
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
        scroll-behavior: smooth;
        min-height: 0;
        height: 100%;
        overflow-y: auto;
        overscroll-behavior-y: contain;
        -webkit-overflow-scrolling: touch;
        padding: 0 16px calc(96px + env(safe-area-inset-bottom, 0px));
    }

    .promo-inline {
        min-height: 110px;
        border-radius: 28px;
        overflow: hidden;
        position: relative;
        box-shadow: 0 12px 32px rgba(0,0,0,.18);
        transition: transform .25s ease, box-shadow .25s ease;
    }

    .promo-inline:hover {
        transform: translateY(-2px);
        box-shadow: 0 20px 42px rgba(0,0,0,.22);
    }

    .promo-image,
    .promo-close {
        width: 34px;
        height: 34px;
        border-radius: 999px;
        object-fit: cover;
        display: block;
        background: color-mix(in srgb, var(--md-sys-color-on-tertiary-container) 12%, transparent);
    }

    .promo-close {
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-on-tertiary-container) 8%, transparent);
    }

    .promo-copy {
        min-width: 0;
        display: grid;
        gap: 2px;
        text-align: left;
    }

    .promo-copy strong,
    .promo-copy small {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .promo-copy strong {
        font-size: 0.9rem;
        line-height: 1.1;
    }

    .promo-copy small {
        font-size: 0.76rem;
        opacity: 0.84;
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
        background:
                color-mix(
                        in srgb,
                        var(--md-sys-color-background)
                        92%,
                        transparent
                );
        padding-top: 8px;
        padding-bottom: 8px;
    }

    .loading-container {
        min-height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
        padding: 32px;
    }

    .loading-container p {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.95rem;
    }

    .empty-state {
        min-height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
        padding: 40px 32px;
        text-align: center;
    }

    .empty-icon {
        font-size: 48px;
        opacity: 0.4;
    }

    .empty-state h3 {
        font-size: 1.1rem;
        font-weight: 500;
        color: var(--md-sys-color-on-surface-variant);
        margin: 0;
    }

    .empty-state p {
        font-size: 0.9rem;
        color: var(--md-sys-color-on-surface-variant);
        margin: 0;
        max-width: 300px;
        opacity: 0.6;
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

    @media (min-width: 1200px) {
        .products-grid {
            grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
        }
    }

    @media (max-width: 768px) {
        .screen-content {
            gap: 12px;
        }

        .exchange-section {
            padding: 0 12px;
        }

        .stock-sync-banner {
            margin: 0 12px;
        }

        .top-row {
            padding: 12px 12px 0;
            grid-template-columns: 1fr;
        }

        .products-region {
            padding: 0 12px calc(128px + env(safe-area-inset-bottom, 0px));
        }

        .products-grid {
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 12px;
        }

        .category-section {
            padding-left: 12px;
            padding-right: 12px;
        }
    }

    @media (max-width: 480px) {
        .screen-content {
            gap: 6px;
        }

        .exchange-section {
            padding: 0 8px;
            display: flex;
            justify-content: flex-start;
        }

        .stock-sync-banner {
            margin: 0 8px;
            padding: 10px 12px;
        }

        .top-row {
            padding: 6px 8px 0;
            border-bottom-color: transparent;
        }

        .products-region {
            padding: 0 8px calc(128px + env(safe-area-inset-bottom, 0px));
        }

        .products-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 12px;
        }

        .category-section {
            top: 56px;
            padding-top: 6px;
            padding-left: 8px;
            padding-right: 8px;
        }
    }
</style>
