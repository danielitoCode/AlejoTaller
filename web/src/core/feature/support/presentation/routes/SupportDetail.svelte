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

    function statusTone(s: string): string {
        if (s === "cerrado" || s === "resuelto") return "done";
        if (s === "en_proceso") return "active";
        return "new";
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

    function formatMessageTime(iso: string): string {
        try {
            return new Date(iso).toLocaleTimeString(undefined, {
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
        <button class="back" type="button" on:click={back} aria-label="Volver a soporte">
            <span class="back-icon"><Icon icon={arrowBackIcon} /></span>
            <span>Soporte</span>
        </button>

        <div class="conversation-card">
            <div class="support-avatar" aria-hidden="true">
                <Icon icon={supportIcon} />
                <span class="online-dot"></span>
            </div>
            <div class="head-text">
                <div class="title-line">
                    <h1>{thread?.subject || "Conversación"}</h1>
                    {#if thread}
                        <span class:done={statusTone(thread.status) === "done"} class:active={statusTone(thread.status) === "active"} class:new={statusTone(thread.status) === "new"} class="status">
                            <span class="status-dot"></span>
                            {statusLabel(thread.status)}
                        </span>
                    {/if}
                </div>
                <div class="head-meta">
                    <span>Equipo de soporte</span>
                    {#if thread?.createdAtIso}
                        <span class="meta-separator">•</span>
                        <span>Desde {formatTime(thread.createdAtIso)}</span>
                    {/if}
                </div>
            </div>
        </div>
    </header>

    {#if !threadId}
        <div class="state surface-state">
            <div class="empty-chat-icon"><Icon icon={supportIcon} /></div>
            <p class="muted">Consulta no encontrada.</p>
        </div>
    {:else}
        <div class="conversation-panel">
            <div class="chat" bind:this={threadEl} aria-label="Mensajes de la conversación">
                <div class="chat-intro">
                    <div class="intro-line"><span></span><small>INICIO DE LA CONVERSACIÓN</small><span></span></div>
                    <p>Estamos aquí para ayudarte. Puedes escribirnos cualquier duda sobre tu pedido o nuestros productos.</p>
                </div>

                {#if messagesLoading && messages.length === 0}
                    <div class="state center">
                        <div class="spinner"></div>
                        <p class="muted">Cargando conversación…</p>
                    </div>
                {:else if messages.length === 0}
                    <div class="state center">
                        <div class="empty-chat-icon"><Icon icon={supportIcon} /></div>
                        <p class="muted">Todavía no hay mensajes.</p>
                        <span class="empty-hint">Escribe abajo para iniciar la conversación.</span>
                    </div>
                {:else}
                    {#each messages as msg (msg.id)}
                        {@const isMe = msg.senderRole === "user"}
                        <div class="bubble-row" class:me={isMe} class:them={!isMe}>
                            {#if !isMe}
                                <div class="message-avatar" aria-hidden="true"><Icon icon={supportIcon} /></div>
                            {/if}
                            <div class="message-stack">
                                <div class="bubble">
                                    <p>{msg.body}</p>
                                </div>
                                <div class="message-meta">
                                    <span>{isMe ? "Tú" : "Soporte"}</span>
                                    <span>{formatMessageTime(msg.createdAtIso)}</span>
                                </div>
                            </div>
                        </div>
                    {/each}
                {/if}
            </div>

            <div class="composer-shell">
                {#if closed}
                    <div class="closed-note">
                        <span class="closed-icon">✓</span>
                        <div>
                            <strong>Conversación cerrada</strong>
                            <span>Abre una nueva consulta si necesitas más ayuda.</span>
                        </div>
                    </div>
                {:else}
                    <div class="composer">
                        <textarea
                            rows="2"
                            placeholder="Escribe un mensaje…"
                            aria-label="Escribe un mensaje"
                            bind:value={draft}
                            on:keydown={onKey}
                            disabled={posting}
                        ></textarea>
                        <div class="composer-footer">
                            <span class="shortcut"><kbd>Ctrl</kbd><span>+</span><kbd>Enter</kbd> para enviar</span>
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
                        </div>
                    </div>
                {/if}
            </div>
        </div>
    {/if}
</section>

<style>
    .screen {
        --chat-radius: 24px;
        display: flex;
        flex-direction: column;
        gap: 14px;
        width: min(100%, 760px);
        height: min(82vh, 800px);
        min-height: 500px;
        margin: 0 auto;
        padding: 14px 18px 20px;
        box-sizing: border-box;
    }

    .head {
        display: grid;
        gap: 10px;
        flex: 0 0 auto;
    }

    .back {
        display: inline-flex;
        align-items: center;
        gap: 7px;
        width: fit-content;
        border: 0;
        background: transparent;
        color: var(--md-sys-color-on-surface-variant);
        cursor: pointer;
        font: inherit;
        font-size: 0.82rem;
        font-weight: 700;
        padding: 2px 0;
        transition: color 0.18s ease;
    }

    .back:hover { color: var(--md-sys-color-primary); }

    .back-icon {
        width: 30px;
        height: 30px;
        display: grid;
        place-items: center;
        border-radius: 10px;
        background: var(--md-sys-color-surface-container-high);
        border: 1px solid var(--md-sys-color-outline-variant);
        transition: transform 0.18s ease, background 0.18s ease;
    }

    .back:hover .back-icon {
        transform: translateX(-2px);
        background: var(--md-sys-color-primary-container);
    }

    .back :global(svg) { width: 18px; height: 18px; }

    .conversation-card {
        display: flex;
        align-items: center;
        gap: 13px;
        padding: 14px 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 18px;
        background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 8px 26px color-mix(in srgb, var(--md-sys-color-shadow, #000) 8%, transparent);
    }

    .support-avatar,
    .message-avatar {
        position: relative;
        flex: 0 0 auto;
        display: grid;
        place-items: center;
        border-radius: 14px;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
    }

    .support-avatar { width: 46px; height: 46px; border-radius: 15px; }
    .support-avatar :global(svg) { width: 24px; height: 24px; }

    .online-dot {
        position: absolute;
        right: -1px;
        bottom: -1px;
        width: 9px;
        height: 9px;
        border-radius: 50%;
        background: #42b883;
        border: 2px solid var(--md-sys-color-surface-container-high);
    }

    .head-text { min-width: 0; flex: 1; }

    .title-line {
        display: flex;
        align-items: center;
        gap: 9px;
        min-width: 0;
    }

    .head-text h1 {
        margin: 0;
        min-width: 0;
        font-size: 1.06rem;
        font-weight: 800;
        letter-spacing: -0.025em;
        color: var(--md-sys-color-on-surface);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .status {
        display: inline-flex;
        align-items: center;
        gap: 5px;
        flex: 0 0 auto;
        padding: 4px 8px;
        border-radius: 999px;
        background: var(--md-sys-color-surface-container-highest);
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.62rem;
        font-weight: 800;
        letter-spacing: 0.035em;
        text-transform: uppercase;
    }

    .status.active, .status.new { color: var(--md-sys-color-primary); }
    .status.done { opacity: 0.72; }
    .status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
    .status.active .status-dot { animation: pulse 1.7s ease-in-out infinite; }

    @keyframes pulse { 50% { opacity: 0.35; transform: scale(0.75); } }

    .head-meta {
        display: flex;
        align-items: center;
        gap: 7px;
        margin-top: 3px;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.72rem;
    }

    .meta-separator { opacity: 0.5; }

    /* The panel behaves like a real chat: messages scroll, composer never scrolls away. */
    .conversation-panel {
        flex: 1 1 auto;
        min-height: 0;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: var(--chat-radius);
        background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 14px 40px color-mix(in srgb, var(--md-sys-color-shadow, #000) 9%, transparent);
    }

    .chat {
        flex: 1 1 auto;
        min-height: 0;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 14px;
        padding: 20px 18px 18px;
        box-sizing: border-box;
        overscroll-behavior: contain;
        scrollbar-width: thin;
        scrollbar-color: var(--md-sys-color-outline-variant) transparent;
        background: var(--md-sys-color-surface-container-high);
    }

    .chat-intro {
        display: grid;
        gap: 8px;
        width: min(100%, 480px);
        margin: 0 auto 6px;
        text-align: center;
        flex: 0 0 auto;
    }

    .intro-line {
        display: flex;
        align-items: center;
        gap: 9px;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.62;
    }

    .intro-line span { height: 1px; flex: 1; background: var(--md-sys-color-outline-variant); }
    .intro-line small { font-size: 0.57rem; font-weight: 800; letter-spacing: 0.1em; white-space: nowrap; }
    .chat-intro p { margin: 0; color: var(--md-sys-color-on-surface-variant); font-size: 0.73rem; line-height: 1.45; }

    .state { display: grid; gap: 8px; justify-items: center; padding: 20px; }
    .center { margin: auto; text-align: center; }
    .surface-state { flex: 1; place-content: center; border: 1px solid var(--md-sys-color-outline-variant); border-radius: var(--chat-radius); background: var(--md-sys-color-surface-container-high); }

    .empty-chat-icon {
        width: 44px;
        height: 44px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
    }

    .empty-chat-icon :global(svg) { width: 23px; height: 23px; }
    .empty-hint { color: var(--md-sys-color-on-surface-variant); font-size: 0.72rem; }

    .spinner {
        width: 22px;
        height: 22px;
        border-radius: 999px;
        border: 2px solid var(--md-sys-color-outline-variant);
        border-top-color: var(--md-sys-color-primary);
        animation: spin 0.7s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .bubble-row {
        display: flex;
        align-items: flex-end;
        gap: 8px;
        width: 100%;
        animation: message-in 0.2s ease-out;
    }

    .bubble-row.me { justify-content: flex-end; }
    .bubble-row.them { justify-content: flex-start; }

    @keyframes message-in {
        from { opacity: 0; transform: translateY(5px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .message-avatar {
        width: 30px;
        height: 30px;
        border-radius: 10px;
        margin-bottom: 20px;
    }

    .message-avatar :global(svg) { width: 17px; height: 17px; }

    .message-stack {
        display: grid;
        gap: 4px;
        max-width: min(78%, 500px);
    }

    .bubble {
        padding: 11px 14px;
        border: 1px solid transparent;
        border-radius: 17px;
        box-shadow: 0 3px 10px color-mix(in srgb, var(--md-sys-color-shadow, #000) 6%, transparent);
    }

    /* Support: cool tinted surface, clearly distinct from the customer's messages. */
    .bubble-row.them .bubble {
        background: var(--md-sys-color-secondary-container);
        color: var(--md-sys-color-on-secondary-container);
        border-color: color-mix(in srgb, var(--md-sys-color-secondary) 22%, transparent);
        border-bottom-left-radius: 5px;
    }

    /* Customer: primary tinted surface. */
    .bubble-row.me .bubble {
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 24%, transparent);
        border-bottom-right-radius: 5px;
    }

    .bubble p {
        margin: 0;
        font-size: 0.9rem;
        line-height: 1.45;
        white-space: pre-wrap;
        word-break: break-word;
    }

    .message-meta {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 0 4px;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.63rem;
        line-height: 1;
        opacity: 0.78;
    }

    .bubble-row.me .message-meta { justify-content: flex-end; }

    .composer-shell {
        flex: 0 0 auto;
        padding: 10px 12px 12px;
        border-top: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container-highest);
        box-shadow: 0 -8px 20px color-mix(in srgb, var(--md-sys-color-shadow, #000) 5%, transparent);
        z-index: 2;
    }

    .composer {
        display: grid;
        gap: 7px;
        padding: 8px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        background: var(--md-sys-color-surface-container-high);
    }

    .composer textarea {
        display: block;
        width: 100%;
        min-height: 44px;
        max-height: 120px;
        box-sizing: border-box;
        border: 0;
        border-radius: 10px;
        outline: none;
        resize: none;
        background: transparent;
        color: var(--md-sys-color-on-surface);
        padding: 7px 9px;
        font: inherit;
        font-size: 0.9rem;
        line-height: 1.4;
    }

    .composer textarea::placeholder { color: var(--md-sys-color-on-surface-variant); opacity: 0.8; }

    .composer:focus-within {
        border-color: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 17%, transparent);
    }

    .composer-footer {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        padding: 0 3px 1px 7px;
    }

    .shortcut {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.62rem;
        opacity: 0.72;
    }

    kbd {
        padding: 2px 5px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-bottom-width: 2px;
        border-radius: 5px;
        background: var(--md-sys-color-surface);
        font: inherit;
        font-size: 0.6rem;
    }

    .composer :global(button) { flex: 0 0 auto; }

    .closed-note {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 7px 5px;
        color: var(--md-sys-color-on-surface-variant);
    }

    .closed-icon {
        width: 32px;
        height: 32px;
        flex: 0 0 auto;
        display: grid;
        place-items: center;
        border-radius: 10px;
        background: var(--md-sys-color-secondary-container);
        color: var(--md-sys-color-on-secondary-container);
        font-weight: 800;
    }

    .closed-note div { display: grid; gap: 2px; }
    .closed-note strong { font-size: 0.78rem; color: var(--md-sys-color-on-surface); }
    .closed-note span:last-child { font-size: 0.7rem; }

    @media (max-width: 600px) {
        .screen {
            width: 100%;
            height: calc(100dvh - 12px);
            min-height: 0;
            padding: 8px 10px 10px;
            gap: 10px;
        }

        .head { gap: 7px; }
        .conversation-card { padding: 11px 12px; border-radius: 16px; }
        .support-avatar { width: 40px; height: 40px; }
        .support-avatar :global(svg) { width: 21px; height: 21px; }
        .head-text h1 { font-size: 0.95rem; }
        .head-meta { font-size: 0.65rem; }
        .status { font-size: 0.56rem; padding: 3px 6px; }

        .conversation-panel { border-radius: 19px; }
        .chat { padding: 15px 10px 14px; gap: 12px; }
        .chat-intro { margin-bottom: 3px; }
        .chat-intro p { font-size: 0.68rem; }
        .message-stack { max-width: 84%; }
        .bubble { padding: 9px 11px; border-radius: 15px; }
        .bubble p { font-size: 0.84rem; }
        .message-avatar { width: 27px; height: 27px; margin-bottom: 18px; }
        .message-avatar :global(svg) { width: 15px; height: 15px; }

        .composer-shell { padding: 7px 7px 8px; }
        .composer { padding: 6px; border-radius: 14px; }
        .composer textarea { min-height: 40px; font-size: 0.85rem; }
        .shortcut { display: none; }
        .composer-footer { justify-content: flex-end; }
    }

    @media (prefers-reduced-motion: reduce) {
        .bubble-row, .status.active .status-dot, .spinner { animation: none; }
        .back-icon { transition: none; }
    }
</style>
