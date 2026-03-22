<script lang="ts">
    import { onMount } from "svelte";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import Icon from "../../../../infrastructure/presentation/components/Icon.svelte";
    import LoadingSpinner from "../../../../infrastructure/presentation/components/LoadingSpinner.svelte";
    import SkeletonList from "../../../../infrastructure/presentation/components/SkeletonList.svelte";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportReason, SupportStatus } from "../../domain/entity/SupportMessage";
    import { supportDetail } from "../../../../infrastructure/presentation/navigation/nested.router";
    import { CircleHelp, Inbox, Mail, MessageSquareText, Search, Shield, Wrench } from "lucide-svelte";

    export let navController: NavController;

    let query = "";
    let reason: SupportReason | "all" = "all";
    let status: SupportStatus | "all" = "all";

    const countsStore = supportInboxStore.counts;

    onMount(() => {
        supportInboxStore.syncAll().catch(() => {});
    });

    $: items = $supportInboxStore.items;
    $: counts = $countsStore;
    $: filtered = items
        .filter((m) => (reason === "all" ? true : m.reason === reason))
        .filter((m) => (status === "all" ? true : m.status === status))
        .filter((m) => {
            const q = query.trim().toLowerCase();
            if (!q) return true;
            return (
                (m.fromName || "").toLowerCase().includes(q) ||
                (m.fromEmail || "").toLowerCase().includes(q) ||
                (m.subject || "").toLowerCase().includes(q) ||
                (m.body || "").toLowerCase().includes(q)
            );
        })
        .sort((a, b) => b.createdAtIso.localeCompare(a.createdAtIso));

    $: isRefreshing = $supportInboxStore.loading && items.length > 0;
    $: isInitialLoading = $supportInboxStore.loading && items.length === 0;

    function openDetail(id: string) {
        navController.navigate(supportDetail.path, { id });
    }

    function reasonLabel(r: SupportReason): string {
        if (r === "soporte") return "Soporte";
        if (r === "pregunta_tecnica") return "Pregunta técnica";
        if (r === "facturacion") return "Facturación";
        return "Otro";
    }

    function reasonIcon(r: SupportReason) {
        if (r === "pregunta_tecnica") return Wrench;
        if (r === "facturacion") return Shield;
        if (r === "soporte") return MessageSquareText;
        return CircleHelp;
    }

    function statusLabel(s: SupportStatus): string {
        if (s === "nuevo") return "Nuevo";
        if (s === "en_proceso") return "En proceso";
        return "Resuelto";
    }

    function badgeClass(s: SupportStatus): string {
        if (s === "nuevo") return "badge new";
        if (s === "en_proceso") return "badge progress";
        return "badge done";
    }
</script>

<section class="mgmt-screen">
    <div class="mgmt-container">
        <header class="mgmt-page-head">
            <div class="mgmt-page-title">
                <h1 class="mgmt-h1">Mensajes</h1>
                <p class="mgmt-muted">Bandeja de soporte y consultas</p>
            </div>
            <div class="mgmt-chip-row">
                <span class="mgmt-chip">
                    <Icon icon={Inbox} size={18} ariaLabel="Total" />
                    {counts.total} total
                </span>
                <span class="mgmt-chip">
                    <Icon icon={Mail} size={18} ariaLabel="Nuevos" />
                    {counts.nuevo} nuevos
                </span>
                <span class="mgmt-chip">
                    <Icon icon={Wrench} size={18} ariaLabel="En proceso" />
                    {counts.enProceso} en proceso
                </span>
                {#if isRefreshing}
                    <span class="mgmt-chip" aria-label="Sincronizando">
                        <LoadingSpinner size={16} label="Sincronizando" subtle />
                        Sincronizando...
                    </span>
                {/if}
            </div>
        </header>

        <div class="mgmt-layout">
            <section class="mgmt-card mgmt-form-card" aria-label="Filtros">
                <h2 class="mgmt-card-title" style="margin:0 0 12px 0">Filtros</h2>

                <div class="mgmt-grid">
                    <label class="mgmt-field" style="grid-column:1/-1">
                        <span>Buscar</span>
                        <div class="mgmt-input-with-icon">
                            <Icon icon={Search} size={18} className="mgmt-input-ico" ariaLabel="Buscar" />
                            <input
                                class="mgmt-input"
                                type="text"
                                bind:value={query}
                                placeholder="Nombre, correo, asunto..."
                            />
                        </div>
                    </label>

                    <label class="mgmt-field">
                        <span>Motivo</span>
                        <select class="mgmt-select" bind:value={reason}>
                            <option value="all">Todos</option>
                            <option value="soporte">Soporte</option>
                            <option value="pregunta_tecnica">Pregunta técnica</option>
                            <option value="facturacion">Facturación</option>
                            <option value="otro">Otro</option>
                        </select>
                    </label>

                    <label class="mgmt-field">
                        <span>Estado</span>
                        <select class="mgmt-select" bind:value={status}>
                            <option value="all">Todos</option>
                            <option value="nuevo">Nuevo</option>
                            <option value="en_proceso">En proceso</option>
                            <option value="resuelto">Resuelto</option>
                        </select>
                    </label>

                    <div class="mgmt-actions" style="grid-column:1/-1">
                        <button class="mgmt-btn ghost" type="button" on:click={() => supportInboxStore.syncAll()}>
                            <Icon icon={Inbox} size={18} ariaLabel="Refrescar" />
                            Refrescar
                        </button>
                    </div>
                </div>
            </section>

            <section class="mgmt-card" aria-label="Entrantes">
                <div class="list-head">
                    <h2 class="mgmt-card-title" style="margin:0">Entrantes</h2>
                    <span class="mgmt-chip">
                        <Icon icon={MessageSquareText} size={18} ariaLabel="Filtrados" />
                        {filtered.length}
                    </span>
                </div>

                <div class="mgmt-list">
                    {#if isInitialLoading}
                        <SkeletonList rows={9} showAvatar={false} showActions={false} lines={2} compact />
                    {:else if filtered.length === 0}
                        <div class="empty">
                            <div class="empty-ico">
                                <Icon icon={Mail} size={22} ariaLabel="Sin mensajes" />
                            </div>
                            <div class="empty-title">Bandeja vacía</div>
                            <div class="mgmt-muted">Aún no hay mensajes entrantes.</div>
                        </div>
                    {/if}

                    {#each filtered as m (m.id)}
                        <button class="row-btn" type="button" on:click={() => openDetail(m.id)} aria-label={m.subject}>
                            <div class="row-left">
                                <div class="row-ico">
                                    <Icon icon={reasonIcon(m.reason)} size={18} ariaLabel={reasonLabel(m.reason)} />
                                </div>
                                <div class="row-main">
                                    <div class="row-top">
                                        <span class="row-title">{m.subject || "Sin asunto"}</span>
                                        <span class={badgeClass(m.status)}>{statusLabel(m.status)}</span>
                                    </div>
                                    <div class="row-sub">
                                        <span>{m.fromName || m.fromEmail}</span>
                                        <span class="dot">•</span>
                                        <span>{reasonLabel(m.reason)}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="row-time" title={m.createdAtIso}>{new Date(m.createdAtIso).toLocaleString()}</div>
                        </button>
                    {/each}
                </div>
            </section>
        </div>
    </div>
</section>

<style>
    .list-head {
        display: flex;
        justify-content: space-between;
        gap: 10px;
        align-items: center;
        margin-bottom: 10px;
    }

    .row-btn {
        width: 100%;
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 14px;
        align-items: center;
        text-align: left;
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
        padding: 12px 14px;
    }

    .row-btn:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary) 10%, var(--md-sys-color-surface) 88%);
    }

    .row-btn:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 55%, white);
        outline-offset: 2px;
    }

    .row-left {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 12px;
        align-items: center;
        min-width: 0;
    }

    .row-ico {
        width: 38px;
        height: 38px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 32%, transparent);
    }

    .row-main {
        min-width: 0;
        display: grid;
        gap: 4px;
    }

    .row-top {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
    }

    .row-title {
        font-weight: 950;
        letter-spacing: -0.01em;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        min-width: 0;
    }

    .row-sub {
        display: inline-flex;
        gap: 8px;
        flex-wrap: wrap;
        align-items: center;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 72%, transparent);
        font-size: 0.88rem;
    }

    .dot {
        opacity: 0.7;
    }

    .row-time {
        color: color-mix(in srgb, var(--md-sys-color-on-background) 70%, transparent);
        font-size: 0.86rem;
        white-space: nowrap;
    }

    .badge {
        font-size: 0.72rem;
        font-weight: 900;
        padding: 5px 10px;
        border-radius: 999px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 35%, transparent);
        white-space: nowrap;
    }

    .badge.new {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
    }

    .badge.progress {
        border-color: color-mix(in srgb, #f59e0b 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #f59e0b 14%, transparent);
    }

    .badge.done {
        border-color: color-mix(in srgb, #22c55e 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #22c55e 12%, transparent);
    }

    .empty {
        height: 100%;
        display: grid;
        place-items: center;
        text-align: center;
        padding: 18px;
        gap: 6px;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 80%, transparent);
    }

    .empty-ico {
        width: 52px;
        height: 52px;
        border-radius: 18px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 45%, transparent);
        border: 1px solid var(--md-sys-color-outline-variant);
    }

    .empty-title {
        font-weight: 900;
        letter-spacing: -0.01em;
    }

    @media (max-width: 720px) {
        .row-time {
            display: none;
        }
    }
</style>





