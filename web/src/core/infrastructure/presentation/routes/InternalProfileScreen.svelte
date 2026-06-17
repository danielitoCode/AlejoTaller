<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card, TextFieldOutlined, TextFieldOutlinedMultiline } from "m3-svelte";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import ImagePicker from "../components/ImagePicker.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { type ProfileDraft, profileStore } from "../../../feature/auth/presentation/viewmodel/profile.store";
    import { authContainer } from "../../../feature/auth/di/auth.container";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;

    $: void navController;
    $: void navBackStackEntry;

    let baseline: ProfileDraft | null = null;
    let loading = true;
    let saving = false;
    let avatarLoading = false;
    let avatarError = false;

    $: draft = $profileStore;
    $: hasChanges = baseline !== null && JSON.stringify(draft) !== JSON.stringify(baseline);

    $: if (draft.avatarUrl) {
        avatarLoading = true;
        avatarError = false;
    } else {
        avatarLoading = false;
        avatarError = false;
    }

    async function hydrateProfile() {
        try {
            const user = await sessionStore.getCurrentUser();
            profileStore.hydrateFromUser(user);

            const initial: ProfileDraft = {
                userId: user.$id ?? "",
                email: user.email ?? "",
                name: user.name ?? "",
                phone: typeof user.prefs?.phone === "string" ? user.prefs.phone : "",
                bio: typeof user.prefs?.bio === "string" ? user.prefs.bio : "",
                avatarUrl: typeof user.prefs?.avatarUrl === "string"
                    ? user.prefs.avatarUrl
                    : typeof user.prefs?.photo_url === "string"
                        ? user.prefs.photo_url
                        : typeof user.prefs?.photoUrl === "string"
                            ? user.prefs.photoUrl
                            : ""
            };

            if (typeof window !== "undefined" && initial.userId) {
                const raw = window.localStorage.getItem(`alejo-taller-web-profile:${initial.userId}`);
                if (raw) {
                    baseline = { ...initial, ...JSON.parse(raw) } as ProfileDraft;
                    profileStore.reset(baseline);
                } else {
                    baseline = initial;
                }
            } else {
                baseline = initial;
            }
        } catch (error) {
            toastStore.error(error instanceof Error ? error.message : "No se pudo cargar el perfil");
        } finally {
            loading = false;
        }
    }

    onMount(() => {
        void hydrateProfile();
        const handleOnline = () => void hydrateProfile();
        window.addEventListener("online", handleOnline);
        return () => window.removeEventListener("online", handleOnline);
    });

    function updateField(partial: Partial<ProfileDraft>) {
        profileStore.patch(partial);
    }

    function handleReset() {
        if (!baseline) return;
        profileStore.reset(baseline);
        toastStore.info("Cambios descartados");
    }

    async function handleSave() {
        if (!baseline || saving) return;
        saving = true;
        try {
            if (draft.name.trim() !== baseline.name.trim()) {
                await authContainer.useCases.accounts.updateName(draft.name.trim());
            }
            if (draft.phone.trim() !== baseline.phone.trim()) {
                await authContainer.useCases.accounts.updatePhone(draft.phone.trim());
            }
            if (draft.avatarUrl.trim() !== baseline.avatarUrl.trim()) {
                await authContainer.useCases.accounts.updatePhotoUrl(draft.avatarUrl.trim());
            }

            baseline = profileStore.save();
            toastStore.success("Perfil actualizado correctamente");
            await hydrateProfile();
        } catch (error) {
            toastStore.error(error instanceof Error ? error.message : "No se pudo guardar el perfil");
        } finally {
            saving = false;
        }
    }

    function handleNameInput(event: InputEvent) {
        const target = event.target as HTMLInputElement | null;
        if (!target) return;
        updateField({ name: target.value });
    }

    function handlePhoneInput(event: InputEvent) {
        const target = event.target as HTMLInputElement | null;
        if (!target) return;
        updateField({ phone: target.value });
    }

    function handleBioInput(event: InputEvent) {
        const target = event.target as HTMLTextAreaElement | null;
        if (!target) return;
        updateField({ bio: target.value });
    }
</script>

<section class="screen profile-screen">
    <div class="hero">
        <p class="eyebrow">Perfil</p>
        <h1>Mi perfil</h1>
        <p class="support">Gestiona tu información personal y cómo apareces ante otros usuarios.</p>
    </div>

    {#if loading}
        <div class="loading-card">
            <Card variant="filled">
                <div class="card-shell">
                    <p>Cargando perfil...</p>
                </div>
            </Card>
        </div>
    {:else}
        <div class="main-content">
            <div class="profile-layout">
                <!-- Avatar -->
                <div class="avatar-card">
                    <Card variant="filled">
                        <div class="card-shell">
                            <div class="card-title">
                                <strong>Foto de perfil</strong>
                                <span>Esta imagen será visible para otros usuarios.</span>
                            </div>
                            <div class="avatar-preview">
                                {#if draft.avatarUrl && !avatarError}
                                    {#if avatarLoading}
                                        <div class="avatar-state loading">
                                            <div class="avatar-spinner"></div>
                                            <span>Cargando foto...</span>
                                        </div>
                                    {/if}
                                    <img
                                            class:loaded={!avatarLoading}
                                            src={draft.avatarUrl}
                                            alt="Avatar de perfil"
                                            on:load={() => { avatarLoading = false; avatarError = false; }}
                                            on:error={() => { avatarLoading = false; avatarError = true; }}
                                    />
                                {:else if draft.avatarUrl && avatarError}
                                    <div class="avatar-state error">
                                        <strong>Sin vista previa</strong>
                                        <span>No se pudo cargar la imagen.</span>
                                    </div>
                                {:else}
                                    <span>{draft.name?.slice(0, 1) || "A"}</span>
                                {/if}
                            </div>
                            <ImagePicker
                                    label="Cambiar foto"
                                    value={draft.avatarUrl}
                                    on:change={(event) => updateField({ avatarUrl: event.detail.url })}
                            />
                        </div>
                    </Card>
                </div>

                <!-- Datos Personales - Rediseño Profesional -->
                <div class="form-card">
                    <Card variant="filled">
                        <div class="card-shell">
                            <div class="card-title">
                                <strong>Datos personales</strong>
                                <span>Información visible y de contacto.</span>
                            </div>

                            <!-- Campos editables -->
                            <div class="editable-section">
                                <div class="section-label">Información editable</div>
                                <div class="field-grid">
                                    <div class="field-cell">
                                        <TextFieldOutlined label="Nombre completo" value={draft.name} oninput={handleNameInput} />
                                    </div>
                                    <div class="field-cell">
                                        <TextFieldOutlined label="Teléfono" value={draft.phone} oninput={handlePhoneInput} />
                                    </div>
                                </div>
                            </div>

                            <!-- Divider -->
                            <div class="divider"></div>

                            <!-- Campos de solo lectura -->
                            <div class="readonly-section">
                                <div class="section-label">Información de cuenta</div>
                                <div class="field-grid">
                                    <div class="field-cell">
                                        <TextFieldOutlined label="Correo electrónico" value={draft.email} disabled={true} />
                                    </div>
                                    <div class="field-cell">
                                        <TextFieldOutlined label="ID de cuenta" value={draft.userId} disabled={true} />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Card>
                </div>
            </div>

            <!-- Biografía -->
            <div class="bio-card">
                <Card variant="elevated">
                    <div class="card-shell">
                        <div class="card-title">
                            <strong>Biografía</strong>
                            <span>Cuéntanos brevemente sobre ti o tu negocio (visible para otros).</span>
                        </div>
                        <TextFieldOutlinedMultiline label="Biografía" rows={5} value={draft.bio} oninput={handleBioInput} />
                    </div>
                </Card>
            </div>
        </div>

        <!-- Acciones -->
        <div class="actions-row">
            <Button variant="outlined" size="m" onclick={handleReset} disabled={!hasChanges || saving}>
                Descartar cambios
            </Button>
            <Button variant="filled" size="m" onclick={handleSave} disabled={!hasChanges || saving}>
                {saving ? "Guardando cambios..." : "Guardar cambios"}
            </Button>
        </div>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 24px;
        align-content: start;
        min-height: 100%;
        padding-bottom: 80px;
    }

    .main-content {
        display: grid;
        gap: 24px;
    }

    .profile-layout {
        display: grid;
        grid-template-columns: minmax(280px, 0.75fr) minmax(0, 1.25fr);
        gap: 24px;
        align-items: start;
    }

    .card-shell {
        display: grid;
        gap: 28px;
        padding: 24px;
    }

    .card-title {
        display: grid;
        gap: 4px;
    }

    /* ==================== SECCIÓN DATOS PERSONALES ==================== */
    .editable-section,
    .readonly-section {
        display: grid;
        gap: 12px;
    }

    .section-label {
        font-size: 0.85rem;
        font-weight: 600;
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .field-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
        gap: 20px;
        width: 100%;
    }

    .field-cell {
        display: flex;
        width: 100%;
        min-width: 0;
    }

    .field-cell > :global(*) {
        flex: 1 1 100%;
        width: 100% !important;
        min-width: 0 !important;
        max-width: 100% !important;
        box-sizing: border-box;
    }

    .field-cell :global(.md-outlined-text-field) {
        width: 100% !important;
    }

    .field-cell :global(.md-outlined-text-field[disabled]) {
        background-color: color-mix(in srgb, var(--md-sys-color-surface-container) 80%, transparent) !important;
        opacity: 0.9;
    }

    .divider {
        height: 1px;
        background: color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);
        margin: 8px 0;
    }

    /* Avatar */
    .avatar-preview {
        width: min(200px, 100%);
        aspect-ratio: 1 / 1;
        border-radius: 32px;
        overflow: hidden;
        display: grid;
        place-items: center;
        background: linear-gradient(135deg, var(--md-sys-color-primary-container), var(--md-sys-color-tertiary-container));
        color: var(--md-sys-color-on-primary-container);
        font-size: 3.5rem;
        font-weight: 700;
        position: relative;
    }

    .avatar-preview img {
        width: 100%;
        height: 100%;
        object-fit: cover;
        opacity: 0;
        transition: opacity 0.2s ease;
    }

    .avatar-preview img.loaded {
        opacity: 1;
    }

    .avatar-state {
        position: absolute;
        inset: 0;
        display: grid;
        place-items: center;
        gap: 12px;
        padding: 20px;
        text-align: center;
        background: rgba(0, 0, 0, 0.65);
        color: white;
    }

    .bio-card :global(.md-outlined-text-field-multiline) {
        width: 100% !important;
    }

    .actions-row {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        padding-top: 8px;
    }

    /* ==================== RESPONSIVE ==================== */
    @media (max-width: 900px) {
        .profile-layout {
            grid-template-columns: 1fr;
        }
    }

    @media (max-width: 720px) {
        .field-grid {
            grid-template-columns: 1fr;
        }
        .actions-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
        }
        .actions-row :global(button) {
            width: 100%;
        }
    }

    @media (max-width: 480px) {
        .card-shell {
            padding: 20px;
        }
        .field-grid {
            gap: 16px;
        }
    }
</style>