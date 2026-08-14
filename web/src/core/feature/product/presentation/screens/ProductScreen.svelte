<script lang="ts">
    import { LoadingIndicator } from "m3-svelte";
    import { fly } from "svelte/transition";
    import SearchBar from "../components/SearchBar.svelte";
    import { exchangeStore, formatMoney } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import CategoryFilter from "../components/CategoryFilter.svelte";
    import ProductCard from "../components/ProductCard.svelte";
    import type { Product } from "../../domain/entity/Product";
    import type { Category } from "../../../category/domain/entity/Category";
    import CurrencySwitch from "../../../exchange/presentation/components/CurrencySwitch.svelte";
    import { promotionStore } from "../../../notification/presentation/viewmodel/promotion.store";
    import {
        effectivePrice,
        findActiveProductPromo,
    } from "../../../notification/domain/policy/PromotionPolicy";
    import { likedStore, isProductLiked } from "../viewmodel/liked.store";

    export let products: Product[] = [];
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
    export let onFavoriteClick: (productId: string) => void = () => {};

    $: exchangeState = $exchangeStore;
    $: promos = $promotionStore.items;
    $: nowMs = Date.now();

    $: filteredProducts = products
        .filter((product) => {
            const name = String(product?.name ?? "");
            const desc = String(product?.description ?? "");
            const q = searchQuery.toLowerCase();
            const matchesSearch =
                name.toLowerCase().includes(q) || desc.toLowerCase().includes(q);
            const matchesCategory =
                !selectedCategoryId || product.categoryId === selectedCategoryId;
            return matchesSearch && matchesCategory;
        })
        .map((product) => {
            const promo = findActiveProductPromo(product.id, promos, nowMs);
            const salePrice = effectivePrice(product.price, product.id, promos, nowMs);
            return {
                ...product,
                listPrice: product.price,
                salePrice,
                activePromo: promo,
                hasPromo: promo != null && salePrice < Number(product.price),
                displayPrice: formatMoney(salePrice, exchangeState),
                displayListPrice:
                    promo != null && salePrice < Number(product.price)
                        ? formatMoney(product.price, exchangeState)
                        : null,
            };
        });
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
                <div class="featured-strip">
                    <span>🔥 Más vendidos</span>
                    {#if stockSyncing}
                        <span class="sync-hint">{realtimeUpdating ? "En vivo · actualizando…" : "Actualizando stock…"}</span>
                    {/if}
                </div>
                <div class="products-grid">
                    {#each filteredProducts as product, index (product.id)}
                        <div transition:fly={{ y: 16, duration: 250, delay: Math.min(index, 12) * 25 }}>
                            <ProductCard
                                {product}
                                stockPending={stockSyncing}
                                salePrice={product.salePrice}
                                listPrice={product.hasPromo ? product.listPrice : null}
                                promoBadge={product.hasPromo}
                                isLiked={isProductLiked(product.id, $likedStore)}
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
        height: 100%;
        min-height: 100%;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        background: linear-gradient(
            180deg,
            var(--md-sys-color-surface-container-low) 0%,
            var(--md-sys-color-background) 25%
        );
    }

    .screen-content {
        flex: 1;
        min-height: 0;
        display: flex;
        flex-direction: column;
        gap: 12px;
        overflow: hidden;
        box-sizing: border-box;
    }

    .top-row,
    .exchange-section,
    .category-section,
    .stock-sync-banner {
        flex-shrink: 0;
    }

    .top-row {
        padding: 12px 16px 0;
        z-index: 20;
    }

    .search-section {
        width: 100%;
        min-width: 0;
    }

    .exchange-section {
        padding: 0 16px;
    }

    .category-section {
        padding: 0 8px;
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
    }

    .stock-sync-banner.realtime {
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 82%, transparent);
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

    .products-region {
        flex: 1 1 auto;
        min-height: 0;
        overflow-x: hidden;
        overflow-y: auto;
        -webkit-overflow-scrolling: touch;
        padding: 0 16px calc(96px + env(safe-area-inset-bottom, 0px));
    }

    .featured-strip {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 14px;
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

    .loading-container,
    .empty-state {
        min-height: 40vh;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 12px;
        padding: 32px;
        text-align: center;
        color: var(--md-sys-color-on-surface-variant);
    }

    .empty-icon {
        font-size: 48px;
        opacity: 0.4;
    }

    .products-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
        gap: 16px;
        align-content: start;
    }

    .products-grid > div {
        min-width: 0;
    }

    @media (max-width: 768px) {
        .top-row {
            padding: 10px 12px 0;
        }
        .exchange-section {
            padding: 0 12px;
        }
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
        .top-row {
            padding: 6px 8px 0;
        }
        .exchange-section {
            padding: 0 8px;
        }
        .products-region {
            padding: 0 8px calc(128px + env(safe-area-inset-bottom, 0px));
        }
        .products-grid {
            grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
            gap: 10px;
        }
    }
</style>
