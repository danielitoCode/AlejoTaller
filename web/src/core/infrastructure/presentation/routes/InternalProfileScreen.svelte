<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card } from "m3-svelte";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import ImagePicker from "../components/ImagePicker.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { type ProfileDraft, profileStore } from "../../../feature/auth/presentation/viewmodel/profile.store";
    import { authContainer } from "../../../feature/auth/di/auth.container";
    import { User, CircleUserRound } from "lucide-svelte";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navController; $: void navBackStackEntry;

    let baseline: ProfileDraft | null = null;
    let loading = true;
    let saving = false;
    $: draft = $profileStore;
    $: hasChanges = baseline !== null && JSON.stringify(draft) !== JSON.stringify(baseline);

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
                avatarUrl: typeof user.prefs?.avatarUrl === "string" ? user.prefs.avatarUrl
                    : typeof user.prefs?.photo_url === "string" ? user.prefs.photo_url
                    : typeof user.prefs?.photoUrl === "string" ? user.prefs.photoUrl : ""
            };
            baseline = initial;
            profileStore.reset(initial);
        } catch (e) {
            toastStore.error(e instanceof Error ? e.message : "No se pudo cargar el perfil");
        } finally {
            loading = false;
        }
    }

    onMount(() => { void hydrateProfile(); });

    function updateField(partial: Partial<ProfileDraft>) { profileStore.patch(partial); }

    async function handleAvatarChange(event: CustomEvent<{ url: string }>) {
        const url = (event?.detail?.url ?? "").trim();
        updateField({ avatarUrl: url });
        if (!url || !baseline) return;
        if (url === (baseline.avatarUrl || "").trim()) return;
        try {
            await authContainer.useCases.accounts.updatePhotoUrl(url);
            baseline = { ...baseline, avatarUrl: url };
            profileStore.save();
            toastStore.success("Foto de perfil actualizada");
            try {
                const user = await sessionStore.getCurrentUser();
                profileStore.hydrateFromUser(user);
            } catch { /* ignore */ }
        } catch (e) {
            toastStore.error(e instanceof Error ? e.message : "No se pudo guardar la foto");
        }
    }

    async function handleSave() {
        if (!baseline || saving) return;
        saving = true;
        try {
            if (draft.name.trim() !== baseline.name.trim())
                await authContainer.useCases.accounts.updateName(draft.name.trim());
            if (draft.phone.trim() !== baseline.phone.trim())
                await authContainer.useCases.accounts.updatePhone(draft.phone.trim());
            if (draft.avatarUrl.trim() !== baseline.avatarUrl.trim())
                await authContainer.useCases.accounts.updatePhotoUrl(draft.avatarUrl.trim());
            baseline = profileStore.save();
            toastStore.success("Perfil actualizado correctamente");
            await hydrateProfile();
        } catch (e) {
            toastStore.error(e instanceof Error ? e.message : "No se pudo guardar el perfil");
        } finally {
            saving = false;
        }
    }

    function handleNameInput(e: InputEvent) {
        const t = e.target as HTMLInputElement | null;
        if (t) updateField({ name: t.value });
    }
    function handlePhoneInput(e: InputEvent) {
        const t = e.target as HTMLInputElement | null;
        if (t) updateField({ phone: t.value });
    }
</script>

<section class="screen profile-screen">
    {#if loading}
        <div class="state-card"><CircleUserRound size={24} /><strong>Cargando perfil…</strong></div>
    {:else}
        <header class="profile-header">
            <h1>Mi perfil</h1>
            {#if hasChanges}<span class="unsaved">Cambios sin guardar</span>{/if}
        </header>
        <Card variant="filled">
            <div class="identity">
                <div class="avatar">
                    {#if draft.avatarUrl}
                        <img src={draft.avatarUrl} alt="Avatar" />
                    {:else}
                        <span>{draft.name?.slice(0, 1).toUpperCase() || "A"}</span>
                    {/if}
                </div>
                <div>
                    <strong>{draft.name || "Tu nombre"}</strong>
                    <span>{draft.email}</span>
                </div>
                <ImagePicker label="Foto de perfil" value={draft.avatarUrl} onchange={handleAvatarChange} />
            </div>
        </Card>
        <Card variant="filled">
            <label class="field">
                <span><User size={14} /> Nombre</span>
                <input type="text" value={draft.name} oninput={handleNameInput} />
            </label>
            <label class="field">
                <span>Teléfono</span>
                <input type="tel" value={draft.phone} oninput={handlePhoneInput} />
            </label>
            <div class="actions">
                <Button variant="filled" onclick={handleSave} disabled={saving || !hasChanges}>
                    {saving ? "Guardando…" : "Guardar"}
                </Button>
            </div>
        </Card>
    {/if}
</section>

<style>
    .profile-screen { padding: 16px; display: grid; gap: 16px; max-width: 720px; }
    .state-card { display: grid; place-items: center; gap: 8px; padding: 40px; }
    .profile-header { display: flex; align-items: center; gap: 12px; }
    .profile-header h1 { margin: 0; font-size: 1.4rem; }
    .unsaved { font-size: .75rem; color: var(--md-sys-color-primary); }
    .identity { display: grid; gap: 14px; padding: 12px; }
    .avatar { width: 72px; height: 72px; border-radius: 18px; overflow: hidden; background: var(--md-sys-color-primary-container); display: grid; place-items: center; font-size: 1.4rem; font-weight: 700; }
    .avatar img { width: 100%; height: 100%; object-fit: cover; }
    .field { display: grid; gap: 6px; margin-bottom: 12px; }
    .field span { font-size: .75rem; font-weight: 600; }
    .field input { min-height: 44px; border-radius: 12px; border: 1px solid var(--md-sys-color-outline-variant); background: var(--md-sys-color-surface); color: var(--md-sys-color-on-surface); padding: 0 12px; font: inherit; }
    .actions { display: flex; gap: 8px; margin-top: 8px; }
</style>
