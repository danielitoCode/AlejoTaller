<script lang="ts">
  import { onMount } from 'svelte'
  import {initGlobalLogger} from "./core/infrastructure/presentation/util/console.interceptor";
  import MainNavigationWrapper from "./core/infrastructure/presentation/navigation/MainNavigationWrapper.svelte";
  import ToastHost from "./core/infrastructure/presentation/components/ToastHost.svelte";
  import DevTerminal from "./core/infrastructure/presentation/components/DevTerminal.svelte";
  import { saleStore } from './core/feature/sale/presentation/viewmodel/sale.store';
  import { logNavSessionStart, logNavAuthCheck, logNavRoute, logProductFlow, logNavError } from "./core/infrastructure/presentation/navigation/debug-logger";
  import { getCapturedHash, getCapturedParsedDeeplink, isProductDeeplinkCaptured } from "./core/infrastructure/presentation/navigation/initial-deep-link";

  let isOnline = true
  /** Raw hash captured before Svelte mounts, so the router never touches it */
  export let initialDeepLink: string | null = null;

  if (import.meta.env.DEV) {
    initGlobalLogger();
  }

  function syncConnectivity() {
    isOnline = navigator.onLine
  }

  // ── Debug navigation logging ──
  function logSessionType(url: string) {
    const parsed = getCapturedParsedDeeplink();
    if (parsed && isProductDeeplinkCaptured()) {
      logNavSessionStart(url, "deeplink", {
        top: parsed.top,
        nested: parsed.nested,
        args: parsed.args,
        productId: parsed.args?.productId,
        isProductDeeplink: true
      });
    } else {
      logNavSessionStart(url, "normal", {
        rawHash: getCapturedHash() ?? "(none)",
        isProductDeeplink: false
      });
    }
  }

  onMount(() => {
    syncConnectivity()
    window.addEventListener('online', syncConnectivity)
    window.addEventListener('offline', syncConnectivity)

    // Pedir permiso de notificaciones si no se ha concedido/denegado
    if (window.Notification && Notification.permission === 'default') {
      Notification.requestPermission();
    }

    // Reintento de sincronización al reconectar
    function handleOnline() {
      saleStore.syncAll();
    }
    window.addEventListener('online', handleOnline);

    // Log session type at startup
    if (import.meta.env.DEV) {
      logSessionType(window.location.href);
    }

    return () => {
      window.removeEventListener('online', syncConnectivity)
      window.removeEventListener('offline', syncConnectivity)
      window.removeEventListener('online', handleOnline);
    }
  })

</script>

<svelte:head>
  <title>Taller Alejo</title>
  <meta
    name="description"
    content="Flujo de navegacion web inspirado en Compose Navigation para Taller Alejo."
  />
</svelte:head>

<main>
  <MainNavigationWrapper initialDeepLink={initialDeepLink}/>
</main>

<ToastHost/>


{#if import.meta.env.DEV}
  <DevTerminal/>
{/if}

{#if !isOnline}
  <div class="offline-banner">Desconectado</div>
{/if}
