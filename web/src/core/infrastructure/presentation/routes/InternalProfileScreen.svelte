<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card, TextFieldOutlinedMultiline } from "m3-svelte";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import ImagePicker from "../components/ImagePicker.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { type ProfileDraft, profileStore } from "../../../feature/auth/presentation/viewmodel/profile.store";
    import { authContainer } from "../../../feature/auth/di/auth.container";
    import { User, Mail, Phone, ShieldCheck, Sparkles, CircleUserRound } from "lucide-svelte";

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
        if (target) updateField({ name: target.value });
    }

    function handlePhoneInput(event: InputEvent) {
        const target = event.target as HTMLInputElement | null;
        if (target) updateField({ phone: target.value });
    }

    function handleBioInput(event: InputEvent) {
        const target = event.target as HTMLTextAreaElement | null;
        if (target) updateField({ bio: target.value });
    }
</script>

<section class="screen profile-screen">
    {#if loading}
        <div class="state-card">
            <div class="state-icon"><CircleUserRound size={24} /></div>
            <strong>Cargando tu perfil</strong>
            <span>Estamos preparando tu información personal…</span>
            <div class="spinner" aria-hidden="true"></div>
        </div>
    {:else}
        <header class="profile-header">
            <div class="header-copy">
                <div class="eyebrow-row">
                    <User size={16} />
                    <span>Cuenta personal</span>
                </div>
                <h1>Mi perfil</h1>
                <p>Gestiona tu información, tu foto y los datos con los que te identificas en la tienda.</p>
            </div>
            {#if hasChanges}
                <div class="unsaved-badge">
                    <span></span>
                    Cambios sin guardar
                </div>
            {/if}
        </header>

        <div class="profile-grid">
            <aside class="identity-column">
                <Card variant="filled">
                    <div class="identity-card">
                        <div class="identity-glow" aria-hidden="true"></div>
                        <div class="avatar-wrap">
                            <div class="avatar-preview">
                                {#if draft.avatarUrl && !avatarError}
                                    {#if avatarLoading}
                                        <div class="avatar-loading"><div class="avatar-spinner"></div></div>
                                    {/if}
                                    <img
                                        class:loaded={!avatarLoading}
                                        src={draft.avatarUrl}
                                        alt="Avatar de perfil"
                                        onload={() => { avatarLoading = false; avatarError = false; }}
                                        onerror={() => { avatarLoading = false; avatarError = true; }}
                                    />
                                {:else if draft.avatarUrl && avatarError}
                                    <div class="avatar-fallback error"><CircleUserRound size={42} /></div>
                                {:else}
                                    <span>{draft.name?.slice(0, 1).toUpperCase() || "A"}</span>
                                {/if}
                            </div>
                            <div class="avatar-status" title="Perfil activo"></div>
                        </div>

                        <div class="identity-copy">
                            <strong>{draft.name || "Tu nombre"}</strong>
                            <span>{draft.email}</span>
                        </div>

                        <div class="identity-divider"></div>

                        <div class="identity-meta">
                            <div class="meta-item">
                                <span class="meta-icon"><ShieldCheck size={16} /></span>
                                <div><strong>Cuenta verificada</strong><small>Información protegida</small></div>
                            </div>
                            <div class="meta-item">
                                <span class="meta-icon"><Mail size={16} /></span>
                                <div><strong>Correo principal</strong><small>Usado para tu cuenta</small></div>
                            </div>
                        </div>

                        <ImagePicker
                            label="Foto de perfil"
                            value={draft.avatarUrl}
                            onchange={(event) => updateField({ avatarUrl: event.detail.url })}
                        />
                    </div>
                </Card>

                <div class="profile-tip">
                    <div class="tip-icon"><Sparkles size={17} /></div>
                    <div><strong>Mantén tu perfil actualizado</strong><span>Una información clara facilita la atención de soporte y la gestión de tus pedidos.</span></div>
                </div>
            </aside>

            <main class="details-column">
                <Card variant="filled">
                    <div class="content-card contact-card">
                        <header class="section-heading">
                            <div class="section-title-wrap">
                                <span class="section-kicker">Información personal</span>
                                <div class="section-title-line">
                                    <span class="section-icon"><User size={17} /></span>
                                    <h2>Datos de contacto</h2>
                                </div>
                                <p>Actualiza los datos que puedes modificar desde tu cuenta.</p>
                            </div>
                            <span class="section-index">01</span>
                        </header>

                        <div class="field-panel editable-panel">
                            <div class="field-grid">
                                <label class="m3-field" for="profile-name">
                                    <span class="field-label"><User size={14} /> Nombre completo</span>
                                    <input id="profile-name" class="m3-input" type="text" value={draft.name} oninput={handleNameInput} autocomplete="name" />
                                </label>

                                <label class="m3-field" for="profile-phone">
                                    <span class="field-label"><Phone size={14} /> Teléfono</span>
                                    <input id="profile-phone" class="m3-input" type="tel" value={draft.phone} oninput={handlePhoneInput} autocomplete="tel" />
                                </label>
                            </div>
                        </div>
                    </div>
                </Card>

                <Card variant="filled">
                    <div class="content-card account-card">
                        <header class="section-heading">
                            <div class="section-title-wrap">
                                <span class="section-kicker">Cuenta</span>
                                <div class="section-title-line">
                                    <span class="section-icon account-icon"><ShieldCheck size={17} /></span>
                                    <h2>Datos de acceso</h2>
                                </div>
                                <p>Información vinculada a tu cuenta. Estos datos no pueden editarse aquí.</p>
                            </div>
                            <span class="readonly-label"><ShieldCheck size={13} /> Solo lectura</span>
                        </header>

                        <div class="field-panel readonly-panel">
                            <div class="field-grid">
                                <label class="m3-field readonly-field" for="profile-email">
                                    <span class="field-label readonly"><Mail size={14} /> Correo electrónico</span>
                                    <input id="profile-email" class="m3-input" type="email" value={draft.email} disabled autocomplete="email" />
                                </label>

                                <label class="m3-field readonly-field" for="profile-user-id">
                                    <span class="field-label readonly"><ShieldCheck size={14} /> ID de cuenta</span>
                                    <input id="profile-user-id" class="m3-input" type="text" value={draft.userId} disabled />
                                </label>
                            </div>
                        </div>
                    </div>
                </Card>

                <Card variant="elevated">
                    <div class="content-card bio-content">
                        <header class="section-heading">
                            <div class="section-title-wrap">
                                <span class="section-kicker">Sobre ti</span>
                                <div class="section-title-line">
                                    <span class="section-icon"><User size={17} /></span>
                                    <h2>Biografía</h2>
                                </div>
                                <p>Un pequeño espacio para contar algo sobre ti o tu negocio.</p>
                            </div>
                            <span class="section-index">02</span>
                        </header>
                        <TextFieldOutlinedMultiline label="Biografía" rows={5} value={draft.bio} oninput={handleBioInput} />
                    </div>
                </Card>
            </main>
        </div>

        <footer class="actions-bar">
            <div class="actions-status">
                {#if hasChanges}
                    <span class="status-dot"></span>
                    <div><strong>Tienes cambios pendientes</strong><small>Guarda para actualizar tu perfil.</small></div>
                {:else}
                    <span class="saved-icon"><ShieldCheck size={15} /></span>
                    <div><strong>Perfil actualizado</strong><small>No hay cambios pendientes.</small></div>
                {/if}
            </div>
            <div class="actions">
                <Button variant="outlined" size="m" onclick={handleReset} disabled={!hasChanges || saving}>Descartar</Button>
                <Button variant="filled" size="m" onclick={handleSave} disabled={!hasChanges || saving}>
                    {saving ? "Guardando…" : "Guardar cambios"}
                </Button>
            </div>
        </footer>
    {/if}
</section>

<style>
    .screen {
        --profile-surface: var(--m3c-surface-container, var(--md-sys-color-surface-container));
        --profile-surface-high: var(--m3c-surface-container-high, var(--md-sys-color-surface-container-high));
        --profile-surface-highest: var(--m3c-surface-container-highest, var(--md-sys-color-surface-container-highest));
        --profile-on: var(--m3c-on-surface, var(--md-sys-color-on-surface));
        --profile-on-variant: var(--m3c-on-surface-variant, var(--md-sys-color-on-surface-variant));
        --profile-primary: var(--m3c-primary, var(--md-sys-color-primary));
        --profile-primary-container: var(--m3c-primary-container, var(--md-sys-color-primary-container));
        --profile-on-primary-container: var(--m3c-on-primary-container, var(--md-sys-color-on-primary-container));
        --profile-secondary: var(--m3c-secondary, var(--md-sys-color-secondary));
        --profile-outline: var(--m3c-outline-variant, var(--md-sys-color-outline-variant));
        display: grid;
        gap: 18px;
        align-content: start;
        width: min(100%, 1080px);
        margin: 0 auto;
        padding: 12px 18px 38px;
        min-height: 100%;
        box-sizing: border-box;
    }

    .profile-header { display:flex; align-items:flex-end; justify-content:space-between; gap:20px; padding:4px 2px 2px; }
    .header-copy { min-width:0; }
    .eyebrow-row { display:flex; align-items:center; gap:6px; color:var(--profile-primary); margin-bottom:5px; }
    .eyebrow-row span { font-size:.68rem; font-weight:800; letter-spacing:.08em; text-transform:uppercase; }
    .header-copy h1 { margin:0; color:var(--profile-on); font-size:1.5rem; line-height:1.1; font-weight:850; letter-spacing:-.035em; }
    .header-copy p { margin:6px 0 0; max-width:62ch; color:var(--profile-on-variant); font-size:.8rem; line-height:1.45; }
    .unsaved-badge { flex:0 0 auto; display:flex; align-items:center; gap:7px; padding:7px 10px; border-radius:999px; border:1px solid color-mix(in srgb,var(--profile-primary) 24%,var(--profile-outline)); background:color-mix(in srgb,var(--profile-primary-container) 55%,var(--profile-surface)); color:var(--profile-on-primary-container); font-size:.65rem; font-weight:750; }
    .unsaved-badge span,.status-dot { width:7px; height:7px; border-radius:50%; background:var(--profile-primary); box-shadow:0 0 0 3px color-mix(in srgb,var(--profile-primary) 15%,transparent); }

    .profile-grid { display:grid; grid-template-columns:minmax(270px,.72fr) minmax(0,1.28fr); gap:16px; align-items:start; }
    .identity-column,.details-column { display:grid; gap:16px; min-width:0; }
    .identity-column :global(.md-card),.details-column :global(.md-card) { border:1px solid var(--profile-outline); border-radius:24px; overflow:hidden; }

    .identity-card { position:relative; display:grid; gap:17px; padding:20px; overflow:hidden; background:radial-gradient(circle at 8% 0%,color-mix(in srgb,var(--profile-primary) 13%,transparent),transparent 36%); }
    .identity-glow { position:absolute; width:180px; height:180px; right:-80px; top:-100px; border-radius:50%; background:color-mix(in srgb,var(--profile-secondary) 10%,transparent); filter:blur(24px); pointer-events:none; }
    .avatar-wrap { position:relative; width:112px; height:112px; margin:2px auto 0; }
    .avatar-preview { width:100%; height:100%; display:grid; place-items:center; border-radius:34px; overflow:hidden; background:linear-gradient(135deg,var(--profile-primary-container),color-mix(in srgb,var(--profile-secondary) 25%,var(--profile-primary-container))); color:var(--profile-on-primary-container); font-size:3rem; font-weight:850; box-shadow:0 12px 26px color-mix(in srgb,var(--profile-primary) 13%,transparent),inset 0 1px 0 color-mix(in srgb,white 24%,transparent); }
    .avatar-preview img { width:100%; height:100%; object-fit:cover; opacity:0; transition:opacity .2s ease; }
    .avatar-preview img.loaded { opacity:1; }
    .avatar-loading { position:absolute; inset:0; display:grid; place-items:center; background:color-mix(in srgb,var(--profile-surface) 35%,transparent); z-index:2; }
    .avatar-spinner { width:23px; height:23px; border-radius:50%; border:2px solid color-mix(in srgb,var(--profile-outline) 75%,transparent); border-top-color:var(--profile-primary); animation:spin .7s linear infinite; }
    .avatar-fallback { width:100%; height:100%; display:grid; place-items:center; }
    .avatar-status { position:absolute; right:-2px; bottom:3px; width:14px; height:14px; border-radius:50%; background:var(--profile-primary); border:4px solid var(--profile-surface); box-shadow:0 0 0 2px color-mix(in srgb,var(--profile-primary) 14%,transparent); }
    .identity-copy { display:grid; gap:4px; text-align:center; }
    .identity-copy strong { color:var(--profile-on); font-size:1.02rem; font-weight:800; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .identity-copy span { color:var(--profile-on-variant); font-size:.7rem; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
    .identity-divider { height:1px; background:color-mix(in srgb,var(--profile-outline) 62%,transparent); }
    .identity-meta { display:grid; gap:10px; }
    .meta-item { display:flex; align-items:center; gap:9px; min-width:0; }
    .meta-icon { flex:0 0 auto; width:31px; height:31px; display:grid; place-items:center; border-radius:10px; background:var(--profile-surface-highest); color:var(--profile-primary); }
    .meta-item div { display:grid; gap:1px; min-width:0; }
    .meta-item strong { color:var(--profile-on); font-size:.68rem; font-weight:750; }
    .meta-item small { color:var(--profile-on-variant); font-size:.6rem; }

    .profile-tip { display:flex; align-items:flex-start; gap:10px; padding:13px 14px; border:1px solid color-mix(in srgb,var(--profile-primary) 16%,var(--profile-outline)); border-radius:17px; background:color-mix(in srgb,var(--profile-primary-container) 35%,var(--profile-surface)); }
    .tip-icon { flex:0 0 auto; width:30px; height:30px; display:grid; place-items:center; border-radius:10px; background:var(--profile-primary-container); color:var(--profile-on-primary-container); }
    .profile-tip div:last-child { display:grid; gap:2px; min-width:0; }
    .profile-tip strong { color:var(--profile-on); font-size:.67rem; }
    .profile-tip span { color:var(--profile-on-variant); font-size:.61rem; line-height:1.4; }

    .content-card { display:grid; gap:18px; padding:20px; }
    .contact-card,.account-card { min-width:0; }
    .bio-content { gap:17px; }
    .section-heading { display:flex; align-items:flex-start; justify-content:space-between; gap:14px; }
    .section-title-wrap { display:grid; gap:5px; min-width:0; }
    .section-title-line { display:flex; align-items:center; gap:9px; min-width:0; }
    .section-icon { flex:0 0 auto; width:31px; height:31px; display:grid; place-items:center; border-radius:10px; background:var(--profile-primary-container); color:var(--profile-on-primary-container); }
    .section-icon.account-icon { background:color-mix(in srgb,var(--profile-secondary) 18%,var(--profile-primary-container)); color:var(--profile-primary); }
    .section-kicker { color:var(--profile-primary); font-size:.61rem; font-weight:850; letter-spacing:.09em; text-transform:uppercase; }
    .section-title-line h2 { margin:0; color:var(--profile-on); font-size:1rem; line-height:1.2; font-weight:800; letter-spacing:-.015em; }
    .section-heading p { margin:0 0 0 40px; color:var(--profile-on-variant); font-size:.68rem; line-height:1.4; max-width:58ch; }
    .section-index { flex:0 0 auto; min-width:28px; height:24px; display:grid; place-items:center; border-radius:8px; background:var(--profile-surface-highest); color:var(--profile-on-variant); font-size:.59rem; font-weight:800; }
    .readonly-label { flex:0 0 auto; display:inline-flex; align-items:center; gap:4px; color:var(--profile-on-variant); font-size:.59rem; font-weight:700; padding:5px 7px; border-radius:999px; background:var(--profile-surface-highest); white-space:nowrap; }

    .field-panel { border:1px solid var(--profile-outline); border-radius:18px; padding:15px; background:var(--profile-surface-high); }
    .editable-panel { background:color-mix(in srgb,var(--profile-primary-container) 18%,var(--profile-surface-high)); border-color:color-mix(in srgb,var(--profile-primary) 13%,var(--profile-outline)); }
    .readonly-panel { background:color-mix(in srgb,var(--profile-surface-highest) 58%,var(--profile-surface)); }
    .field-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:18px; }

    /* Native HTML fields styled with the same M3 tokens. This keeps the label/value hierarchy deterministic. */
    .m3-field { display:grid; gap:8px; min-width:0; width:100%; cursor:text; }
    .field-label { display:flex; align-items:center; gap:6px; min-height:18px; padding:0 2px; color:var(--profile-on-variant); font-size:.67rem; font-weight:750; line-height:1.2; }
    .field-label :global(svg) { flex:0 0 auto; color:var(--profile-primary); }
    .field-label.readonly { color:var(--profile-on-variant); }
    .field-label.readonly :global(svg) { color:var(--profile-secondary); }
    .m3-input { display:block; width:100%; min-width:0; height:48px; box-sizing:border-box; padding:0 14px; border:1px solid var(--profile-outline); border-radius:12px; outline:none; background:var(--profile-surface); color:var(--profile-on); font:inherit; font-size:.76rem; line-height:48px; transition:border-color .18s ease,box-shadow .18s ease,background .18s ease; }
    .m3-input:hover { border-color:var(--profile-on-variant); }
    .m3-input:focus { border:2px solid var(--profile-primary); padding-inline:13px; box-shadow:0 0 0 1px color-mix(in srgb,var(--profile-primary) 12%,transparent); }
    .m3-input:disabled { cursor:not-allowed; border-color:color-mix(in srgb,var(--profile-outline) 72%,transparent); background:color-mix(in srgb,var(--profile-surface-highest) 72%,var(--profile-surface)); color:var(--profile-on-variant); opacity:1; }
    .readonly-field .m3-input { font-size:.72rem; }

    .bio-content :global(.md-outlined-text-field-multiline) { width:100% !important; }

    .actions-bar { position:sticky; bottom:10px; z-index:5; display:flex; align-items:center; justify-content:space-between; gap:14px; padding:11px 12px 11px 15px; border:1px solid var(--profile-outline); border-radius:19px; background:color-mix(in srgb,var(--profile-surface-high) 91%,transparent); backdrop-filter:blur(18px); box-shadow:0 14px 34px color-mix(in srgb,black 10%,transparent); }
    .actions-status { display:flex; align-items:center; gap:9px; min-width:0; }
    .actions-status div { display:grid; gap:1px; min-width:0; }
    .actions-status strong { color:var(--profile-on); font-size:.65rem; font-weight:750; }
    .actions-status small { color:var(--profile-on-variant); font-size:.59rem; }
    .saved-icon { flex:0 0 auto; width:25px; height:25px; display:grid; place-items:center; border-radius:8px; background:var(--profile-primary-container); color:var(--profile-on-primary-container); }
    .actions { display:flex; align-items:center; gap:8px; }

    .state-card { min-height:210px; display:grid; justify-items:center; align-content:center; gap:8px; padding:28px; border:1px solid var(--profile-outline); border-radius:24px; background:var(--profile-surface); color:var(--profile-on-variant); text-align:center; }
    .state-icon { width:48px; height:48px; display:grid; place-items:center; margin-bottom:3px; border-radius:15px; background:var(--profile-primary-container); color:var(--profile-on-primary-container); }
    .state-card strong { color:var(--profile-on); font-size:.9rem; }
    .state-card > span { font-size:.7rem; }
    .spinner { width:20px; height:20px; margin-top:8px; border-radius:50%; border:2px solid var(--profile-outline); border-top-color:var(--profile-primary); animation:spin .7s linear infinite; }
    @keyframes spin { to { transform:rotate(360deg); } }

    @media (max-width: 900px) {
        .profile-grid { grid-template-columns:1fr; }
        .identity-column { grid-template-columns:minmax(250px,.7fr) minmax(0,1.3fr); align-items:start; }
        .profile-tip { align-self:stretch; }
    }
    @media (max-width: 680px) {
        .screen { padding:10px 13px 30px; gap:14px; }
        .profile-header { align-items:flex-start; flex-direction:column; gap:10px; }
        .header-copy h1 { font-size:1.32rem; }
        .identity-column { grid-template-columns:1fr; }
        .field-grid { grid-template-columns:1fr; gap:14px; }
        .content-card { padding:17px; }
        .field-panel { padding:12px; }
        .section-heading p { margin-left:0; }
        .actions-bar { position:relative; bottom:auto; flex-direction:column; align-items:stretch; padding:12px; }
        .actions-status { padding:0 2px 4px; }
        .actions { display:grid; grid-template-columns:1fr 1.35fr; }
        .actions :global(button) { width:100%; }
    }
    @media (max-width: 420px) {
        .screen { padding-inline:10px; }
        .identity-card { padding:17px; }
        .avatar-wrap { width:96px; height:96px; }
        .avatar-preview { border-radius:29px; font-size:2.6rem; }
        .section-heading { gap:8px; }
        .section-icon { width:28px; height:28px; }
        .section-title-line h2 { font-size:.92rem; }
        .section-heading p { font-size:.64rem; }
        .readonly-label { font-size:.55rem; }
        .m3-input { height:46px; line-height:46px; }
        .actions { grid-template-columns:1fr; }
    }
</style>
