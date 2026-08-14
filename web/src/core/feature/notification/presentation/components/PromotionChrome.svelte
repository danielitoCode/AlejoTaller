<script lang="ts">
    import { Icon } from "m3-svelte";
    import localOfferIcon from "@ktibow/iconset-material-symbols/campaign-rounded";
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
    <button
        type="button"
        class="promo-nav-btn"
        class:has-active={activeCount > 0}
        on:click={openDrawer}
    >
        <span class="promo-nav-shine" aria-hidden="true"></span>
        <span class="promo-nav-icon-wrap" aria-hidden="true">
            <span class="promo-nav-ring"></span>
            <span class="promo-nav-icon">
                <Icon icon={localOfferIcon} />
            </span>
        </span>
        <span class="promo-nav-copy">
            <strong>Promociones</strong>
            <small>
                {#if activeCount > 0}
                    <span class="promo-live-dot" aria-hidden="true"></span>
                    {activeCount} activa{activeCount === 1 ? "" : "s"} · toca para ver
                {:else}
                    Ver ofertas del día
                {/if}
            </small>
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
            class:has-active={activeCount > 0}
            aria-label="Abrir promociones"
            on:click={openDrawer}
        >
            <span class="promo-float-ring" aria-hidden="true"></span>
            <Icon icon={localOfferIcon} />
            {#if activeCount > 0}
                <span class="promo-float-badge">{activeCount}</span>
            {/if}
        </button>
    </div>
    <PromotionDrawer {open} onClose={() => (open = false)} />
{/if}

<style>
    /* ─── NAV (rail inferior): tarjeta de oferta ─── */
    .promo-nav-btn {
        position: relative;
        width: 100%;
        display: flex;
        align-items: center;
        gap: 14px;
        padding: 14px 16px;
        border-radius: 20px;
        border: none;
        overflow: hidden;
        isolation: isolate;
        font: inherit;
        cursor: pointer;
        text-align: left;
        color: #fff;
        background: linear-gradient(135deg, #ff6b35 0%, #f7c948 48%, #ff3d81 100%);
        background-size: 200% 200%;
        box-shadow:
            0 10px 28px color-mix(in srgb, #ff3d81 38%, transparent),
            0 4px 12px color-mix(in srgb, #ff6b35 35%, transparent),
            inset 0 1px 0 color-mix(in srgb, #fff 35%, transparent);
        transition: transform 0.15s ease, filter 0.2s ease;
    }

    .promo-nav-btn.has-active {
        animation: promo-gradient-shift 4s ease infinite;
    }

    .promo-nav-btn:hover {
        filter: brightness(1.08) saturate(1.1);
        transform: translateY(-1px);
    }

    .promo-nav-btn:active {
        transform: scale(0.98);
    }

    /* Barrido de brillo */
    .promo-nav-shine {
        position: absolute;
        inset: 0;
        z-index: 0;
        pointer-events: none;
        background: linear-gradient(
            105deg,
            transparent 40%,
            color-mix(in srgb, #fff 45%, transparent) 50%,
            transparent 60%
        );
        transform: translateX(-120%);
        animation: promo-shine 3.2s ease-in-out infinite;
    }

    .promo-nav-icon-wrap,
    .promo-nav-copy,
    .promo-nav-badge {
        position: relative;
        z-index: 1;
    }

    .promo-nav-icon-wrap {
        position: relative;
        width: 52px;
        height: 52px;
        flex-shrink: 0;
        display: grid;
        place-items: center;
    }

    .promo-nav-ring {
        position: absolute;
        inset: 0;
        border-radius: 16px;
        border: 2px solid color-mix(in srgb, #fff 55%, transparent);
        animation: promo-ring-pulse 1.8s ease-out infinite;
    }

    .promo-nav-icon {
        width: 44px;
        height: 44px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, #fff 22%, transparent);
        color: #fff;
        font-size: 1.55rem;
        box-shadow: 0 4px 12px color-mix(in srgb, #000 18%, transparent);
        animation: promo-icon-bob 1.6s ease-in-out infinite;
    }

    .promo-nav-icon :global(svg) {
        width: 28px;
        height: 28px;
    }

    .promo-nav-copy {
        flex: 1;
        min-width: 0;
        display: grid;
        gap: 3px;
    }

    .promo-nav-copy strong {
        font-size: 1.05rem;
        font-weight: 900;
        letter-spacing: -0.02em;
        color: #fff;
        text-shadow: 0 1px 2px color-mix(in srgb, #000 25%, transparent);
    }

    .promo-nav-copy small {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 0.78rem;
        font-weight: 700;
        color: color-mix(in srgb, #fff 92%, transparent);
    }

    .promo-live-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #fff;
        box-shadow: 0 0 0 0 color-mix(in srgb, #fff 70%, transparent);
        animation: promo-live-dot 1.4s ease-out infinite;
        flex-shrink: 0;
    }

    .promo-nav-badge {
        min-width: 30px;
        height: 30px;
        padding: 0 8px;
        border-radius: 999px;
        display: grid;
        place-items: center;
        font-size: 0.85rem;
        font-weight: 900;
        background: #fff;
        color: #e91e63;
        box-shadow: 0 4px 12px color-mix(in srgb, #000 22%, transparent);
        animation: promo-badge-pop 2s ease-in-out infinite;
    }

    /* ─── FLOAT (móvil) ─── */
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
        position: relative;
        width: 60px;
        height: 60px;
        border-radius: 20px;
        border: none;
        display: grid;
        place-items: center;
        cursor: pointer;
        color: #fff;
        background: linear-gradient(135deg, #ff6b35 0%, #ff3d81 100%);
        box-shadow:
            0 12px 28px color-mix(in srgb, #ff3d81 42%, transparent),
            0 4px 12px color-mix(in srgb, #ff6b35 30%, transparent);
        font-size: 1.5rem;
    }

    .promo-float-btn.has-active .promo-float-ring {
        animation: promo-ring-pulse 1.8s ease-out infinite;
    }

    .promo-float-btn.shake {
        animation: promo-shake 0.55s ease-in-out 0.4s 3;
    }

    .promo-float-ring {
        position: absolute;
        inset: -4px;
        border-radius: 22px;
        border: 2px solid color-mix(in srgb, #ff3d81 70%, transparent);
        pointer-events: none;
        opacity: 0;
    }

    .promo-float-badge {
        position: absolute;
        top: -5px;
        right: -5px;
        min-width: 22px;
        height: 22px;
        padding: 0 6px;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 900;
        display: grid;
        place-items: center;
        background: #fff;
        color: #e91e63;
        border: 2px solid #ff3d81;
        box-shadow: 0 2px 8px color-mix(in srgb, #000 25%, transparent);
    }

    /* ─── Keyframes ─── */
    @keyframes promo-gradient-shift {
        0%,
        100% {
            background-position: 0% 50%;
        }
        50% {
            background-position: 100% 50%;
        }
    }

    @keyframes promo-shine {
        0%,
        55% {
            transform: translateX(-120%);
        }
        75%,
        100% {
            transform: translateX(120%);
        }
    }

    @keyframes promo-icon-bob {
        0%,
        100% {
            transform: translateY(0) rotate(-4deg);
        }
        50% {
            transform: translateY(-3px) rotate(4deg);
        }
    }

    @keyframes promo-ring-pulse {
        0% {
            transform: scale(1);
            opacity: 0.85;
        }
        100% {
            transform: scale(1.35);
            opacity: 0;
        }
    }

    @keyframes promo-live-dot {
        0% {
            box-shadow: 0 0 0 0 color-mix(in srgb, #fff 70%, transparent);
        }
        70% {
            box-shadow: 0 0 0 8px color-mix(in srgb, #fff 0%, transparent);
        }
        100% {
            box-shadow: 0 0 0 0 color-mix(in srgb, #fff 0%, transparent);
        }
    }

    @keyframes promo-badge-pop {
        0%,
        100% {
            transform: scale(1);
        }
        40% {
            transform: scale(1.12);
        }
        60% {
            transform: scale(0.96);
        }
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
        .promo-nav-btn.has-active,
        .promo-nav-shine,
        .promo-nav-icon,
        .promo-nav-ring,
        .promo-live-dot,
        .promo-nav-badge,
        .promo-float-btn.shake,
        .promo-float-btn.has-active .promo-float-ring,
        .promo-tip {
            animation: none !important;
        }
    }
</style>
