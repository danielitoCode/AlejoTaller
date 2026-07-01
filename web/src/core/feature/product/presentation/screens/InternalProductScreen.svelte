<script lang="ts">
    import { onMount } from "svelte";
    import { fade, fly } from "svelte/transition";
    import { productStore } from "../viewmodel/product.store";
    import { promotionStore } from "../../../notification/presentation/viewmodel/promotion.store";
    import { categoryStore } from "../../../category/presentation/viewmodel/category.store";
    import { cartStore } from "../../../sale/presentation/viewmodel/cart.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import ProductScreen from "./ProductScreen.svelte";
    import ProductDetailScreen from "./ProductDetailScreen.svelte";
    import GuestAuthOverlay from "../../../auth/presentation/components/GuestAuthOverlay.svelte";
    import type {NavBackStackEntry} from "../../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../../lib/navigation/NavController";
    import {productDetail} from "../../../../infrastructure/presentation/navigation/nested.router";
    import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
    import { rememberPendingDeepLink } from "../../../../infrastructure/presentation/navigation/pending-deeplink.store";

    export let navBackStackEntry: NavBackStackEntry<{ productId?: string }> | undefined = undefined;
    export let navController: NavController | undefined = undefined;
    /** Called when a guest user confirms they want to log in from the auth overlay. */
    export let onRequestLogin: (() => void) | undefined = undefined;

    let searchQuery = "";
    let selectedCategoryId: string | null = null;
    let selectedProduct: any = null;
    let pendingProductId: string | null = null;
    let resolvingPendingProductId: string | null = null;
    let isLoading = false;
    let showAuthOverlay = false;

    // Subscribe to stores
    let products: any[] = [];
    let promotions: any[] = [];
    let categories: any[] = [];

    async function resolvePendingProduct() {
        if (!pendingProductId || resolvingPendingProductId === pendingProductId) return;

        const product = products.find(p => p.id === pendingProductId);
        if (product) {
            selectedProduct = product;
            pendingProductId = null;
            return;
        }

        const productIdToResolve = pendingProductId;
        resolvingPendingProductId = productIdToResolve;
        try {
            const syncedProduct = await productStore.syncById(productIdToResolve);
            if (pendingProductId !== productIdToResolve) return;

            if (syncedProduct) {
                selectedProduct = syncedProduct;
            } else {
                toastStore.error("No se pudo abrir el producto compartido");
            }
            pendingProductId = null;
        } catch (error) {
            console.error("Error resolving pending product:", error);
            if (pendingProductId === productIdToResolve) {
                toastStore.error("No se pudo abrir el producto compartido");
                pendingProductId = null;
            }
        } finally {
            if (resolvingPendingProductId === productIdToResolve) {
                resolvingPendingProductId = null;
            }
        }
    }

    const unsubscribeProducts = productStore.subscribe((state) => {
        products = state.items;
        isLoading = state.loading;
        resolvePendingProduct();
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

    function initPendingProductId() {
        pendingProductId = navBackStackEntry?.args?.productId ?? null;
    }

    initPendingProductId();

    onMount(() => {
        try {
            productStore.syncAll();
            if (!$sessionStore.isGuest) {
                promotionStore.syncAll({ suppressPermissionError: true });
            }
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

    $: if (navBackStackEntry?.args?.productId) {
        pendingProductId = navBackStackEntry.args.productId;
        resolvePendingProduct();
    }

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
        if ($sessionStore.isGuest) {
            showAuthOverlay = true;
            return;
        }
        cartStore.addProduct(selectedProduct, 1);
        toastStore.success(`${selectedProduct.name} agregado al carrito`);
    };

    const handleAuthRequiredClick = () => {
        showAuthOverlay = true;
    };

    function handleOverlayLogin() {
        showAuthOverlay = false;
        // Notify via prop (when passed directly) and via window event
        // (when rendered through NavHost which can't forward custom props)
        onRequestLogin?.();
        if (typeof window !== "undefined") {
            window.dispatchEvent(new CustomEvent("request-guest-login"));
        }
    }

    const closeProductDetail = () => {
        selectedProduct = null;
        if (navBackStackEntry?.route === productDetail.path && navController) {
            navController.popBackStack();
        }
    };

</script>

<div class="internal-product-screen">
    <div class="product-list-panel">
        <ProductScreen
            {products}
            {promotions}
            {categories}
            {searchQuery}
            {selectedCategoryId}
            loading={isLoading}
            onSearchQueryChanged={handleSearchQueryChanged}
            onCategorySelected={handleCategorySelected}
            onProductClick={handleProductClick}
            onPromotionClick={handlePromotionClick}
            onFavoriteClick={handleFavoriteClick}
        />
    </div>

    {#if selectedProduct}
        <div class="product-detail-modal" role="presentation" out:fade={{ duration: 120 }}>
            <button
                    class="product-detail-scrim"
                    type="button"
                    aria-label="Cerrar detalle del producto"
                    on:click={closeProductDetail}
            ></button>
            <div
                    class="product-detail-dialog"
                    role="dialog"
                    aria-modal="true"
                    aria-label={`Detalle de ${selectedProduct.name}`}
                    in:fly={{ y: 32, duration: 220, opacity: 0.25 }}
                    out:fly={{ y: 32, duration: 150, opacity: 0.2 }}
            >
                <ProductDetailScreen
                        product={selectedProduct}
                        showTopBar={true}
                        onBackClick={closeProductDetail}
                        onFavoriteClick={() => handleFavoriteClick(selectedProduct.id)}
                        canAddToCart={!$sessionStore.isGuest}
                        isGuest={$sessionStore.isGuest}
                        onAddToCartClick={handleAddToCartClick}
                        onAuthRequiredClick={handleAuthRequiredClick}
                />
            </div>
        </div>
    {/if}
</div>

<GuestAuthOverlay
    open={showAuthOverlay}
    on:login={handleOverlayLogin}
    on:close={() => (showAuthOverlay = false)}
/>

<style>
    .internal-product-screen {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        height: 100%;
        min-height: 0;
        display: grid;
        grid-template-columns: minmax(0, 1fr);
        gap: 0;
        background: var(--md-sys-color-background);
    }

    .product-list-panel {
        width: 100%;
        max-width: 100%;
        min-width: 0;
        box-sizing: border-box;
        height: 100%;
        min-height: 0;
        overflow: hidden;
    }

    .product-detail-modal {
        position: fixed;
        inset: 0;
        z-index: 90;
        display: grid;
        place-items: stretch;
        padding: 18px;
    }

    .product-detail-scrim {
        position: absolute;
        inset: 0;
        border: 0;
        background:
                radial-gradient(circle at 50% 0%, color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent), transparent 42%),
                color-mix(in srgb, black 68%, transparent);
        backdrop-filter: blur(4px);
        cursor: pointer;
    }

    .product-detail-dialog {
        position: relative;
        z-index: 1;
        width: min(100%, 980px);
        height: min(100dvh, 900px);
        justify-self: center;
        align-self: center;
        display: grid;
        min-height: 0;
        background-color: #1a1c19; /* Solid dark background fallback */
        background: var(--md-sys-color-surface-container, #1a1c19);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        border-radius: 32px;
        overflow-y: auto;
        overscroll-behavior: contain;
        box-shadow: 0 28px 72px color-mix(in srgb, black 42%, transparent);
    }

    /* Responsive: Stack on smaller screens */
    @media (max-width: 768px) {
        .product-detail-modal {
            padding: 0;
        }

        .product-detail-dialog {
            width: 100%;
            height: 100%;
            border-radius: 0;
            border: 0;
            padding-bottom: env(safe-area-inset-bottom);
        }
    }
</style>
