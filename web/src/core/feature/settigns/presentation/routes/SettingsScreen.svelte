<script lang="ts">
    import { Card, Icon, Switch } from "m3-svelte";
    import darkModeIcon from "@ktibow/iconset-material-symbols/dark-mode-rounded";
    import notificationsIcon from "@ktibow/iconset-material-symbols/notifications-rounded";
    import vibrationIcon from "@ktibow/iconset-material-symbols/vibration-rounded";
    import paletteIcon from "@ktibow/iconset-material-symbols/format-paint-rounded";
    import securityIcon from "@ktibow/iconset-material-symbols/security-rounded";
    import type { NavBackStackEntry } from "../../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { settingsStore } from "../viewmodel/settings.store";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navController;
    $: void navBackStackEntry;

    $: settings = $settingsStore;

    let darkMode = settings.darkMode;
    let notifications = settings.notificationsEnabled;
    let haptics = settings.hapticFeedbackEnabled;

    $: if (darkMode !== settings.darkMode) darkMode = settings.darkMode;
    $: if (notifications !== settings.notificationsEnabled) notifications = settings.notificationsEnabled;
    $: if (haptics !== settings.hapticFeedbackEnabled) haptics = settings.hapticFeedbackEnabled;

    $: settingsStore.updateDarkMode(darkMode);
    $: settingsStore.updateNotifications(notifications);
    $: settingsStore.updateHapticFeedback(haptics);
</script>

<section class="screen">
    <div class="hero">
        <p class="eyebrow">Ajustes</p>
        <h1>Ajustes</h1>
        <p class="support">Estos cambios se guardan automaticamente en cache local.</p>
    </div>

    <Card variant="elevated" class="setting-item">
        <div class="icon-box"><Icon icon={darkModeIcon} /></div>
        <div class="copy">
            <strong>Tema oscuro</strong>
            <span>Controla el tema general de toda la app.</span>
        </div>
        <label><Switch bind:checked={darkMode} /></label>
    </Card>

    <Card variant="elevated" class="setting-item">
        <div class="icon-box"><Icon icon={notificationsIcon} /></div>
        <div class="copy">
            <strong>Notificaciones</strong>
            <span>Recibe alertas sobre pedidos y novedades.</span>
        </div>
        <label><Switch bind:checked={notifications} /></label>
    </Card>

    <Card variant="elevated" class="setting-item">
        <div class="icon-box"><Icon icon={vibrationIcon} /></div>
        <div class="copy">
            <strong>Vibracion</strong>
            <span>Activa respuesta haptica en interacciones.</span>
        </div>
        <label><Switch bind:checked={haptics} /></label>
    </Card>

    <Card variant="filled" class="info-card">
        <div class="info-head"><Icon icon={paletteIcon} /><strong>Apariencia</strong></div>
        <p>El tema se aplica de forma global desde este panel.</p>
    </Card>

    <Card variant="outlined" class="info-card">
        <div class="info-head"><Icon icon={securityIcon} /><strong>Privacidad</strong></div>
        <p>Tus datos locales de ajustes se guardan en cache segura del navegador.</p>
    </Card>
</section>

<style>
    .screen {
        display: grid;
        gap: 18px;
        align-content: start;
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
    .hero {
        padding: 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }
    .support,
    .copy span,
    .info-card p {
        color: var(--md-sys-color-on-surface-variant);
    }
    .setting-item {
        display: grid;
        grid-template-columns: auto minmax(0, 1fr) auto;
        gap: 12px;
        align-items: center;
        border-radius: 24px;
    }
    .icon-box,
    .info-head {
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    .icon-box {
        width: 44px;
        height: 44px;
        justify-content: center;
        border-radius: 14px;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-primary);
    }
    .copy {
        display: grid;
        gap: 4px;
    }
    .info-card {
        display: grid;
        gap: 10px;
        border-radius: 24px;
    }
    @media (max-width: 640px) {
        .setting-item {
            grid-template-columns: auto 1fr;
        }
        .setting-item label {
            grid-column: 1 / -1;
            justify-self: end;
        }
    }
</style>
