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
    import { logProductFlow, logNavError } from "../../../../infrastructure/presentation/navigation/debug-logger";
    import { availableStock } from "../../domain/entity/Product";

    export let navBackStackEntry: NavBackStackEntry<{ productId?: string }> | undefined = undefined;
    export let navController: NavController | undefined = undefined;
    export let onRequestLogin: (() => void) | undefined = undefined;

    let searchQuery = "";
    let selectedCategoryId: string | null = null;
    let selectedProduct: any = null;
    let pendingProductId: string | null = null;
    let resolvingPendingProductId: string | null = null;
    let isLoading = false;
    let stockSyncing = false;
    let realtimeUpdating = false;
    let syncMessage: string | null = null;
    let showAuthOverlay = false;

    let products: any[] = [];
    let promotions: any[] = [];
    let categories: any[] = [];

    $: isLoading = $productStore.loading && products.length === 0;
    $: stockSyncing = $productStore.stockSyncing === true;
    $: realtimeUpdating = $productStore.realtimeUpdating === true;
    $: syncMessage = $productStore.syncMessage ?? null;

    function resolvePendingProduct() {
        if (!pendingProductId) return;
        if (resolvingPendingProductId === pendingProductId) return;
        const found = products.find((p) => p.id === pendingProductId);
        if (found) {
            selectedProduct = found;
            pendingProductId = null;
            resolvingPendingProductId = null;
        }
    }

    const unsubscribeProducts = productStore.subscribe((state) => {
        products = state.items;
        resolvePendingProduct();
        if (state.items.length > 0) {
            cartStore.refreshProductStock(state.items);
        }
        if (selectedProduct) {
            const updated = products.find(p => p.id === selectedProduct.id);
            if (updated) {
                selectedProduct = updated;
            } else if (!products.find(p => p.id === selectedProduct.id)) {
                selectedProduct = null;
            }
        }
    });

    let knownPromoIds: Set<string> | null = null;

    const unsubscribePromotions = promotionStore.subscribe((state) => {
        const nextIds = state.items.map((p) => p.id);
        if (knownPromoIds === null) {
            knownPromoIds = new Set(nextIds);
        } else {
            for (const p of state.items) {
                if (!knownPromoIds.has(p.id)) {
                    toastStore.promo(
                        p.title
                            ? `Nueva promo: ${p.title}`
                            : "Hay una nueva promoción disponible"
                    );
                    try {
                        sessionStorage.removeItem("alejo-web-dismissed-promos");
                    } catch {
                        /* ignore */
                    }
                }
            }
            knownPromoIds = new Set(nextIds);
        }
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
            productStore.startStockRealtime();
            productStore.syncAll();
            // Promos públicas / staff: silent si el visitante no tiene permiso
            promotionStore.syncAll({ suppressPermissionError: true });
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
        const promo = promotions.find((p) => p.id === promotionId);
        if (promo?.productId) {
            const product = products.find((p) => p.id === promo.productId);
            if (product) {
                selectedProduct = product;
                return;
            }
        }
        toastStore.info(promo?.message || "Promoción activa");
    };

    const handleFavoriteClick = (productId: string) => {
        console.log("Favorite clicked:", productId);
    };

    const handleAddToCartClick = () => {
        if (!selectedProduct) return;
        if ($sessionStore.isGuest) {
            showAuthOverlay = true;
            return;
        }

        const max = availableStock(selectedProduct);
        if (max <= 0) {
            toastStore.warning("Producto agotado");
            return;
        }

        const result = cartStore.addProduct(selectedProduct, 1);
        if (!result.ok) {
            toastStore.warning(result.reason === "out_of_stock" ? "Sin stock disponible" : "No se pudo añadir");
            return;
        }
        if (result.clamped) {
            toastStore.info(`Cantidad ajustada al máximo disponible (${result.max})`);
        } else {
            toastStore.success("Añadido al carrito");
        }
    };

    const handleCloseDetail = () => {
        selectedProduct = null;
        if (navController && navBackStackEntry?.args?.productId) {
            try {
                navController.popBackStack();
            } catch (e) {
                logNavError("popBackStack product detail", e);
            }
        }
    };

    function handleAuthSuccess() {
        showAuthOverlay = false;
        if (selectedProduct) {
            handleAddToCartClick();
        }
    }
</script>

<div class="internal-product-host">
    {#if selectedProduct}
        <div class="detail-layer" transition:fly={{ x: 24, duration: 220 }}>
            <ProductDetailScreen
                product={selectedProduct}
                onClose={handleCloseDetail}
                onAddToCart={handleAddToCartClick}
                onFavorite={() => handleFavoriteClick(selectedProduct.id)}
            />
        </div>
    {:else}
        <div class="list-layer" transition:fade={{ duration: 160 }}>
            <ProductScreen
                products={products}
                promotions={promotions}
                categories={categories}
                {searchQuery}
                {selectedCategoryId}
                loading={isLoading}
                {stockSyncing}
                {realtimeUpdating}
                {syncMessage}
                onSearchQueryChanged={handleSearchQueryChanged}
                onCategorySelected={handleCategorySelected}
                onProductClick={handleProductClick}
                onPromotionClick={handlePromotionClick}
                onFavoriteClick={handleFavoriteClick}
            />
        </div>
    {/if}

    {#if showAuthOverlay}
        <GuestAuthOverlay
            onSuccess={handleAuthSuccess}
            onDismiss={() => (showAuthOverlay = false)}
            onRequestLogin={onRequestLogin}
        />
    {/if}
</div>

<style>
    .internal-product-host {
        position: relative;
        width: 100%;
        height: 100%;
        min-height: 0;
        overflow: hidden;
    }

    .list-layer,
    .detail-layer {
        position: absolute;
        inset: 0;
        min-height: 0;
    }
</style>
