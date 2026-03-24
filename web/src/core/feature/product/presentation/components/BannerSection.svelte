<script lang="ts">
    import { onMount } from "svelte";
    import type { Promotion } from "../../../notification/domain/entity/Promotion";

    export let visible: boolean = true;
    export let promotions: Promotion[] = [];
    export let onPromotionClick: (id: string) => void = () => {};
    export let closable: boolean = false;
    export let onClose: () => void = () => {};

    let activeIndex = 0;
    let autoRotateInterval: number | undefined;

    $: activePromotion = promotions.length > 0 ? promotions[activeIndex % promotions.length] : null;

    onMount(() => {
        // Start auto-rotation if multiple promotions
        if (promotions.length > 1) {
            autoRotateInterval = window.setInterval(() => {
                activeIndex = (activeIndex + 1) % promotions.length;
            }, 4000) as unknown as number;
        }

        return () => {
            if (autoRotateInterval) {
                clearInterval(autoRotateInterval);
            }
        };
    });
</script>

{#if visible && activePromotion}
    <div
        class="banner-section"
        role="button"
        tabindex="0"
        on:click={() => onPromotionClick(activePromotion.id)}
        on:keydown={(e) => e.key === "Enter" && onPromotionClick(activePromotion.id)}
    >
        {#if closable}
            <button
                class="close-banner"
                type="button"
                aria-label="Cerrar promocion"
                on:click={(e) => {
                    e.stopPropagation();
                    onClose();
                }}
            >
                ×
            </button>
        {/if}
        <div class="banner-box">
            <div class="banner-content">
                <div class="banner-text">
                    <h3 class="banner-title">
                        {activePromotion.title || "Promoción especial"}
                    </h3>
                    <div class="message-badge">
                        <span class="badge-dot">🎁</span>
                        <span class="badge-text">
                            {activePromotion.message || "Hasta 50% descuento"}
                        </span>
                    </div>
                </div>
            </div>
        </div>
        {#if promotions.length > 1}
            <div class="banner-dots">
                {#each promotions as _, idx}
                    <div
                        class={`dot ${idx === activeIndex ? "active" : ""}`}
                        role="button"
                        tabindex="0"
                        on:click={(e) => {
                            e.stopPropagation();
                            activeIndex = idx;
                        }}
                        on:keydown={(e) => {
                            if (e.key === 'Enter') {
                                e.stopPropagation();
                                activeIndex = idx;
                            }
                        }}
                        aria-label={`Promoción ${idx + 1}`}
                    ></div>
                {/each}
            </div>
        {/if}
    </div>
{/if}

<style>
    .banner-section {
        width: 100%;
        height: 160px;
        border-radius: 20px;
        background: linear-gradient(135deg, var(--md-sys-color-primary) 0%, var(--md-sys-color-primary-container) 100%);
        cursor: pointer;
        position: relative;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        transition: all 0.3s ease;
    }

    .close-banner {
        position: absolute;
        top: 12px;
        right: 12px;
        width: 32px;
        height: 32px;
        border: 0;
        border-radius: 999px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-on-primary) 20%, transparent);
        color: var(--md-sys-color-on-primary);
        font-size: 1.1rem;
        cursor: pointer;
        z-index: 1;
    }

    .banner-section:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgb(0 0 0 / 0.15);
    }

    .banner-box {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px;
    }

    .banner-content {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        gap: 16px;
    }

    .banner-text {
        display: flex;
        flex-direction: column;
        gap: 12px;
        flex: 1;
    }

    .banner-title {
        font-size: 1.25rem;
        font-weight: 600;
        color: var(--md-sys-color-on-primary);
        margin: 0;
        line-height: 1.2;
    }

    .message-badge {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        background: var(--md-sys-color-on-primary);
        background: rgb(255 255 255 / 0.2);
        color: var(--md-sys-color-on-primary);
        padding: 6px 12px;
        border-radius: 20px;
        font-size: 0.75rem;
        font-weight: 600;
        width: fit-content;
    }

    .badge-dot {
        font-size: 0.9rem;
    }

    .badge-text {
        line-height: 1;
    }

    .banner-dots {
        display: flex;
        justify-content: center;
        gap: 6px;
        padding: 8px 20px;
    }

    .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: var(--md-sys-color-on-primary);
        opacity: 0.3;
        cursor: pointer;
        transition: all 0.3s ease;
    }

    .dot.active {
        opacity: 1;
        width: 24px;
        border-radius: 4px;
    }

    .dot:hover {
        opacity: 0.6;
    }
</style>
