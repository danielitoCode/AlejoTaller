<script lang="ts">
    import { onMount } from "svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import { supportDetail } from "../../../../infrastructure/presentation/navigation/nested.router";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportReason } from "../../domain/entity/SupportMessage";
    import { sessionStore } from "../../../auth/presentation/viewmodel/session.store";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";

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
            <p class="eyebrow">Soporte</p>
            <h1>Mis consultas</h1>
            <p class="muted">Pedidos, productos o facturación con el taller.</p>
        </div>
        {#if ready && !isGuest}
            <button
                class="btn"
                class:primary={!showForm}
                class:ghost={showForm}
                type="button"
                on:click={() => (showForm = !showForm)}
            >
                {showForm ? "Cancelar" : "Nueva consulta"}
            </button>
        {/if}
    </header>

    {#if !ready}
        <div class="state-card">
            <div class="spinner"></div>
            <p class="muted">Cargando consultas…</p>
        </div>
    {:else if isGuest}
        <div class="state-card guest">
            <div class="state-icon" aria-hidden="true">🔒</div>
            <p class="state-title">Inicia sesión</p>
            <p class="muted">Para contactar soporte y ver el historial de tus consultas.</p>
            <button class="btn primary" type="button" on:click={goLogin}>Iniciar sesión</button>
        </div>
    {:else}
        {#if showForm}
            <div class="card form-card">
                <div class="card-title">
                    <strong>Nueva consulta</strong>
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
                    <button
                        class="btn primary"
                        type="button"
                        disabled={creating || !subject.trim() || !body.trim()}
                        on:click={submitNew}
                    >
                        {creating ? "Enviando…" : "Enviar consulta"}
                    </button>
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
                <div class="state-icon" aria-hidden="true">💬</div>
                <p class="state-title">Aún no tienes consultas</p>
                <p class="muted">Cuando necesites ayuda del taller, abre una consulta desde aquí.</p>
                <button class="btn primary compact" type="button" on:click={() => (showForm = true)}>
                    Abrir la primera
                </button>
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

    .eyebrow {
        margin: 0;
        font-size: 0.7rem;
        font-weight: 650;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--md-sys-color-primary);
        opacity: 0.9;
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

    .btn {
        flex-shrink: 0;
        border-radius: 999px;
        border: 1px solid transparent;
        padding: 8px 16px;
        font: inherit;
        font-size: 0.86rem;
        font-weight: 650;
        cursor: pointer;
        transition: background 0.15s ease, border-color 0.15s ease, opacity 0.15s ease;
    }

    .btn.primary {
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
    }

    .btn.primary:hover:not(:disabled) {
        filter: brightness(1.06);
    }

    .btn.ghost {
        background: transparent;
        color: var(--md-sys-color-on-surface);
        border-color: var(--md-sys-color-outline-variant);
    }

    .btn.compact {
        padding: 8px 18px;
    }

    .btn:disabled {
        opacity: 0.45;
        cursor: not-allowed;
    }

    .card,
    .state-card {
        border-radius: 20px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 70%, transparent);
        background:
            linear-gradient(
                165deg,
                color-mix(in srgb, var(--md-sys-color-surface-container) 88%, transparent),
                var(--md-sys-color-surface-container-low)
            );
        box-shadow: 0 8px 24px color-mix(in srgb, black 8%, transparent);
    }

    .form-card {
        display: grid;
        gap: 14px;
        padding: 18px;
    }

    .card-title {
        display: grid;
        gap: 2px;
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
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-surface) 70%, transparent);
        color: var(--md-sys-color-on-surface);
        padding: 10px 12px;
        font: inherit;
        font-size: 0.92rem;
        outline: none;
        transition: border-color 0.15s ease, box-shadow 0.15s ease;
    }

    .field input:focus,
    .field select:focus,
    .field textarea:focus {
        border-color: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 22%, transparent);
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
        gap: 8px;
        justify-items: center;
        text-align: center;
        padding: 28px 20px;
    }

    .state-icon {
        font-size: 1.75rem;
        line-height: 1;
        margin-bottom: 4px;
        opacity: 0.9;
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

    .state-card .btn {
        margin-top: 8px;
    }

    .spinner {
        width: 22px;
        height: 22px;
        border-radius: 999px;
        border: 2px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 50%, transparent);
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
        text-align: left;
        border-radius: 16px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 65%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-surface-container) 90%, transparent);
        padding: 12px 14px;
        color: inherit;
        cursor: pointer;
        transition: border-color 0.15s ease, background 0.15s ease, transform 0.12s ease;
    }

    .thread-row:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 85%, transparent);
    }

    .thread-row.unread {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 45%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 18%, var(--md-sys-color-surface-container));
    }

    .thread-main {
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
        color: var(--md-sys-color-on-surface);
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
        background: color-mix(in srgb, var(--md-sys-color-surface-container-highest) 80%, transparent);
        color: var(--md-sys-color-on-surface-variant);
    }

    .time {
        font-size: 0.72rem;
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.85;
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
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
        color: var(--md-sys-color-primary);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 30%, transparent);
    }

    .tone-progress {
        background: color-mix(in srgb, #f9a825 18%, transparent);
        color: #f9a825;
        border-color: color-mix(in srgb, #f9a825 30%, transparent);
    }

    .tone-done {
        background: color-mix(in srgb, #4caf50 18%, transparent);
        color: #81c784;
        border-color: color-mix(in srgb, #4caf50 28%, transparent);
    }

    .tone-closed {
        background: color-mix(in srgb, var(--md-sys-color-outline) 18%, transparent);
        color: var(--md-sys-color-on-surface-variant);
        border-color: color-mix(in srgb, var(--md-sys-color-outline-variant) 50%, transparent);
    }

    .dot {
        display: inline-block;
        width: 7px;
        height: 7px;
        border-radius: 999px;
        margin-right: 6px;
        vertical-align: middle;
        background: var(--md-sys-color-primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--md-sys-color-primary) 25%, transparent);
    }

    @media (max-width: 480px) {
        .screen {
            padding: 10px 12px 28px;
        }
        .form-card {
            padding: 14px;
        }
        .state-card {
            padding: 22px 16px;
        }
    }
</style>
