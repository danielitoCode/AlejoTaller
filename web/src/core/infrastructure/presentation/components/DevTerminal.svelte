<script lang="ts">
    import type {LogEntry} from "../viewmodel/log.store";
    import {logStore} from "../viewmodel/log.store";

    let container: HTMLDivElement | null = null;
    let expanded = true;

    $: logs = $logStore as LogEntry[];
    $: errorCount = logs.filter((l) => l.type === "error").length;

    $: if (expanded && container) {
        container.scrollTop = container.scrollHeight;
    }

    function toggle() {
        expanded = !expanded;
    }

    function clear() {
        logStore.clear();
    }
</script>

<div class="logs-wrapper {expanded ? 'open' : 'closed'}" aria-label="Logs de desarrollo">
    <div class="header">
        <button class="toggle" type="button" on:click={toggle} aria-expanded={expanded}>
            <span class="left">
                <span class="title">Logs</span>
                {#if errorCount > 0}
                    <span class="badge" aria-label="Errores">{errorCount}</span>
                {/if}
            </span>
            <span class="chev" aria-hidden="true">{expanded ? "▼" : "▲"}</span>
        </button>

        <button class="clear" type="button" on:click={clear} aria-label="Limpiar logs">Clear</button>
    </div>

    {#if expanded}
        <div class="body" bind:this={container}>
            {#each logs as log (log.id)}
                <div class="log {log.type}">
                    <div class="meta">
                        <span class="time">{log.timestamp.toLocaleTimeString()}</span>
                        <span class="type">{log.type.toUpperCase()}</span>
                    </div>

                    <div class="message">{log.message}</div>

                    {#if log.stack}
                        <details>
                            <summary>Stack trace</summary>
                            <pre>{log.stack}</pre>
                        </details>
                    {/if}
                </div>
            {/each}
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
        background: color-mix(in srgb, #0b1220 92%, transparent);
        color: #e2e8f0;
        border-radius: 14px 14px 0 0;
        box-shadow: 0 -10px 40px rgba(0, 0, 0, 0.45);
        overflow: hidden;
        transition: height 0.22s ease;
        z-index: 9999;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        margin: 0 8px;
    }

    .logs-wrapper.closed {
        height: 44px;
    }

    .logs-wrapper.open {
        height: 340px;
    }

    .header {
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
    .clear:focus-visible {
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

    .clear {
        background: transparent;
        border: 1px solid rgba(148, 163, 184, 0.22);
        color: #cbd5e1;
        font-size: 12px;
        padding: 6px 10px;
        border-radius: 10px;
        cursor: pointer;
    }

    .clear:hover {
        border-color: rgba(148, 163, 184, 0.35);
        background: rgba(255, 255, 255, 0.04);
    }

    .body {
        overflow-y: auto;
        height: calc(100% - 56px);
        padding: 10px;
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
        opacity: 0.7;
        display: flex;
        justify-content: space-between;
        gap: 10px;
    }

    .message {
        margin-top: 6px;
        white-space: pre-wrap;
        word-break: break-word;
    }

    details {
        margin-top: 8px;
    }

    summary {
        cursor: pointer;
        opacity: 0.85;
    }

    pre {
        margin: 8px 0 0 0;
        padding: 10px;
        border-radius: 10px;
        background: rgba(0, 0, 0, 0.35);
        overflow: auto;
    }
</style>

