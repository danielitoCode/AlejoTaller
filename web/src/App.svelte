<script lang="ts">
  import { onMount } from 'svelte'
  import NavHost from './lib/navigation/NavHost.svelte'
  import { rememberNavController } from './lib/navigation/rememberNavController'
  import { MAIN_ROUTES } from './demo/routes'
  import SplashScreen from './demo/screens/SplashScreen.svelte'
  import LandingScreen from './demo/screens/LandingScreen.svelte'
  import LoginScreen from './demo/screens/LoginScreen.svelte'
  import RegisterScreen from './demo/screens/RegisterScreen.svelte'
  import MainHomeScreen from './demo/screens/MainHomeScreen.svelte'
  import {initGlobalLogger} from "./core/infrastructure/presentation/util/console.interceptor";
  import MainNavigationWrapper from "./core/infrastructure/presentation/navigation/MainNavigationWrapper.svelte";
  import ToastHost from "./core/infrastructure/presentation/components/ToastHost.svelte";
  import DevTerminal from "./core/infrastructure/presentation/components/DevTerminal.svelte";

  const navController = rememberNavController(MAIN_ROUTES.splash)

  const routes = [
    { route: { path: MAIN_ROUTES.splash }, component: SplashScreen },
    { route: { path: MAIN_ROUTES.landing }, component: LandingScreen },
    { route: { path: MAIN_ROUTES.login }, component: LoginScreen },
    { route: { path: MAIN_ROUTES.register }, component: RegisterScreen },
    { route: { path: MAIN_ROUTES.mainHome }, component: MainHomeScreen },
  ]

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

    return () => {
      window.removeEventListener('online', syncConnectivity)
      window.removeEventListener('offline', syncConnectivity)
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

<!-- Terminal solo en desarrollo -->
{#if import.meta.env.DEV}
  <DevTerminal/>
{/if}

{#if !isOnline}
  <div class="offline-banner">Desconectado</div>
{/if}

