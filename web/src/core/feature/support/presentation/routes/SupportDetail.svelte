<script lang="ts">
    import { onDestroy, onMount, tick } from "svelte";
    import type { NavBackStackEntry } from "../../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportMessage } from "../../domain/entity/SupportMessage";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    const threadId = navBackStackEntry?.args?.id ?? "";
    let draft = "";
    let threadEl: HTMLDivElement | null = null;

    $: thread = threadId
        ? ($supportInboxStore.items.find((m) => m.id === threadId) as SupportMessage | undefined) ?? null
        : null;
    $: messages = $supportInboxStore.messages;
    $: messagesLoading = $supportInboxStore.messagesLoading;
    $: posting = $supportInboxStore.posting;
    $: closed = thread?.status === "cerrado";

    async function scrollBottom() {
        await tick();
        if (threadEl) threadEl.scrollTop = threadEl.scrollHeight;
    }
    $: if (messages.length) scrollBottom();

    onMount(() => {
        if (!threadId) return;
        const stop = supportInboxStore.startRealtime();
        Promise.all([
            supportInboxStore.syncMine().catch(() => {}),
            supportInboxStore.loadMessages(threadId)
        ]).catch((e) => {
            logger.error(e?.message ?? e, e?.stack);
            toastStore.error("No se pudo cargar la conversación");
        });
        return () => {
            stop();
            supportInboxStore.clearActive();
        };
    });

    onDestroy(() => supportInboxStore.clearActive());

    function back() {
        navController.popBackStack();
    }

    async function send() {
        if (!threadId || closed) return;
        const text = draft.trim();
        if (!text) return;
        try {
            await supportInboxStore.postUserReply(threadId, text);
            draft = "";
        } catch (e: any) {
            toastStore.error(e?.message ?? "No se pudo enviar");
        }
    }

    function onKey(e: KeyboardEvent) {
        if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
            e.preventDefault();
            send();
        }
    }
</script>

<section class="screen">
    <header class="head">
        <button class="back" type="button" on:click={back}>← Volver</button>
        <div>
            <h1>{thread?.subject || "Conversación"}</h1>
            <p class="muted">{thread ? thread.status : "…"}</p>
        </div>
    </header>

    {#if !threadId}
        <p class="muted">Falta el id del hilo.</p>
    {:else}
        <div class="chat" bind:this={threadEl}>
            {#if messagesLoading && messages.length === 0}
                <p class="muted center">Cargando mensajes…</p>
            {:else if messages.length === 0}
                <p class="muted center">Sin mensajes todavía.</p>
            {:else}
                {#each messages as msg (msg.id)}
                    <div class="bubble-row" class:me={msg.senderRole === "user"} class:them={msg.senderRole === "staff"}>
                        <div class="bubble">
                            <div class="meta">
                                <span>{msg.senderName || (msg.senderRole === "staff" ? "Soporte" : "Tú")}</span>
                                <span>{new Date(msg.createdAtIso).toLocaleString()}</span>
                            </div>
                            <p>{msg.body}</p>
                        </div>
                    </div>
                {/each}
            {/if}
        </div>

        <div class="composer">
            {#if closed}
                <p class="muted center">Este hilo está cerrado.</p>
            {:else}
                <textarea
                    rows="3"
                    placeholder="Escribe un mensaje… (Ctrl+Enter)"
                    bind:value={draft}
                    on:keydown={onKey}
                    disabled={posting}
                ></textarea>
                <button class="send" type="button" disabled={posting || !draft.trim()} on:click={send}>
                    {posting ? "Enviando…" : "Enviar"}
                </button>
            {/if}
        </div>
    {/if}
</section>

<style>
    .screen { padding: 12px 16px 24px; max-width: 720px; margin: 0 auto; display: flex; flex-direction: column; gap: 12px; min-height: min(70vh, 640px); }
    .head { display: flex; gap: 12px; align-items: flex-start; }
    .back { border: 0; background: transparent; color: inherit; cursor: pointer; font-weight: 650; padding: 6px 0; }
    h1 { margin: 0; font-size: 1.15rem; }
    .muted { margin: 0; opacity: 0.7; font-size: 0.88rem; }
    .chat { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 10px; padding: 12px; border-radius: 16px; border: 1px solid var(--md-sys-color-outline-variant, #444); min-height: 240px; max-height: 48vh; }
    .center { text-align: center; margin: auto; }
    .bubble-row { display: flex; width: 100%; }
    .bubble-row.me { justify-content: flex-end; }
    .bubble-row.them { justify-content: flex-start; }
    .bubble { max-width: 88%; border-radius: 14px; padding: 10px 12px; border: 1px solid var(--md-sys-color-outline-variant, #444); background: color-mix(in srgb, var(--md-sys-color-surface, #1c1b1f) 94%, transparent); }
    .bubble-row.me .bubble { background: color-mix(in srgb, var(--md-sys-color-primary, #6750a4) 18%, transparent); }
    .meta { display: flex; justify-content: space-between; gap: 10px; font-size: 0.7rem; opacity: 0.75; margin-bottom: 4px; }
    .bubble p { margin: 0; white-space: pre-wrap; word-break: break-word; line-height: 1.4; }
    .composer { display: grid; gap: 8px; }
    textarea { width: 100%; border-radius: 12px; border: 1px solid var(--md-sys-color-outline-variant, #444); background: transparent; color: inherit; padding: 12px; font: inherit; resize: vertical; }
    .send { justify-self: end; border-radius: 12px; border: 0; padding: 10px 16px; font-weight: 700; cursor: pointer; background: var(--md-sys-color-primary, #6750a4); color: var(--md-sys-color-on-primary, #fff); }
    .send:disabled { opacity: 0.5; cursor: not-allowed; }
</style>
