<script lang="ts">
    export let points: number[] = [];
    export let height = 28;
    export let stroke = "var(--md-sys-color-primary)";
    export let fill = "color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent)";

    $: safe = points.filter((n) => Number.isFinite(n));
    $: max = Math.max(1, ...safe);
    $: min = Math.min(max, ...safe);
    $: range = Math.max(1, max - min);
    $: w = Math.max(1, safe.length - 1);

    function x(i: number): number {
        return (i / w) * 100;
    }

    function y(v: number): number {
        const t = (v - min) / range;
        return Math.round((1 - t) * (height - 4) + 2);
    }

    $: d = safe.length
        ? safe
              .map((v, i) => `${i === 0 ? "M" : "L"} ${x(i).toFixed(2)} ${y(v)}`)
              .join(" ")
        : "";

    $: area = safe.length
        ? `${d} L 100 ${height} L 0 ${height} Z`
        : "";
</script>

<svg class="spark" viewBox={`0 0 100 ${height}`} preserveAspectRatio="none" aria-hidden="true">
    {#if safe.length >= 2}
        <path d={area} fill={fill} />
        <path d={d} fill="none" stroke={stroke} stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" />
    {:else}
        <rect x="0" y="0" width="100" height={height} fill="color-mix(in srgb, var(--md-sys-color-outline) 12%, transparent)" rx="10" />
    {/if}
</svg>

<style>
    .spark {
        width: 100%;
        height: auto;
        display: block;
        overflow: visible;
        opacity: 0.95;
    }
</style>

