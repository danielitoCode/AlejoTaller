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
        min-height: 100dvh;
        overflow-x: hidden;
        overflow-y: hidden;
    }

    .screen.scrollable {
        overflow-y: auto;
        overscroll-behavior-y: contain;
        -webkit-overflow-scrolling: touch;
    }
</style>
