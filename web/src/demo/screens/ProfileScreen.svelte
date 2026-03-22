<script lang="ts">
  import { Button, Card, TextField } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { profile } from '../data'

  export let navController: NavController

  let name = profile.name
  let phone = profile.phone
</script>

<section class="stack-screen profile-shell">
  <div class="profile-header">
    <button class="circle-action" type="button" onclick={() => navController.popBackStack()}>&lt;-</button>
    <h2>Mi perfil</h2>
    <span class="header-spacer"></span>
  </div>

  <Card variant="filled" class="profile-hero-card">
    <div class="profile-photo">{profile.photoInitials}</div>
    <p>{profile.accountType}</p>
  </Card>

  <div class="form-grid">
    <TextField label="Nombre" bind:value={name} />
    <TextField label="Correo" value={profile.email} disabled />
    <TextField label="Telefono" bind:value={phone} />
  </div>

  <Button variant="filled" size="m">Guardar cambios</Button>
</section>
