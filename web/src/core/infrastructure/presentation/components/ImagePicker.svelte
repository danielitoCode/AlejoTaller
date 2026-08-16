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
    export let onchange: ((event: CustomEvent<{ url: string }>) => void) | undefined = undefined;

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

    function closeMenu() { open = false; }

    function setUrl(next: string) {
        value = next;
        const event = new CustomEvent("change", { detail: { url: next } });
        dispatch("change", { url: next });
        onchange?.(event);
    }

    function getUrlFrameSrc(): string {
        const params = new URLSearchParams({ parent_origin: window.location.origin, value: value || "" });
        return `/image-url.html#${params.toString()}`;
    }

    function openUrlFrame() { closeMenu(); urlFrameSrc = getUrlFrameSrc(); urlFrameOpen = true; }
    function pickLocal() { closeMenu(); fileInput?.click(); }

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
        if (target && rootEl && !rootEl.contains(target)) open = false;
    }

    onMount(() => document.addEventListener("click", handleOutsideClick, true));
    onDestroy(() => {
        document.removeEventListener("click", handleOutsideClick, true);
        clearLocal();
    });
</script>

<div class="picker" bind:this={rootEl} aria-label={label}>
    <div class="picker-head">
        <span class="picker-label">{label}{required ? " *" : ""}</span>
        <button class="picker-btn" type="button" onclick={() => (open = !open)} disabled={isDisabled}>
            <div class="picker-btn-main">
                {#if previewSrc && !previewError}
                    <div class="thumb-wrap" aria-hidden="true">
                        {#if previewLoading}<div class="thumb thumb-loading"><div class="thumb-spinner"></div></div>{/if}
                        <img class="thumb" class:ready={!previewLoading} src={previewSrc} alt="" aria-hidden="true" onload={() => { previewLoading = false; previewError = false; }} onerror={() => { previewLoading = false; previewError = true; }} />
                    </div>
                {:else if previewSrc && previewError}
                    <div class="thumb placeholder error" aria-hidden="true"><Icon icon={ImageIcon} size={18} /></div>
                {:else}
                    <div class="thumb placeholder" aria-hidden="true"><Icon icon={ImageIcon} size={18} /></div>
                {/if}
                <span class="picker-btn-text">{previewSrc ? "Cambiar imagen" : "Seleccionar imagen"}</span>
            </div>
            <Icon icon={ChevronDown} size={18} className="chev" ariaLabel="Opciones" />
        </button>
    </div>

    {#if open}
        <div class="menu" role="menu" aria-label="Opciones de imagen">
            <button class="menu-item" type="button" role="menuitem" onclick={openUrlFrame} disabled={isDisabled}><Icon icon={Globe} size={18} ariaLabel="Desde la web" />Desde la web</button>
            <button class="menu-item" type="button" role="menuitem" onclick={pickLocal} disabled={isDisabled}><Icon icon={CloudUpload} size={18} ariaLabel="Desde tu dispositivo" />Desde tu dispositivo</button>
        </div>
    {/if}

    {#if localFile}
        <div class="local-card" aria-label="Preview local">
            <div class="local-preview"><img src={localPreview} alt="Preview" /></div>
            <div class="local-meta">
                <div class="local-name">{localFile.name}</div>
                <div class="local-sub mgmt-muted">{(localFile.size / 1024 / 1024).toFixed(2)} MB · {localFile.type || "image/*"}</div>
                <div class="local-actions">
                    <button class="mgmt-btn ghost" type="button" onclick={clearLocal} disabled={uploading}><Icon icon={X} size={18} ariaLabel="Quitar" />Quitar</button>
                    <button class="mgmt-btn primary" type="button" onclick={uploadLocal} disabled={uploading}><Icon icon={Upload} size={18} ariaLabel="Subir" />{uploading ? "Subiendo…" : "Subir"}</button>
                </div>
            </div>
        </div>
    {/if}

    <input class="file" bind:this={fileInput} type="file" accept="image/*" onchange={handleFileChange} disabled={isDisabled} />
</div>

<FrameModal open={urlFrameOpen} title="Imagen desde la web" ariaLabel="Imagen desde la web" src={urlFrameOpen ? urlFrameSrc : ""} on:close={() => (urlFrameOpen = false)} on:frameMessage={handleFrameMessage} />

<style>
    .picker { display:grid; gap:10px; position:relative; width:100%; }
    .picker-head { display:grid; gap:6px; }
    .picker-label { font-size:.72rem; color:var(--md-sys-color-on-surface-variant); font-weight:700; }
    .picker-btn { width:100%; min-height:50px; border-radius:14px; border:1px solid var(--md-sys-color-outline-variant); background:var(--md-sys-color-surface); cursor:pointer; display:inline-flex; align-items:center; justify-content:space-between; gap:10px; padding:0 11px; font:inherit; transition:transform 120ms ease,box-shadow 160ms ease,border-color 160ms ease,background 160ms ease; }
    .picker-btn:hover { border-color:color-mix(in srgb,var(--md-sys-color-primary) 35%,var(--md-sys-color-outline-variant)); background:var(--md-sys-color-surface-container-high); box-shadow:0 7px 16px color-mix(in srgb,var(--md-sys-color-primary) 7%,transparent); }
    .picker-btn:active { transform:translateY(1px); }
    .picker-btn:disabled { opacity:.6; cursor:not-allowed; }
    .picker-btn-main { display:inline-flex; align-items:center; gap:9px; min-width:0; }
    .picker-btn-text { font-weight:750; color:var(--md-sys-color-on-surface); white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
    .thumb,.thumb-wrap { width:34px; height:34px; border-radius:11px; flex:0 0 auto; }
    .thumb { object-fit:cover; border:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 80%,transparent); background:var(--md-sys-color-surface-variant); }
    .thumb-wrap { position:relative; }
    .thumb-wrap .thumb { position:absolute; inset:0; }
    .thumb-wrap img.thumb { opacity:0; transition:opacity 160ms ease; }
    .thumb-wrap img.thumb.ready { opacity:1; }
    .thumb.placeholder { display:grid; place-items:center; color:var(--md-sys-color-on-surface-variant); }
    .thumb.placeholder.error { background:var(--md-sys-color-error-container); color:var(--md-sys-color-on-error-container); }
    .thumb-loading { display:grid; place-items:center; }
    .thumb-spinner { width:13px; height:13px; border-radius:50%; border:2px solid var(--md-sys-color-outline-variant); border-top-color:var(--md-sys-color-primary); animation:spin .8s linear infinite; }
    .menu { position:absolute; top:73px; left:0; width:min(320px,100%); background:var(--md-sys-color-surface-container-high); border:1px solid var(--md-sys-color-outline-variant); border-radius:16px; box-shadow:0 18px 42px color-mix(in srgb,black 18%,transparent); padding:6px; display:grid; gap:4px; z-index:20; }
    .menu-item { min-height:42px; border-radius:12px; border:1px solid transparent; background:transparent; cursor:pointer; display:inline-flex; align-items:center; gap:10px; padding:0 11px; font:inherit; font-size:.76rem; font-weight:700; color:var(--md-sys-color-on-surface); }
    .menu-item:hover { background:var(--md-sys-color-surface-container-highest); }
    .file { display:none; }
    .local-card { display:grid; grid-template-columns:88px 1fr; gap:11px; align-items:center; padding:9px; border-radius:15px; border:1px solid var(--md-sys-color-outline-variant); background:var(--md-sys-color-surface-container); }
    .local-preview { width:88px; height:68px; border-radius:12px; overflow:hidden; background:var(--md-sys-color-surface-variant); }
    .local-preview img { width:100%; height:100%; object-fit:cover; display:block; }
    .local-meta { min-width:0; display:grid; gap:3px; }
    .local-name { font-size:.7rem; font-weight:800; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .local-sub { font-size:.59rem; }
    .local-actions { display:inline-flex; gap:7px; flex-wrap:wrap; margin-top:5px; }
    @keyframes spin { to { transform:rotate(360deg); } }
    @media (max-width:420px) { .local-card { grid-template-columns:1fr; } .local-preview { width:100%; height:120px; } }
</style>
