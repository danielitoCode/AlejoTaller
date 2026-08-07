<script lang="ts">
    import type {LogEntry} from "../viewmodel/log.store";
    import {logStore} from "../viewmodel/log.store";

    type PanelSize = "collapsed" | "normal" | "large" | "full";

    let container: HTMLDivElement | null = null;
    let size: PanelSize = "normal";
    let copyFeedback = "";
    let copyTimer: ReturnType<typeof setTimeout> | null = null;

    $: logs = $logStore as LogEntry[];
    $: errorCount = logs.filter((l) => l.type === "error").length;
    $: expanded = size !== "collapsed";

    $: if (expanded && container) {
        // auto-scroll al final solo si el usuario está cerca del fondo
        const nearBottom =
            container.scrollHeight - container.scrollTop - container.clientHeight < 80;
        if (nearBottom) {
            container.scrollTop = container.scrollHeight;
        }
    }

    function formatEntry(log: LogEntry): string {
        const head = `[${log.type.toUpperCase()}] ${log.timestamp.toLocaleTimeString()} — ${log.message}`;
        if (log.stack) {
            return `${head}\n--- stack ---\n${log.stack}\n`;
        }
        return head;
    }

    function formatAll(): string {
        return logs.map(formatEntry).join("\n\n");
    }

    async function writeClipboard(text: string, okLabel = "Copiado"): Promise<void> {
        try {
            await navigator.clipboard.writeText(text);
            showCopyFeedback(okLabel);
        } catch {
            // fallback execCommand
            try {
                const ta = document.createElement("textarea");
                ta.value = text;
                ta.style.position = "fixed";
                ta.style.left = "-9999px";
                document.body.appendChild(ta);
                ta.select();
                document.execCommand("copy");
                document.body.removeChild(ta);
                showCopyFeedback(okLabel);
            } catch {
                showCopyFeedback("No se pudo copiar");
            }
        }
    }

    function showCopyFeedback(msg: string) {
        copyFeedback = msg;
        if (copyTimer) clearTimeout(copyTimer);
        copyTimer = setTimeout(() => {
            copyFeedback = "";
        }, 1600);
    }

    function cycleSize() {
        const order: PanelSize[] = ["collapsed", "normal", "large", "full"];
        const i = order.indexOf(size);
        size = order[(i + 1) % order.length];
    }

    function setSize(next: PanelSize) {
        size = next;
    }

    function clear() {
        logStore.clear();
    }

    function copyAll() {
        void writeClipboard(formatAll(), "Todo copiado (con stacks)");
    }

    function copyEntry(log: LogEntry) {
        void writeClipboard(formatEntry(log), "Entrada copiada");
    }

    function copySelection() {
        const selection = window.getSelection()?.toString();
        if (selection?.trim()) {
            void writeClipboard(selection, "Selección copiada");
        }
    }
</script>

<div
    class="logs-wrapper size-{size}"
    aria-label="Logs de desarrollo"
>
    <div class="header">
        <button
            class="toggle"
            type="button"
            on:click={cycleSize}
            aria-expanded={expanded}
            title="Clic para ciclar: recoger → normal → grande → pantalla completa"
        >
            <span class="left">
                <span class="title">Logs</span>
                {#if errorCount > 0}
                    <span class="badge" aria-label="Errores">{errorCount}</span>
                {/if}
                <span class="size-hint">{size}</span>
            </span>
            <span class="chev" aria-hidden="true">
                {#if size === "collapsed"}▲
                {:else if size === "full"}⤓
                {:else}▼
                {/if}
            </span>
        </button>

        <div class="actions">
            {#if copyFeedback}
                <span class="copy-feedback" role="status">{copyFeedback}</span>
            {/if}
            <button
                class="action-btn"
                type="button"
                title="Tamaño normal"
                on:click={() => setSize("normal")}
            >▭</button>
            <button
                class="action-btn"
                type="button"
                title="Grande"
                on:click={() => setSize("large")}
            >▢</button>
            <button
                class="action-btn"
                type="button"
                title="Pantalla completa"
                on:click={() => setSize("full")}
            >⛶</button>
            <button
                class="action-btn"
                type="button"
                title="Recoger"
                on:click={() => setSize("collapsed")}
            >—</button>
            <button
                class="action-btn"
                type="button"
                title="Copiar todos los logs incluyendo stack traces"
                on:click={copyAll}
            >📋 Copy</button>
            <button
                class="action-btn"
                type="button"
                title="Limpiar logs"
                on:click={clear}
            >🗑 Clear</button>
        </div>
    </div>

    {#if expanded}
        <div
            class="body selectable"
            bind:this={container}
            on:mouseup={copySelection}
            role="log"
            aria-live="polite"
        >
            {#if logs.length === 0}
                <div class="empty">Sin logs todavía</div>
            {:else}
                {#each logs as log (log.id)}
                    <div class="log {log.type}">
                        <div class="meta">
                            <span class="time">{log.timestamp.toLocaleTimeString()}</span>
                            <span class="type">{log.type.toUpperCase()}</span>
                            <button
                                class="entry-copy"
                                type="button"
                                title="Copiar esta entrada (mensaje + stack)"
                                on:click={() => copyEntry(log)}
                            >copy</button>
                        </div>

                        <div class="message selectable">{log.message}</div>

                        {#if log.stack}
                            <details open={log.type === "error"}>
                                <summary>Stack trace</summary>
                                <pre class="selectable stack">{log.stack}</pre>
                            </details>
                        {/if}
                    </div>
                {/each}
            {/if}
        </div>
    {/if}
</div>

<style>
    .logs-wrapper {
        position: fixed;
        bottom: 0;
        right: 0;
        width: min(520px, calc(100vw - 16px));
        font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace;
        background: color-mix(in srgb, #0b1220 94%, transparent);
        color: #e2e8f0;
        border-radius: 14px 14px 0 0;
        box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.45);
        overflow: hidden;
        transition: height 0.22s ease, width 0.22s ease, max-width 0.22s ease;
        z-index: 9999;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        margin: 0 8px;
        display: flex;
        flex-direction: column;
    }

    .logs-wrapper.size-collapsed {
        height: 48px;
    }

    .logs-wrapper.size-normal {
        height: min(340px, 45vh);
    }

    .logs-wrapper.size-large {
        height: min(70vh, 720px);
        width: min(720px, calc(100vw - 16px));
    }

    .logs-wrapper.size-full {
        height: calc(100dvh - 8px);
        width: calc(100vw - 16px);
        max-width: none;
        right: 8px;
        left: 8px;
        margin: 0;
        border-radius: 14px;
        bottom: 4px;
    }

    .header {
        flex-shrink: 0;
        background: color-mix(in srgb, #1e293b 88%, transparent);
        padding: 8px 10px;
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 10px;
        align-items: center;
        border-bottom: 1px solid rgba(148, 163, 184, 0.18);
    }

    .toggle {
        width: 100%;
        text-align: left;
        background: transparent;
        border: 1px solid rgba(148, 163, 184, 0.22);
        color: inherit;
        padding: 6px 10px;
        border-radius: 10px;
        cursor: pointer;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        user-select: none;
    }

    .toggle:hover {
        border-color: rgba(148, 163, 184, 0.35);
        background: rgba(255, 255, 255, 0.04);
    }

    .toggle:focus-visible,
    .action-btn:focus-visible,
    .entry-copy:focus-visible {
        outline: 2px solid rgba(56, 189, 248, 0.45);
        outline-offset: 2px;
    }

    .left {
        display: inline-flex;
        gap: 8px;
        align-items: center;
        min-width: 0;
    }

    .title {
        font-weight: 900;
        letter-spacing: -0.01em;
    }

    .size-hint {
        font-size: 10px;
        opacity: 0.55;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .chev {
        opacity: 0.85;
    }

    .badge {
        background: #ef4444;
        color: white;
        padding: 2px 6px;
        border-radius: 999px;
        font-size: 11px;
        font-weight: 900;
    }

    .actions {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        align-items: center;
        justify-content: flex-end;
    }

    .copy-feedback {
        font-size: 11px;
        color: #4ade80;
        font-weight: 700;
        white-space: nowrap;
    }

    .action-btn {
        background: transparent;
        border: 1px solid rgba(148, 163, 184, 0.22);
        color: #cbd5e1;
        font-size: 12px;
        padding: 6px 10px;
        border-radius: 10px;
        cursor: pointer;
        user-select: none;
    }

    .action-btn:hover {
        border-color: rgba(148, 163, 184, 0.35);
        background: rgba(255, 255, 255, 0.04);
    }

    .body {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overflow-x: auto;
        padding: 10px;
        -webkit-overflow-scrolling: touch;
    }

    .empty {
        opacity: 0.6;
        padding: 16px;
        text-align: center;
        font-size: 13px;
    }

    .log {
        margin-bottom: 10px;
        padding: 8px 10px;
        border-radius: 10px;
        border: 1px solid rgba(148, 163, 184, 0.16);
        background: rgba(255, 255, 255, 0.03);
    }

    .log.error {
        border-left: 4px solid #ef4444;
    }
    .log.warn {
        border-left: 4px solid #f59e0b;
    }
    .log.info {
        border-left: 4px solid #38bdf8;
    }
    .log.log {
        border-left: 4px solid #94a3b8;
    }

    .meta {
        font-size: 11px;
        opacity: 0.85;
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
    }

    .meta .type {
        font-weight: 800;
        letter-spacing: 0.03em;
    }

    .entry-copy {
        margin-left: auto;
        background: transparent;
        border: 1px solid rgba(148, 163, 184, 0.25);
        color: #94a3b8;
        font-size: 10px;
        padding: 2px 8px;
        border-radius: 999px;
        cursor: pointer;
        text-transform: uppercase;
        letter-spacing: 0.04em;
    }

    .entry-copy:hover {
        color: #e2e8f0;
        border-color: rgba(148, 163, 184, 0.45);
    }

    .message {
        margin-top: 6px;
        white-space: pre-wrap;
        word-break: break-word;
    }

    .selectable {
        user-select: text;
        -webkit-user-select: text;
        cursor: text;
    }

    details {
        margin-top: 8px;
    }

    summary {
        cursor: pointer;
        opacity: 0.85;
        user-select: none;
    }

    pre.stack {
        margin: 8px 0 0 0;
        padding: 10px;
        border-radius: 10px;
        background: rgba(0, 0, 0, 0.35);
        overflow: auto;
        max-height: 40vh;
        white-space: pre-wrap;
        word-break: break-word;
        font-size: 11px;
        line-height: 1.45;
    }

    @media (max-width: 640px) {
        .logs-wrapper {
            width: calc(100vw - 16px);
            right: 8px;
            left: 8px;
            margin: 0;
        }

        .logs-wrapper.size-large,
        .logs-wrapper.size-full {
            width: calc(100vw - 16px);
        }

        .actions .action-btn:nth-child(n + 2):nth-child(-n + 5) {
            /* keep size buttons */
        }
    }
</style>
