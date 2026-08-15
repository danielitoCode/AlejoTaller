<script lang="ts">
    import { onDestroy, onMount, tick } from "svelte";
    import { Button, Icon } from "m3-svelte";
    import type { NavBackStackEntry } from "../../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportMessage } from "../../domain/entity/SupportMessage";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import sendIcon from "@ktibow/iconset-material-symbols/send-rounded";
    import supportIcon from "@ktibow/iconset-material-symbols/support-agent-rounded";

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
        const tasks = [
            supportInboxStore.syncMine().catch(() => {}),
            supportInboxStore.loadMessages(threadId)
        ];
        Promise.all(tasks)
            .then(() => supportInboxStore.markUserRead(threadId))
            .catch((e) => {
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
            void send();
        }
    }

    function statusLabel(s: string): string {
        if (s === "nuevo") return "Nuevo";
        if (s === "en_proceso") return "En proceso";
        if (s === "resuelto") return "Resuelto";
        if (s === "cerrado") return "Cerrado";
        return s ?? "";
    }

    function formatTime(iso: string): string {
        try {
            return new Date(iso).toLocaleString(undefined, {
                day: "2-digit",
                month: "short",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return "";
        }
    }
</script>

<section class="screen">
    <header class="head">
        <button class="back" type="button" on:click={back} aria-label="Volver">
            <Icon icon={arrowBackIcon} />
            <span>Volver</span>
        </button>
        <div class="head-text">
            <h1>{thread?.subject || "Conversación"}</h1>
            {#if thread}
                <div class="head-meta">
                    <span class="pill">{statusLabel(thread.status)}</span>
                    {#if thread.createdAtIso}
                        <span class="muted">{formatTime(thread.createdAtIso)}</span>
                    {/if}
                </div>
            {/if}
        </div>
    </header>

    {#if !threadId}
        <div class="state">
            <p class="muted">Consulta no encontrada.</p>
        </div>
    {:else}
        <div class="chat" bind:this={threadEl}>
            {#if messagesLoading && messages.length === 0}
                <div class="state center">
                    <div class="spinner"></div>
                    <p class="muted">Cargando mensajes…</p>
                </div>
            {:else if messages.length === 0}
                <div class="state center">
                    <div class="empty-chat-icon">
                        <Icon icon={supportIcon} />
                    </div>
                    <p class="muted">Sin mensajes aún. Escribe el primero abajo.</p>
                </div>
            {:else}
                {#each messages as msg (msg.id)}
                    {@const isMe = msg.senderRole === "user"}
                    <div class="bubble-row" class:me={isMe} class:them={!isMe}>
                        <div class="bubble">
                            <div class="meta">
                                <span>{isMe ? "Tú" : "Soporte"}</span>
                                <span>{formatTime(msg.createdAtIso)}</span>
                            </div>
                            <p>{msg.body}</p>
                        </div>
                    </div>
                {/each}
            {/if}
        </div>

        <div class="composer">
            {#if closed}
                <p class="closed-note">Esta consulta está cerrada. Abre una nueva si necesitas más ayuda.</p>
            {:else}
                <textarea
                    rows="2"
                    placeholder="Escribe un mensaje… (Ctrl+Enter)"
                    bind:value={draft}
                    on:keydown={onKey}
                    disabled={posting}
                ></textarea>
                <Button
                    variant="filled"
                    size="s"
                    iconType="left"
                    disabled={posting || !draft.trim()}
                    onclick={send}
                >
                    <Icon icon={sendIcon} />
                    {posting ? "Enviando…" : "Enviar"}
                </Button>
            {/if}
        </div>
    {/if}
</section>

<style>
    .screen {
        display: flex;
        flex-direction: column;
        gap: 12px;
        max-width: 640px;
        margin: 0 auto;
        padding: 10px 16px 20px;
        min-height: min(72vh, 680px);
        height: 100%;
        box-sizing: border-box;
    }

    .head {
        display: grid;
        gap: 6px;
    }

    .back {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        width: fit-content;
        border: 0;
        background: transparent;
        color: var(--md-sys-color-primary);
        cursor: pointer;
        font: inherit;
        font-size: 0.86rem;
        font-weight: 650;
        padding: 2px 0;
    }

    .back :global(svg) {
        width: 18px;
        height: 18px;
    }

    .head-text h1 {
        margin: 0;
        font-size: 1.12rem;
        font-weight: 800;
        letter-spacing: -0.02em;
        color: var(--md-sys-color-on-surface);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .head-meta {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
    }

    .muted {
        margin: 0;
        font-size: 0.8rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    .pill {
        font-size: 0.65rem;
        font-weight: 750;
        letter-spacing: 0.03em;
        text-transform: uppercase;
        padding: 3px 8px;
        border-radius: 999px;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent);
    }

    .chat {
        flex: 1;
        min-height: 200px;
        max-height: 52vh;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 10px;
        padding: 14px;
        border-radius: 20px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container);
        overscroll-behavior: contain;
    }

    .state {
        display: grid;
        gap: 8px;
        justify-items: center;
        padding: 16px;
    }

    .center {
        margin: auto;
        text-align: center;
    }

    .empty-chat-icon {
        width: 40px;
        height: 40px;
        border-radius: 12px;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        margin-bottom: 4px;
    }

    .empty-chat-icon :global(svg) {
        width: 22px;
        height: 22px;
    }

    .spinner {
        width: 20px;
        height: 20px;
        border-radius: 999px;
        border: 2px solid var(--md-sys-color-outline-variant);
        border-top-color: var(--md-sys-color-primary);
        animation: spin 0.7s linear infinite;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    .bubble-row {
        display: flex;
        width: 100%;
    }

    .bubble-row.me {
        justify-content: flex-end;
    }

    .bubble-row.them {
        justify-content: flex-start;
    }

    .bubble {
        max-width: min(86%, 420px);
        border-radius: 16px;
        padding: 9px 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container-high);
    }

    .bubble-row.me .bubble {
        border-radius: 16px 16px 4px 16px;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 30%, transparent);
    }

    .bubble-row.them .bubble {
        border-radius: 16px 16px 16px 4px;
        background: var(--md-sys-color-surface);
    }

    .meta {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        font-size: 0.68rem;
        font-weight: 650;
        opacity: 0.8;
        margin-bottom: 3px;
    }

    .bubble p {
        margin: 0;
        font-size: 0.9rem;
        line-height: 1.4;
        white-space: pre-wrap;
        word-break: break-word;
    }

    .composer {
        display: grid;
        gap: 10px;
        padding: 12px;
        border-radius: 18px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container);
    }

    .composer textarea {
        width: 100%;
        box-sizing: border-box;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        padding: 10px 12px;
        font: inherit;
        font-size: 0.92rem;
        resize: none;
        outline: none;
        line-height: 1.4;
        transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }

    .composer textarea:focus {
        border-color: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
    }

    .composer :global(button) {
        justify-self: end;
    }

    .closed-note {
        margin: 0;
        text-align: center;
        font-size: 0.86rem;
        color: var(--md-sys-color-on-surface-variant);
        padding: 4px 0;
    }

    @media (max-width: 480px) {
        .screen { padding: 8px 12px 16px; }
        .chat { padding: 10px; max-height: 48vh; }
    }
</style>
