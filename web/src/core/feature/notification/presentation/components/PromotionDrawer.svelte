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
        return resolvePromotionKind(promo) === "banner" ? "Banner" : "Descuento";
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
                                    class="promo-card"
                                    on:click={() => {
                                        onSelect(promo);
                                        onClose();
                                    }}
                                >
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
                                    <span class="promo-card-copy">
                                        <span class="promo-kind">{kindLabel(promo)}</span>
                                        <strong>{promo.title}</strong>
                                        <small>{promo.message}</small>
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
        background: color-mix(in srgb, black 62%, transparent);
        backdrop-filter: blur(3px);
        -webkit-backdrop-filter: blur(3px);
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
        /* Fondo sólido: evita que el contenido de detrás se vea a través */
        background: var(--md-sys-color-surface-container-lowest, var(--md-sys-color-surface));
        color: var(--md-sys-color-on-surface);
        box-shadow: -16px 0 48px color-mix(in srgb, black 35%, transparent);
        outline: none;
        isolation: isolate;
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
        background: var(--md-sys-color-surface-container-lowest, var(--md-sys-color-surface));
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
        align-items: center;
        text-align: left;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        padding: 10px;
        background: var(--md-sys-color-surface-container);
        color: inherit;
        font: inherit;
        cursor: pointer;
    }

    .promo-card:hover {
        border-color: color-mix(
            in srgb,
            var(--md-sys-color-primary) 40%,
            var(--md-sys-color-outline-variant)
        );
        background: var(--md-sys-color-surface-container-high);
    }

    .promo-card-img {
        width: 56px;
        height: 56px;
        border-radius: 12px;
        object-fit: cover;
        flex-shrink: 0;
        background: var(--md-sys-color-surface-container-high);
    }

    .promo-card-img.placeholder {
        display: grid;
        place-items: center;
        color: var(--md-sys-color-primary);
    }

    .promo-card-copy {
        min-width: 0;
        display: grid;
        gap: 2px;
    }

    .promo-card-copy strong,
    .promo-card-copy small {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .promo-kind {
        font-size: 0.68rem;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        color: var(--md-sys-color-primary);
        opacity: 0.9;
    }

    .promo-card-copy strong {
        font-weight: 800;
        font-size: 0.95rem;
    }

    .promo-card-copy small {
        font-size: 0.8rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    .promo-price {
        display: flex;
        gap: 8px;
        align-items: baseline;
        font-size: 0.85rem;
    }

    .promo-price s {
        opacity: 0.65;
    }

    .promo-price em {
        font-style: normal;
        font-weight: 800;
        color: var(--md-sys-color-primary);
    }
</style>
