<script lang="ts">
    import { createEventDispatcher, onDestroy, onMount } from "svelte";
    import FrameModal from "../../../feature/auth/presentation/components/FrameModal.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import Icon from "./Icon.svelte";
    import { compressImageFile } from "../storage/image-compress";
    import { uploadImageToStorage } from "../storage/image-upload";
    import { ChevronDown, CloudUpload, Globe, Image as ImageIcon, Upload, X } from "lucide-svelte";

    export let label = "Imagen";
    export let value = "";
    export let disabled = false;
    export let required = false;
    export let pending = false;

    const dispatch = createEventDispatcher<{ change: { url: string } }>();

    let open = false;
    let rootEl: HTMLElement | null = null;
    let fileInput: HTMLInputElement | null = null;

    let urlFrameOpen = false;
    let urlFrameSrc = "";

    let localFile: File | null = null;
    let localPreview = "";
    let uploading = false;
    let previewLoading = false;
    let previewError = false;

    $: previewSrc = localPreview || value;
    $: pending = uploading || !!localFile;
    $: isDisabled = disabled || uploading;
    $: if (previewSrc) {
        previewLoading = true;
        previewError = false;
    } else {
        previewLoading = false;
        previewError = false;
    }

    function closeMenu() {
        open = false;
    }

    function setUrl(next: string) {
        value = next;
        dispatch("change", { url: next });
    }

    function getUrlFrameSrc(): string {
        const params = new URLSearchParams({
            parent_origin: window.location.origin,
            value: value || ""
        });
        return `/image-url.html#${params.toString()}`;
    }

    function openUrlFrame() {
        closeMenu();
        urlFrameSrc = getUrlFrameSrc();
        urlFrameOpen = true;
    }

    function pickLocal() {
        closeMenu();
        fileInput?.click();
    }

    function clearLocal() {
        localFile = null;
        if (localPreview) URL.revokeObjectURL(localPreview);
        localPreview = "";
    }

    async function uploadLocal() {
        if (!localFile || uploading) return;
        uploading = true;

        try {
            toastStore.info("Comprimiendo imagen…");
            const compressed = await compressImageFile(localFile, { maxSide: 1600, quality: 0.86, mimeType: "image/webp" });

            toastStore.info("Subiendo a la nube…");
            const url = await uploadImageToStorage(compressed);

            setUrl(url);
            toastStore.success("Imagen subida correctamente.");
            clearLocal();
        } catch (e) {
            toastStore.error(e instanceof Error ? e.message : "No se pudo subir la imagen.");
        } finally {
            uploading = false;
        }
    }

    function handleFrameMessage(event: CustomEvent<{ data: any }>) {
        const data = event.detail.data;

        if (data?.type === "image-url-cancel") urlFrameOpen = false;

        if (data?.type === "image-url-selected" && typeof data.url === "string") {
            urlFrameOpen = false;
            const next = data.url.trim();
            if (!next) return;
            clearLocal();
            setUrl(next);
            toastStore.success("URL de imagen aplicada.");
        }
    }

    function handleFileChange(event: Event) {
        const input = event.currentTarget as HTMLInputElement;
        const file = input.files?.[0] ?? null;
        input.value = "";
        clearLocal();
        if (!file) return;
        localFile = file;
        localPreview = URL.createObjectURL(file);
        toastStore.info("Imagen seleccionada. Sube para guardarla.");
    }

    function handleOutsideClick(event: MouseEvent) {
        if (!open) return;
        const target = event.target as Node | null;
        if (!target) return;
        if (rootEl && !rootEl.contains(target)) open = false;
    }

    onMount(() => {
        document.addEventListener("click", handleOutsideClick, true);
    });

    onDestroy(() => {
        document.removeEventListener("click", handleOutsideClick, true);
        clearLocal();
    });
</script>

<div class="picker" bind:this={rootEl} aria-label={label}>
    <div class="picker-head">
        <span class="picker-label">
            {label}{required ? " *" : ""}
        </span>

        <button class="picker-btn" type="button" on:click={() => (open = !open)} disabled={isDisabled}>
            <div class="picker-btn-main">
                {#if previewSrc && !previewError}
                    <div class="thumb-wrap" aria-hidden="true">
                        {#if previewLoading}
                            <div class="thumb thumb-loading">
                                <div class="thumb-spinner"></div>
                            </div>
                        {/if}
                        <img
                            class="thumb"
                            class:ready={!previewLoading}
                            src={previewSrc}
                            alt=""
                            aria-hidden="true"
                            on:load={() => {
                                previewLoading = false;
                                previewError = false;
                            }}
                            on:error={() => {
                                previewLoading = false;
                                previewError = true;
                            }}
                        />
                    </div>
                {:else if previewSrc && previewError}
                    <div class="thumb placeholder error" aria-hidden="true">
                        <Icon icon={ImageIcon} size={18} />
                    </div>
                {:else}
                    <div class="thumb placeholder" aria-hidden="true">
                        <Icon icon={ImageIcon} size={18} />
                    </div>
                {/if}
                <span class="picker-btn-text">{previewSrc ? "Cambiar imagen" : "Seleccionar imagen"}</span>
            </div>
            <Icon icon={ChevronDown} size={18} className="chev" ariaLabel="Opciones" />
        </button>
    </div>

    {#if open}
        <div class="menu" role="menu" aria-label="Opciones de imagen">
            <button class="menu-item" type="button" role="menuitem" on:click={openUrlFrame} disabled={isDisabled}>
                <Icon icon={Globe} size={18} ariaLabel="Desde la web" />
                Desde la web
            </button>
            <button class="menu-item" type="button" role="menuitem" on:click={pickLocal} disabled={isDisabled}>
                <Icon icon={CloudUpload} size={18} ariaLabel="Desde tu dispositivo" />
                Desde tu dispositivo
            </button>
        </div>
    {/if}

    {#if localFile}
        <div class="local-card" aria-label="Preview local">
            <div class="local-preview">
                <img src={localPreview} alt="Preview" />
            </div>
            <div class="local-meta">
                <div class="local-name">{localFile.name}</div>
                <div class="local-sub mgmt-muted">
                    {(localFile.size / 1024 / 1024).toFixed(2)} MB · {localFile.type || "image/*"}
                </div>
                <div class="local-actions">
                    <button class="mgmt-btn ghost" type="button" on:click={clearLocal} disabled={uploading}>
                        <Icon icon={X} size={18} ariaLabel="Quitar" />
                        Quitar
                    </button>
                    <button class="mgmt-btn primary" type="button" on:click={uploadLocal} disabled={uploading}>
                        <Icon icon={Upload} size={18} ariaLabel="Subir" />
                        {uploading ? "Subiendo…" : "Subir"}
                    </button>
                </div>
            </div>
        </div>
    {/if}

    <input
        class="file"
        bind:this={fileInput}
        type="file"
        accept="image/*"
        on:change={handleFileChange}
        disabled={isDisabled}
    />
</div>

<FrameModal
    open={urlFrameOpen}
    title="Imagen desde la web"
    ariaLabel="Imagen desde la web"
    src={urlFrameOpen ? urlFrameSrc : ""}
    on:close={() => (urlFrameOpen = false)}
    on:frameMessage={handleFrameMessage}
/>

<style>
    .picker {
        display: grid;
        gap: 10px;
        position: relative;
        width: 100%;
    }

    .picker-head {
        display: grid;
        gap: 6px;
    }

    .picker-label {
        font-size: 0.9rem;
        color: var(--md-sys-color-on-surface-variant);
        font-weight: 600;
    }

    .picker-btn {
        width: 100%;
        height: 54px;
        border-radius: 14px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, var(--md-sys-color-surface-variant));
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        padding: 0 12px;
        transition: transform 120ms ease, box-shadow 160ms ease, border-color 160ms ease, filter 160ms ease;
    }

    .picker-btn:active {
        transform: translateY(1px);
    }

    .picker-btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .picker-btn:hover {
        filter: brightness(1.02);
        box-shadow: 0 10px 18px color-mix(in srgb, var(--md-sys-color-outline) 12%, transparent);
    }

    .picker-btn-main {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        min-width: 0;
    }

    .picker-btn-text {
        font-weight: 750;
        color: var(--md-sys-color-on-surface);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    .thumb {
        width: 36px;
        height: 36px;
        border-radius: 12px;
        object-fit: cover;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        background: var(--md-sys-color-surface-variant);
        flex: 0 0 auto;
    }

    .thumb-wrap {
        width: 36px;
        height: 36px;
        position: relative;
        flex: 0 0 auto;
    }

    .thumb-wrap .thumb {
        position: absolute;
        inset: 0;
    }

    .thumb.ready {
        opacity: 1;
    }

    .thumb-wrap img.thumb {
        opacity: 0;
        transition: opacity 160ms ease;
    }

    .thumb.placeholder {
        display: grid;
        place-items: center;
        color: var(--md-sys-color-on-surface-variant);
    }

    .thumb.placeholder.error {
        background: color-mix(in srgb, var(--md-sys-color-error-container) 82%, transparent);
        color: var(--md-sys-color-on-error-container);
    }

    .thumb-loading {
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 82%, transparent);
    }

    .thumb-spinner {
        width: 14px;
        height: 14px;
        border-radius: 50%;
        border: 2px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        border-top-color: var(--md-sys-color-primary);
        animation: thumb-spin 0.8s linear infinite;
    }

    @keyframes thumb-spin {
        to {
            transform: rotate(360deg);
        }
    }

    .menu {
        position: absolute;
        top: 76px;
        left: 0;
        width: min(320px, 100%);
        background: var(--md-sys-color-surface);
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        box-shadow: 0 18px 42px color-mix(in srgb, black 22%, transparent);
        padding: 6px;
        display: grid;
        gap: 6px;
        z-index: 20;
    }

    .menu-item {
        height: 44px;
        border-radius: 14px;
        border: 1px solid transparent;
        background: transparent;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 10px;
        padding: 0 12px;
        font: inherit;
        font-weight: 700;
        color: var(--md-sys-color-on-surface);
    }

    .menu-item:hover {
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 40%, transparent);
    }

    .file {
        display: none;
    }

    .local-card {
        display: grid;
        grid-template-columns: 96px 1fr;
        gap: 12px;
        align-items: center;
        padding: 10px;
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 86%, transparent);
    }

    .local-preview {
        width: 96px;
        height: 72px;
        border-radius: 14px;
        overflow: hidden;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        background: var(--md-sys-color-surface-variant);
    }

    .local-preview img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        display: block;
    }

    .local-meta {
        min-width: 0;
        display: grid;
        gap: 4px;
    }

    .local-name {
        font-weight: 800;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .local-actions {
        display: inline-flex;
        gap: 10px;
        flex-wrap: wrap;
        margin-top: 6px;
    }
</style>
