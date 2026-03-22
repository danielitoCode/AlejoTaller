<script lang="ts">
  import { Button } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { MAIN_ROUTES } from '../routes'
  import AppRoot from '../../core/infrastructure/presentation/components/AppRoot.svelte'
  import Screen from '../../core/infrastructure/presentation/components/Screen.svelte'

  export let navController: NavController
</script>

<AppRoot>
  <Screen ariaLabel="Bienvenida">
    <section class="initial-screen welcome-mobile-screen">
      <header class="welcome-hero">
        <div class="welcome-emblem-shell">
          <div class="welcome-emblem">
            <span class="sparkle-cluster">*</span>
          </div>
        </div>
        <h1>Bienvenido</h1>
        <p class="welcome-copy">Inicia sesion o crea tu cuenta para continuar.</p>
      </header>

      <div class="welcome-gallery" aria-hidden="true">
        <article class="welcome-card welcome-card-large">
          <div class="product-illustration power-station">
            <div class="power-top"></div>
            <div class="power-screen"></div>
            <div class="power-grill"></div>
            <div class="power-brand"></div>
          </div>
        </article>

        <article class="welcome-card welcome-card-small">
          <div class="product-illustration battery-cell">
            <div class="battery-cap"></div>
          </div>
        </article>
      </div>

      <div class="welcome-actions-panel">
        <Button variant="filled" size="m" onclick={() => navController.navigate(MAIN_ROUTES.login)}>
          Iniciar sesion
        </Button>
        <Button variant="text" size="m" onclick={() => navController.navigate(MAIN_ROUTES.register)}>
          Crear cuenta
        </Button>
      </div>
    </section>
  </Screen>
</AppRoot>
