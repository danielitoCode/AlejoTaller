<script lang="ts">
  import { Button } from "m3-svelte";
  import type { AgentPendingTool } from "../../domain/entity/AgentTurn";

  export let pending: AgentPendingTool;
  export let busy = false;
  export let onConfirm: () => void;
  export let onCancel: () => void;

  $: argsPreview = (() => {
    try {
      return JSON.stringify(pending.args ?? {}, null, 2);
    } catch {
      return String(pending.args);
    }
  })();
</script>

<div class="confirm-root" role="presentation">
  <button
    class="scrim"
    type="button"
    aria-label="Cerrar"
    disabled={busy}
    on:click={onCancel}
  ></button>
  <div
    class="panel"
    role="dialog"
    aria-modal="true"
    aria-labelledby="agent-confirm-title"
  >
    <header class="head">
      <h2 id="agent-confirm-title">Confirmar acción</h2>
      <p class="reason">{pending.reason}</p>
    </header>

    <div class="body">
      <div class="row">
        <span class="label">Herramienta</span>
        <code class="tool-name">{pending.name}</code>
      </div>
      {#if argsPreview && argsPreview !== "{}"}
        <div class="row col">
          <span class="label">Argumentos</span>
          <pre class="args">{argsPreview}</pre>
        </div>
      {/if}
    </div>

    <footer class="actions">
      <Button variant="text" disabled={busy} onclick={onCancel}>
        Cancelar
      </Button>
      <Button variant="filled" disabled={busy} onclick={onConfirm}>
        {busy ? "Ejecutando…" : "Confirmar"}
      </Button>
    </footer>
  </div>
</div>

<style>
  .confirm-root {
    position: fixed;
    inset: 0;
    z-index: 1200;
    display: grid;
    place-items: center;
    padding: 16px;
  }

  .scrim {
    position: absolute;
    inset: 0;
    border: 0;
    background: color-mix(in srgb, black 44%, transparent);
    backdrop-filter: blur(6px);
    cursor: pointer;
  }

  .panel {
    position: relative;
    z-index: 1;
    width: min(420px, calc(100vw - 24px));
    max-height: min(80dvh, 560px);
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 20px;
    border-radius: 16px;
    background: var(--m3c-surface, #fff);
    color: var(--m3c-on-surface, #1a1c19);
    box-shadow: var(--shadow-soft, 0 22px 52px rgb(15 47 24 / 0.12));
  }

  .head h2 {
    margin: 0 0 6px;
    font-size: 1.15rem;
  }

  .reason {
    margin: 0;
    font-size: 0.9rem;
    color: var(--m3c-on-surface-variant, #44483e);
  }

  .body {
    display: flex;
    flex-direction: column;
    gap: 10px;
    overflow: auto;
  }

  .row {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 8px;
  }

  .row.col {
    flex-direction: column;
    align-items: stretch;
  }

  .label {
    font-size: 0.75rem;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    color: var(--m3c-on-surface-variant, #44483e);
  }

  .tool-name {
    font-family: ui-monospace, monospace;
    font-size: 0.9rem;
    padding: 2px 8px;
    border-radius: 6px;
    background: var(--m3c-primary-container, #c8e6c9);
    color: var(--m3c-on-primary-container, #102015);
  }

  .args {
    margin: 0;
    padding: 10px 12px;
    border-radius: 10px;
    background: var(--m3c-surface-container, #edf1eb);
    font-family: ui-monospace, monospace;
    font-size: 0.78rem;
    overflow: auto;
    max-height: 180px;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 4px;
  }
</style>
