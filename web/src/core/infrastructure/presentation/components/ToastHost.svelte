<script lang="ts">
    import { toastStore } from "../viewmodel/toast.store";
</script>

<section class="toast-host" aria-live="polite" aria-atomic="true">
    {#each $toastStore.queue as toast (toast.id)}
        <article class={`toast ${toast.type}`} role="status">
            <span>{toast.text}</span>
            <button aria-label="Cerrar notificación" on:click={() => toastStore.remove(toast.id)}>✕</button>
        </article>
    {/each}
</section>

<style>
    .toast-host {
        position: fixed;
        right: 16px;
        bottom: 16px;
        z-index: 1000;
        display: grid;
        gap: 8px;
        width: min(360px, calc(100vw - 24px));
        pointer-events: none;
    }

    .toast {
        pointer-events: auto;
        display: grid;
        grid-template-columns: 1fr auto;
        align-items: center;
        gap: 10px;
        border-radius: 12px;
        padding: 10px 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        box-shadow: 0 10px 24px color-mix(in srgb, var(--md-sys-color-outline) 24%, transparent);
    }

    .toast.success {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 45%, var(--md-sys-color-outline-variant));
    }

    .toast.error {
        border-color: color-mix(in srgb, var(--md-sys-color-error) 65%, var(--md-sys-color-outline-variant));
    }

    .toast.info {
        border-color: color-mix(in srgb, var(--md-sys-color-secondary) 45%, var(--md-sys-color-outline-variant));
    }

    button {
        border: 0;
        width: 28px;
        height: 28px;
        border-radius: 8px;
        cursor: pointer;
        color: inherit;
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 55%, transparent);
    }
</style>