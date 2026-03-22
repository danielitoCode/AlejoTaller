<script lang="ts">
  import { Button, Card, Switch, TextField } from 'm3-svelte'

  let enabled = true
  let projectName = 'Taller Alejo'

  function handleLaunch() {
    window.alert(`Proyecto listo: ${projectName}`)
  }
</script>

<svelte:head>
  <title>Taller Alejo</title>
  <meta
    name="description"
    content="Base Svelte configurada con componentes Material 3 usando m3-svelte."
  />
</svelte:head>

<main class="shell">
  <section class="hero">
    <div class="eyebrow">Material 3 para Svelte</div>
    <h1>m3-svelte ya está configurado en este proyecto</h1>
    <p class="lede">
      La app carga estilos M3, tokens de color, tipografía y componentes listos para extender.
    </p>

    <div class="actions">
      <Button variant="filled" size="m" onclick={handleLaunch}>Probar integración</Button>
      <Button variant="outlined" size="m" href="https://kendell.dev/m3-svelte" target="_blank">
        Ver documentación
      </Button>
    </div>
  </section>

  <section class="grid">
    <Card variant="filled" class="panel">
      <span class="panel-label">Ejemplo base</span>
      <h2>Formulario con componentes M3</h2>
      <p class="panel-copy">
        Puedes reutilizar esta estructura como punto de partida para tu interfaz.
      </p>

      <div class="form">
        <TextField label="Nombre del proyecto" bind:value={projectName} />

        <label class="toggle-row">
          <div>
            <strong>Modo productivo</strong>
            <span>Controlado con `bind:checked` sobre `Switch`.</span>
          </div>
          <Switch bind:checked={enabled} />
        </label>
      </div>
    </Card>

    <Card variant="outlined" class="panel status">
      <span class="panel-label">Estado</span>
      <h2>Resumen de configuración</h2>

      <dl>
        <div>
          <dt>Librería</dt>
          <dd>`m3-svelte` activa</dd>
        </div>
        <div>
          <dt>Tema</dt>
          <dd>Tokens M3 en `src/app.css`</dd>
        </div>
        <div>
          <dt>Tipografía</dt>
          <dd>Google Sans Flex local</dd>
        </div>
        <div>
          <dt>Switch</dt>
          <dd>{enabled ? 'Encendido' : 'Apagado'}</dd>
        </div>
      </dl>
    </Card>
  </section>
</main>
