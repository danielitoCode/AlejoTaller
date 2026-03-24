<script lang="ts">
    import { Card } from "m3-svelte";

    export let modifier: string = "";
</script>

<div class={`product-details-placeholder ${modifier}`}>
    <Card variant="filled">
        <div class="placeholder-content">
            <div class="placeholder-circle-outer">
                <div class="placeholder-circle-inner"></div>
            </div>

            <p class="placeholder-text">
                Selecciona un producto para ver detalles
            </p>
        </div>
    </Card>
</div>

<style>
    .product-details-placeholder {
        width: 100%;
        height: 100%;
        display: grid;
        place-items: center;
        padding: 32px;
    }

    .product-details-placeholder :global(.m3-container) {
        width: 100%;
        height: 100%;
        display: grid;
        place-items: center;
        padding: 32px;
    }

    .placeholder-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 24px;
        text-align: center;
    }

    .placeholder-circle-outer {
        width: 120px;
        height: 120px;
        border-radius: 50%;
        background-color: var(--md-sys-color-surface-variant);
        opacity: 0.3;
        display: grid;
        place-items: center;
    }

    .placeholder-circle-inner {
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background-color: var(--md-sys-color-surface-variant);
        opacity: 0.6;
    }

    .placeholder-text {
        font-size: 1rem;
        line-height: 1.5;
        color: var(--md-sys-color-on-surface-variant);
        margin: 0;
        max-width: 280px;
    }
</style>
