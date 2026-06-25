<script lang="ts">
    import { Button, Icon } from "m3-svelte";
    import ArrowBackRounded from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import FavoriteBorderRounded from "@ktibow/iconset-material-symbols/favorite-outline-rounded";
    import ShareRounded from "@ktibow/iconset-material-symbols/share-eta-rounded";
    import ShoppingCartRounded from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import type { Product } from "../../domain/entity/Product";
    import { exchangeStore, formatMoney } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import { parseProductImageUrls } from "../utils/product.images";

    const placeholderImageUrl = `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='400' height='400'%3E%3Crect fill='%23f5f5f5' width='400' height='400'/%3E%3C/svg%3E`;

    export let product: Product;
    export let showTopBar: boolean = true;
    export let onBackClick: () => void = () => {};
    export let onFavoriteClick: () => void = () => {};
    export let onShareClick: () => void = () => {};
    export let onAddToCartClick: () => void = () => {};

    $: imageUrls = parseProductImageUrls(product.photoUrl);

    let activeIndex = 0;
    let galleryEl: HTMLDivElement;

    function goToImage(index: number) {
        if (!galleryEl || imageUrls.length === 0) return;
        const total = imageUrls.length;
        const target = ((index % total) + total) % total;
        galleryEl.scrollTo({
            left: target * galleryEl.clientWidth,
            behavior: "smooth"
        });
        activeIndex = target;
    }

    function onGalleryScroll() {
        if (!galleryEl) return;
        const idx = Math.round(galleryEl.scrollLeft / galleryEl.clientWidth);
        if (idx !== activeIndex && idx >= 0 && idx < imageUrls.length) {
            activeIndex = idx;
        }
    }
</script>

<div class="product-detail-screen">
    {#if showTopBar}
        <header class="header-section">
            <button
                class="icon-button back-button"
                type="button"
                aria-label="Volver"
                title="Volver"
                on:click={onBackClick}
            >
                <Icon icon={ArrowBackRounded} />
            </button>

            <div class="header-actions">
                <button
                    class="icon-button action-button"
                    type="button"
                    aria-label="Agregar a favoritos"
                    title="Favoritos"
                    on:click={onFavoriteClick}
                >
                    <Icon icon={FavoriteBorderRounded} />
                </button>
                <button
                    class="icon-button action-button"
                    type="button"
                    aria-label="Compartir"
                    title="Compartir"
                    on:click={onShareClick}
                >
                    <Icon icon={ShareRounded} />
                </button>
            </div>
        </header>
    {/if}

    <div class="product-image-section">
        {#if imageUrls.length > 0}
            <div class="carousel-container">
                <div
                    class="product-image-gallery"
                    aria-label={`Imagenes de ${product.name}`}
                    bind:this={galleryEl}
                    on:scroll={onGalleryScroll}
                >
                    {#each imageUrls as imageUrl, imageIndex}
                        <img
                            src={imageUrl}
                            alt={imageUrls.length === 1 ? product.name : `${product.name} - imagen ${imageIndex + 1}`}
                            class="product-image"
                        />
                    {/each}
                </div>

                {#if imageUrls.length > 1}
                    <button
                        class="carousel-btn carousel-btn--prev"
                        type="button"
                        aria-label="Imagen anterior"
                        title="Imagen anterior"
                        on:click={() => goToImage(activeIndex - 1)}
                    >
                        <span class="carousel-btn-icon">&#9664;</span>
                    </button>
                    <button
                        class="carousel-btn carousel-btn--next"
                        type="button"
                        aria-label="Imagen siguiente"
                        title="Imagen siguiente"
                        on:click={() => goToImage(activeIndex + 1)}
                    >
                        <span class="carousel-btn-icon">&#9654;</span>
                    </button>
                {/if}
            </div>

            {#if imageUrls.length > 1}
                <div class="carousel-dots" role="tablist" aria-label="Selector de imagen">
                    {#each imageUrls as _, dotIndex}
                        <button
                            class="carousel-dot"
                            class:active={dotIndex === activeIndex}
                            role="tab"
                            aria-selected={dotIndex === activeIndex}
                            aria-label={`Imagen ${dotIndex + 1}`}
                            on:click={() => goToImage(dotIndex)}
                        />
                    {/each}
                </div>
            {/if}
        {:else}
            <img
                src={placeholderImageUrl}
                alt={product.name}
                class="product-image"
            />
        {/if}
    </div>

    <div class="detail-copy-card">
        <section class="product-info-section">
            <h1 class="product-name">{product.name}</h1>

            {#if product.price}
                <div class="price-section">
                    <span class="price-value">{formatMoney(product.price, $exchangeStore)}</span>
                </div>
            {/if}
        </section>

        <section class="description-section">
            <h2 class="description-title">Descripcion</h2>
            <div class="description-scroll">
                <p class="product-description">
                    {product.description}
                </p>
            </div>
        </section>
    </div>

    <footer class="bottom-bar">
        <div class="cart-action">
            <Button variant="filled" size="m" onclick={onAddToCartClick}>
                <Icon icon={ShoppingCartRounded} />
                <span>Agregar al carrito</span>
            </Button>
        </div>
    </footer>
</div>

<style>
    .product-detail-screen {
        width: 100%;
        height: 100%;
        min-height: 0;

        display: grid;
        grid-template-rows:
            minmax(260px, 38vh)
            minmax(0, 1fr)
            auto;

        background:
                var(--md-sys-color-surface);

        overflow: hidden;
    }

    .header-section {
        position: absolute;

        top: 16px;
        left: 16px;
        right: 16px;

        display: flex;
        justify-content: space-between;
        align-items: center;

        z-index: 10;
    }

    .icon-button {
        width: 46px;
        height: 46px;

        border-radius: 999px;

        border: 1px solid var(--md-sys-color-outline-variant);

        color: var(--md-sys-color-on-surface);

        display: flex;
        align-items: center;
        justify-content: center;

        cursor: pointer;

        box-shadow:
                0 6px 18px rgba(0, 0, 0, 0.25);

        transition:
                transform 0.15s ease,
                background 0.2s ease,
                box-shadow 0.2s ease;
    }

    .icon-button:hover {
        transform: scale(1.05);
        box-shadow: 0 10px 26px rgba(0, 0, 0, 0.28);
    }

    .icon-button:active {
        transform: scale(0.96);
    }

    .icon-button :global(svg) {
        color: var(--md-sys-color-on-surface);
    }

    .back-button {
        background:
                radial-gradient(
                        circle at center,
                        #242724 0%,
                        var(--m3c-outline-variant) 100%
                );
        color: var(--md-sys-color-on-primary-container);
    }

    .action-button {
        background:
                radial-gradient(
                        circle at center,
                        #242724 0%,
                        var(--m3c-outline-variant) 100%
                );
    }

    .header-actions {
        display: flex;
        gap: 10px;
    }

    .product-image-section {
        position: relative;

        overflow: hidden;

        border-radius: 0 0 32px 32px;

        background:
                var(--md-sys-color-surface-container-high);

        box-shadow:
                0 8px 32px rgba(0, 0, 0, 0.18);
    }

    .product-image-gallery {
        width: 100%;
        height: 100%;

        display: grid;
        grid-auto-flow: column;
        grid-auto-columns: 100%;

        overflow-x: auto;
        overflow-y: hidden;

        scroll-snap-type: x mandatory;
        scroll-behavior: smooth;
    }

    .product-image-gallery::-webkit-scrollbar {
        height: 8px;
    }

    .product-image-gallery::-webkit-scrollbar-thumb {
        border-radius: 999px;

        background:
                var(--md-sys-color-outline-variant);
    }

    .carousel-container {
        position: relative;
        width: 100%;
        height: 100%;
    }

    .carousel-btn {
        position: absolute;
        top: 50%;
        transform: translateY(-50%);
        z-index: 5;
        width: 40px;
        height: 40px;
        border-radius: 999px;
        border: none;
        background:
                radial-gradient(
                        circle at center,
                        #242724 0%,
                        var(--m3c-outline-variant) 100%
                );
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        display: grid;
        place-items: center;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        transition: transform 0.15s ease, opacity 0.2s ease;
        opacity: 0;
    }

    .carousel-container:hover .carousel-btn {
        opacity: 1;
    }

    .carousel-btn:hover {
        transform: translateY(-50%) scale(1.1);
    }

    .carousel-btn:active {
        transform: translateY(-50%) scale(0.95);
    }

    .carousel-btn--prev {
        left: 12px;
    }

    .carousel-btn--next {
        right: 12px;
    }

    .carousel-btn-icon {
        font-size: 1.1rem;
        line-height: 1;
    }

    .carousel-dots {
        position: absolute;
        bottom: 14px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        gap: 8px;
        z-index: 5;
        padding: 6px 12px;
        border-radius: 999px;
        background: var(--md-sys-color-surface-container-high);
    }

    .carousel-dot {
        width: 8px;
        height: 8px;
        border-radius: 999px;
        border: none;
        background: color-mix(in srgb, var(--md-sys-color-on-surface) 40%, transparent);
        cursor: pointer;
        padding: 0;
        transition: background 0.2s ease, transform 0.2s ease;
    }

    .carousel-dot.active {
        background: var(--md-sys-color-primary);
        transform: scale(1.4);
    }

    .carousel-dot:hover {
        background: var(--md-sys-color-primary);
    }

    @media (hover: none) {
        .carousel-btn {
            opacity: 1;
        }
    }

    @media (max-width: 768px) {
        .carousel-btn {
            width: 36px;
            height: 36px;
            opacity: 1;
        }

        .carousel-btn--prev {
            left: 8px;
        }

        .carousel-btn--next {
            right: 8px;
        }

        .carousel-dots {
            bottom: 10px;
            gap: 6px;
            padding: 4px 10px;
        }

        .carousel-dot {
            width: 6px;
            height: 6px;
        }
    }

    .product-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        object-position: center;
        scroll-snap-align: center;
    }

    .product-image:hover {
        transform: scale(1.03);
    }

    .detail-copy-card {
        display: grid;
        grid-template-rows:
            auto
            minmax(0, 1fr);

        overflow: hidden;

        margin-top: -12px;

        border-radius: 28px 28px 0 0;

        background:
                var(--md-sys-color-surface);

        position: relative;

        z-index: 2;
    }

    .product-info-section {
        padding:
                28px
                24px
                12px;

        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .product-name {
        margin: 0;

        font-size:
                clamp(
                        1.5rem,
                        3vw,
                        2.1rem
                );

        font-weight: 700;

        line-height: 1.15;

        color:
                var(--md-sys-color-on-surface);
    }

    .price-section {
        display: flex;
        align-items: center;
    }

    .price-value {
        font-size:
                clamp(
                        1.6rem,
                        3vw,
                        2rem
                );

        font-weight: 800;

        color:
                var(--md-sys-color-primary);

        letter-spacing: -0.03em;
    }

    .description-section {
        display: grid;
        grid-template-rows:
            auto
            minmax(0, 1fr);

        gap: 12px;

        min-height: 0;

        overflow: hidden;

        padding:
                0
                24px
                24px;
    }

    .description-title {
        margin: 0;

        font-size: 0.8rem;

        font-weight: 700;

        text-transform: uppercase;

        letter-spacing: 0.08em;

        color:
                var(--md-sys-color-on-surface-variant);
    }

    .description-scroll {
        overflow-y: auto;

        min-height: 0;

        padding-right: 6px;
    }

    .description-scroll::-webkit-scrollbar {
        width: 6px;
    }

    .description-scroll::-webkit-scrollbar-thumb {
        border-radius: 999px;

        background:
                var(--md-sys-color-outline-variant);
    }

    .product-description {
        margin: 0;

        font-size: 1rem;

        line-height: 1.75;

        color:
                var(--md-sys-color-on-surface);

        white-space: pre-wrap;
    }

    .bottom-bar {
        position: sticky;

        bottom: 0;

        padding:
                16px
                20px
                calc(
                        env(safe-area-inset-bottom, 0px) + 16px
                );

        background:
                color-mix(
                        in srgb,
                        var(--md-sys-color-surface) 92%,
                        transparent
                );

        backdrop-filter: blur(18px);

        border-top:
                1px solid
                var(--md-sys-color-outline-variant);
    }

    .cart-action {
        display: grid;
    }

    .cart-action :global(.m3-container) {
        width: 100%;

        min-height: 60px;

        border-radius: 999px;

        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;

        font-size: 1rem;
        font-weight: 700;

        box-shadow:
                0 8px 24px
                rgba(0, 0, 0, 0.18);
    }

    @media (max-width: 768px) {
        .product-detail-screen {
            grid-template-rows:
                minmax(220px, 32vh)
                minmax(0, 1fr)
                auto;
        }

        .product-info-section {
            padding:
                    22px
                    18px
                    10px;
        }

        .description-section {
            padding:
                    0
                    18px
                    18px;
        }

        .bottom-bar {
            padding:
                    14px
                    16px
                    calc(
                            env(safe-area-inset-bottom, 0px) + 14px
                    );
        }

        .icon-button {
            width: 44px;
            height: 44px;
        }
    }

    /* MOBILE */

    @media (max-width: 480px) {
        .product-detail-screen {
            grid-template-rows:
                minmax(200px, 28vh)
                minmax(0, 1fr)
                auto;
        }

        .product-image-section {
            border-radius: 0 0 24px 24px;
        }

        .detail-copy-card {
            border-radius: 24px 24px 0 0;
        }

        .product-name {
            font-size: 1.35rem;
        }

        .price-value {
            font-size: 1.6rem;
        }

        .product-description {
            font-size: 0.95rem;
        }

        .icon-button {
            width: 42px;
            height: 42px;
        }
    }
</style>
