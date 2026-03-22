<script lang="ts">
  import { onMount } from 'svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { MAIN_ROUTES } from '../routes'
  import AppRoot from '../../core/infrastructure/presentation/components/AppRoot.svelte'
  import Screen from '../../core/infrastructure/presentation/components/Screen.svelte'

  export let navController: NavController

  onMount(() => {
    const timer = window.setTimeout(() => {
      navController.navigate(MAIN_ROUTES.landing)
    }, 850)

    return () => window.clearTimeout(timer)
  })
</script>

<AppRoot>
  <Screen ariaLabel="Pantalla de carga">
    <section class="initial-screen splash-screen">
      <div class="splash-mark">
        <div class="splash-ring"></div>
        <div class="brand-mark">TA</div>
      </div>
      <p class="eyebrow">Taller Alejo</p>
      <h1>Preparando panel y sincronizacion</h1>
      <p class="body-copy">Carga inicial del flujo publico.</p>
    </section>
  </Screen>
</AppRoot>
