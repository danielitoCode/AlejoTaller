<script lang="ts">
    import {toastStore} from "../viewmodel/toast.store";

    const toneLabel = {
        success: "Exito",
        error: "Error",
        warning: "Aviso",
        info: "Info",
        promo: "Promo"
    } as const;
</script>

<section class="toast-host" aria-live="polite" aria-atomic="true">
    {#each $toastStore.queue as toast (toast.id)}
        <article class={`toast ${toast.type}`} role="status">
            <div class="toast-copy">
                <strong>{toneLabel[toast.type]}</strong>
                <span>{toast.text}</span>
            </div>
            <button aria-label="Cerrar notificacion" on:click={() => toastStore.remove(toast.id)}>x</button>
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
        align-items: start;
        gap: 12px;
        border-radius: 18px;
        padding: 12px 14px;
        border-left: 6px solid var(--md-sys-color-outline);
        border-top: 1px solid var(--md-sys-color-outline-variant);
        border-right: 1px solid var(--md-sys-color-outline-variant);
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container-high);
        color: var(--md-sys-color-on-surface);
        box-shadow: 0 14px 28px color-mix(in srgb, var(--md-sys-color-outline) 24%, transparent);
    }

    .toast.success {
        border-left-color: #2f8f3a;
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 78%, var(--md-sys-color-surface-container-high));
    }

    .toast.error {
        border-left-color: #d92d20;
        background: color-mix(in srgb, var(--md-sys-color-error-container) 82%, var(--md-sys-color-surface-container-high));
    }

    .toast.warning {
        border-left-color: #f59e0b;
        background: color-mix(in srgb, #f59e0b 18%, var(--md-sys-color-surface-container-high));
    }

    .toast.info {
        border-left-color: #0ea5e9;
        background: color-mix(in srgb, var(--md-sys-color-secondary-container) 78%, var(--md-sys-color-surface-container-high));
    }

    .toast.promo {
        border-left-color: #a855f7;
        background: linear-gradient(
            135deg,
            color-mix(in srgb, var(--md-sys-color-tertiary-container) 84%, var(--md-sys-color-surface-container-high)) 0%,
            color-mix(in srgb, var(--md-sys-color-primary-container) 72%, var(--md-sys-color-surface-container-high)) 100%
        );
    }

    .toast-copy {
        display: grid;
        gap: 3px;
    }

    .toast-copy strong {
        font-size: 0.82rem;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .toast-copy span {
        line-height: 1.35;
    }

    button {
        border: 0;
        width: 28px;
        height: 28px;
        border-radius: 10px;
        cursor: pointer;
        color: inherit;
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 55%, transparent);
        font-size: 1rem;
    }
</style>
