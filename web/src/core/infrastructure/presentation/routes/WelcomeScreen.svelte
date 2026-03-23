<script lang="ts">
  import { Button, Icon } from 'm3-svelte'
  import AutoAwesome from "@ktibow/iconset-material-symbols/auto-awesome"
  import Screen from "../components/Screen.svelte";
  import type {NavController} from "../../../../lib/navigation/NavController";
  import {MAIN_ROUTES} from "../../../../demo/routes";
  export let navController: NavController

  type ShowcaseItem = {
    id: string;
    title: string;
    subtitle: string;
    imageUrl: string;
  };

  const baseItems: ShowcaseItem[] = [
    {
      id: "1",
      title: "EcoFlow",
      subtitle: "Power · Solar",
      imageUrl:
              "https://us.ecoflow.com/cdn/shop/files/ecoflow-ecoflow-delta-pro-ultra-whole-home-backup-power-ul-9540-certificated-dpu-bundle-delta-pro-ultra-1-x-inverter-1-x-battery-1179495744.png?v=1767511531&width=1240"
    },
    {
      id: "2",
      title: "BMS",
      subtitle: "Battery Safety",
      imageUrl:
              "https://www.anernstore.com/cdn/shop/articles/Technician_troubleshooting_a_LiFePO4_battery_BMS_w.png?v=1761728830&width=1200"
    },
    {
      id: "3",
      title: "LI3-2A",
      subtitle: "Controller",
      imageUrl: "https://lian-li.com/wp-content/uploads/2024/10/TL_Controller_02a.jpg"
    },
    {
      id: "4",
      title: "T2N3904",
      subtitle: "Electronics",
      imageUrl:
              "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=900&q=80"
    }
  ];

  const rows = [{ id: 0, reverse: false, speed: 48, items: [...baseItems, ...baseItems] }];
</script>


<Screen ariaLabel="Bienvenida">
  <section class="welcome-screen">
    <header class="header">
      <div class="welcome-emblem-shell">
        <div class="welcome-emblem">
          <Icon icon="{AutoAwesome}"/>
        </div>
      </div>
      <h1>Bienvenido</h1>
    </header>

    <section class="auto-scroll" aria-label="Galería automática">
      {#each rows as row}
        <div class="row-mask">
          <div
                  class="row-track {row.reverse ? 'reverse' : ''}"
                  style={`--duration:${Math.max(18, 220 / row.speed)}s`}
          >
            {#each row.items as item, idx (`${row.id}-${item.id}-${idx}`)}
              <article class="media-card" aria-label={item.title}>
                <img
                        class="media-image"
                        src={item.imageUrl}
                        alt={item.title}
                        loading="lazy"
                        decoding="async"
                />
                <div class="media-overlay">
                  <span class="media-title">{item.title}</span>
                  <span class="media-subtitle">{item.subtitle}</span>
                </div>
              </article>
            {/each}
          </div>
        </div>
      {/each}
    </section>

    <section class="buttons-container">
      <div class="landing-buttons">
        <p class="landing-buttons-tittle">Inicia sesion o crea tu cuenta para continuar.</p>
        <Button variant="filled" size="s" onclick={() => navController.navigate(MAIN_ROUTES.login)}>
          <p class="button-tittle">Iniciar sesion</p>
        </Button>
        <Button variant="text" size="s" onclick={() => navController.navigate(MAIN_ROUTES.register)}>
          <p class="button-tittle">Crear cuenta</p>
        </Button>
      </div>
    </section>

  </section>
</Screen>

<style>
  .header {
    display: grid;
    align-items: center;
    justify-content: center;
  }

  .landing-buttons-tittle {
    margin: 0;
    text-align: center;
    max-width: 460px;
    color: color-mix(in srgb, var(--m3c-on-background) 90%, transparent);
    font-size: clamp(1rem, 2vw, 1.1rem);
  }

  .button-tittle {
    padding: 10px;
  }

  .welcome-screen {
    min-height: 100dvh;
    width: 100%;
    display: grid;
    grid-template-rows: 1fr auto 1fr;
    place-items: center;
    --screen-pad: clamp(10px, 2.2vw, 16px);
    --card-size: clamp(150px, 18vmin, 220px);
    padding:
            calc(var(--screen-pad) + env(safe-area-inset-top))
            calc(var(--screen-pad) + env(safe-area-inset-right))
            calc(var(--screen-pad) + env(safe-area-inset-bottom))
            calc(var(--screen-pad) + env(safe-area-inset-left));
  }

  h1 {
    text-align: center;
    margin: 0;
    font-size: clamp(1.9rem, 3.3vw, 2.35rem);
    line-height: 1.12;
    letter-spacing: -0.02em;
  }

  .auto-scroll {
    display: grid;
    gap: 20px;
    min-height: 0;
    align-content: center;
    z-index: 0;
  }

  .row-mask {
    overflow: hidden;
    width: 100%;
    height: var(--card-size);
    mask-image: linear-gradient(to right, transparent, black 8%, black 92%, transparent);
  }

  .row-track {
    display: flex;
    width: max-content;
    gap: 18px;
    will-change: transform;
    animation: marquee var(--duration) linear infinite;
  }

  .row-track.reverse {
    animation-name: marquee-reverse;
  }

  .auto-scroll:hover .row-track {
    animation-play-state: paused;
  }

  .media-card {
    width: var(--card-size);
    aspect-ratio: 1;
    border-radius: 30px;
    overflow: hidden;
    position: relative;
    box-shadow: 0 12px 28px rgba(0, 0, 0, 0.2);
    border: 1px solid color-mix(in srgb, white 28%, transparent);
    background: var(--md-sys-color-surface-variant);
  }

  .media-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transform: scale(1.02);
  }

  .media-overlay {
    position: absolute;
    inset: auto 0 0;
    display: grid;
    gap: 2px;
    padding: 16px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.66), rgba(0, 0, 0, 0.08));
    color: #fff;
  }

  .media-title {
    font-weight: 700;
    letter-spacing: 0.02em;
  }

  .media-subtitle {
    font-size: 0.88rem;
    opacity: 0.92;
  }

  .landing-buttons {
    width: min(100%, 420px);
    justify-self: center;
    --landing-button-pad: clamp(5px, 1.5vw, 5px);
    padding: calc(var(--landing-button-pad));
    display: grid;
    gap: 10px;
  }

  .buttons-container {
    display: flex;
    text-align: center;
  }

  @media (orientation: landscape) and (max-height: 600px) {
    .welcome-screen {
      min-height: 100dvh;
      width: 100%;
      display: grid;
      grid-template-rows: 1fr auto 1fr;
      align-items: center;
      place-items: center;
      --screen-pad: clamp(10px, 2.2vw, 16px);
      --card-size: clamp(150px, 18vmin, 220px);
      padding:
              calc(var(--screen-pad) + env(safe-area-inset-top))
              calc(var(--screen-pad) + env(safe-area-inset-right))
              calc(var(--screen-pad) + env(safe-area-inset-bottom))
              calc(var(--screen-pad) + env(safe-area-inset-left));
    }

    .auto-scroll {
      position: absolute;
      inset: -12% -24%;
      opacity: 0.18;
      pointer-events: none;
      filter: saturate(0.9) contrast(1.05);
    }

    .landing-buttons {
      width: min(100%, 300px);
      justify-self: start;
      align-self: start;
    }
  }

  @media (min-width: 1024px) {
    .auto-scroll {
      position: static;
      inset: auto;
      opacity: 1;
      pointer-events: auto;
      filter: none;
    }

    .landing-buttons {
      justify-self: center;
      width: min(100%, 320px);
    }
  }

  @keyframes marquee {
    from {
      transform: translateX(0);
    }
    to {
      transform: translateX(-50%);
    }
  }

  @keyframes marquee-reverse {
    from {
      transform: translateX(-50%);
    }
    to {
      transform: translateX(0);
    }
  }

  @keyframes reveal {
    from {
      opacity: 0;
      transform: translateY(28px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
</style>
