<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card, Icon } from "m3-svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { supportDetail } from "../../../../infrastructure/presentation/navigation/nested.router";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportReason } from "../../domain/entity/SupportMessage";
    import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";
    import supportIcon from "@ktibow/iconset-material-symbols/support-agent-rounded";
    import addIcon from "@ktibow/iconset-material-symbols/add-rounded";
    import chatIcon from "@ktibow/iconset-material-symbols/chat-rounded";
    import sendIcon from "@ktibow/iconset-material-symbols/send-rounded";
    import lockIcon from "@ktibow/iconset-material-symbols/lock-outline";
    import inboxIcon from "@ktibow/iconset-material-symbols/inbox-rounded";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";

    export let navController: NavController;

    let ready = false;
    let isGuest = false;
    let showForm = false;
    let reason: SupportReason = "soporte";
    let subject = "";
    let body = "";

    $: items = $supportInboxStore.items;
    $: loading = $supportInboxStore.loading;
    $: creating = $supportInboxStore.creating;
    $: sorted = [...items].sort((a, b) => b.createdAtIso.localeCompare(a.createdAtIso));

    onMount(() => {
        let stopRt: (() => void) | null = null;
        (async () => {
            try {
                if ($sessionStore.isGuest) {
                    isGuest = true;
                    ready = true;
                    return;
                }
                await supportInboxStore.syncMine();
                stopRt = supportInboxStore.startRealtime();
            } catch (e: any) {
                logger.error(e?.message ?? e, e?.stack);
                isGuest = true;
            } finally {
                ready = true;
            }
        })();
        return () => stopRt?.();
    });

    function open(id: string) {
        navController.navigate(supportDetail.path, { id });
    }

    function statusLabel(s: string): string {
        if (s === "nuevo") return "Nuevo";
        if (s === "en_proceso") return "En proceso";
        if (s === "resuelto") return "Resuelto";
        if (s === "cerrado") return "Cerrado";
        return s;
    }

    function statusTone(s: string): string {
        if (s === "nuevo") return "tone-new";
        if (s === "en_proceso") return "tone-progress";
        if (s === "resuelto") return "tone-done";
        if (s === "cerrado") return "tone-closed";
        return "";
    }

    function reasonLabel(r: string): string {
        if (r === "soporte") return "Soporte";
        if (r === "pregunta_tecnica") return "Técnica";
        if (r === "facturacion") return "Facturación";
        if (r === "otro") return "Otro";
        return r;
    }

    async function submitNew() {
        try {
            const id = await supportInboxStore.createThread({ reason, subject, body });
            subject = "";
            body = "";
            reason = "soporte";
            showForm = false;
            toastStore.success("Consulta enviada", 1400);
            open(id);
        } catch (e: any) {
            toastStore.error(e?.message ?? "No se pudo crear la consulta");
        }
    }

    function goLogin() {
        navController.navigate("welcome");
    }

    function formatRelative(iso: string): string {
        try {
            const d = new Date(iso);
            const diff = Date.now() - d.getTime();
            const mins = Math.floor(diff / 60000);
            if (mins < 1) return "Ahora";
            if (mins < 60) return `Hace ${mins} min`;
            const hours = Math.floor(mins / 60);
            if (hours < 24) return `Hace ${hours} h`;
            const days = Math.floor(hours / 24);
            if (days < 7) return `Hace ${days} d`;
            return d.toLocaleDateString();
        } catch {
            return "";
        }
    }
</script>

<section class="screen">
    <header class="head">
        <div class="head-text">
            <div class="eyebrow-row">
                <Icon icon={supportIcon} />
                <p class="eyebrow">Soporte</p>
            </div>
            <h1>Mis consultas</h1>
            <p class="muted">Pedidos, productos o facturación con el taller.</p>
        </div>
        {#if ready && !isGuest}
            {#if showForm}
                <Button variant="outlined" size="s" iconType="left" onclick={() => (showForm = false)}>
                    <Icon icon={closeIcon} />
                    Cancelar
                </Button>
            {:else}
                <Button variant="filled" size="s" iconType="left" onclick={() => (showForm = true)}>
                    <Icon icon={addIcon} />
                    Nueva consulta
                </Button>
            {/if}
        {/if}
    </header>

    {#if !ready}
        <div class="state-card">
            <div class="spinner"></div>
            <p class="muted">Cargando consultas…</p>
        </div>
    {:else if isGuest}
        <div class="state-card">
            <div class="state-icon-wrap">
                <Icon icon={lockIcon} />
            </div>
            <p class="state-title">Inicia sesión</p>
            <p class="muted">Para contactar soporte y ver el historial de tus consultas.</p>
            <Button variant="filled" size="m" onclick={goLogin}>Iniciar sesión</Button>
        </div>
    {:else}
        {#if showForm}
            <div class="form-card">
                <div class="card-title">
                    <div class="card-title-row">
                        <Icon icon={chatIcon} />
                        <strong>Nueva consulta</strong>
                    </div>
                    <span>Describe el motivo y el detalle. Te respondemos por aquí.</span>
                </div>

                <div class="field">
                    <label for="support-reason">Motivo</label>
                    <select id="support-reason" bind:value={reason}>
                        <option value="soporte">Soporte general</option>
                        <option value="pregunta_tecnica">Pregunta técnica</option>
                        <option value="facturacion">Facturación</option>
                        <option value="otro">Otro</option>
                    </select>
                </div>

                <div class="field">
                    <label for="support-subject">Asunto</label>
                    <input
                        id="support-subject"
                        type="text"
                        maxlength="120"
                        bind:value={subject}
                        placeholder="Resumen breve"
                    />
                </div>

                <div class="field">
                    <label for="support-body">Mensaje</label>
                    <textarea
                        id="support-body"
                        rows="4"
                        bind:value={body}
                        placeholder="Cuéntanos el detalle…"
                    ></textarea>
                </div>

                <div class="form-actions">
                    <Button
                        variant="filled"
                        size="m"
                        iconType="left"
                        disabled={creating || !subject.trim() || !body.trim()}
                        onclick={submitNew}
                    >
                        <Icon icon={sendIcon} />
                        {creating ? "Enviando…" : "Enviar consulta"}
                    </Button>
                </div>
            </div>
        {/if}

        {#if loading && sorted.length === 0}
            <div class="state-card">
                <div class="spinner"></div>
                <p class="muted">Cargando consultas…</p>
            </div>
        {:else if sorted.length === 0 && !showForm}
            <div class="state-card empty">
                <div class="state-icon-wrap">
                    <Icon icon={inboxIcon} />
                </div>
                <p class="state-title">Aún no tienes consultas</p>
                <p class="muted">Cuando necesites ayuda del taller, abre una consulta desde aquí.</p>
                <Button variant="filled" size="m" iconType="left" onclick={() => (showForm = true)}>
                    <Icon icon={addIcon} />
                    Abrir la primera
                </Button>
            </div>
        {:else if sorted.length > 0}
            <ul class="list">
                {#each sorted as m (m.id)}
                    <li>
                        <button
                            class="thread-row"
                            class:unread={(m.unreadUser ?? 0) > 0}
                            type="button"
                            on:click={() => open(m.id)}
                        >
                            <div class="thread-icon" aria-hidden="true">
                                <Icon icon={chatIcon} />
                            </div>
                            <div class="thread-main">
                                <div class="thread-top">
                                    <strong class="thread-subject">
                                        {#if (m.unreadUser ?? 0) > 0}<span class="dot" title="Sin leer"></span>{/if}
                                        {m.subject || "Sin asunto"}
                                    </strong>
                                    <span class="pill {statusTone(m.status)}">{statusLabel(m.status)}</span>
                                </div>
                                <p class="preview">{m.body || "—"}</p>
                                <div class="thread-meta">
                                    <span class="chip">{reasonLabel(m.reason ?? "")}</span>
                                    <span class="time">{formatRelative(m.createdAtIso)}</span>
                                </div>
                            </div>
                        </button>
                    </li>
                {/each}
            </ul>
        {/if}
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 16px;
        align-content: start;
        max-width: 640px;
        margin: 0 auto;
        padding: 12px 16px 32px;
        min-height: 100%;
    }

    .head {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        gap: 12px;
        flex-wrap: wrap;
    }

    .head-text {
        display: grid;
        gap: 2px;
        min-width: 0;
    }

    .eyebrow-row {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        color: var(--md-sys-color-primary);
    }

    .eyebrow-row :global(svg) {
        width: 16px;
        height: 16px;
    }

    .eyebrow {
        margin: 0;
        font-size: 0.7rem;
        font-weight: 650;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--md-sys-color-primary);
    }

    h1 {
        margin: 0;
        font-size: 1.28rem;
        font-weight: 800;
        letter-spacing: -0.02em;
        color: var(--md-sys-color-on-surface);
    }

    .muted {
        margin: 0;
        font-size: 0.86rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
    }

    .form-card,
    .state-card {
        border-radius: 20px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container);
        box-shadow: 0 6px 18px color-mix(in srgb, black 10%, transparent);
    }

    .form-card {
        display: grid;
        gap: 14px;
        padding: 18px;
    }

    .card-title {
        display: grid;
        gap: 4px;
    }

    .card-title-row {
        display: flex;
        align-items: center;
        gap: 8px;
        color: var(--md-sys-color-primary);
    }

    .card-title-row :global(svg) {
        width: 20px;
        height: 20px;
    }

    .card-title strong {
        font-size: 1rem;
        font-weight: 750;
        color: var(--md-sys-color-on-surface);
    }

    .card-title span {
        font-size: 0.82rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    .field {
        display: grid;
        gap: 6px;
    }

    .field label {
        font-size: 0.78rem;
        font-weight: 650;
        letter-spacing: 0.02em;
        color: var(--md-sys-color-on-surface-variant);
    }

    .field input,
    .field select,
    .field textarea {
        width: 100%;
        box-sizing: border-box;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        padding: 11px 12px;
        font: inherit;
        font-size: 0.92rem;
        outline: none;
        transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }

    .field input:focus,
    .field select:focus,
    .field textarea:focus {
        border-color: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 24%, transparent);
    }

    .field textarea {
        resize: vertical;
        min-height: 96px;
        line-height: 1.4;
    }

    .form-actions {
        display: flex;
        justify-content: flex-end;
        padding-top: 2px;
    }

    .state-card {
        display: grid;
        gap: 10px;
        justify-items: center;
        text-align: center;
        padding: 28px 20px;
    }

    .state-icon-wrap {
        width: 48px;
        height: 48px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        margin-bottom: 2px;
    }

    .state-icon-wrap :global(svg) {
        width: 24px;
        height: 24px;
    }

    .state-title {
        margin: 0;
        font-size: 1rem;
        font-weight: 700;
        color: var(--md-sys-color-on-surface);
    }

    .state-card .muted {
        max-width: 28ch;
    }

    .spinner {
        width: 22px;
        height: 22px;
        border-radius: 999px;
        border: 2px solid var(--md-sys-color-outline-variant);
        border-top-color: var(--md-sys-color-primary);
        animation: spin 0.7s linear infinite;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    .list {
        list-style: none;
        margin: 0;
        padding: 0;
        display: grid;
        gap: 8px;
    }

    .thread-row {
        width: 100%;
        display: flex;
        align-items: flex-start;
        gap: 12px;
        text-align: left;
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface-container);
        padding: 12px 14px;
        color: inherit;
        cursor: pointer;
        transition: border-color 0.15s ease, background 0.15s ease;
    }

    .thread-row:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 40%, var(--md-sys-color-outline-variant));
        background: var(--md-sys-color-surface-container-high);
    }

    .thread-row.unread {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 50%, transparent);
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
    }

    .thread-icon {
        flex-shrink: 0;
        width: 36px;
        height: 36px;
        border-radius: 12px;
        display: grid;
        place-items: center;
        background: var(--md-sys-color-surface-container-highest);
        color: var(--md-sys-color-primary);
    }

    .thread-row.unread .thread-icon {
        background: color-mix(in srgb, var(--md-sys-color-on-primary-container) 12%, transparent);
        color: var(--md-sys-color-on-primary-container);
    }

    .thread-icon :global(svg) {
        width: 18px;
        height: 18px;
    }

    .thread-main {
        flex: 1;
        min-width: 0;
        display: grid;
        gap: 4px;
    }

    .thread-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
    }

    .thread-subject {
        font-size: 0.94rem;
        font-weight: 700;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        min-width: 0;
    }

    .preview {
        margin: 0;
        font-size: 0.84rem;
        line-height: 1.35;
        color: var(--md-sys-color-on-surface-variant);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .thread-row.unread .preview {
        color: inherit;
        opacity: 0.85;
    }

    .thread-meta {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
        margin-top: 2px;
    }

    .chip {
        font-size: 0.68rem;
        font-weight: 650;
        padding: 2px 8px;
        border-radius: 999px;
        background: var(--md-sys-color-surface-container-highest);
        color: var(--md-sys-color-on-surface-variant);
    }

    .thread-row.unread .chip {
        background: color-mix(in srgb, var(--md-sys-color-on-primary-container) 14%, transparent);
        color: inherit;
    }

    .time {
        font-size: 0.72rem;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.9;
    }

    .pill {
        flex-shrink: 0;
        font-size: 0.65rem;
        font-weight: 750;
        letter-spacing: 0.03em;
        text-transform: uppercase;
        padding: 3px 8px;
        border-radius: 999px;
        border: 1px solid transparent;
    }

    .tone-new {
        background: color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
        color: var(--md-sys-color-primary);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, transparent);
    }

    .tone-progress {
        background: color-mix(in srgb, #f9a825 22%, transparent);
        color: #f9a825;
        border-color: color-mix(in srgb, #f9a825 35%, transparent);
    }

    .tone-done {
        background: color-mix(in srgb, #4caf50 22%, transparent);
        color: #81c784;
        border-color: color-mix(in srgb, #4caf50 32%, transparent);
    }

    .tone-closed {
        background: var(--md-sys-color-surface-container-highest);
        color: var(--md-sys-color-on-surface-variant);
        border-color: var(--md-sys-color-outline-variant);
    }

    .dot {
        display: inline-block;
        width: 7px;
        height: 7px;
        border-radius: 999px;
        margin-right: 6px;
        vertical-align: middle;
        background: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent);
    }

    @media (max-width: 480px) {
        .screen { padding: 10px 12px 28px; }
        .form-card { padding: 14px; }
        .state-card { padding: 22px 16px; }
    }
</style>
