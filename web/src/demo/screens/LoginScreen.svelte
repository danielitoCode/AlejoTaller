<script lang="ts">
  import { Button, Card, LoadingIndicator, TextField } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { MAIN_ROUTES } from '../routes'
  import AppRoot from '../../core/infrastructure/presentation/components/AppRoot.svelte'
  import Screen from '../../core/infrastructure/presentation/components/Screen.svelte'
  import appLogo from '../../assets/app-logo.webp'

  export let navController: NavController

  let email = ''
  let password = ''
</script>

<AppRoot>
  <Screen ariaLabel="Inicio de sesion">
    <section class="initial-screen login-mobile-screen">
      <div class="login-mobile-top">
        <div class="login-mobile-icon-shell">
          <div class="login-mobile-loader-wrap">
            <LoadingIndicator size={160} aria-label="Cargando" />
          </div>
          <img class="login-mobile-logo" src={appLogo} alt="Logo de la aplicacion" />
        </div>

        <div class="login-mobile-copy">
          <h2>Iniciar sesion</h2>
          <p>Accede con tu cuenta para continuar.</p>
        </div>
      </div>

      <Card variant="filled" class="login-mobile-card">
        <div class="login-field-shell">
          <div class="login-field-icon">@</div>
          <TextField label="Correo" bind:value={email} />
        </div>

        <div class="login-field-shell">
          <div class="login-field-icon">*</div>
          <TextField label="Contrasena" type="password" bind:value={password} />
        </div>

        <div class="auth-buttons auth-buttons-column login-actions">
          <Button variant="filled" size="m" onclick={() => navController.navigate(MAIN_ROUTES.mainHome)}>
            Entrar
          </Button>
          <Button variant="filled" size="m" onclick={() => navController.navigate(MAIN_ROUTES.mainHome)}>
            Google
          </Button>
          <Button variant="text" size="m" onclick={() => navController.navigate(MAIN_ROUTES.register)}>
            No tienes cuenta? Registrate
          </Button>
        </div>
      </Card>

      <footer class="elitec-signature">
        <div class="elitec-row">
          <span>Disenado por:</span>
          <strong>ELITEC</strong>
          <span class="elitec-mark">X</span>
        </div>
        <div class="elitec-row elitec-phone">
          <span>+5356628793</span>
        </div>
      </footer>
    </section>
  </Screen>
</AppRoot>
