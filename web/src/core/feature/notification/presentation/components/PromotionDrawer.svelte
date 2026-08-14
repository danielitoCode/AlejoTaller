<script lang="ts">
    import { onMount } from "svelte";
    import { fly, fade } from "svelte/transition";
    import { Icon } from "m3-svelte";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";
    import localOfferIcon from "@ktibow/iconset-material-symbols/campaign-rounded";
    import { promotionStore } from "../viewmodel/promotion.store";
    import {
        isActiveBanner,
        isActiveProductDiscount,
        resolvePromotionKind,
    } from "../../domain/policy/PromotionPolicy";
    import type { Promotion } from "../../domain/entity/Promotion";
    import { productStore } from "../../../product/presentation/viewmodel/product.store";
    import { resolvePromotionImageUrl } from "../utils/promotion.images";

    export let open = false;
    export let onClose: () => void = () => {};
    export let onSelect: (promo: Promotion) => void = () => {};

    onMount(() => {
        promotionStore.syncAll({ suppressPermissionError: true }).catch(() => {});
    });

    $: items = $promotionStore.items;
    $: now = Date.now();
    $: active = items.filter(
        (p) => isActiveBanner(p, now) || isActiveProductDiscount(p, now)
    );
    $: productPhotoById = new Map(
        ($productStore.items ?? []).map((prod) => [prod.id, prod.photoUrl] as const)
    );

    function imageFor(promo: Promotion): string | null {
        return resolvePromotionImageUrl(promo, productPhotoById);
    }

    function kindLabel(promo: Promotion): string {
        return resolvePromotionKind(promo) === "banner" ? "Banner" : "Oferta de producto";
    }

    function kindClass(promo: Promotion): string {
        return resolvePromotionKind(promo) === "banner" ? "is-banner" : "is-product";
    }

    function discountPct(promo: Promotion): number | null {
        const oldP = Number(promo.oldPrice);
        const cur = Number(promo.currentPrice);
        if (!Number.isFinite(oldP) || oldP <= 0 || !Number.isFinite(cur) || cur >= oldP) return null;
        return Math.round(((oldP - cur) / oldP) * 100);
    }

    function handleKey(e: KeyboardEvent) {
        if (e.key === "Escape") onClose();
    }
</script>

{#if open}
    <div class="promo-overlay" role="presentation" transition:fade={{ duration: 160 }}>
        <button class="promo-scrim" type="button" aria-label="Cerrar promociones" on:click={onClose}></button>
        <aside
            class="promo-drawer"
            role="dialog"
            aria-modal="true"
            aria-label="Promociones"
            tabindex="-1"
            transition:fly={{ x: 28, duration: 260 }}
            on:keydown={handleKey}
        >
            <header class="promo-drawer-head">
                <div class="promo-drawer-title">
                    <Icon icon={localOfferIcon} />
                    <div>
                        <strong>Promociones</strong>
                        <small>{active.length} activa{active.length === 1 ? "" : "s"}</small>
                    </div>
                </div>
                <button class="promo-drawer-close" type="button" aria-label="Cerrar" on:click={onClose}>
                    <Icon icon={closeIcon} />
                </button>
            </header>

            <div class="promo-drawer-body">
                {#if $promotionStore.loading && active.length === 0}
                    <p class="promo-muted">Cargando ofertas…</p>
                {:else if active.length === 0}
                    <p class="promo-muted">No hay promociones activas por ahora.</p>
                {:else}
                    <ul class="promo-list">
                        {#each active as promo (promo.id)}
                            <li>
                                <button
                                    type="button"
                                    class="promo-card {kindClass(promo)}"
                                    on:click={() => {
                                        onSelect(promo);
                                        onClose();
                                    }}
                                >
                                    <span class="promo-card-media">
                                        {#if imageFor(promo)}
                                            <img
                                                src={imageFor(promo)}
                                                alt=""
                                                class="promo-card-img"
                                                referrerpolicy="no-referrer"
                                                loading="lazy"
                                                on:error={(e) => {
                                                    const el = e.currentTarget;
                                                    if (el) el.style.display = "none";
                                                }}
                                            />
                                        {:else}
                                            <span class="promo-card-img placeholder" aria-hidden="true">
                                                <Icon icon={localOfferIcon} />
                                            </span>
                                        {/if}
                                        {#if kindClass(promo) === "is-product" && discountPct(promo) != null}
                                            <span class="promo-discount-chip">-{discountPct(promo)}%</span>
                                        {/if}
                                    </span>
                                    <span class="promo-card-copy">
                                        <span class="promo-kind-row">
                                            <span class="promo-kind">{kindLabel(promo)}</span>
                                        </span>
                                        <strong>{promo.title}</strong>
                                        <small>{promo.message}</small>
                                        {#if kindClass(promo) === "is-product"}
                                            {#if promo.oldPrice != null && promo.currentPrice != null}
                                                <span class="promo-price">
                                                    <s>{promo.oldPrice}</s>
                                                    <em>{promo.currentPrice}</em>
                                                </span>
                                            {:else if promo.currentPrice != null}
                                                <span class="promo-price">
                                                    <em>{promo.currentPrice}</em>
                                                </span>
                                            {/if}
                                        {:else}
                                            <span class="promo-banner-hint">Anuncio general · no ligado a un producto</span>
                                        {/if}
                                    </span>
                                </button>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </div>
        </aside>
    </div>
{/if}

<style>
    .promo-overlay {
        position: fixed;
        inset: 0;
        z-index: 90;
        display: flex;
        justify-content: flex-end;
    }

    .promo-scrim {
        position: absolute;
        inset: 0;
        border: none;
        background: color-mix(in srgb, black 78%, transparent);
        backdrop-filter: blur(6px);
        -webkit-backdrop-filter: blur(6px);
        cursor: pointer;
    }

    .promo-drawer {
        position: relative;
        z-index: 1;
        width: min(380px, 92vw);
        height: 100%;
        max-height: 100dvh;
        display: flex;
        flex-direction: column;
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        box-shadow: -20px 0 56px color-mix(in srgb, black 45%, transparent);
        outline: none;
        isolation: isolate;
        background-image: linear-gradient(
            var(--md-sys-color-surface),
            var(--md-sys-color-surface)
        );
    }

    .promo-drawer-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 16px 14px;
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
        flex-shrink: 0;
        background: var(--md-sys-color-surface-container-low, var(--md-sys-color-surface));
    }

    .promo-drawer-title {
        display: flex;
        align-items: center;
        gap: 10px;
        min-width: 0;
    }

    .promo-drawer-title strong {
        display: block;
        font-size: 1.05rem;
        font-weight: 850;
    }

    .promo-drawer-title small {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.8rem;
    }

    .promo-drawer-close {
        border: none;
        background: var(--md-sys-color-surface-container-high);
        border-radius: 12px;
        width: 40px;
        height: 40px;
        display: grid;
        place-items: center;
        cursor: pointer;
        color: inherit;
    }

    .promo-drawer-body {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        padding: 12px;
        background: var(--md-sys-color-surface);
    }

    .promo-muted {
        margin: 24px 8px;
        text-align: center;
        color: var(--md-sys-color-on-surface-variant);
    }

    .promo-list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        gap: 10px;
    }

    .promo-card {
        width: 100%;
        display: flex;
        gap: 12px;
        align-items: stretch;
        text-align: left;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 18px;
        padding: 12px;
        background: var(--md-sys-color-surface-container);
        color: inherit;
        font: inherit;
        cursor: pointer;
        transition: border-color 0.15s ease, box-shadow 0.15s ease, transform 0.12s ease;
        position: relative;
        overflow: hidden;
    }

    .promo-card:hover {
        transform: translateY(-1px);
        box-shadow: 0 8px 20px color-mix(in srgb, black 14%, transparent);
    }

    .promo-card.is-banner {
        border-color: color-mix(in srgb, #ff3d81 35%, var(--md-sys-color-outline-variant));
        background:
            linear-gradient(
                135deg,
                color-mix(in srgb, #ff6b35 12%, var(--md-sys-color-surface-container)) 0%,
                color-mix(in srgb, #ff3d81 10%, var(--md-sys-color-surface-container)) 100%
            );
    }

    .promo-card.is-banner:hover {
        border-color: color-mix(in srgb, #ff3d81 55%, var(--md-sys-color-outline-variant));
    }

    .promo-card.is-banner .promo-kind {
        background: linear-gradient(135deg, #ff6b35, #ff3d81);
        color: #fff;
    }

    .promo-card.is-banner .promo-card-media {
        background: color-mix(in srgb, #ff3d81 14%, var(--md-sys-color-surface-container-high));
    }

    .promo-card.is-product {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 28%, var(--md-sys-color-outline-variant));
        background: var(--md-sys-color-surface-container);
    }

    .promo-card.is-product:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 50%, var(--md-sys-color-outline-variant));
    }

    .promo-card.is-product .promo-kind {
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
        color: var(--md-sys-color-primary);
    }

    .promo-card-media {
        position: relative;
        flex-shrink: 0;
        width: 64px;
        height: 64px;
        border-radius: 14px;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-high);
    }

    .promo-card-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .promo-card-img.placeholder {
        display: grid;
        place-items: center;
        width: 100%;
        height: 100%;
        color: var(--md-sys-color-primary);
    }

    .promo-discount-chip {
        position: absolute;
        left: 4px;
        bottom: 4px;
        padding: 2px 6px;
        border-radius: 999px;
        font-size: 0.68rem;
        font-weight: 900;
        letter-spacing: 0.02em;
        background: var(--md-sys-color-error);
        color: var(--md-sys-color-on-error);
        box-shadow: 0 2px 6px color-mix(in srgb, black 25%, transparent);
    }

    .promo-card-copy {
        min-width: 0;
        flex: 1;
        display: grid;
        gap: 3px;
        align-content: center;
    }

    .promo-kind-row {
        display: flex;
        align-items: center;
        gap: 6px;
    }

    .promo-kind {
        display: inline-flex;
        align-items: center;
        padding: 3px 8px;
        border-radius: 999px;
        font-size: 0.66rem;
        font-weight: 850;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        width: fit-content;
    }

    .promo-card-copy strong {
        font-weight: 850;
        font-size: 0.96rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: var(--md-sys-color-on-surface);
    }

    .promo-card-copy small {
        font-size: 0.8rem;
        color: var(--md-sys-color-on-surface-variant);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .promo-price {
        display: flex;
        gap: 8px;
        align-items: baseline;
        font-size: 0.88rem;
        margin-top: 2px;
    }

    .promo-price s {
        opacity: 0.6;
        font-weight: 600;
        color: var(--md-sys-color-on-surface-variant);
    }

    .promo-price em {
        font-style: normal;
        font-weight: 900;
        color: var(--md-sys-color-primary);
        font-size: 0.98rem;
    }

    .promo-banner-hint {
        margin-top: 2px;
        font-size: 0.72rem;
        font-weight: 650;
        color: color-mix(in srgb, #ff3d81 75%, var(--md-sys-color-on-surface-variant));
    }

    @media (max-width: 840px) {
        .promo-drawer {
            width: min(100vw, 420px);
        }
    }

    @media (max-width: 480px) {
        .promo-drawer {
            width: 100vw;
            max-width: 100vw;
        }
    }
</style>
