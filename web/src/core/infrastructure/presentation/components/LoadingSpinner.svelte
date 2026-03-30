<script lang="ts">
    export let size = 16;
    export let label = "Cargando";
    export let subtle = false;
</script>

<span
    class="spinner {subtle ? 'subtle' : ''}"
    style={`--s:${size}px`}
    role="status"
    aria-label={label}
></span>

<style>
    .spinner {
        width: var(--s);
        height: var(--s);
        border-radius: 50%;
        border: max(2px, calc(var(--s) / 8)) solid color-mix(in srgb, var(--md-sys-color-outline) 35%, transparent);
        border-top-color: var(--md-sys-color-primary);
        animation: spin 0.9s linear infinite;
        display: inline-block;
        flex: 0 0 auto;
        filter: drop-shadow(0 10px 18px color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent));
    }

    .spinner.subtle {
        border-top-color: color-mix(in srgb, var(--md-sys-color-on-surface) 70%, transparent);
        filter: none;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
</style>
