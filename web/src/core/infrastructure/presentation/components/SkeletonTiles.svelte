<script lang="ts">
    export let count = 6;
    export let columns = 2;

    $: grid = `repeat(${columns}, minmax(0, 1fr))`;
</script>

<div class="sk-tiles" style={`grid-template-columns:${grid}`} aria-busy="true" aria-live="polite">
    {#each Array(count) as _, i (i)}
        <div class="tile">
            <div class="top">
                <div class="line sk" style="width:62%"></div>
                <div class="line sk" style="width:28%"></div>
            </div>
            <div class="mid">
                <div class="line sk" style="width:46%"></div>
                <div class="line sk" style="width:34%"></div>
            </div>
            <div class="bottom">
                <div class="line sk" style="width:36%"></div>
                <div class="chev sk"></div>
            </div>
        </div>
    {/each}
</div>

<style>
    .sk-tiles {
        display: grid;
        gap: 12px;
    }

    .tile {
        border-radius: 20px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
        padding: 14px;
        display: grid;
        gap: 12px;
        overflow: hidden;
        box-shadow: 0 14px 34px color-mix(in srgb, black 24%, transparent);
    }

    .top,
    .bottom {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: center;
    }

    .mid {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
        align-items: center;
    }

    .line {
        height: 14px;
        border-radius: 999px;
    }

    .chev {
        width: 18px;
        height: 18px;
        border-radius: 6px;
    }

    .sk {
        background: linear-gradient(
            90deg,
            color-mix(in srgb, var(--md-sys-color-surface-variant) 36%, transparent) 0%,
            color-mix(in srgb, var(--md-sys-color-on-surface) 10%, transparent) 35%,
            color-mix(in srgb, var(--md-sys-color-surface-variant) 36%, transparent) 70%
        );
        background-size: 240% 100%;
        animation: shimmer 1.25s ease-in-out infinite;
    }

    @keyframes shimmer {
        0% {
            background-position: 0 0;
        }
        100% {
            background-position: 100% 0;
        }
    }
</style>

