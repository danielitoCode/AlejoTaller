<script lang="ts">
  import { Card, Switch } from 'm3-svelte'
  import { settingsOptions } from '../data'

  let options = settingsOptions.map((option) => ({ ...option }))
</script>

<section class="stack-screen">
  <div class="settings-header">
    <h2>Ajustes</h2>
    <p class="body-copy">Estos cambios se guardan automaticamente en cache local.</p>
  </div>

  <div class="settings-list">
    {#each options as option}
      <Card variant="filled" class="settings-item">
        <div>
          <strong>{option.title}</strong>
          <p>{option.description}</p>
        </div>
        <Switch bind:checked={option.enabled} />
      </Card>
    {/each}

    <Card variant="filled" class="info-card accent-card">
      <h3>Apariencia</h3>
      <p>El tema se aplica de forma global desde este panel.</p>
    </Card>

    <Card variant="outlined" class="info-card">
      <h3>Privacidad</h3>
      <p>Tus datos locales de ajustes se guardan en cache segura.</p>
    </Card>
  </div>
</section>
