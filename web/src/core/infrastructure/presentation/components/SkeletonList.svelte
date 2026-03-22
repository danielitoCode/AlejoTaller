<script lang="ts">
    export let rows = 7;
    export let showAvatar = true;
    export let showActions = true;
    export let lines = 2;
    export let compact = false;

    const widths = ["92%", "76%", "68%", "84%", "60%", "72%", "88%"];
</script>

<div class="sk-list {compact ? 'compact' : ''}" aria-busy="true" aria-live="polite">
    {#each Array(rows) as _, i (i)}
        <div class="sk-row">
            {#if showAvatar}
                <div class="sk-avatar sk"></div>
            {/if}

            <div class="sk-main">
                <div class="sk-line sk" style={`width:${widths[i % widths.length]}`}></div>
                {#if lines >= 2}
                    <div class="sk-line sk dim" style={`width:${widths[(i + 2) % widths.length]}`}></div>
                {/if}
                {#if lines >= 3}
                    <div class="sk-line sk dim" style={`width:${widths[(i + 4) % widths.length]}`}></div>
                {/if}
            </div>

            {#if showActions}
                <div class="sk-actions">
                    <div class="sk-pill sk"></div>
                    <div class="sk-pill sk"></div>
                </div>
            {/if}
        </div>
    {/each}
</div>

<style>
    .sk-list {
        display: grid;
        gap: 10px;
    }

    .sk-row {
        display: grid;
        grid-template-columns: auto 1fr auto;
        gap: 12px;
        align-items: center;
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
        padding: 12px 14px;
        overflow: hidden;
        position: relative;
    }

    .sk-list.compact .sk-row {
        padding: 10px 12px;
        border-radius: 14px;
    }

    .sk-avatar {
        width: 58px;
        height: 58px;
        border-radius: 18px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 30%, transparent);
    }

    .sk-list.compact .sk-avatar {
        width: 44px;
        height: 44px;
        border-radius: 16px;
    }

    .sk-main {
        min-width: 0;
        display: grid;
        gap: 8px;
    }

    .sk-line {
        height: 14px;
        border-radius: 999px;
    }

    .sk-line.dim {
        opacity: 0.9;
        height: 12px;
    }

    .sk-actions {
        display: inline-flex;
        gap: 10px;
        align-items: center;
        justify-content: end;
    }

    .sk-pill {
        width: 86px;
        height: 36px;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
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
            background-position: 0% 0%;
        }
        100% {
            background-position: 100% 0%;
        }
    }
</style>

