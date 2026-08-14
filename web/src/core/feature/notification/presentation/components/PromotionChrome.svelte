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
        <span class="promo-nav-icon" aria-hidden="true">
            <Icon icon={localOfferIcon} />
        </span>
        <span class="promo-nav-copy">
            <strong>Promociones</strong>
            <small>{activeCount > 0 ? `${activeCount} activas · toca para ver` : "Ver ofertas"}</small>
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
        gap: 12px;
        padding: 12px 14px;
        border-radius: 18px;
        border: none;
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        font: inherit;
        cursor: pointer;
        text-align: left;
        box-shadow:
            0 6px 16px color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent),
            0 2px 6px color-mix(in srgb, black 18%, transparent);
        transition: transform 0.15s ease, box-shadow 0.2s ease, filter 0.2s ease;
    }

    .promo-nav-btn:hover {
        filter: brightness(1.06);
        box-shadow:
            0 10px 22px color-mix(in srgb, var(--md-sys-color-primary) 50%, transparent),
            0 3px 8px color-mix(in srgb, black 20%, transparent);
    }

    .promo-nav-btn:active {
        transform: scale(0.98);
    }

    .promo-nav-btn.has-active {
        animation: promo-nav-pulse 2.4s ease-in-out infinite;
    }

    .promo-nav-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: grid;
        place-items: center;
        flex-shrink: 0;
        background: color-mix(in srgb, var(--md-sys-color-on-primary) 18%, transparent);
        color: var(--md-sys-color-on-primary);
    }

    .promo-nav-copy {
        flex: 1;
        min-width: 0;
        display: grid;
        gap: 2px;
    }

    .promo-nav-copy strong {
        font-size: 0.95rem;
        font-weight: 850;
        letter-spacing: -0.01em;
        color: var(--md-sys-color-on-primary);
    }

    .promo-nav-copy small {
        font-size: 0.75rem;
        font-weight: 600;
        color: color-mix(in srgb, var(--md-sys-color-on-primary) 82%, transparent);
    }

    .promo-nav-badge {
        min-width: 26px;
        height: 26px;
        padding: 0 7px;
        border-radius: 999px;
        display: grid;
        place-items: center;
        font-size: 0.78rem;
        font-weight: 850;
        background: var(--md-sys-color-on-primary);
        color: var(--md-sys-color-primary);
        box-shadow: 0 2px 6px color-mix(in srgb, black 20%, transparent);
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
        width: 56px;
        height: 56px;
        border-radius: 18px;
        border: none;
        display: grid;
        place-items: center;
        position: relative;
        cursor: pointer;
        color: var(--md-sys-color-on-primary);
        background: var(--md-sys-color-primary);
        box-shadow:
            0 12px 28px color-mix(in srgb, var(--md-sys-color-primary) 40%, transparent),
            0 4px 12px color-mix(in srgb, black 22%, transparent);
    }

    .promo-float-btn.has-active {
        animation: promo-nav-pulse 2.4s ease-in-out infinite;
    }

    .promo-float-btn.shake {
        animation: promo-shake 0.55s ease-in-out 0.4s 3;
    }

    .promo-float-btn.shake.has-active {
        animation:
            promo-shake 0.55s ease-in-out 0.4s 3,
            promo-nav-pulse 2.4s ease-in-out 2.2s infinite;
    }

    .promo-float-badge {
        position: absolute;
        top: -4px;
        right: -4px;
        min-width: 20px;
        height: 20px;
        padding: 0 5px;
        border-radius: 999px;
        font-size: 0.7rem;
        font-weight: 850;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-error);
        color: var(--md-sys-color-on-error);
        border: 2px solid var(--md-sys-color-surface);
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

    @keyframes promo-nav-pulse {
        0%,
        100% {
            box-shadow:
                0 6px 16px color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent),
                0 2px 6px color-mix(in srgb, black 18%, transparent),
                0 0 0 0 color-mix(in srgb, var(--md-sys-color-primary) 55%, transparent);
        }
        50% {
            box-shadow:
                0 8px 20px color-mix(in srgb, var(--md-sys-color-primary) 50%, transparent),
                0 2px 6px color-mix(in srgb, black 18%, transparent),
                0 0 0 8px color-mix(in srgb, var(--md-sys-color-primary) 0%, transparent);
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .promo-float-btn.shake,
        .promo-tip,
        .promo-nav-btn.has-active,
        .promo-float-btn.has-active {
            animation: none !important;
        }
    }
</style>
