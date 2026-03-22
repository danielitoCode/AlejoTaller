<script lang="ts">
    export let scrollable = false;
    export let ariaLabel: string | undefined = undefined;
</script>

<div class={`screen ${scrollable ? "scrollable" : ""}`} aria-label={ariaLabel}>
    <slot />
</div>

<style>
    .screen {
        width: 100%;
        height: 100dvh;
        min-height: 100dvh;
        overflow: hidden;
    }

    .screen.scrollable {
        overflow: auto;
        overscroll-behavior: contain;
        -webkit-overflow-scrolling: touch;
    }
</style>
