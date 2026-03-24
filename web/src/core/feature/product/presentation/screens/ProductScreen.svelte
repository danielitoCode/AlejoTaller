<script lang="ts">
    import { LoadingIndicator, Icon } from "m3-svelte";
    import { fly } from "svelte/transition";
    import SearchBar from "../components/SearchBar.svelte";
    import CategoryFilter from "../components/CategoryFilter.svelte";
    import ProductCard from "../components/ProductCard.svelte";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";
    import promoImage from "../../../../../assets/hero.png";
    import type { Product } from "../../domain/entity/Product";
    import type { Promotion } from "../../../notification/domain/entity/Promotion";
    import type { Category } from "../../../category/domain/entity/Category";

    export let products: Product[] = [];
    export let promotions: Promotion[] = [];
    export let categories: Category[] = [];
    export let searchQuery: string = "";
    export let selectedCategoryId: string | null = null;
    export let loading: boolean = false;
    export let onSearchQueryChanged: (query: string) => void = () => {};
    export let onCategorySelected: (categoryId: string | null) => void = () => {};
    export let onProductClick: (productId: string) => void = () => {};
    export let onPromotionClick: (promotionId: string) => void = () => {};
    export let onFavoriteClick: (productId: string) => void = () => {};

    let isPromoVisible = true;

    $: filteredProducts = products.filter((product) => {
        const matchesSearch =
            product.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
            product.description?.toLowerCase().includes(searchQuery.toLowerCase());

        const matchesCategory =
            !selectedCategoryId || product.categoryId === selectedCategoryId;

        return matchesSearch && matchesCategory;
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
            {#if loading}
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
                <div class="products-grid">
                    {#each filteredProducts as product (product.id)}
                        <ProductCard
                            {product}
                            onClick={() => onProductClick(product.id)}
                            onFavoriteClick={() => onFavoriteClick(product.id)}
                        />
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
        min-height: 0;
        background: var(--md-sys-color-background);
        display: flex;
        flex-direction: column;
        overflow: hidden;
    }

    .screen-content {
        flex: 1;
        min-height: 0;
        overflow: hidden;
        display: grid;
        grid-template-rows: auto auto minmax(0, 1fr);
        gap: 16px;
        padding: 0;
    }

    .top-row {
        padding: 16px 16px 0;
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(220px, 280px);
        gap: 12px;
        align-items: start;
    }

    .search-section {
        min-width: 0;
    }

    .promo-inline {
        min-width: 0;
        border: 0;
        border-radius: 24px;
        padding: 10px 12px;
        display: grid;
        grid-template-columns: auto minmax(0, 1fr) auto;
        gap: 10px;
        align-items: center;
        background: linear-gradient(
            135deg,
            var(--md-sys-color-tertiary-container) 0%,
            color-mix(in srgb, var(--md-sys-color-primary-container) 78%, var(--md-sys-color-tertiary-container)) 100%
        );
        color: var(--md-sys-color-on-tertiary-container);
        cursor: pointer;
        box-shadow: 0 10px 24px color-mix(in srgb, black 10%, transparent);
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
        padding: 0 16px;
    }

    .products-region {
        min-height: 0;
        height: 100%;
        overflow-y: auto;
        overscroll-behavior-y: contain;
        -webkit-overflow-scrolling: touch;
        padding: 0 16px 28px;
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
        grid-template-columns: repeat(auto-fill, minmax(168px, 1fr));
        grid-auto-rows: max-content;
        gap: 16px;
        align-content: start;
    }

    @media (max-width: 768px) {
        .screen-content {
            gap: 12px;
        }

        .top-row {
            padding: 12px 12px 0;
            grid-template-columns: 1fr;
        }

        .products-region {
            padding: 0 12px 24px;
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
            gap: 8px;
        }

        .top-row {
            padding: 8px 8px 0;
        }

        .products-region {
            padding: 0 8px 22px;
        }

        .products-grid {
            grid-template-columns: repeat(2, 1fr);
            gap: 8px;
        }

        .category-section {
            padding-left: 8px;
            padding-right: 8px;
        }
    }
</style>
