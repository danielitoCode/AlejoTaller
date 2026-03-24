<script lang="ts">
    import { onMount } from "svelte";
    import { fade, fly } from "svelte/transition";
    import { productStore } from "../viewmodel/product.store";
    import { promotionStore } from "../../../notification/presentation/viewmodel/promotion.store";
    import { categoryStore } from "../../../category/presentation/viewmodel/category.store";
    import { LoadingIndicator } from "m3-svelte";
    import { cartStore } from "../../../sale/presentation/viewmodel/cart.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import ProductScreen from "./ProductScreen.svelte";
    import ProductDetailScreen from "./ProductDetailScreen.svelte";

    let searchQuery = "";
    let selectedCategoryId: string | null = null;
    let selectedProduct: any = null;
    let isLoading = false;
    $: hasSelection = !!selectedProduct;

    // Subscribe to stores
    let products: any[] = [];
    let promotions: any[] = [];
    let categories: any[] = [];

    const unsubscribeProducts = productStore.subscribe((state) => {
        products = state.items;
        isLoading = state.loading;
        // Reset selected product if not in list
        if (selectedProduct && !products.find(p => p.id === selectedProduct.id)) {
            selectedProduct = null;
        }
    });

    const unsubscribePromotions = promotionStore.subscribe((state) => {
        promotions = state.items;
    });

    const unsubscribeCategories = categoryStore.subscribe((state) => {
        categories = state.items;
    });

    onMount(() => {
        // Load initial data
        try {
            productStore.syncAll();
            promotionStore.syncAll();
            categoryStore.syncAll();
        } catch (error) {
            console.error("Error loading data:", error);
        }

        return () => {
            unsubscribeProducts();
            unsubscribePromotions();
            unsubscribeCategories();
        };
    });

    const handleSearchQueryChanged = (query: string) => {
        searchQuery = query;
    };

    const handleCategorySelected = (categoryId: string | null) => {
        selectedCategoryId = categoryId;
    };

    const handleProductClick = (productId: string) => {
        const product = products.find(p => p.id === productId);
        if (product) {
            selectedProduct = product;
        }
    };

    const handlePromotionClick = (promotionId: string) => {
        // Handle promotion click
        console.log("Promotion clicked:", promotionId);
    };

    const handleFavoriteClick = (productId: string) => {
        // Handle favorite click
        console.log("Favorite clicked:", productId);
    };

    const handleAddToCartClick = () => {
        if (!selectedProduct) return;
        cartStore.addProduct(selectedProduct, 1);
        toastStore.success(`${selectedProduct.name} agregado al carrito`);
    };

</script>

<div class="internal-product-screen" class:has-selection={hasSelection}>
    <div class="product-list-panel">
        <ProductScreen
            {products}
            {promotions}
            {categories}
            {searchQuery}
            {selectedCategoryId}
            loading={false}
            onSearchQueryChanged={handleSearchQueryChanged}
            onCategorySelected={handleCategorySelected}
            onProductClick={handleProductClick}
            onPromotionClick={handlePromotionClick}
            onFavoriteClick={handleFavoriteClick}
        />
    </div>

    {#if selectedProduct}
        <div
            class="product-detail-panel"
            in:fly={{ x: 28, duration: 220, opacity: 0.2 }}
            out:fade={{ duration: 150 }}
        >
            <ProductDetailScreen
                product={selectedProduct}
                showTopBar={true}
                onBackClick={() => (selectedProduct = null)}
                onFavoriteClick={() => handleFavoriteClick(selectedProduct.id)}
                onAddToCartClick={handleAddToCartClick}
            />
        </div>
    {/if}
</div>

<style>
    .internal-product-screen {
        width: 100%;
        height: 100%;
        min-height: 0;
        display: grid;
        grid-template-columns: minmax(0, 1fr);
        gap: 0;
        background: var(--md-sys-color-background);
    }

    .internal-product-screen.has-selection {
        grid-template-columns: minmax(0, 1.1fr) minmax(340px, 0.9fr);
    }

    .product-list-panel {
        height: 100%;
        min-height: 0;
        overflow: hidden;
    }

    .internal-product-screen.has-selection .product-list-panel {
        border-right: 1px solid var(--md-sys-color-outline-variant);
    }

    .product-detail-panel {
        height: 100%;
        min-height: 0;
        overflow: hidden;
        padding-left: 14px;
    }

    /* Responsive: Stack on smaller screens */
    @media (max-width: 1100px) {
        .internal-product-screen {
            grid-template-columns: 1fr;
        }

        .product-list-panel {
            border-right: none;
        }

        .product-detail-panel {
            display: none;
        }
    }
</style>
