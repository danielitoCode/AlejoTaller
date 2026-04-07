<script lang="ts">
  import {onMount} from "svelte";
  import {Button, Card, Icon} from 'm3-svelte'
  import AutoAwesome from "@ktibow/iconset-material-symbols/auto-awesome"
  import {ArrowRight, Download, Smartphone, UserRoundPlus, Wrench} from "lucide-svelte";
  import Screen from "../components/Screen.svelte";
  import type {NavController} from "../../../../lib/navigation/NavController";
  import {ENV} from "../../env";

  export let navController: NavController

  let isAndroidDevice = false;

  const apkOptions = [
    {
      id: "client-apk",
      label: "Instalar app cliente",
      icon: Smartphone,
      url: ENV.clientAndroidApkUrl,
      recommended: true
    },
    {
      id: "operator-apk",
      label: "Instalar app operadora",
      icon: Wrench,
      url: ENV.operatorAndroidApkUrl,
      recommended: false
    }
  ].filter((item) => Boolean(item.url));

  const hasApkDownloads = apkOptions.length > 0 || Boolean(ENV.githubReleasesUrl);

  onMount(() => {
    isAndroidDevice = /android/i.test(window.navigator.userAgent);
  });

  function openDownload(url: string | undefined) {
    if (!url) return;
    window.location.href = url;
  }

  type ShowcaseItem = {
    id: string;
    title: string;
    imageUrl: string;
    featured?: boolean;
  };

  const showcaseItems: ShowcaseItem[] = [
    {
      id: "power-anker-solix",
      title: "Estacion de energia",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Anker_SOLIX_C300X_Portable_Power_Station.jpg",
      featured: true
    },
    {
      id: "battery-18650",
      title: "Bateria 18650",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Liion-18650-AA-battery.jpg"
    },
    {
      id: "components-closeup",
      title: "Componentes electronicos",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Electronic_components_(8370189100).jpg"
    },
    {
      id: "soldering-kit",
      title: "Kit de soldadura",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Soldering_iron_and_accessories.jpg"
    },
    {
      id: "lithium-pack",
      title: "Bateria de litio",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Lithium_ion_battery.jpg"
    },
    {
      id: "bench-components",
      title: "Mesa de electronica",
      imageUrl: "https://commons.wikimedia.org/wiki/Special:FilePath/Electronic_components.jpg"
    }
  ];

  const marqueeItems = [...showcaseItems, ...showcaseItems];
</script>

<Screen ariaLabel="Bienvenida" scrollable={false}>
  <section class="welcome-mobile">
    <section class="welcome-gallery-shell">
      <header class="welcome-head">
        <div class="welcome-emblem">
          <Icon icon={AutoAwesome} />
        </div>
        <h1>Bienvenido</h1>
        <p>Inicia sesion o crea tu cuenta para continuar.</p>
      </header>

      <section class="welcome-gallery" aria-label="Productos destacados en desplazamiento automatico">
        <div class="gallery-fade gallery-fade-left" aria-hidden="true"></div>
        <div class="gallery-fade gallery-fade-right" aria-hidden="true"></div>
        <div class="gallery-brush" aria-hidden="true"></div>
        <ul class="marquee-track" role="list">
          {#each marqueeItems as item, index}
            <li class={`preview-card ${item.featured ? "featured" : ""}`} aria-hidden={index >= showcaseItems.length}>
              <img src={item.imageUrl} alt={item.title} loading={index < 3 ? "eager" : "lazy"} />
            </li>
          {/each}
        </ul>
      </section>
    </section>

    <aside class="welcome-copy-panel">
      <div class="actions-panel">
        <Card variant="filled">
          <div class="actions-content">
            <div class="actions-copy">
              <span class="actions-eyebrow">Acceso</span>
              <strong>Entra al panel o crea tu cuenta</strong>
              <p class="actions-support">Gestiona productos, mensajes y ventas desde una interfaz clara y directa.</p>
            </div>
            <div class="actions-stack">
              <Button variant="filled" size="m" onclick={() => navController.navigate("login") }>
                <span class="btn-content">
                  <span>Iniciar sesion</span>
                  <ArrowRight size={18} />
                </span>
              </Button>
              <Button variant="text" size="m" onclick={() => navController.navigate("register") }>
                <span class="btn-content">
                  <UserRoundPlus size={18} />
                  <span>Crear cuenta</span>
                </span>
              </Button>
            </div>

            {#if hasApkDownloads}
              <section class:apk-panel-android={isAndroidDevice} class="apk-panel" aria-label="Descarga de aplicaciones Android">
                <div class="apk-copy">
                  <span class="apk-eyebrow">{isAndroidDevice ? "Android detectado" : "Aplicaciones Android"}</span>
                  <strong>{isAndroidDevice ? "Instala la APK desde este dispositivo" : "Descarga las APK del release"}</strong>
                  <p>
                    {#if isAndroidDevice}
                      Se descargara la APK desde GitHub Releases y Android te pedira confirmar la instalacion.
                    {:else}
                      Usa estos enlaces directos de GitHub Releases para compartir o instalar las APK oficiales.
                    {/if}
                  </p>
                </div>

                <div class="apk-actions">
                  {#each apkOptions as apk}
                    <Button
                      variant={apk.recommended && isAndroidDevice ? "filled" : "outlined"}
                      size="m"
                      onclick={() => openDownload(apk.url)}
                    >
                      <span class="btn-content apk-btn-content">
                        <svelte:component this={apk.icon} size={18} />
                        <span>{apk.label}</span>
                        <Download size={18} />
                      </span>
                    </Button>
                  {/each}

                  {#if ENV.githubReleasesUrl}
                    <Button variant="text" size="m" onclick={() => openDownload(ENV.githubReleasesUrl)}>
                      <span class="btn-content apk-btn-content apk-secondary-link">
                        <Download size={18} />
                        <span>{apkOptions.length > 0 ? "Ver todos los releases" : "Descargar desde GitHub Releases"}</span>
                      </span>
                    </Button>
                  {/if}
                </div>
              </section>
            {/if}
          </div>
        </Card>
      </div>
    </aside>
  </section>
</Screen>

<style>
  :global(.screen[aria-label="Bienvenida"]) {
    width: 100%;
    max-width: none;
    height: 100dvh;
    min-height: 100dvh;
    padding: 0;
    margin: 0;
  }

  .welcome-mobile {
    width: 100%;
    height: 100%;
    max-height: 100%;
    box-sizing: border-box;
    padding:
      max(18px, calc(env(safe-area-inset-top) + 10px))
      16px
      max(28px, calc(env(safe-area-inset-bottom) + 18px));
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    gap: clamp(16px, 3vh, 28px);
    overflow-x: hidden;
    overflow-y: hidden;
    color: var(--md-sys-color-on-background);
    background:
      radial-gradient(
        circle at 50% 9%,
        color-mix(in srgb, var(--md-sys-color-primary) 20%, transparent),
        transparent 34%
      ),
      linear-gradient(
        180deg,
        color-mix(in srgb, var(--md-sys-color-primary-container) 18%, var(--md-sys-color-background)) 0%,
        color-mix(in srgb, var(--md-sys-color-surface-container-low) 76%, var(--md-sys-color-background)) 58%,
        var(--md-sys-color-background) 100%
      );
  }

  .welcome-gallery-shell,
  .welcome-copy-panel {
    display: grid;
    gap: clamp(16px, 2.8vh, 24px);
    min-height: 0;
  }

  .welcome-head {
    display: grid;
    justify-items: center;
    text-align: center;
    gap: 10px;
    flex-shrink: 0;
    padding-top: 6px;
  }

  .welcome-emblem {
    width: clamp(54px, 14vw, 66px);
    height: clamp(54px, 14vw, 66px);
    border-radius: 999px;
    display: grid;
    place-items: center;
    background: color-mix(in srgb, var(--md-sys-color-primary-container) 72%, transparent);
    color: var(--md-sys-color-on-primary-container);
  }

  .welcome-head h1 {
    margin: 0;
    font-size: clamp(2rem, 8vw, 2.8rem);
    letter-spacing: -0.04em;
    line-height: 1.02;
  }

  .welcome-head p {
    margin: 0;
    color: var(--md-sys-color-on-surface-variant);
    font-size: clamp(1rem, 3.8vw, 1.25rem);
    max-width: 22rem;
    line-height: 1.4;
  }

  .welcome-gallery {
    position: relative;
    width: 100%;
    height: clamp(148px, 21vh, 210px);
    overflow: hidden;
    border-radius: 32px;
    flex-shrink: 0;
    box-shadow: 0 24px 54px rgb(0 0 0 / 0.24);
    background: var(--md-sys-color-surface-container);
  }

  .gallery-brush {
    position: absolute;
    inset: 0;
    z-index: 1;
    pointer-events: none;
    background:
      linear-gradient(
        90deg,
        color-mix(in srgb, var(--md-sys-color-background) 0%, transparent) 0%,
        color-mix(in srgb, var(--md-sys-color-surface-container-low) 22%, transparent) 24%,
        color-mix(in srgb, var(--md-sys-color-surface-container-high) 68%, transparent) 60%,
        color-mix(in srgb, var(--md-sys-color-surface) 88%, transparent) 100%
      ),
      radial-gradient(circle at 72% 48%, color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent), transparent 34%);
  }

  .marquee-track {
    margin: 0;
    padding: 0;
    list-style: none;
    display: flex;
    gap: 12px;
    width: max-content;
    height: 100%;
    animation: marquee-x 28s linear infinite;
  }

  .preview-card {
    flex: 0 0 clamp(140px, 38vw, 200px);
    width: clamp(140px, 38vw, 200px);
    height: 100%;
    border-radius: 24px;
    overflow: hidden;
    background: var(--md-sys-color-surface-container-high);
    box-shadow: 0 14px 34px rgb(0 0 0 / 0.22);
  }

  .preview-card.featured {
    flex-basis: clamp(200px, 55vw, 300px);
    width: clamp(200px, 55vw, 300px);
  }

  .preview-card img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    object-position: center;
    display: block;
  }

  .gallery-fade {
    position: absolute;
    top: 0;
    bottom: 0;
    width: 42px;
    z-index: 2;
    pointer-events: none;
  }

  .gallery-fade-left {
    left: 0;
    background: linear-gradient(90deg, var(--md-sys-color-background) 0%, color-mix(in srgb, var(--md-sys-color-background) 0%, transparent) 100%);
  }

  .gallery-fade-right {
    right: 0;
    background: linear-gradient(270deg, var(--md-sys-color-background) 0%, color-mix(in srgb, var(--md-sys-color-background) 0%, transparent) 100%);
  }

  .actions-panel {
    width: 100%;
    flex-shrink: 0;
    margin-top: auto;
    padding-top: 4px;
  }

  .actions-panel :global(.m3-container.filled) {
    border-radius: 28px;
    background: var(--md-sys-color-surface-container-high);
    --m3v-background: var(--md-sys-color-surface-container-high);
    box-shadow:
      0 22px 46px rgb(0 0 0 / 0.24),
      inset 0 1px 0 rgb(255 255 255 / 0.08);
    border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 85%, transparent);
  }

  .actions-content {
    padding: 16px;
    border-radius: 28px;
    display: grid;
    gap: 14px;
    background: transparent;
    backdrop-filter: blur(10px);
    min-height: 0;
  }

  .actions-copy {
    display: grid;
    gap: 4px;
    padding-inline: 2px;
    color: var(--md-sys-color-on-surface);
  }

  .actions-eyebrow {
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--md-sys-color-primary);
  }

  .actions-copy strong {
    font-size: 1rem;
    line-height: 1.3;
    letter-spacing: -0.01em;
  }

  .actions-support {
    margin: 0;
    color: var(--md-sys-color-on-surface-variant);
    font-size: 0.92rem;
    line-height: 1.45;
  }

  .actions-stack {
    display: grid;
    gap: 10px;
  }

  .apk-panel {
    display: grid;
    gap: 12px;
    padding: 14px;
    border-radius: 22px;
    background:
      linear-gradient(
        135deg,
        color-mix(in srgb, var(--md-sys-color-secondary-container) 78%, transparent),
        color-mix(in srgb, var(--md-sys-color-surface-container-highest) 86%, transparent)
      );
    border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 82%, transparent);
  }

  .apk-panel-android {
    background:
      radial-gradient(circle at top right, color-mix(in srgb, var(--md-sys-color-primary) 20%, transparent), transparent 34%),
      linear-gradient(
        135deg,
        color-mix(in srgb, var(--md-sys-color-primary-container) 84%, transparent),
        color-mix(in srgb, var(--md-sys-color-tertiary-container) 72%, transparent)
      );
  }

  .apk-copy {
    display: grid;
    gap: 4px;
  }

  .apk-eyebrow {
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.08em;
    text-transform: uppercase;
    color: var(--md-sys-color-primary);
  }

  .apk-copy strong {
    font-size: 0.98rem;
    line-height: 1.35;
    color: var(--md-sys-color-on-surface);
  }

  .apk-copy p {
    margin: 0;
    font-size: 0.9rem;
    line-height: 1.45;
    color: var(--md-sys-color-on-surface-variant);
  }

  .apk-actions {
    display: grid;
    gap: 10px;
  }

  .apk-btn-content {
    justify-content: space-between;
  }

  .apk-secondary-link {
    justify-content: center;
  }

  .actions-content :global(.m3-button) {
    width: 100%;
  }

  .actions-content :global(.m3-container.m) {
    min-width: 0;
    max-width: 100%;
    min-height: 58px;
    padding-inline: clamp(1.1rem, 4vw, 1.6rem);
    border-radius: 18px;
  }

  .btn-content {
    width: 100%;
    min-width: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    font-weight: 600;
    padding: 14px 0;
  }

  .actions-content :global(.m3-button.filled .btn-content) {
    justify-content: space-between;
    padding-inline: 2px;
  }

  .actions-content :global(.m3-button.text .btn-content) {
    justify-content: center;
  }

  @keyframes marquee-x {
    from {
      transform: translateX(0);
    }
    to {
      transform: translateX(-50%);
    }
  }

  @media (max-height: 760px) {
    .welcome-mobile {
      padding-top: max(14px, calc(env(safe-area-inset-top) + 8px));
      padding-bottom: max(22px, calc(env(safe-area-inset-bottom) + 14px));
      gap: 12px;
    }

    .welcome-head h1 {
      font-size: clamp(1.7rem, 7.2vw, 2.2rem);
    }

    .welcome-head p {
      font-size: clamp(0.9rem, 3.5vw, 1.05rem);
    }

    .welcome-gallery {
      height: clamp(124px, 18vh, 156px);
    }

    .actions-content {
      padding: 12px;
      gap: 8px;
    }

    .apk-panel {
      padding: 12px;
      gap: 10px;
    }
  }

  @media (min-width: 800px) and (orientation: portrait) {
    .welcome-mobile {
      max-width: 1024px;
      margin: 0 auto;
      padding-inline: 24px;
      justify-content: space-between;
    }

    .welcome-gallery {
      height: 220px;
    }

    .actions-panel {
      width: min(100%, 760px);
      margin-inline: auto;
    }

    .preview-card {
      flex-basis: clamp(180px, 25vw, 260px);
      width: clamp(180px, 25vw, 260px);
      height: 100%;
    }

    .preview-card.featured {
      flex-basis: clamp(260px, 40vw, 400px);
      width: clamp(260px, 40vw, 400px);
    }
  }

  @media (orientation: landscape) and (min-width: 700px), (min-width: 1100px) {
    .welcome-mobile {
      max-width: 1240px;
      margin: 0 auto;
      padding:
        max(22px, calc(env(safe-area-inset-top) + 12px))
        24px
        max(24px, calc(env(safe-area-inset-bottom) + 14px));
      display: grid;
      grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.82fr);
      align-items: stretch;
      gap: clamp(24px, 4vw, 48px);
    }

    .welcome-gallery-shell {
      min-height: 0;
      grid-template-rows: auto minmax(0, 1fr);
      align-content: stretch;
    }

    .welcome-head {
      justify-items: start;
      text-align: left;
      max-width: 32rem;
      padding-top: 0;
    }

    .welcome-head p {
      max-width: 28rem;
    }

    .welcome-gallery {
      height: auto;
      min-height: 0;
      max-height: clamp(240px, 46dvh, 420px);
    }

    .gallery-fade-left,
    .gallery-fade-right {
      display: none;
    }

    .marquee-track {
      gap: 16px;
      align-items: stretch;
    }

    .preview-card {
      flex-basis: clamp(220px, 24vw, 300px);
      width: clamp(220px, 24vw, 300px);
      border-radius: 28px;
    }

    .preview-card.featured {
      flex-basis: clamp(280px, 32vw, 380px);
      width: clamp(280px, 32vw, 380px);
    }

    .welcome-copy-panel {
      min-height: 0;
      align-content: stretch;
    }

    .actions-panel {
      margin-top: 0;
      align-self: stretch;
    }

    .actions-panel :global(.m3-container.filled) {
      height: 100%;
      min-height: 0;
    }

    .actions-content {
      height: 100%;
      box-sizing: border-box;
      padding: clamp(20px, 2.5vw, 28px);
      grid-template-rows: auto 1fr;
      align-content: stretch;
    }

    .actions-copy {
      gap: 8px;
    }

    .actions-copy strong {
      font-size: clamp(1.15rem, 1.6vw, 1.45rem);
    }

    .actions-support {
      font-size: 0.98rem;
      max-width: 24rem;
    }

    .actions-stack {
      gap: 14px;
      align-self: center;
      width: 100%;
    }

    .actions-content :global(.m3-container.m) {
      min-height: 58px;
    }
  }
</style>
