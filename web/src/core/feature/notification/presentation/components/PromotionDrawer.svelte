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

    function isBanner(promo: Promotion): boolean {
        return resolvePromotionKind(promo) === "banner";
    }

    function kindLabel(promo: Promotion): string {
        return isBanner(promo) ? "Banner" : "Oferta";
    }

    function discountPct(promo: Promotion): number | null {
        const oldP = Number(promo.oldPrice);
        const cur = Number(promo.currentPrice);
        if (!Number.isFinite(oldP) || oldP <= 0 || !Number.isFinite(cur) || cur >= oldP) return null;
        return Math.round(((oldP - cur) / oldP) * 100);
    }

    function formatMoney(value: number | null | undefined): string {
        if (value == null || !Number.isFinite(Number(value))) return "";
        const n = Number(value);
        return new Intl.NumberFormat("en-US", {
            style: "currency",
            currency: "USD",
            minimumFractionDigits: n % 1 === 0 ? 0 : 2,
            maximumFractionDigits: 2,
        }).format(n);
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
                    <span class="promo-head-icon" aria-hidden="true">
                        <Icon icon={localOfferIcon} />
                    </span>
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
                            {@const banner = isBanner(promo)}
                            {@const pct = discountPct(promo)}
                            {@const img = imageFor(promo)}
                            <li>
                                <button
                                    type="button"
                                    class="promo-card {banner ? 'is-banner' : 'is-product'}"
                                    on:click={() => {
                                        onSelect(promo);
                                        onClose();
                                    }}
                                >
                                    {#if banner}
                                        <span class="banner-accent" aria-hidden="true"></span>
                                    {/if}

                                    <span class="promo-card-media">
                                        {#if img}
                                            <img
                                                src={img}
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
                                        {#if !banner && pct != null}
                                            <span class="promo-discount-chip">
                                                <span class="chip-pct">-{pct}%</span>
                                            </span>
                                        {/if}
                                        {#if banner}
                                            <span class="banner-media-badge" aria-hidden="true">
                                                <Icon icon={localOfferIcon} />
                                            </span>
                                        {/if}
                                    </span>

                                    <span class="promo-card-copy">
                                        <span class="promo-kind-row">
                                            <span class="promo-kind">
                                                <Icon icon={localOfferIcon} />
                                                {kindLabel(promo)}
                                            </span>
                                            {#if !banner && pct != null}
                                                <span class="promo-save">Ahorra {pct}%</span>
                                            {/if}
                                        </span>

                                        <strong class="promo-title">{promo.title}</strong>
                                        <small class="promo-msg">{promo.message}</small>

                                        {#if banner}
                                            <span class="promo-banner-hint">Anuncio general · no ligado a un producto</span>
                                        {:else}
                                            <span class="promo-price-row">
                                                {#if promo.oldPrice != null && Number(promo.oldPrice) > 0}
                                                    <s class="old">{formatMoney(promo.oldPrice)}</s>
                                                {/if}
                                                {#if promo.currentPrice != null}
                                                    <em class="now">{formatMoney(promo.currentPrice)}</em>
                                                {/if}
                                            </span>
                                        {/if}
                                    </span>

                                    <span class="promo-chevron" aria-hidden="true">›</span>
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
        width: min(400px, 92vw);
        height: 100%;
        max-height: 100dvh;
        display: flex;
        flex-direction: column;
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        box-shadow: -20px 0 56px color-mix(in srgb, black 45%, transparent);
        outline: none;
        isolation: isolate;
    }

    .promo-drawer-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 16px 16px 14px;
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
        flex-shrink: 0;
        background: var(--md-sys-color-surface-container-low, var(--md-sys-color-surface));
    }

    .promo-drawer-title {
        display: flex;
        align-items: center;
        gap: 12px;
        min-width: 0;
    }

    .promo-head-icon {
        display: grid;
        place-items: center;
        width: 40px;
        height: 40px;
        border-radius: 14px;
        background: linear-gradient(135deg, #ff6b35, #ff3d81);
        color: #fff;
        flex-shrink: 0;
        box-shadow: 0 6px 16px color-mix(in srgb, #ff3d81 35%, transparent);
    }

    .promo-drawer-title strong {
        display: block;
        font-size: 1.08rem;
        font-weight: 850;
        letter-spacing: -0.01em;
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
        transition: background 0.15s ease;
    }

    .promo-drawer-close:hover {
        background: var(--md-sys-color-surface-container-highest, var(--md-sys-color-surface-container-high));
    }

    .promo-drawer-body {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        padding: 14px;
        background: var(--md-sys-color-surface);
        -webkit-overflow-scrolling: touch;
    }

    .promo-muted {
        margin: 32px 12px;
        text-align: center;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.92rem;
    }

    .promo-list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        gap: 12px;
    }

    .promo-card {
        width: 100%;
        display: grid;
        grid-template-columns: auto 1fr auto;
        gap: 12px;
        align-items: center;
        text-align: left;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 18px;
        padding: 12px;
        background: var(--md-sys-color-surface-container);
        color: inherit;
        font: inherit;
        cursor: pointer;
        transition:
            border-color 0.18s ease,
            box-shadow 0.18s ease,
            transform 0.15s ease,
            background 0.18s ease;
        position: relative;
        overflow: hidden;
        min-height: 96px;
    }

    .promo-card:hover {
        transform: translateY(-2px);
        box-shadow:
            0 10px 28px color-mix(in srgb, black 18%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
    }

    .promo-card:active {
        transform: translateY(0);
    }

    .promo-card:focus-visible {
        outline: 2px solid var(--md-sys-color-primary);
        outline-offset: 2px;
    }

    .promo-card.is-product {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 22%, var(--md-sys-color-outline-variant));
        background:
            linear-gradient(
                145deg,
                color-mix(in srgb, var(--md-sys-color-primary) 6%, var(--md-sys-color-surface-container)) 0%,
                var(--md-sys-color-surface-container) 55%
            );
    }

    .promo-card.is-product:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 48%, var(--md-sys-color-outline-variant));
    }

    .promo-card.is-product .promo-kind {
        background: color-mix(in srgb, var(--md-sys-color-primary) 16%, transparent);
        color: var(--md-sys-color-primary);
    }

    .promo-card.is-banner {
        border-color: color-mix(in srgb, #ff3d81 32%, var(--md-sys-color-outline-variant));
        background:
            linear-gradient(
                135deg,
                color-mix(in srgb, #ff6b35 14%, var(--md-sys-color-surface-container)) 0%,
                color-mix(in srgb, #ff3d81 12%, var(--md-sys-color-surface-container)) 100%
            );
    }

    .promo-card.is-banner:hover {
        border-color: color-mix(in srgb, #ff3d81 58%, var(--md-sys-color-outline-variant));
        box-shadow:
            0 10px 28px color-mix(in srgb, #ff3d81 18%, transparent),
            0 0 0 1px color-mix(in srgb, #ff3d81 20%, transparent);
    }

    .banner-accent {
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        width: 4px;
        background: linear-gradient(180deg, #ff6b35, #ff3d81);
        border-radius: 18px 0 0 18px;
    }

    .promo-card.is-banner .promo-kind {
        background: linear-gradient(135deg, #ff6b35, #ff3d81);
        color: #fff;
    }

    .promo-card.is-banner .promo-card-media {
        background: color-mix(in srgb, #ff3d81 16%, var(--md-sys-color-surface-container-high));
    }

    .promo-card-media {
        position: relative;
        flex-shrink: 0;
        width: 76px;
        height: 76px;
        border-radius: 16px;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-high);
        box-shadow: inset 0 0 0 1px color-mix(in srgb, white 6%, transparent);
    }

    .promo-card-img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
        transition: transform 0.25s ease;
    }

    .promo-card:hover .promo-card-img {
        transform: scale(1.06);
    }

    .promo-card-img.placeholder {
        display: grid;
        place-items: center;
        width: 100%;
        height: 100%;
        color: var(--md-sys-color-primary);
        background:
            radial-gradient(
                circle at 30% 30%,
                color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent),
                transparent 70%
            );
    }

    .promo-discount-chip {
        position: absolute;
        left: 5px;
        bottom: 5px;
        padding: 3px 7px;
        border-radius: 999px;
        font-size: 0.7rem;
        font-weight: 900;
        letter-spacing: 0.02em;
        background: linear-gradient(135deg, #e53935, #c62828);
        color: #fff;
        box-shadow: 0 3px 10px color-mix(in srgb, #c62828 45%, transparent);
        line-height: 1.1;
    }

    .chip-pct {
        display: block;
    }

    .banner-media-badge {
        position: absolute;
        right: 5px;
        top: 5px;
        width: 26px;
        height: 26px;
        border-radius: 50%;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, black 45%, transparent);
        color: #fff;
        backdrop-filter: blur(4px);
    }

    .promo-card-copy {
        min-width: 0;
        display: grid;
        gap: 4px;
        align-content: center;
    }

    .promo-kind-row {
        display: flex;
        align-items: center;
        gap: 8px;
        flex-wrap: wrap;
    }

    .promo-kind {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 3px 9px;
        border-radius: 999px;
        font-size: 0.65rem;
        font-weight: 850;
        text-transform: uppercase;
        letter-spacing: 0.06em;
        width: fit-content;
        line-height: 1.2;
    }

    .promo-save {
        font-size: 0.68rem;
        font-weight: 750;
        color: var(--md-sys-color-error);
        letter-spacing: 0.01em;
    }

    .promo-title {
        font-weight: 850;
        font-size: 0.95rem;
        line-height: 1.25;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        white-space: normal;
        color: var(--md-sys-color-on-surface);
        letter-spacing: -0.01em;
    }

    .promo-msg {
        font-size: 0.78rem;
        line-height: 1.3;
        color: var(--md-sys-color-on-surface-variant);
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        white-space: normal;
    }

    .promo-price-row {
        display: flex;
        gap: 8px;
        align-items: baseline;
        margin-top: 2px;
        flex-wrap: wrap;
    }

    .promo-price-row .old {
        font-size: 0.78rem;
        font-weight: 600;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.75;
    }

    .promo-price-row .now {
        font-style: normal;
        font-weight: 900;
        font-size: 1.05rem;
        color: var(--md-sys-color-primary);
        letter-spacing: -0.02em;
    }

    .promo-banner-hint {
        margin-top: 2px;
        font-size: 0.7rem;
        font-weight: 650;
        color: color-mix(in srgb, #ff3d81 80%, var(--md-sys-color-on-surface-variant));
    }

    .promo-chevron {
        flex-shrink: 0;
        width: 22px;
        height: 22px;
        display: grid;
        place-items: center;
        border-radius: 50%;
        font-size: 1.15rem;
        font-weight: 700;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.55;
        transition: opacity 0.15s ease, transform 0.15s ease, color 0.15s ease;
    }

    .promo-card:hover .promo-chevron {
        opacity: 1;
        transform: translateX(2px);
        color: var(--md-sys-color-primary);
    }

    @media (max-width: 840px) {
        .promo-drawer {
            width: min(100vw, 420px);
        }

        .promo-card-media {
            width: 72px;
            height: 72px;
        }
    }

    @media (max-width: 480px) {
        .promo-drawer {
            width: 100vw;
            max-width: 100vw;
        }

        .promo-drawer-body {
            padding: 12px;
        }

        .promo-card {
            grid-template-columns: auto 1fr;
            gap: 10px;
            padding: 10px;
            min-height: 88px;
            border-radius: 16px;
        }

        .promo-chevron {
            display: none;
        }

        .promo-card-media {
            width: 68px;
            height: 68px;
            border-radius: 14px;
        }

        .promo-title {
            font-size: 0.9rem;
            -webkit-line-clamp: 2;
            line-clamp: 2;
        }

        .promo-msg {
            -webkit-line-clamp: 1;
            line-clamp: 1;
        }

        .promo-price-row .now {
            font-size: 0.98rem;
        }
    }

    @media (min-width: 600px) and (max-width: 1024px) {
        .promo-drawer {
            width: min(420px, 48vw);
        }

        .promo-card-media {
            width: 84px;
            height: 84px;
        }

        .promo-title {
            font-size: 1rem;
        }
    }
</style>
