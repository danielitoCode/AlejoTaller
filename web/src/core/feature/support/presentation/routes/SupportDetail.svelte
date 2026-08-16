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
            <div class="avatar-orbit" aria-hidden="true">
                <div class="support-avatar"><Icon icon={supportIcon} /></div>
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
                    <span class="team-label">Equipo de soporte</span>
                    <span class="meta-separator">•</span>
                    <span>Atención en línea</span>
                    {#if thread?.createdAtIso}
                        <span class="meta-separator">•</span>
                        <span>Desde {formatTime(thread.createdAtIso)}</span>
                    {/if}
                </div>
            </div>

            <div class="header-signal" aria-hidden="true">
                <span></span><span></span><span></span>
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
            <div class="panel-glow panel-glow-a" aria-hidden="true"></div>
            <div class="panel-glow panel-glow-b" aria-hidden="true"></div>

            <div class="chat" bind:this={threadEl} aria-label="Mensajes de la conversación">
                <div class="chat-intro">
                    <div class="intro-line">
                        <span></span>
                        <small>INICIO DE LA CONVERSACIÓN</small>
                        <span></span>
                    </div>
                    <p>Estamos aquí para ayudarte con tu pedido, tus productos o cualquier duda que tengas.</p>
                    <div class="intro-badge"><span></span> Soporte conectado</div>
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
                    {#each messages as msg, index (msg.id)}
                        {@const isMe = msg.senderRole === "user"}
                        {@const previous = index > 0 ? messages[index - 1] : null}
                        {@const grouped = previous?.senderRole === msg.senderRole}
                        <div class="bubble-row" class:me={isMe} class:them={!isMe} class:grouped={grouped}>
                            {#if !isMe}
                                <div class="message-avatar" class:avatar-hidden={grouped} aria-hidden="true">
                                    <Icon icon={supportIcon} />
                                </div>
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
                        <div class="composer-input-row">
                            <span class="input-status" aria-hidden="true"><span></span></span>
                            <textarea
                                rows="2"
                                placeholder="Escribe un mensaje…"
                                aria-label="Escribe un mensaje"
                                bind:value={draft}
                                on:keydown={onKey}
                                disabled={posting}
                            ></textarea>
                        </div>
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
        --chat-radius: 26px;
        --chat-surface: var(--m3c-surface-container, var(--md-sys-color-surface-container));
        --chat-surface-high: var(--m3c-surface-container-high, var(--md-sys-color-surface-container-high));
        --chat-surface-low: var(--m3c-surface-container-low, var(--md-sys-color-surface-container-low));
        --chat-surface-highest: var(--md-sys-color-surface-container-highest, var(--m3c-surface-container-high));
        --chat-on-surface: var(--m3c-on-surface, var(--md-sys-color-on-surface));
        --chat-on-variant: var(--m3c-on-surface-variant, var(--md-sys-color-on-surface-variant));
        --chat-primary: var(--m3c-primary, var(--md-sys-color-primary));
        --chat-primary-container: var(--m3c-primary-container, var(--md-sys-color-primary-container));
        --chat-on-primary-container: var(--m3c-on-primary-container, var(--md-sys-color-on-primary-container));
        --chat-secondary: var(--m3c-secondary, var(--md-sys-color-secondary));
        --chat-secondary-container: var(--md-sys-color-secondary-container, color-mix(in srgb, var(--chat-surface-high) 84%, var(--chat-secondary) 16%));
        --chat-on-secondary-container: var(--md-sys-color-on-secondary-container, var(--chat-on-surface));
        --chat-outline: var(--m3c-outline-variant, var(--md-sys-color-outline-variant));
        display: flex;
        flex-direction: column;
        gap: 14px;
        width: min(100%, 820px);
        height: min(84vh, 820px);
        min-height: 500px;
        margin: 0 auto;
        padding: 12px 18px 20px;
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
        gap: 8px;
        width: fit-content;
        border: 0;
        background: transparent;
        color: var(--chat-on-variant);
        cursor: pointer;
        font: inherit;
        font-size: 0.82rem;
        font-weight: 750;
        padding: 2px 0;
        transition: color 0.18s ease, transform 0.18s ease;
    }

    .back:hover { color: var(--chat-primary); transform: translateX(-1px); }

    .back-icon {
        width: 32px;
        height: 32px;
        display: grid;
        place-items: center;
        border-radius: 11px;
        background: var(--chat-surface-high);
        border: 1px solid var(--chat-outline);
        box-shadow: 0 5px 16px color-mix(in srgb, var(--chat-primary) 6%, transparent);
        transition: transform 0.18s ease, background 0.18s ease, border-color 0.18s ease;
    }

    .back:hover .back-icon {
        transform: translateX(-2px);
        background: var(--chat-primary-container);
        border-color: color-mix(in srgb, var(--chat-primary) 28%, var(--chat-outline));
    }

    .back :global(svg) { width: 18px; height: 18px; }

    .conversation-card {
        position: relative;
        isolation: isolate;
        display: flex;
        align-items: center;
        gap: 14px;
        min-height: 70px;
        padding: 13px 16px;
        overflow: hidden;
        border: 1px solid color-mix(in srgb, var(--chat-primary) 13%, var(--chat-outline));
        border-radius: 20px;
        background:
            radial-gradient(circle at 4% 0%, color-mix(in srgb, var(--chat-primary) 15%, transparent), transparent 34%),
            linear-gradient(135deg, color-mix(in srgb, var(--chat-primary-container) 28%, var(--chat-surface-high)), var(--chat-surface-high) 58%, var(--chat-surface));
        box-shadow: 0 12px 30px color-mix(in srgb, var(--chat-primary) 7%, transparent), 0 3px 8px color-mix(in srgb, black 5%, transparent);
    }

    .conversation-card::after {
        content: "";
        position: absolute;
        inset: auto -20% -70% 38%;
        height: 130px;
        border-radius: 50%;
        background: color-mix(in srgb, var(--chat-secondary) 9%, transparent);
        filter: blur(24px);
        pointer-events: none;
        z-index: -1;
    }

    .avatar-orbit {
        position: relative;
        flex: 0 0 auto;
        width: 50px;
        height: 50px;
    }

    .avatar-orbit::before {
        content: "";
        position: absolute;
        inset: -5px;
        border: 1px solid color-mix(in srgb, var(--chat-primary) 18%, transparent);
        border-radius: 17px;
        opacity: 0.85;
    }

    .support-avatar,
    .message-avatar {
        display: grid;
        place-items: center;
        background: var(--chat-primary-container);
        color: var(--chat-on-primary-container);
    }

    .support-avatar {
        position: relative;
        width: 50px;
        height: 50px;
        border-radius: 16px;
        box-shadow: inset 0 1px 0 color-mix(in srgb, white 22%, transparent), 0 8px 18px color-mix(in srgb, var(--chat-primary) 14%, transparent);
    }

    .support-avatar :global(svg) { width: 26px; height: 26px; }

    .online-dot {
        position: absolute;
        right: -4px;
        bottom: -3px;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        background: var(--chat-primary);
        border: 3px solid var(--chat-surface-high);
        box-shadow: 0 0 0 2px color-mix(in srgb, var(--chat-primary) 15%, transparent);
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
        color: var(--chat-on-surface);
        font-size: 1.08rem;
        font-weight: 800;
        letter-spacing: -0.025em;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .status {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        flex: 0 0 auto;
        padding: 5px 9px;
        border: 1px solid color-mix(in srgb, currentColor 18%, transparent);
        border-radius: 999px;
        background: color-mix(in srgb, var(--chat-primary-container) 58%, var(--chat-surface-high));
        color: var(--chat-on-variant);
        font-size: 0.61rem;
        font-weight: 800;
        letter-spacing: 0.045em;
        text-transform: uppercase;
    }

    .status.active, .status.new { color: var(--chat-primary); }
    .status.done { opacity: 0.72; }
    .status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
    .status.active .status-dot { animation: pulse 1.7s ease-in-out infinite; }

    @keyframes pulse { 50% { opacity: 0.35; transform: scale(0.75); } }

    .head-meta {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 6px;
        margin-top: 4px;
        color: var(--chat-on-variant);
        font-size: 0.69rem;
    }

    .team-label { font-weight: 700; color: var(--chat-on-surface); opacity: 0.82; }
    .meta-separator { opacity: 0.45; }

    .header-signal {
        display: flex;
        align-items: flex-end;
        gap: 3px;
        height: 20px;
        margin-left: auto;
        padding: 0 3px;
        opacity: 0.7;
    }

    .header-signal span {
        width: 3px;
        border-radius: 999px;
        background: var(--chat-primary);
    }

    .header-signal span:nth-child(1) { height: 7px; opacity: 0.42; }
    .header-signal span:nth-child(2) { height: 12px; opacity: 0.66; }
    .header-signal span:nth-child(3) { height: 17px; }

    .conversation-panel {
        position: relative;
        isolation: isolate;
        flex: 1 1 auto;
        min-height: 0;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        border: 1px solid var(--chat-outline);
        border-radius: var(--chat-radius);
        background: var(--chat-surface);
        box-shadow: 0 20px 46px color-mix(in srgb, var(--chat-primary) 7%, transparent), 0 5px 16px color-mix(in srgb, black 6%, transparent);
    }

    .conversation-panel::before {
        content: "";
        position: absolute;
        inset: 0 0 auto;
        height: 1px;
        background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--chat-primary) 42%, transparent), color-mix(in srgb, var(--chat-secondary) 28%, transparent), transparent);
        z-index: 3;
        pointer-events: none;
    }

    .panel-glow {
        position: absolute;
        width: 260px;
        height: 180px;
        border-radius: 50%;
        filter: blur(45px);
        pointer-events: none;
        z-index: -1;
    }

    .panel-glow-a {
        top: -100px;
        left: -100px;
        background: color-mix(in srgb, var(--chat-primary) 7%, transparent);
    }

    .panel-glow-b {
        right: -120px;
        bottom: 80px;
        background: color-mix(in srgb, var(--chat-secondary) 5%, transparent);
    }

    .chat {
        position: relative;
        flex: 1 1 auto;
        min-height: 0;
        overflow-y: auto;
        display: flex;
        flex-direction: column;
        gap: 15px;
        padding: 22px 22px 20px;
        box-sizing: border-box;
        overscroll-behavior: contain;
        scrollbar-width: thin;
        scrollbar-color: color-mix(in srgb, var(--chat-primary) 25%, var(--chat-outline)) transparent;
        background:
            radial-gradient(circle at 50% 0%, color-mix(in srgb, var(--chat-primary) 4%, transparent), transparent 35%),
            linear-gradient(180deg, color-mix(in srgb, var(--chat-surface-high) 48%, transparent), transparent 34%);
    }

    .chat::before {
        content: "";
        position: absolute;
        inset: 0;
        background-image: radial-gradient(color-mix(in srgb, var(--chat-primary) 9%, transparent) 0.65px, transparent 0.65px);
        background-size: 24px 24px;
        mask-image: linear-gradient(to bottom, black, transparent 65%);
        opacity: 0.28;
        pointer-events: none;
    }

    .chat > * { position: relative; z-index: 1; }

    .chat-intro {
        display: grid;
        gap: 8px;
        width: min(100%, 540px);
        margin: 0 auto 7px;
        text-align: center;
        flex: 0 0 auto;
    }

    .intro-line {
        display: flex;
        align-items: center;
        gap: 10px;
        color: var(--chat-on-variant);
        opacity: 0.62;
    }

    .intro-line span { height: 1px; flex: 1; background: var(--chat-outline); }
    .intro-line small { font-size: 0.56rem; font-weight: 850; letter-spacing: 0.11em; white-space: nowrap; }
    .chat-intro p { color: var(--chat-on-variant); font-size: 0.72rem; line-height: 1.48; }

    .intro-badge {
        justify-self: center;
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 4px 8px;
        border: 1px solid color-mix(in srgb, var(--chat-primary) 14%, var(--chat-outline));
        border-radius: 999px;
        background: color-mix(in srgb, var(--chat-primary-container) 34%, var(--chat-surface-high));
        color: var(--chat-primary);
        font-size: 0.58rem;
        font-weight: 800;
    }

    .intro-badge span { width: 5px; height: 5px; border-radius: 50%; background: currentColor; box-shadow: 0 0 0 3px color-mix(in srgb, currentColor 12%, transparent); }

    .state { display: grid; gap: 8px; justify-items: center; padding: 20px; }
    .center { margin: auto; text-align: center; }
    .surface-state { flex: 1; place-content: center; border: 1px solid var(--chat-outline); border-radius: var(--chat-radius); background: var(--chat-surface); }

    .empty-chat-icon {
        width: 46px;
        height: 46px;
        border-radius: 15px;
        display: grid;
        place-items: center;
        background: var(--chat-primary-container);
        color: var(--chat-on-primary-container);
        box-shadow: 0 8px 20px color-mix(in srgb, var(--chat-primary) 10%, transparent);
    }

    .empty-chat-icon :global(svg) { width: 23px; height: 23px; }
    .muted { color: var(--chat-on-variant); }
    .empty-hint { color: var(--chat-on-variant); font-size: 0.72rem; opacity: 0.82; }

    .spinner {
        width: 22px;
        height: 22px;
        border-radius: 999px;
        border: 2px solid var(--chat-outline);
        border-top-color: var(--chat-primary);
        animation: spin 0.7s linear infinite;
    }

    @keyframes spin { to { transform: rotate(360deg); } }

    .bubble-row {
        display: flex;
        align-items: flex-end;
        gap: 9px;
        width: 100%;
        animation: message-in 0.2s ease-out;
    }

    .bubble-row.me { justify-content: flex-end; }
    .bubble-row.them { justify-content: flex-start; }
    .bubble-row.grouped { margin-top: -7px; }

    @keyframes message-in {
        from { opacity: 0; transform: translateY(5px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .message-avatar {
        width: 31px;
        height: 31px;
        flex: 0 0 31px;
        border-radius: 11px;
        margin-bottom: 20px;
        box-shadow: inset 0 1px 0 color-mix(in srgb, white 16%, transparent);
        transition: opacity 0.18s ease;
    }

    .message-avatar :global(svg) { width: 17px; height: 17px; }
    .message-avatar.avatar-hidden { visibility: hidden; }

    .message-stack {
        display: grid;
        gap: 4px;
        max-width: min(76%, 540px);
    }

    .bubble {
        position: relative;
        padding: 11px 14px;
        border: 1px solid transparent;
        border-radius: 18px;
        box-shadow: 0 5px 14px color-mix(in srgb, black 5%, transparent);
        transition: transform 0.16s ease, box-shadow 0.16s ease;
    }

    .bubble:hover { transform: translateY(-1px); box-shadow: 0 7px 18px color-mix(in srgb, black 7%, transparent); }

    .bubble-row.them .bubble {
        background: linear-gradient(145deg, var(--chat-secondary-container), color-mix(in srgb, var(--chat-secondary-container) 78%, var(--chat-surface-high)));
        color: var(--chat-on-secondary-container);
        border-color: color-mix(in srgb, var(--chat-secondary) 19%, transparent);
        border-bottom-left-radius: 6px;
    }

    .bubble-row.me .bubble {
        background: linear-gradient(145deg, var(--chat-primary-container), color-mix(in srgb, var(--chat-primary-container) 84%, var(--chat-primary) 16%));
        color: var(--chat-on-primary-container);
        border-color: color-mix(in srgb, var(--chat-primary) 24%, transparent);
        border-bottom-right-radius: 6px;
        box-shadow: 0 6px 17px color-mix(in srgb, var(--chat-primary) 10%, transparent);
    }

    .bubble p {
        margin: 0;
        font-size: 0.9rem;
        line-height: 1.46;
        white-space: pre-wrap;
        word-break: break-word;
    }

    .message-meta {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 0 5px;
        color: var(--chat-on-variant);
        font-size: 0.61rem;
        line-height: 1;
        opacity: 0.76;
    }

    .message-meta span:first-child { font-weight: 700; }
    .bubble-row.me .message-meta { justify-content: flex-end; }

    .composer-shell {
        position: relative;
        flex: 0 0 auto;
        padding: 10px 12px 12px;
        border-top: 1px solid var(--chat-outline);
        background: color-mix(in srgb, var(--chat-surface-high) 92%, var(--chat-primary) 8%);
        box-shadow: 0 -10px 24px color-mix(in srgb, black 6%, transparent);
        z-index: 2;
    }

    .composer {
        display: grid;
        gap: 7px;
        padding: 8px;
        border: 1px solid var(--chat-outline);
        border-radius: 18px;
        background: var(--chat-surface-high);
        box-shadow: inset 0 1px 0 color-mix(in srgb, white 10%, transparent), 0 5px 14px color-mix(in srgb, black 5%, transparent);
        transition: border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
    }

    .composer:focus-within {
        border-color: color-mix(in srgb, var(--chat-primary) 70%, var(--chat-outline));
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--chat-primary) 13%, transparent), 0 8px 18px color-mix(in srgb, var(--chat-primary) 7%, transparent);
        transform: translateY(-1px);
    }

    .composer-input-row { display: flex; align-items: flex-start; gap: 8px; }

    .input-status {
        display: grid;
        place-items: center;
        width: 26px;
        height: 26px;
        flex: 0 0 26px;
        margin-top: 8px;
        border-radius: 9px;
        background: var(--chat-primary-container);
    }

    .input-status span { width: 7px; height: 7px; border-radius: 50%; background: var(--chat-primary); box-shadow: 0 0 0 4px color-mix(in srgb, var(--chat-primary) 12%, transparent); }

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
        color: var(--chat-on-surface);
        padding: 7px 8px;
        font: inherit;
        font-size: 0.9rem;
        line-height: 1.4;
    }

    .composer textarea::placeholder { color: var(--chat-on-variant); opacity: 0.8; }

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
        color: var(--chat-on-variant);
        font-size: 0.62rem;
        opacity: 0.72;
    }

    kbd {
        padding: 2px 5px;
        border: 1px solid var(--chat-outline);
        border-bottom-width: 2px;
        border-radius: 5px;
        background: var(--chat-surface);
        color: var(--chat-on-variant);
        font: inherit;
        font-size: 0.6rem;
    }

    .composer :global(button) { flex: 0 0 auto; }

    .closed-note {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 7px 5px;
        color: var(--chat-on-variant);
    }

    .closed-icon {
        width: 34px;
        height: 34px;
        flex: 0 0 auto;
        display: grid;
        place-items: center;
        border-radius: 11px;
        background: var(--chat-secondary-container);
        color: var(--chat-on-secondary-container);
        font-weight: 800;
    }

    .closed-note div { display: grid; gap: 2px; }
    .closed-note strong { font-size: 0.78rem; color: var(--chat-on-surface); }
    .closed-note span:last-child { font-size: 0.7rem; }

    @media (max-width: 700px) {
        .screen {
            width: 100%;
            height: calc(100dvh - 8px);
            min-height: 0;
            padding: 8px 10px 10px;
            gap: 9px;
        }

        .head { gap: 7px; }
        .back { font-size: 0.78rem; }
        .back-icon { width: 30px; height: 30px; }
        .conversation-card { min-height: 58px; padding: 10px 11px; border-radius: 17px; gap: 11px; }
        .avatar-orbit, .support-avatar { width: 42px; height: 42px; }
        .avatar-orbit::before { border-radius: 14px; }
        .support-avatar { border-radius: 14px; }
        .support-avatar :global(svg) { width: 22px; height: 22px; }
        .head-text h1 { font-size: 0.92rem; }
        .head-meta { font-size: 0.61rem; gap: 5px; }
        .status { font-size: 0.54rem; padding: 4px 7px; }
        .header-signal { display: none; }

        .conversation-panel { border-radius: 20px; }
        .chat { padding: 16px 10px 14px; gap: 13px; }
        .chat-intro { margin-bottom: 3px; }
        .chat-intro p { font-size: 0.67rem; }
        .intro-badge { font-size: 0.55rem; }
        .message-stack { max-width: 84%; }
        .bubble { padding: 9px 11px; border-radius: 15px; }
        .bubble p { font-size: 0.84rem; }
        .message-avatar { width: 27px; height: 27px; flex-basis: 27px; margin-bottom: 18px; }
        .message-avatar :global(svg) { width: 15px; height: 15px; }
        .message-meta { font-size: 0.58rem; }

        .composer-shell { padding: 7px 7px 8px; }
        .composer { padding: 6px; border-radius: 15px; }
        .input-status { width: 24px; height: 24px; flex-basis: 24px; }
        .composer textarea { min-height: 40px; font-size: 0.85rem; }
        .shortcut { display: none; }
        .composer-footer { justify-content: flex-end; }
    }

    @media (prefers-reduced-motion: reduce) {
        .bubble-row, .status.active .status-dot, .spinner { animation: none; }
        .back, .back-icon, .bubble, .composer { transition: none; }
    }
</style>
