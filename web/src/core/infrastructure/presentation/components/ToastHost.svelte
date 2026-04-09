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
                <div class="toast-header">
                    <strong>{toneLabel[toast.type]}</strong>
                </div>
                <span>{toast.text}</span>
            </div>
            <div class="toast-actions">
                <button aria-label="Cerrar notificacion" on:click={() => toastStore.remove(toast.id)}>×</button>
            </div>
        </article>
    {/each}
</section>

<style>
    .toast-host {
        position: fixed;
        right: 16px;
        top: 16px;
        z-index: 1900;
        display: grid;
        gap: 12px;
        width: min(360px, calc(100vw - 24px));
        pointer-events: none;
    }

    .toast {
        pointer-events: auto;
        display: grid;
        grid-template-columns: 1fr auto;
        align-items: center;
        gap: 12px;
        padding: 14px 16px;
        border-radius: 12px;
        color: white;
        backdrop-filter: blur(4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        animation: slideIn 0.3s ease-out;
    }

    .toast.success {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.95), rgba(34, 197, 94, 0.85));
        border-left: 4px solid #22c55e;
    }

    .toast.error {
        background: linear-gradient(135deg, rgba(239, 68, 68, 0.95), rgba(239, 68, 68, 0.85));
        border-left: 4px solid #ef4444;
    }

    .toast.warning {
        background: linear-gradient(135deg, rgba(245, 158, 11, 0.96), rgba(217, 119, 6, 0.88));
        border-left: 4px solid #f59e0b;
    }

    .toast.info {
        background: linear-gradient(135deg, rgba(14, 165, 233, 0.95), rgba(2, 132, 199, 0.85));
        border-left: 4px solid #0ea5e9;
    }

    .toast.promo {
        background: linear-gradient(
            135deg,
            rgba(168, 85, 247, 0.96) 0%,
            rgba(99, 102, 241, 0.88) 100%
        );
        border-left: 4px solid #a855f7;
    }

    .toast-copy {
        display: grid;
        gap: 6px;
    }

    .toast-header {
        display: flex;
        gap: 8px;
        align-items: center;
    }

    .toast-copy strong {
        font-weight: 700;
        font-size: 14px;
    }

    .toast-copy span {
        margin: 0;
        font-size: 14px;
        font-weight: 500;
        line-height: 1.3;
    }

    button {
        border: 0;
        padding: 4px 8px;
        min-width: 28px;
        border-radius: 8px;
        cursor: pointer;
        color: white;
        background: transparent;
        font-size: 1rem;
        opacity: 0.7;
        transition: opacity 0.2s, background 0.2s;
    }

    button:hover {
        opacity: 1;
        background: rgba(255, 255, 255, 0.12);
    }

    .toast-actions {
        display: flex;
        gap: 8px;
        align-self: start;
    }

    @keyframes slideIn {
        from {
            transform: translateX(400px);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }

    @media (max-width: 600px) {
        .toast-host {
            left: 8px;
            right: 8px;
            width: auto;
            max-width: none;
        }

        .toast {
            grid-template-columns: 1fr;
        }

        .toast-actions {
            justify-self: end;
        }
    }
</style>
