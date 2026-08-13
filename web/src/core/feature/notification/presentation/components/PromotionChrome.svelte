<script lang="ts">
    import { Icon } from "m3-svelte";
    import localOfferIcon from "@ktibow/iconset-material-symbols/local-offer-rounded";
    import { promotionStore } from "../viewmodel/promotion.store";
    import PromotionDrawer from "./PromotionDrawer.svelte";

    /** "nav" = botón bajo el rail (desktop); "float" = FAB + tooltip (móvil) */
    export let variant: "nav" | "float" = "nav";

    let open = false;
    let tipVisible = true;
    let shake = true;

    if (typeof window !== "undefined" && variant === "float") {
        window.setTimeout(() => {
            tipVisible = false;
            shake = false;
        }, 3200);
    }

    function openDrawer() {
        open = true;
        tipVisible = false;
        shake = false;
    }

    $: activeCount = $promotionStore.items.filter((p) => {
        const now = Date.now();
        const from = Number(p.validFromEpochMillis) || 0;
        const until = Number(p.validUntilEpochMillis) || Number.MAX_SAFE_INTEGER;
        if (p.status === "cancelled" || p.status === "ended" || p.status === "draft") return false;
        return now >= from && now <= until;
    }).length;
</script>

{#if variant === "nav"}
    <button type="button" class="promo-nav-btn" on:click={openDrawer}>
        <Icon icon={localOfferIcon} />
        <span class="promo-nav-copy">
            <strong>Promociones</strong>
            <small>{activeCount > 0 ? `${activeCount} activas` : "Ver ofertas"}</small>
        </span>
        {#if activeCount > 0}
            <span class="promo-nav-badge">{activeCount}</span>
        {/if}
    </button>
    <PromotionDrawer {open} onClose={() => (open = false)} />
{:else}
    <div class="promo-float">
        {#if tipVisible}
            <div class="promo-tip" role="status">Toca para ver promociones</div>
        {/if}
        <button
            type="button"
            class="promo-float-btn"
            class:shake
            aria-label="Abrir promociones"
            on:click={openDrawer}
        >
            <Icon icon={localOfferIcon} />
            {#if activeCount > 0}
                <span class="promo-float-badge">{activeCount}</span>
            {/if}
        </button>
    </div>
    <PromotionDrawer {open} onClose={() => (open = false)} />
{/if}

<style>
    .promo-nav-btn {
        width: 100%;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 16px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 30%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 70%, transparent);
        color: inherit;
        font: inherit;
        cursor: pointer;
        text-align: left;
    }

    .promo-nav-copy {
        flex: 1;
        min-width: 0;
        display: grid;
        gap: 1px;
    }

    .promo-nav-copy strong {
        font-size: 0.9rem;
        font-weight: 800;
    }

    .promo-nav-copy small {
        font-size: 0.75rem;
        opacity: 0.85;
    }

    .promo-nav-badge {
        min-width: 22px;
        height: 22px;
        padding: 0 6px;
        border-radius: 999px;
        display: grid;
        place-items: center;
        font-size: 0.72rem;
        font-weight: 800;
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
    }

    .promo-float {
        position: fixed;
        top: max(12px, env(safe-area-inset-top));
        right: max(12px, env(safe-area-inset-right));
        z-index: 70;
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 8px;
        pointer-events: none;
    }

    .promo-float > * {
        pointer-events: auto;
    }

    .promo-tip {
        max-width: 180px;
        padding: 8px 12px;
        border-radius: 12px;
        font-size: 0.78rem;
        font-weight: 650;
        background: var(--md-sys-color-inverse-surface);
        color: var(--md-sys-color-inverse-on-surface);
        box-shadow: 0 8px 20px color-mix(in srgb, black 18%, transparent);
        animation: promo-tip-in 280ms ease both;
    }

    .promo-float-btn {
        width: 52px;
        height: 52px;
        border-radius: 16px;
        border: none;
        display: grid;
        place-items: center;
        position: relative;
        cursor: pointer;
        color: var(--md-sys-color-on-primary-container);
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 92%, white);
        box-shadow:
            0 10px 24px color-mix(in srgb, black 16%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 25%, transparent);
    }

    .promo-float-btn.shake {
        animation: promo-shake 0.55s ease-in-out 0.4s 3;
    }

    .promo-float-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        min-width: 18px;
        height: 18px;
        padding: 0 5px;
        border-radius: 999px;
        font-size: 0.68rem;
        font-weight: 800;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-error);
        color: var(--md-sys-color-on-error);
    }

    @keyframes promo-tip-in {
        from {
            opacity: 0;
            transform: translateY(-6px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @keyframes promo-shake {
        0%,
        100% {
            transform: rotate(0deg);
        }
        20% {
            transform: rotate(-12deg);
        }
        40% {
            transform: rotate(10deg);
        }
        60% {
            transform: rotate(-8deg);
        }
        80% {
            transform: rotate(6deg);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .promo-float-btn.shake,
        .promo-tip {
            animation: none !important;
        }
    }
</style>
