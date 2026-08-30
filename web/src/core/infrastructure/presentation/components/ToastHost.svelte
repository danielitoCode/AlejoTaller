<script lang="ts">
    import { fly, fade } from "svelte/transition";
    import { flip } from "svelte/animate";
    import { toastStore, type ToastType } from "../viewmodel/toast.store";
    import Icon from "./Icon.svelte";
    import {
        CheckCircle2,
        XCircle,
        Info,
        AlertTriangle,
        Loader2,
        Sparkles,
        X
    } from "lucide-svelte";

    const iconMap: Record<ToastType, typeof CheckCircle2> = {
        success: CheckCircle2,
        error: XCircle,
        info: Info,
        warning: AlertTriangle,
        promo: Sparkles,
        loading: Loader2
    };

    const labelMap: Record<ToastType, string> = {
        success: "Éxito",
        error: "Error",
        info: "Info",
        warning: "Aviso",
        promo: "Promo",
        loading: "En curso"
    };

    $: loadings = $toastStore.queue.filter((t) => t.type === "loading");
</script>

<section class="toast-host" aria-live="polite" aria-atomic="false">
    {#if loadings.length > 1}
        <div class="activity-chip" transition:fade={{ duration: 160 }}>
            <Icon icon={Loader2} size={14} className="spin" ariaLabel="Operaciones en curso" />
            <span>{loadings.length} operaciones en curso</span>
        </div>
    {/if}

    {#each $toastStore.queue as toast (toast.id)}
        <article
            class="toast {toast.type}"
            role="status"
            in:fly={{ x: 24, duration: 240 }}
            out:fade={{ duration: 160 }}
            animate:flip={{ duration: 200 }}
        >
            <span class="toast-ico" class:spin={toast.type === "loading"}>
                <Icon icon={iconMap[toast.type]} size={20} ariaLabel={labelMap[toast.type]} />
            </span>
            <div class="toast-body">
                <strong class="toast-label">{labelMap[toast.type]}</strong>
                <span class="toast-text">{toast.text}</span>
            </div>
            {#if toast.type !== "loading"}
                <button
                    class="toast-close"
                    type="button"
                    aria-label="Cerrar notificación"
                    on:click={() => toastStore.remove(toast.id)}
                >
                    <Icon icon={X} size={16} ariaLabel="Cerrar" />
                </button>
            {/if}
            {#if toast.type === "loading"}
                <div class="toast-progress" aria-hidden="true"></div>
            {/if}
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
        gap: 10px;
        width: min(380px, calc(100vw - 24px));
        pointer-events: none;
    }

    .activity-chip {
        pointer-events: none;
        justify-self: end;
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 6px 12px;
        border-radius: 999px;
        font-size: 0.78rem;
        font-weight: 700;
        color: #e2e8f0;
        background: rgba(15, 23, 42, 0.72);
        border: 1px solid rgba(148, 163, 184, 0.35);
        backdrop-filter: blur(10px);
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
    }

    .toast {
        position: relative;
        pointer-events: auto;
        display: grid;
        grid-template-columns: auto 1fr auto;
        align-items: start;
        gap: 12px;
        padding: 12px 14px;
        border-radius: 14px;
        color: white;
        backdrop-filter: blur(8px);
        box-shadow: 0 10px 28px rgba(0, 0, 0, 0.22);
        overflow: hidden;
        font-size: 0.92rem;
        line-height: 1.35;
    }

    .toast-ico {
        display: grid;
        place-items: center;
        margin-top: 1px;
        flex-shrink: 0;
    }

    .toast-body {
        min-width: 0;
        display: grid;
        gap: 2px;
    }

    .toast-label {
        font-size: 0.72rem;
        font-weight: 800;
        letter-spacing: 0.04em;
        text-transform: uppercase;
        opacity: 0.9;
    }

    .toast-text {
        font-weight: 600;
        word-break: break-word;
    }

    .toast-close {
        border: 0;
        width: 28px;
        height: 28px;
        border-radius: 8px;
        cursor: pointer;
        color: inherit;
        background: transparent;
        display: grid;
        place-items: center;
        opacity: 0.7;
        transition: opacity 0.15s, background 0.15s;
    }

    .toast-close:hover {
        opacity: 1;
        background: rgba(255, 255, 255, 0.12);
    }

    .toast.success {
        background: linear-gradient(135deg, rgba(34, 197, 94, 0.95), rgba(22, 163, 74, 0.88));
        border-left: 4px solid #22c55e;
    }

    .toast.error {
        background: linear-gradient(135deg, rgba(239, 68, 68, 0.95), rgba(220, 38, 38, 0.88));
        border-left: 4px solid #ef4444;
    }

    .toast.warning {
        background: linear-gradient(135deg, rgba(245, 158, 11, 0.96), rgba(217, 119, 6, 0.9));
        border-left: 4px solid #f59e0b;
    }

    .toast.info {
        background: linear-gradient(135deg, rgba(14, 165, 233, 0.95), rgba(2, 132, 199, 0.88));
        border-left: 4px solid #0ea5e9;
    }

    .toast.promo {
        background: linear-gradient(135deg, rgba(168, 85, 247, 0.96), rgba(99, 102, 241, 0.9));
        border-left: 4px solid #a855f7;
    }

    .toast.loading {
        background: linear-gradient(135deg, rgba(139, 92, 246, 0.94), rgba(109, 40, 217, 0.88));
        border-left: 4px solid #a78bfa;
    }

    .toast-progress {
        position: absolute;
        left: 0;
        bottom: 0;
        height: 3px;
        width: 40%;
        border-radius: 0 2px 0 0;
        background: rgba(255, 255, 255, 0.75);
        animation: progress-indeterminate 1.4s ease-in-out infinite;
    }

    :global(.spin) {
        animation: spin 0.9s linear infinite;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    @keyframes progress-indeterminate {
        0% {
            left: 0;
            width: 30%;
        }
        50% {
            left: 35%;
            width: 40%;
        }
        100% {
            left: 100%;
            width: 20%;
        }
    }

    @media (max-width: 600px) {
        .toast-host {
            left: 8px;
            right: 8px;
            top: 12px;
            width: auto;
            max-width: none;
        }
    }
</style>
