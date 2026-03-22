<script lang="ts">
  import { Button, Card, TextField } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { MAIN_ROUTES } from '../routes'

  export let navController: NavController

  let name = ''
  let email = ''
  let phone = ''
  let password = ''
</script>

<section class="screen register-layout">
  <div class="register-copy">
    <div class="landing-emblem register-emblem">
      <div class="sparkle">O</div>
    </div>
    <h2>Crear cuenta</h2>
    <p>Completa tus datos para comenzar.</p>
  </div>

  <Card variant="filled" class="auth-card">
    <div class="form-grid">
      <TextField label="Nombre completo" bind:value={name} />
      <TextField label="Correo" bind:value={email} />
      <TextField label="Telefono" bind:value={phone} />
      <TextField label="Contrasena" type="password" bind:value={password} />
    </div>

    <div class="auth-buttons auth-buttons-column">
      <Button variant="filled" size="m" onclick={() => navController.navigate(MAIN_ROUTES.mainHome)}>
        Registrarse
      </Button>
      <Button variant="text" size="m" onclick={() => navController.popBackStack()}>
        Ya tienes cuenta? Inicia sesion
      </Button>
    </div>
  </Card>
</section>
