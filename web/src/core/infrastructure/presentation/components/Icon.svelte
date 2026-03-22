<script lang="ts">
    export let icon: any;
    export let size: number | string = 18;
    export let strokeWidth: number | string = 2;
    export let className = "";
    export let ariaLabel: string | undefined = undefined;
</script>

<svelte:component
    this={icon}
    {size}
    {strokeWidth}
    class={className}
    aria-label={ariaLabel}
/>

