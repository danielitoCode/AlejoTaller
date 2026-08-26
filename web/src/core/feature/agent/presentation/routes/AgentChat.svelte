<script lang="ts">
  import { onMount, tick } from "svelte";
  import { Button, Icon } from "m3-svelte";
  import type { NavController } from "../../../../../lib/navigation/NavController";
  import { agentStore } from "../viewmodel/agent.store";
  import AgentConfirmDialog from "../components/AgentConfirmDialog.svelte";
  import sendIcon from "@ktibow/iconset-material-symbols/send-rounded";
  import smartToyIcon from "@ktibow/iconset-material-symbols/smart-toy-rounded";
  import deleteIcon from "@ktibow/iconset-material-symbols/delete-outline-rounded";

  /** Injected by NavHost */
  export let navController: NavController;

  let draft = "";
  let listEl: HTMLDivElement | null = null;

  $: bubbles = $agentStore.bubbles;
  $: sending = $agentStore.sending;
  $: error = $agentStore.error;
  $: pendingTool = $agentStore.pendingTool;
  $: confirming = $agentStore.confirming;

  async function scrollToBottom() {
    await tick();
    if (listEl) listEl.scrollTop = listEl.scrollHeight;
  }

  $: if (bubbles.length || sending) scrollToBottom();

  onMount(() => {
    void navController;
  });

  async function onSubmit() {
    const text = draft.trim();
    if (!text || sending) return;
    draft = "";
    await agentStore.sendMessage(text);
  }

  function onKeydown(e: KeyboardEvent) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      void onSubmit();
    }
  }

  function clearChat() {
    agentStore.clearChat();
    draft = "";
  }
</script>

<section class="agent-chat" aria-label="Asistente AlejoTaller">
  <header class="head">
    <div class="title-row">
      <span class="avatar" aria-hidden="true">
        <Icon icon={smartToyIcon} />
      </span>
      <div>
        <h1>Asistente</h1>
        <p class="sub">Catálogo, pedidos y ayuda · confirmación en escrituras</p>
      </div>
    </div>
    <Button variant="text" size="s" iconType="left" onclick={clearChat} disabled={sending}>
      <Icon icon={deleteIcon} />
      Limpiar
    </Button>
  </header>

  <div class="messages" bind:this={listEl} role="log" aria-live="polite">
    {#if bubbles.length === 0 && !sending}
      <div class="empty">
        <span class="empty-icon" aria-hidden="true">
          <Icon icon={smartToyIcon} />
        </span>
        <p>Pregunta por productos, categorías o el estado de un pedido.</p>
        <p class="hint">Las acciones que modifican datos (crear/cancelar pedido) pedirán confirmación.</p>
      </div>
    {:else}
      {#each bubbles as b (b.id)}
        <article
          class="bubble"
          class:user={b.role === "user"}
          class:assistant={b.role === "assistant"}
          class:system={b.role === "system"}
          class:error={b.isError}
        >
          {#if b.toolName}
            <span class="tool-tag">{b.toolName}</span>
          {/if}
          <div class="content">{b.content}</div>
        </article>
      {/each}
    {/if}
    {#if sending && !confirming}
      <div class="typing" aria-live="polite" aria-label="El asistente está escribiendo">
        <span class="typing-dots" aria-hidden="true">
          <span></span><span></span><span></span>
        </span>
        <span class="typing-label">Escribiendo…</span>
      </div>
    {/if}
  </div>

  {#if error}
    <div class="error-bar" role="alert">
      <span>{error}</span>
      <button type="button" on:click={() => agentStore.clearError()}>Cerrar</button>
    </div>
  {/if}

  <form class="composer" on:submit|preventDefault={onSubmit}>
    <textarea
      bind:value={draft}
      placeholder="Escribe un mensaje…"
      rows="2"
      disabled={sending}
      on:keydown={onKeydown}
      aria-label="Mensaje al asistente"
    ></textarea>
    <Button variant="filled" size="m" iconType="left" onclick={onSubmit} disabled={sending || !draft.trim()}>
      <Icon icon={sendIcon} />
      Enviar
    </Button>
  </form>
</section>

{#if pendingTool}
  <AgentConfirmDialog
    pending={pendingTool}
    busy={confirming || sending}
    onConfirm={() => agentStore.confirmPendingTool()}
    onCancel={() => agentStore.cancelPendingTool()}
  />
{/if}

<style>
  .agent-chat {
    display: flex;
    flex-direction: column;
    height: 100%;
    min-height: 0;
    max-width: 720px;
    margin: 0 auto;
    padding: 12px 16px 16px;
    gap: 12px;
  }

  .head {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    gap: 12px;
    flex-shrink: 0;
    padding-bottom: 4px;
    border-bottom: 1px solid color-mix(in srgb, var(--m3c-outline-variant, #c4c8be) 55%, transparent);
  }

  .title-row {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .avatar {
    display: grid;
    place-items: center;
    width: 40px;
    height: 40px;
    border-radius: 12px;
    background: var(--m3c-primary-container, #c8e6c9);
    color: var(--m3c-on-primary-container, #102015);
    flex-shrink: 0;
  }

  .title-row h1 {
    margin: 0;
    font-size: 1.2rem;
    font-weight: 700;
    letter-spacing: -0.02em;
  }

  .sub {
    margin: 2px 0 0;
    font-size: 0.78rem;
    color: var(--m3c-on-surface-variant, #44483e);
  }

  .messages {
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding: 8px 6px 8px 2px;
    scroll-behavior: smooth;
    scrollbar-width: thin;
    scrollbar-color: color-mix(in srgb, var(--m3c-outline-variant, #c4c8be) 80%, transparent) transparent;
  }

  .messages::-webkit-scrollbar {
    width: 8px;
  }
  .messages::-webkit-scrollbar-track {
    background: transparent;
    margin: 4px 0;
  }
  .messages::-webkit-scrollbar-thumb {
    background: color-mix(in srgb, var(--m3c-outline-variant, #c4c8be) 70%, transparent);
    border-radius: 999px;
    border: 2px solid transparent;
    background-clip: padding-box;
  }
  .messages::-webkit-scrollbar-thumb:hover {
    background: color-mix(in srgb, var(--m3c-on-surface-variant, #44483e) 45%, transparent);
    background-clip: padding-box;
    border: 2px solid transparent;
  }

  .empty {
    margin: auto;
    text-align: center;
    color: var(--m3c-on-surface-variant, #44483e);
    padding: 28px 20px;
    display: grid;
    gap: 8px;
    justify-items: center;
  }

  .empty-icon {
    display: grid;
    place-items: center;
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: color-mix(in srgb, var(--m3c-primary-container, #c8e6c9) 70%, transparent);
    color: var(--m3c-on-primary-container, #102015);
    margin-bottom: 6px;
  }

  .empty .hint {
    font-size: 0.85rem;
    opacity: 0.85;
    max-width: 320px;
  }

  .bubble {
    max-width: min(92%, 520px);
    padding: 11px 14px;
    border-radius: 16px;
    font-size: 0.95rem;
    line-height: 1.5;
    white-space: pre-wrap;
    word-break: break-word;
  }

  .bubble.user {
    align-self: flex-end;
    background: var(--m3c-primary-container, #c8e6c9);
    color: var(--m3c-on-primary-container, #102015);
    border-bottom-right-radius: 4px;
  }

  .bubble.assistant {
    align-self: flex-start;
    background: var(--m3c-surface-container, #edf1eb);
    color: var(--m3c-on-surface, #1a1c19);
    border-bottom-left-radius: 4px;
  }

  .bubble.system {
    align-self: center;
    max-width: 100%;
    background: var(--m3c-surface-container-high, #e7ece6);
    font-size: 0.82rem;
    font-family: ui-monospace, monospace;
  }

  .bubble.system.error {
    border: 1px solid var(--m3c-error, #ba1a1a);
  }

  .tool-tag {
    display: inline-block;
    margin-bottom: 4px;
    font-size: 0.7rem;
    text-transform: uppercase;
    letter-spacing: 0.04em;
    opacity: 0.75;
  }

  .typing {
    align-self: flex-start;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: 16px;
    border-bottom-left-radius: 4px;
    background: var(--m3c-surface-container, #edf1eb);
    color: var(--m3c-on-surface-variant, #44483e);
    font-size: 0.85rem;
  }

  .typing-dots {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .typing-dots span {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--m3c-primary, #388e3c);
    opacity: 0.45;
    animation: typing-bounce 1.2s ease-in-out infinite;
  }

  .typing-dots span:nth-child(2) {
    animation-delay: 0.18s;
  }

  .typing-dots span:nth-child(3) {
    animation-delay: 0.36s;
  }

  @keyframes typing-bounce {
    0%,
    60%,
    100% {
      transform: translateY(0);
      opacity: 0.35;
    }
    30% {
      transform: translateY(-5px);
      opacity: 1;
    }
  }

  .typing-label {
    font-weight: 500;
  }

  .error-bar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    padding: 8px 12px;
    border-radius: 10px;
    background: color-mix(in srgb, var(--m3c-error, #ba1a1a) 16%, transparent);
    color: var(--m3c-error, #ba1a1a);
    font-size: 0.85rem;
    flex-shrink: 0;
  }

  .error-bar button {
    border: 0;
    background: transparent;
    color: inherit;
    cursor: pointer;
    font-weight: 600;
  }

  .composer {
    display: flex;
    gap: 10px;
    align-items: flex-end;
    flex-shrink: 0;
    padding-top: 6px;
    border-top: 1px solid color-mix(in srgb, var(--m3c-outline-variant, #c4c8be) 55%, transparent);
  }

  .composer textarea {
    flex: 1;
    resize: none;
    min-height: 52px;
    max-height: 140px;
    padding: 12px 14px;
    border-radius: 14px;
    border: 1px solid var(--m3c-outline-variant, #c4c8be);
    background: var(--m3c-surface, #fff);
    color: var(--m3c-on-surface, #1a1c19);
    font: inherit;
    font-size: 0.95rem;
    line-height: 1.4;
    transition: border-color 0.15s ease, box-shadow 0.15s ease;
  }

  .composer textarea:focus {
    outline: none;
    border-color: var(--m3c-primary, #388e3c);
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--m3c-primary, #388e3c) 22%, transparent);
  }

  .composer textarea:disabled {
    opacity: 0.7;
  }
</style>
