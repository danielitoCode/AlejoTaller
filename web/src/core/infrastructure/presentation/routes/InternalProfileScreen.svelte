<script lang="ts">
    import {onMount} from "svelte";
    import {Button, Card, TextFieldOutlined, TextFieldOutlinedMultiline} from "m3-svelte";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import ImagePicker from "../components/ImagePicker.svelte";
    import {toastStore} from "../viewmodel/toast.store";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {type ProfileDraft, profileStore} from "../../../feature/auth/presentation/viewmodel/profile.store";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;

    let baseline: ProfileDraft | null = null;
    let loading = true;

    $: draft = $profileStore;
    $: hasChanges = baseline !== null && JSON.stringify(draft) !== JSON.stringify(baseline);

    onMount(async () => {
        try {
            const user = await sessionStore.getCurrentUser();
            profileStore.hydrateFromUser(user);

            const initial: ProfileDraft = {
                userId: user.$id ?? "",
                email: user.email ?? "",
                name: user.name ?? "",
                phone: typeof user.prefs?.phone === "string" ? user.prefs.phone : "",
                bio: typeof user.prefs?.bio === "string" ? user.prefs.bio : "",
                avatarUrl: typeof user.prefs?.avatarUrl === "string" ? user.prefs.avatarUrl : ""
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
    });

    function updateField(partial: Partial<ProfileDraft>) {
        profileStore.patch(partial);
    }

    function handleReset() {
        if (!baseline) return;
        profileStore.reset(baseline);
        toastStore.info("Cambios descartados");
    }

    function handleSave() {
        baseline = profileStore.save();
        toastStore.success("Perfil guardado localmente");
    }

    function handleNameInput(event: Event) {
        updateField({ name: (event.target as HTMLInputElement).value });
    }
    function handlePhoneInput(event: Event) {
        updateField({ phone: (event.target as HTMLInputElement).value });
    }
    function handleBioInput(event: Event) {
        updateField({ bio: (event.target as HTMLTextAreaElement).value });
    }
</script>

<section class="screen">
    <div class="hero">
        <p class="eyebrow">Perfil</p>
        <h1>Cuenta</h1>
        <p class="support">Edita tu identidad visible, telefono, bio y foto con una distribucion mas cercana a escritorio.</p>
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
        <div class="profile-layout">
            <div class="avatar-card">
                <Card variant="filled">
                    <div class="card-shell">
                        <div class="card-title">
                            <strong>Foto de perfil</strong>
                            <span>Sube o reemplaza tu imagen principal.</span>
                        </div>
                        <div class="avatar-preview">
                            {#if draft.avatarUrl}
                                <img src={draft.avatarUrl} alt="Avatar de perfil" />
                            {:else}
                                <span>{draft.name?.slice(0, 1) || "A"}</span>
                            {/if}
                        </div>
                        <ImagePicker
                            label="Foto"
                            value={draft.avatarUrl}
                            on:change={(event) => updateField({ avatarUrl: event.detail.url })}
                        />
                    </div>
                </Card>
            </div>

            <div class="form-card">
                <Card variant="filled">
                    <div class="card-shell">
                        <div class="card-title">
                            <strong>Datos personales</strong>
                            <span>Edita nombre visible y telefono de contacto.</span>
                        </div>
                        <div class="field-grid">
                            <TextFieldOutlined
                                label="Nombre"
                                value={draft.name}
                                oninput={handleNameInput}
                            />
                            <TextFieldOutlined
                                label="Telefono"
                                value={draft.phone}
                                oninput={handlePhoneInput}
                            />
                            <TextFieldOutlined label="Email" value={draft.email} disabled={true} />
                            <TextFieldOutlined label="ID de cuenta" value={draft.userId} disabled={true} />
                        </div>
                    </div>
                </Card>
            </div>
        </div>

        <div class="bio-card">
            <Card variant="elevated">
                <div class="card-shell">
                    <div class="card-title">
                        <strong>Bio</strong>
                        <span>Cuenta brevemente que tipo de cliente eres o que te interesa.</span>
                    </div>
                    <TextFieldOutlinedMultiline
                        label="Biografia"
                        rows={6}
                        value={draft.bio}
                        oninput={handleBioInput}
                    />
                </div>
            </Card>
        </div>

        <div class="actions-row">
            <Button variant="outlined" size="m" onclick={handleReset} disabled={!hasChanges}>Resetear cambios</Button>
            <Button variant="filled" size="m" onclick={handleSave} disabled={!hasChanges}>Guardar cambios</Button>
        </div>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 18px;
        align-content: start;
        min-height: 100%;
        grid-template-rows: auto auto minmax(0, 1fr) auto;
        padding-bottom: 8px;
    }

    .eyebrow,
    h1,
    p {
        margin: 0;
    }

    .eyebrow {
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        font-size: 0.78rem;
        font-weight: 800;
        letter-spacing: 0.08em;
    }

    .hero,
    .avatar-card,
    .form-card,
    .bio-card,
    .loading-card {
        border-radius: 28px;
    }

    .hero {
        padding: 18px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }

    .support,
    .card-title span {
        margin-top: 6px;
        color: var(--md-sys-color-on-surface-variant);
    }

    .profile-layout {
        display: grid;
        grid-template-columns: minmax(260px, 0.78fr) minmax(0, 1.22fr);
        gap: 18px;
        align-items: start;
    }

    .card-shell {
        display: grid;
        gap: 16px;
        padding: 18px;
        align-content: start;
    }

    .card-title {
        display: grid;
        gap: 2px;
    }

    .avatar-preview {
        width: min(180px, 100%);
        aspect-ratio: 1 / 1;
        border-radius: 32px;
        overflow: hidden;
        display: grid;
        place-items: center;
        background: linear-gradient(135deg, var(--md-sys-color-primary-container) 0%, var(--md-sys-color-tertiary-container) 100%);
        color: var(--md-sys-color-on-primary-container);
        font-size: 3rem;
        font-weight: 800;
    }

    .avatar-preview img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    .field-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 14px;
    }

    .actions-row {
        display: flex;
        justify-content: flex-end;
        gap: 12px;
        align-items: center;
        padding-bottom: 4px;
    }

    @media (max-width: 1100px) {
        .screen {
            grid-template-rows: auto auto auto auto;
        }

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
        }

        .actions-row :global(button) {
            width: 100%;
        }
    }
</style>
