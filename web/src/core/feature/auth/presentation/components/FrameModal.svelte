<script lang="ts">
    import { createEventDispatcher, onDestroy, onMount } from "svelte";

    export let open = false;
    export let title = "";
    export let ariaLabel = title || "Dialogo";
    export let src = "";
    export let allowedOrigin = typeof window !== "undefined" ? window.location.origin : "";

    const dispatch = createEventDispatcher<{
        close: void;
        frameMessage: { data: any };
    }>();

    let frameEl: HTMLIFrameElement | null = null;

    function handleMessage(event: MessageEvent) {
        if (!open) return;
        if (allowedOrigin && event.origin !== allowedOrigin) return;
        if (frameEl?.contentWindow && event.source !== frameEl.contentWindow) return;
        dispatch("frameMessage", { data: event.data });
    }

    function close() {
        dispatch("close");
    }

    function handleKeydown(event: KeyboardEvent) {
        if (open && event.key === "Escape") close();
    }

    onMount(() => {
        window.addEventListener("message", handleMessage);
        window.addEventListener("keydown", handleKeydown);
    });

    onDestroy(() => {
        window.removeEventListener("message", handleMessage);
        window.removeEventListener("keydown", handleKeydown);
    });
</script>

{#if open}
    <div class="frame-modal">
        <button class="scrim" type="button" aria-label="Cerrar modal" on:click={close}></button>
        <div class="panel" role="dialog" aria-modal="true" aria-label={ariaLabel}>
            <header class="head">
                <strong>{title}</strong>
                <button class="close-btn" type="button" aria-label="Cerrar" on:click={close}>x</button>
            </header>
            <div class="frame-shell">
                <iframe bind:this={frameEl} title={title} src={src} loading="eager"></iframe>
            </div>
        </div>
    </div>
{/if}

<style>
    .frame-modal {
        position: fixed;
        inset: 0;
        z-index: 1100;
        display: grid;
        place-items: center;
        padding: 16px;
    }

    .scrim {
        position: absolute;
        inset: 0;
        border: 0;
        background: color-mix(in srgb, black 44%, transparent);
        backdrop-filter: blur(6px);
    }

    .panel {
        position: relative;
        z-index: 1;
        width: min(460px, calc(100vw - 24px));
        max-height: min(720px, calc(100dvh - 24px));
        display: grid;
        grid-template-rows: auto minmax(0, 1fr);
        border-radius: 24px;
        overflow: hidden;
        background: var(--md-sys-color-surface-container-high);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 85%, transparent);
        box-shadow: 0 24px 60px color-mix(in srgb, black 38%, transparent);
    }

    .head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 14px 16px;
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
        color: var(--md-sys-color-on-surface);
    }

    .close-btn {
        width: 36px;
        height: 36px;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 86%, transparent);
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        font: inherit;
        font-weight: 700;
    }

    .frame-shell {
        min-height: 0;
        background: color-mix(in srgb, var(--md-sys-color-surface-container) 80%, transparent);
    }

    iframe {
        width: 100%;
        height: min(640px, calc(100dvh - 112px));
        border: 0;
        display: block;
        background: transparent;
    }

    @media (max-width: 640px) {
        .frame-modal {
            padding: 8px;
        }

        .panel {
            width: calc(100vw - 16px);
            max-height: calc(100dvh - 16px);
            border-radius: 20px;
        }

        iframe {
            height: calc(100dvh - 92px);
        }
    }
</style>
