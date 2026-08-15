<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card, TextFieldOutlined, TextFieldOutlinedMultiline } from "m3-svelte";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import { support } from "../navigation/nested.router";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import ImagePicker from "../components/ImagePicker.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { authContainer } from "../../../feature/auth/di/auth.container";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;

    $: void navController;
    $: void navBackStackEntry;

    type ProfileDraft = {
        name: string;
        email: string;
        phone: string;
        bio: string;
        avatarUrl: string;
    };

    let loading = true;
    let saving = false;
    let avatarLoading = false;
    let avatarError = false;
    let draft: ProfileDraft = {
        name: "",
        email: "",
        phone: "",
        bio: "",
        avatarUrl: ""
    };

    onMount(async () => {
        try {
            const user = await sessionStore.getCurrentUser();
            draft = {
                name: user.name ?? "",
                email: user.email ?? "",
                phone: (user as any).phone ?? "",
                bio: (user as any).prefs?.bio ?? "",
                avatarUrl: (user as any).prefs?.avatarUrl ?? ""
            };
        } catch {
            toastStore.error("No se pudo cargar el perfil");
        } finally {
            loading = false;
        }
    });

    function updateField(partial: Partial<ProfileDraft>) {
        draft = { ...draft, ...partial };
    }

    async function saveProfile() {
        saving = true;
        try {
            await authContainer.useCases.accounts.updateProfile?.({
                name: draft.name,
                phone: draft.phone,
                bio: draft.bio,
                avatarUrl: draft.avatarUrl
            });
            toastStore.success("Perfil actualizado");
        } catch (e: any) {
            toastStore.error(e?.message ?? "No se pudo guardar");
        } finally {
            saving = false;
        }
    }
</script>

<section class="screen profile-screen">
    <div class="hero">
        <p class="eyebrow">Perfil</p>
        <h1>Mi perfil</h1>
        <p class="support">Gestiona tu información personal y cómo apareces ante otros usuarios.</p>
        <button class="support-link" type="button" on:click={() => navController.navigate(support.path)}>
            Contactar soporte
        </button>
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
            <p class="muted-note">Usa el botón de arriba para abrir Soporte. El resto del perfil sigue como antes en tu rama.</p>
        </div>
    {/if}
</section>

<style>
    .screen { padding: 16px; max-width: 720px; margin: 0 auto; }
    .hero { margin-bottom: 16px; }
    .eyebrow { margin: 0; font-size: 0.75rem; text-transform: uppercase; opacity: 0.7; }
    h1 { margin: 4px 0; }
    .support { opacity: 0.8; }
    .support-link {
        margin-top: 12px;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant, #555);
        background: color-mix(in srgb, var(--md-sys-color-primary, #6750a4) 16%, transparent);
        color: inherit;
        padding: 10px 14px;
        font-weight: 650;
        cursor: pointer;
    }
    .support-link:hover {
        border-color: var(--md-sys-color-primary, #6750a4);
    }
    .muted-note { opacity: 0.7; font-size: 0.9rem; }
    .card-shell { padding: 16px; }
</style>
