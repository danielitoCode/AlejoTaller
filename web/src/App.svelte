<script lang="ts">
  import { onMount } from 'svelte'
  import {initGlobalLogger} from "./core/infrastructure/presentation/util/console.interceptor";
  import MainNavigationWrapper from "./core/infrastructure/presentation/navigation/MainNavigationWrapper.svelte";
  import ToastHost from "./core/infrastructure/presentation/components/ToastHost.svelte";
  import DevTerminal from "./core/infrastructure/presentation/components/DevTerminal.svelte";
  import FAB from "./demo/FAB.svelte";
  import ListItems from "./demo/ListItems.svelte";
  import ListComponents from "./demo/ListComponents.svelte";
  import NoteAppDemo from "./demo/NoteAppDemo.svelte";
  import Buttons from "./demo/Buttons.svelte";
  import CardsDemo from "./demo/CardsDemo.svelte";
  import MenuDemo from "./demo/MenuDemo.svelte";
  import ProgressIndicatorDemos from "./demo/ProgressIndicatorDemos.svelte";
  import SelectDemo from "./demo/SelectDemo.svelte";
  import { saleStore } from './core/feature/sale/presentation/viewmodel/sale.store';

  let isOnline = true

  if (import.meta.env.DEV) {
    initGlobalLogger();
  }

  function syncConnectivity() {
    isOnline = navigator.onLine
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
  <MainNavigationWrapper/>
</main>

<ToastHost/>


{#if import.meta.env.DEV}
  <DevTerminal/>
{/if}

{#if !isOnline}
  <div class="offline-banner">Desconectado</div>
{/if}
