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
    import type { NavBackStackEntry } from "../../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
    import { logNavError } from "../../../../infrastructure/presentation/navigation/debug-logger";
    import { availableStock } from "../../domain/entity/Product";
    import { likedStore } from "../viewmodel/liked.store";

    export let navBackStackEntry: NavBackStackEntry<{ productId?: string }> | undefined = undefined;
    export let navController: NavController | undefined = undefined;
    export let onRequestLogin: (() => void) | undefined = undefined;

    let searchQuery = "";
    let selectedCategoryId: string | null = null;
    let selectedProduct: any = null;
    let pendingProductId: string | null = null;
    let resolvingPendingProductId: string | null = null;
    let showAuthOverlay = false;

    let products: any[] = [];
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
        if (state.items.length > 0) cartStore.refreshProductStock(state.items);
        if (selectedProduct) {
            const updated = products.find((p) => p.id === selectedProduct.id);
            if (updated) selectedProduct = updated;
            else if (!products.find((p) => p.id === selectedProduct.id)) selectedProduct = null;
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
                    toastStore.promo(p.title ? `Nueva promo: ${p.title}` : "Hay una nueva promoción disponible");
                }
            }
            knownPromoIds = new Set(nextIds);
        }
    });

    const unsubscribeCategories = categoryStore.subscribe((state) => {
        categories = state.items;
    });

    pendingProductId = navBackStackEntry?.args?.productId ?? null;

    onMount(() => {
        try {
            productStore.startStockRealtime();
            productStore.syncAll();
            promotionStore.syncAll({ suppressPermissionError: true });
            categoryStore.syncAll();
            likedStore.hydrate();
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
        const product = products.find((p) => p.id === productId);
        if (product) selectedProduct = product;
    };
    const handleFavoriteClick = (productId: string) => {
        likedStore.toggle(productId);
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
        if (result.clamped) toastStore.info(`Cantidad ajustada al máximo disponible (${result.max})`);
        else toastStore.success("Añadido al carrito");
    };

    const closeProductDetail = () => {
        selectedProduct = null;
        if (navController && navBackStackEntry?.args?.productId) {
            try {
                navController.popBackStack();
            } catch (e) {
                logNavError("popBackStack product detail", e);
            }
        }
    };

    function handleAuthRequired() {
        showAuthOverlay = true;
    }

    function handleAuthSuccess() {
        showAuthOverlay = false;
        if (selectedProduct) handleAddToCartClick();
    }
</script>

<div class="internal-product-host" class:has-detail={!!selectedProduct}>
    <div class="list-layer" class:list-hidden-mobile={!!selectedProduct} transition:fade={{ duration: 160 }}>
        <ProductScreen
            {products}
            {categories}
            {searchQuery}
            {selectedCategoryId}
            loading={isLoading}
            {stockSyncing}
            {realtimeUpdating}
            {syncMessage}
            onSearchQueryChanged={handleSearchQueryChanged}
            onCategorySelected={handleCategorySelected}
            onProductClick={handleProductClick}
            onFavoriteClick={handleFavoriteClick}
        />
    </div>

    {#if selectedProduct}
        <div class="detail-layer" transition:fly={{ x: 24, duration: 220 }}>
            <button
                type="button"
                class="detail-scrim"
                aria-label="Cerrar detalle"
                on:click={closeProductDetail}
            ></button>
            <div class="detail-panel" role="dialog" aria-modal="true" aria-label="Detalle del producto">
                <ProductDetailScreen
                    product={selectedProduct}
                    showTopBar={true}
                    onBackClick={closeProductDetail}
                    onFavoriteClick={() => handleFavoriteClick(selectedProduct.id)}
                    canAddToCart={!$sessionStore.isGuest}
                    isGuest={$sessionStore.isGuest}
                    onAddToCartClick={handleAddToCartClick}
                    onAuthRequiredClick={handleAuthRequired}
                />
            </div>
        </div>
    {/if}

    <GuestAuthOverlay
        open={showAuthOverlay}
        on:login={() => {
            showAuthOverlay = false;
            onRequestLogin?.();
        }}
        on:close={() => (showAuthOverlay = false)}
    />
</div>

<style>
    .internal-product-host {
        width: 100%;
        height: 100%;
        min-height: 0;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        box-sizing: border-box;
        position: relative;
    }

    .list-layer {
        flex: 1 1 auto;
        min-height: 0;
        width: 100%;
        height: 100%;
        overflow: hidden;
        position: relative;
        display: flex;
        flex-direction: column;
        box-sizing: border-box;
    }

    .list-layer :global(.product-screen) {
        flex: 1 1 auto;
        min-height: 0;
        height: 100%;
    }

    .detail-layer {
        position: absolute;
        inset: 0;
        z-index: 40;
        display: flex;
        align-items: stretch;
        justify-content: stretch;
        box-sizing: border-box;
    }

    .detail-scrim {
        display: none;
        border: none;
        padding: 0;
        cursor: pointer;
    }

    .detail-panel {
        flex: 1 1 auto;
        min-height: 0;
        width: 100%;
        height: 100%;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        background: var(--md-sys-color-surface);
        box-sizing: border-box;
    }

    .detail-panel :global(.product-detail-screen) {
        flex: 1 1 auto;
        min-height: 0;
        height: 100%;
    }

    @media (max-width: 840px) {
        .list-hidden-mobile {
            visibility: hidden;
            pointer-events: none;
        }

        .detail-layer {
            background: var(--md-sys-color-surface);
        }
    }

    @media (min-width: 841px) {
        .detail-layer {
            align-items: center;
            justify-content: center;
            padding: max(16px, env(safe-area-inset-top)) 24px max(16px, env(safe-area-inset-bottom));
            background: transparent;
        }

        .detail-scrim {
            display: block;
            position: absolute;
            inset: 0;
            background: color-mix(in srgb, black 88%, transparent);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            z-index: 0;
        }

        .detail-panel {
            position: relative;
            z-index: 1;
            flex: 0 1 auto;
            width: min(960px, 100%);
            height: min(820px, calc(100dvh - 48px));
            max-height: calc(100dvh - 48px);
            border-radius: 28px;
            overflow: hidden;
            isolation: isolate;
            background: var(--md-sys-color-surface);
            background-image: linear-gradient(
                var(--md-sys-color-surface),
                var(--md-sys-color-surface)
            );
            box-shadow:
                0 28px 72px color-mix(in srgb, black 50%, transparent),
                0 0 0 1px color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);
        }

        .detail-panel :global(.product-detail-screen),
        .detail-panel :global(.detail-copy-card),
        .detail-panel :global(.bottom-bar),
        .detail-panel :global(.product-info-section),
        .detail-panel :global(.description-section) {
            background: var(--md-sys-color-surface) !important;
            background-image: linear-gradient(
                var(--md-sys-color-surface),
                var(--md-sys-color-surface)
            ) !important;
        }

        .detail-panel :global(.bottom-bar) {
            backdrop-filter: none !important;
            -webkit-backdrop-filter: none !important;
            background: var(--md-sys-color-surface-container-low, var(--md-sys-color-surface)) !important;
            background-image: linear-gradient(
                var(--md-sys-color-surface-container-low, var(--md-sys-color-surface)),
                var(--md-sys-color-surface-container-low, var(--md-sys-color-surface))
            ) !important;
        }
    }

    @media (min-width: 841px) and (max-width: 1100px) {
        .detail-panel {
            width: min(720px, 100%);
            height: min(760px, calc(100dvh - 40px));
        }
    }
</style>
