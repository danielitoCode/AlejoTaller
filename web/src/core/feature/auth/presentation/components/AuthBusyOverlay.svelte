<script lang="ts">
    import { fade } from "svelte/transition";

    /** Visible mientras se valida credenciales / crea cuenta */
    export let open = false;
    /** Texto principal */
    export let title = "Validando…";
    /** Texto secundario */
    export let subtitle = "Un momento, estamos comprobando tus datos";
</script>

{#if open}
    <div
        class="auth-busy"
        role="status"
        aria-live="polite"
        aria-busy="true"
        transition:fade={{ duration: 160 }}
    >
        <div class="auth-busy-card">
            <div class="orbit" aria-hidden="true">
                <span class="ring ring-a"></span>
                <span class="ring ring-b"></span>
                <span class="core">
                    <img src="/alejoicon_clean.svg" alt="" />
                </span>
            </div>
            <p class="title">{title}</p>
            <p class="subtitle">{subtitle}</p>
            <div class="dots" aria-hidden="true">
                <span></span><span></span><span></span>
            </div>
        </div>
    </div>
{/if}

<style>
    .auth-busy {
        position: fixed;
        inset: 0;
        z-index: 1200;
        display: grid;
        place-items: center;
        padding: 24px;
        background:
            radial-gradient(
                circle at 50% 30%,
                color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent),
                transparent 55%
            ),
            color-mix(in srgb, var(--md-sys-color-scrim, #000) 58%, transparent);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
    }

    .auth-busy-card {
        width: min(100%, 320px);
        display: grid;
        justify-items: center;
        gap: 14px;
        padding: 28px 22px 24px;
        border-radius: 28px;
        text-align: center;
        background:
            linear-gradient(
                165deg,
                color-mix(in srgb, var(--md-sys-color-surface-container-high) 92%, transparent),
                color-mix(in srgb, var(--md-sys-color-surface) 94%, transparent)
            );
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow:
            0 20px 48px color-mix(in srgb, black 28%, transparent),
            0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
    }

    .orbit {
        position: relative;
        width: 96px;
        height: 96px;
        display: grid;
        place-items: center;
        margin-bottom: 4px;
    }

    .ring {
        position: absolute;
        inset: 0;
        border-radius: 50%;
        border: 2px solid transparent;
    }

    .ring-a {
        border-top-color: var(--md-sys-color-primary);
        border-right-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
        animation: spin 1.1s linear infinite;
    }

    .ring-b {
        inset: 10px;
        border-bottom-color: var(--md-sys-color-tertiary, #c9a227);
        border-left-color: color-mix(in srgb, var(--md-sys-color-tertiary, #c9a227) 40%, transparent);
        animation: spin 1.6s linear infinite reverse;
    }

    .core {
        width: 52px;
        height: 52px;
        border-radius: 16px;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-surface-container-highest);
        box-shadow: 0 8px 20px color-mix(in srgb, black 18%, transparent);
    }

    .core img {
        width: 34px;
        height: 34px;
        object-fit: contain;
    }

    .title {
        margin: 0;
        font-size: 1.05rem;
        font-weight: 800;
        letter-spacing: -0.02em;
        color: var(--md-sys-color-on-surface);
    }

    .subtitle {
        margin: 0;
        font-size: 0.86rem;
        line-height: 1.4;
        color: var(--md-sys-color-on-surface-variant);
    }

    .dots {
        display: flex;
        gap: 6px;
        margin-top: 2px;
    }

    .dots span {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background: var(--md-sys-color-primary);
        opacity: 0.35;
        animation: pulse 1.2s ease-in-out infinite;
    }

    .dots span:nth-child(2) {
        animation-delay: 0.18s;
    }

    .dots span:nth-child(3) {
        animation-delay: 0.36s;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    @keyframes pulse {
        0%,
        100% {
            opacity: 0.3;
            transform: translateY(0);
        }
        50% {
            opacity: 1;
            transform: translateY(-3px);
        }
    }
</style>
