<script lang="ts">
    import { createEventDispatcher } from "svelte";
    import { fade, fly } from "svelte/transition";
    import { Button } from "m3-svelte";
    import { LogIn, X } from "lucide-svelte";

    export let open = false;

    const dispatch = createEventDispatcher<{ login: void; close: void }>();

    function handleLogin() {
        dispatch("login");
    }

    function handleClose() {
        dispatch("close");
    }

    function handleKeydown(event: KeyboardEvent) {
        if (event.key === "Escape") handleClose();
    }
</script>

<svelte:window on:keydown={handleKeydown} />

{#if open}
    <!-- svelte-ignore a11y-click-events-have-key-events -->
    <!-- svelte-ignore a11y-no-static-element-interactions -->
    <div
        class="overlay"
        role="presentation"
        in:fade={{ duration: 180 }}
        out:fade={{ duration: 140 }}
    >
        <button
            class="scrim"
            type="button"
            aria-label="Cerrar"
            on:click={handleClose}
        ></button>

        <div
            class="panel"
            role="dialog"
            aria-modal="true"
            aria-labelledby="guest-overlay-title"
            in:fly={{ y: 24, duration: 240, opacity: 0 }}
            out:fly={{ y: 16, duration: 160, opacity: 0 }}
        >
            <div class="panel-header">
                <div class="icon-wrap" aria-hidden="true">
                    <LogIn size={28} />
                </div>
                <button
                    class="close-btn"
                    type="button"
                    aria-label="Cerrar"
                    on:click={handleClose}
                >
                    <X size={20} />
                </button>
            </div>

            <div class="panel-body">
                <h2 id="guest-overlay-title">Inicia sesión para continuar</h2>
                <p>
                    Estás navegando como visitante. Inicia sesión o crea una
                    cuenta para acceder a esta función.
                </p>
            </div>

            <div class="panel-actions">
                <Button variant="filled" size="m" onclick={handleLogin}>
                    <span class="btn-content">
                        <LogIn size={16} />
                        <span>Iniciar sesión</span>
                    </span>
                </Button>
                <Button variant="text" size="m" onclick={handleClose}>
                    Cancelar
                </Button>
            </div>
        </div>
    </div>
{/if}

<style>
    .overlay {
        position: fixed;
        inset: 0;
        z-index: 1100;
        display: grid;
        place-items: center;
        padding: 20px;
    }

    .scrim {
        position: absolute;
        inset: 0;
        border: 0;
        background: color-mix(in srgb, var(--md-sys-color-scrim) 58%, transparent);
        backdrop-filter: blur(5px);
        -webkit-backdrop-filter: blur(5px);
        cursor: pointer;
    }

    .panel {
        position: relative;
        z-index: 1;
        width: min(420px, calc(100vw - 32px));
        border-radius: 28px;
        background: var(--md-sys-color-surface-container-high);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        box-shadow:
            0 24px 56px color-mix(in srgb, black 38%, transparent),
            0 8px 20px color-mix(in srgb, black 18%, transparent);
        overflow: hidden;
    }

    .panel-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px 20px 0;
    }

    .icon-wrap {
        width: 52px;
        height: 52px;
        border-radius: 16px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 80%, transparent);
        color: var(--md-sys-color-on-primary-container);
    }

    .close-btn {
        width: 36px;
        height: 36px;
        border: none;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-on-surface) 10%, transparent);
        color: var(--md-sys-color-on-surface-variant);
        display: grid;
        place-items: center;
        cursor: pointer;
        transition: background 0.18s ease;
    }

    .close-btn:hover {
        background: color-mix(in srgb, var(--md-sys-color-on-surface) 16%, transparent);
    }

    .panel-body {
        padding: 18px 22px 8px;
        display: grid;
        gap: 8px;
    }

    .panel-body h2 {
        margin: 0;
        font-size: 1.15rem;
        font-weight: 700;
        letter-spacing: -0.02em;
        color: var(--md-sys-color-on-surface);
    }

    .panel-body p {
        margin: 0;
        font-size: 0.92rem;
        line-height: 1.5;
        color: var(--md-sys-color-on-surface-variant);
    }

    .panel-actions {
        padding: 16px 22px 22px;
        display: grid;
        gap: 10px;
    }

    .panel-actions :global(.m3-container) {
        width: 100%;
    }

    .btn-content {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        font-weight: 600;
        padding: 10px 0;
    }
</style>
