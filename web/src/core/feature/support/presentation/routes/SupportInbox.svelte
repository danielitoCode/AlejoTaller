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
</script>

<section class="screen">
    <header class="head">
        <div>
            <p class="eyebrow">Soporte</p>
            <h1>Mis consultas</h1>
            <p class="muted">Habla con el taller sobre pedidos, productos o facturación.</p>
        </div>
        {#if ready && !isGuest}
            <button class="btn primary" type="button" on:click={() => (showForm = !showForm)}>
                {showForm ? "Cancelar" : "Nueva consulta"}
            </button>
        {/if}
    </header>

    {#if !ready}
        <p class="muted">Cargando…</p>
    {:else if isGuest}
        <div class="card guest">
            <p>Inicia sesión para contactar soporte y ver tus consultas.</p>
            <button class="btn primary" type="button" on:click={goLogin}>Iniciar sesión</button>
        </div>
    {:else}
        {#if showForm}
            <div class="card form">
                <h2>Nueva consulta</h2>
                <label>
                    <span>Motivo</span>
                    <select bind:value={reason}>
                        <option value="soporte">Soporte</option>
                        <option value="pregunta_tecnica">Pregunta técnica</option>
                        <option value="facturacion">Facturación</option>
                        <option value="otro">Otro</option>
                    </select>
                </label>
                <label>
                    <span>Asunto</span>
                    <input type="text" maxlength="120" bind:value={subject} placeholder="Resumen breve" />
                </label>
                <label>
                    <span>Mensaje</span>
                    <textarea rows="4" bind:value={body} placeholder="Cuéntanos el detalle…"></textarea>
                </label>
                <button
                    class="btn primary"
                    type="button"
                    disabled={creating || !subject.trim() || !body.trim()}
                    on:click={submitNew}
                >
                    {creating ? "Enviando…" : "Enviar"}
                </button>
            </div>
        {/if}

        {#if loading && sorted.length === 0}
            <p class="muted">Cargando consultas…</p>
        {:else if sorted.length === 0}
            <div class="card empty">
                <p>Aún no tienes consultas.</p>
                <button class="btn primary" type="button" on:click={() => (showForm = true)}>Abrir la primera</button>
            </div>
        {:else}
            <ul class="list">
                {#each sorted as m (m.id)}
                    <li>
                        <button
                            class="row"
                            class:unread={(m.unreadUser ?? 0) > 0}
                            type="button"
                            on:click={() => open(m.id)}
                        >
                            <div class="row-main">
                                <div class="row-top">
                                    <strong>
                                        {#if (m.unreadUser ?? 0) > 0}<span class="dot"></span>{/if}
                                        {m.subject || "Sin asunto"}
                                    </strong>
                                    <span class="pill">{statusLabel(m.status)}</span>
                                </div>
                                <p class="preview">{m.body || "—"}</p>
                                <span class="time">{new Date(m.createdAtIso).toLocaleString()}</span>
                            </div>
                        </button>
                    </li>
                {/each}
            </ul>
        {/if}
    {/if}
</section>

<style>
    .screen { padding: 16px; max-width: 720px; margin: 0 auto; display: grid; gap: 16px; }
    .head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; flex-wrap: wrap; }
    .eyebrow { margin: 0; font-size: 0.75rem; text-transform: uppercase; letter-spacing: 0.06em; opacity: 0.7; }
    h1 { margin: 4px 0; font-size: 1.45rem; }
    .muted { margin: 0; opacity: 0.75; font-size: 0.92rem; }
    .btn { border-radius: 12px; border: 1px solid var(--md-sys-color-outline-variant, #444); background: transparent; color: inherit; padding: 10px 14px; font-weight: 650; cursor: pointer; }
    .btn.primary { background: var(--md-sys-color-primary, #6750a4); color: var(--md-sys-color-on-primary, #fff); border-color: transparent; }
    .btn:disabled { opacity: 0.5; cursor: not-allowed; }
    .card { border-radius: 16px; border: 1px solid var(--md-sys-color-outline-variant, #444); padding: 16px; background: color-mix(in srgb, var(--md-sys-color-surface, #1c1b1f) 92%, transparent); display: grid; gap: 12px; }
    .form label { display: grid; gap: 6px; font-size: 0.88rem; }
    .form input, .form select, .form textarea { border-radius: 10px; border: 1px solid var(--md-sys-color-outline-variant, #444); background: transparent; color: inherit; padding: 10px 12px; font: inherit; }
    .list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
    .row { width: 100%; text-align: left; border-radius: 14px; border: 1px solid var(--md-sys-color-outline-variant, #444); background: color-mix(in srgb, var(--md-sys-color-surface, #1c1b1f) 94%, transparent); padding: 12px 14px; color: inherit; cursor: pointer; }
    .row.unread { border-color: color-mix(in srgb, var(--md-sys-color-primary, #6750a4) 40%, #444); }
    .row-top { display: flex; justify-content: space-between; gap: 8px; align-items: center; }
    .preview { margin: 6px 0 4px; opacity: 0.8; font-size: 0.9rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    .time { font-size: 0.78rem; opacity: 0.65; }
    .pill { font-size: 0.7rem; font-weight: 700; padding: 4px 8px; border-radius: 999px; border: 1px solid var(--md-sys-color-outline-variant, #444); text-transform: uppercase; }
    .dot { display: inline-block; width: 8px; height: 8px; border-radius: 999px; margin-right: 6px; background: var(--md-sys-color-primary, #6750a4); }
    .guest { text-align: center; place-items: center; }
</style>
