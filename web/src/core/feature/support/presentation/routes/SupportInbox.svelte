<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Icon } from "m3-svelte";
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
        } catch { return ""; }
    }
</script>

<section class="screen">
    <header class="head">
        <div class="head-text">
            <div class="eyebrow-row"><Icon icon={supportIcon} /><p class="eyebrow">Soporte</p></div>
            <h1>Mis consultas</h1>
            <p class="muted">Pedidos, productos o facturación con el taller.</p>
        </div>
        {#if ready && !isGuest}
            {#if showForm}
                <Button variant="outlined" size="s" iconType="left" onclick={() => (showForm = false)}><Icon icon={closeIcon} />Cancelar</Button>
            {:else}
                <Button variant="filled" size="s" iconType="left" onclick={() => (showForm = true)}><Icon icon={addIcon} />Nueva consulta</Button>
            {/if}
        {/if}
    </header>

    {#if !ready}
        <div class="state-card"><div class="spinner"></div><p class="muted">Cargando consultas…</p></div>
    {:else if isGuest}
        <div class="state-card">
            <div class="state-icon-wrap"><Icon icon={lockIcon} /></div>
            <p class="state-title">Inicia sesión</p>
            <p class="muted">Para contactar soporte y ver el historial de tus consultas.</p>
            <Button variant="filled" size="m" onclick={goLogin}>Iniciar sesión</Button>
        </div>
    {:else}
        {#if showForm}
            <div class="ticket-card">
                <div class="ticket-hero">
                    <div class="hero-orbit" aria-hidden="true">
                        <div class="hero-avatar"><Icon icon={supportIcon} /></div><span class="hero-dot"></span>
                    </div>
                    <div class="hero-copy">
                        <span class="hero-kicker">NUEVA CONVERSACIÓN</span>
                        <h2>¿En qué podemos ayudarte?</h2>
                        <p>Cuéntanos qué ocurre y continuaremos la atención directamente por el chat.</p>
                    </div>
                    <div class="hero-signal" aria-hidden="true"><span></span><span></span><span></span></div>
                </div>

                <div class="ticket-content">
                    <div class="section-heading">
                        <div><strong>Cuéntanos el motivo</strong><span>Selecciona la opción que mejor describe tu consulta.</span></div>
                        <span class="step">1 / 3</span>
                    </div>

                    <div class="reason-grid" role="group" aria-label="Motivo de la consulta">
                        <button class="reason-option" class:chosen={reason === "soporte"} type="button" onclick={() => (reason = "soporte")}>
                            <span class="reason-icon"><Icon icon={supportIcon} /></span><span class="reason-copy"><strong>Soporte general</strong><small>Ayuda con la tienda</small></span><span class="check"></span>
                        </button>
                        <button class="reason-option" class:chosen={reason === "pregunta_tecnica"} type="button" onclick={() => (reason = "pregunta_tecnica")}>
                            <span class="reason-icon"><Icon icon={chatIcon} /></span><span class="reason-copy"><strong>Pregunta técnica</strong><small>Productos o funcionamiento</small></span><span class="check"></span>
                        </button>
                        <button class="reason-option" class:chosen={reason === "facturacion"} type="button" onclick={() => (reason = "facturacion")}>
                            <span class="reason-icon"><span class="reason-symbol">$</span></span><span class="reason-copy"><strong>Facturación</strong><small>Pagos y comprobantes</small></span><span class="check"></span>
                        </button>
                        <button class="reason-option" class:chosen={reason === "otro"} type="button" onclick={() => (reason = "otro")}>
                            <span class="reason-icon"><span class="reason-symbol">•••</span></span><span class="reason-copy"><strong>Otro</strong><small>Algo diferente</small></span><span class="check"></span>
                        </button>
                    </div>

                    <div class="section-heading details-heading">
                        <div><strong>Ahora cuéntanos un poco más</strong><span>Con esta información podremos responderte mejor.</span></div>
                        <span class="step">2 / 3</span>
                    </div>

                    <div class="field-grid">
                        <div class="field">
                            <label for="support-subject">Asunto</label>
                            <div class="input-shell">
                                <input id="support-subject" type="text" maxlength="120" bind:value={subject} placeholder="Ej. No puedo completar mi pedido" />
                                <span class="field-count">{subject.length}/120</span>
                            </div>
                        </div>
                        <div class="field">
                            <label for="support-body">Mensaje</label>
                            <textarea id="support-body" rows="5" maxlength="1000" bind:value={body} placeholder="Describe lo que sucede, qué necesitas o qué esperabas que ocurriera…"></textarea>
                            <div class="field-bottom"><span>Tu mensaje será visible para el equipo de soporte.</span><span>{body.length}/1000</span></div>
                        </div>
                    </div>
                </div>

                <div class="ticket-footer">
                    <div class="footer-info"><span class="connected-dot"></span><div><strong>Soporte conectado</strong><span>Te responderemos en esta conversación.</span></div></div>
                    <div class="footer-actions">
                        <span class="step last-step">3 / 3</span>
                        <Button variant="filled" size="m" iconType="left" disabled={creating || !subject.trim() || !body.trim()} onclick={submitNew}>
                            <Icon icon={sendIcon} />{creating ? "Enviando…" : "Iniciar conversación"}
                        </Button>
                    </div>
                </div>
            </div>
        {/if}

        {#if loading && sorted.length === 0}
            <div class="state-card"><div class="spinner"></div><p class="muted">Cargando consultas…</p></div>
        {:else if sorted.length === 0 && !showForm}
            <div class="state-card empty">
                <div class="state-icon-wrap"><Icon icon={inboxIcon} /></div>
                <p class="state-title">Aún no tienes consultas</p>
                <p class="muted">Cuando necesites ayuda del taller, abre una consulta desde aquí.</p>
                <Button variant="filled" size="m" iconType="left" onclick={() => (showForm = true)}><Icon icon={addIcon} />Abrir la primera</Button>
            </div>
        {:else if sorted.length > 0}
            <ul class="list">
                {#each sorted as m (m.id)}
                    <li>
                        <button class="thread-row" class:unread={(m.unreadUser ?? 0) > 0} type="button" onclick={() => open(m.id)}>
                            <div class="thread-icon" aria-hidden="true"><Icon icon={chatIcon} /></div>
                            <div class="thread-main">
                                <div class="thread-top"><strong class="thread-subject">{#if (m.unreadUser ?? 0) > 0}<span class="dot" title="Sin leer"></span>{/if}{m.subject || "Sin asunto"}</strong><span class="pill {statusTone(m.status)}">{statusLabel(m.status)}</span></div>
                                <p class="preview">{m.body || "—"}</p>
                                <div class="thread-meta"><span class="chip">{reasonLabel(m.reason ?? "")}</span><span class="time">{formatRelative(m.createdAtIso)}</span></div>
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
        --chat-surface: var(--m3c-surface-container, var(--md-sys-color-surface-container));
        --chat-surface-high: var(--m3c-surface-container-high, var(--md-sys-color-surface-container-high));
        --chat-surface-highest: var(--md-sys-color-surface-container-highest, var(--m3c-surface-container-high));
        --chat-on-surface: var(--m3c-on-surface, var(--md-sys-color-on-surface));
        --chat-on-variant: var(--m3c-on-surface-variant, var(--md-sys-color-on-surface-variant));
        --chat-primary: var(--m3c-primary, var(--md-sys-color-primary));
        --chat-primary-container: var(--m3c-primary-container, var(--md-sys-color-primary-container));
        --chat-on-primary-container: var(--m3c-on-primary-container, var(--md-sys-color-on-primary-container));
        --chat-secondary: var(--m3c-secondary, var(--md-sys-color-secondary));
        --chat-outline: var(--m3c-outline-variant, var(--md-sys-color-outline-variant));
        display: grid; gap: 16px; align-content: start; width: min(100%, 820px); margin: 0 auto; padding: 12px 18px 32px; min-height: 100%; box-sizing: border-box;
    }
    .head { display: flex; justify-content: space-between; align-items: flex-start; gap: 12px; flex-wrap: wrap; }
    .head-text { display: grid; gap: 2px; min-width: 0; }
    .eyebrow-row { display: inline-flex; align-items: center; gap: 6px; color: var(--chat-primary); }
    .eyebrow-row :global(svg) { width: 16px; height: 16px; }
    .eyebrow { margin: 0; font-size: .7rem; font-weight: 650; text-transform: uppercase; letter-spacing: .08em; color: var(--chat-primary); }
    h1 { margin: 0; font-size: 1.28rem; font-weight: 800; letter-spacing: -.02em; color: var(--chat-on-surface); }
    .muted { margin: 0; font-size: .86rem; line-height: 1.35; color: var(--chat-on-variant); }

    .ticket-card,.state-card { border: 1px solid var(--chat-outline); border-radius: 26px; background: var(--chat-surface); box-shadow: 0 20px 46px color-mix(in srgb,var(--chat-primary) 7%,transparent),0 5px 16px color-mix(in srgb,black 6%,transparent); overflow: hidden; }
    .ticket-card { position: relative; isolation: isolate; }
    .ticket-card::before { content:""; position:absolute; inset:0 0 auto; height:1px; background:linear-gradient(90deg,transparent,color-mix(in srgb,var(--chat-primary) 45%,transparent),color-mix(in srgb,var(--chat-secondary) 30%,transparent),transparent); z-index:4; }

    .ticket-hero { position:relative; isolation:isolate; display:flex; align-items:center; gap:16px; min-height:104px; padding:18px 20px; overflow:hidden; background:radial-gradient(circle at 4% 0%,color-mix(in srgb,var(--chat-primary) 16%,transparent),transparent 34%),linear-gradient(135deg,color-mix(in srgb,var(--chat-primary-container) 30%,var(--chat-surface-high)),var(--chat-surface-high) 58%,var(--chat-surface)); border-bottom:1px solid color-mix(in srgb,var(--chat-primary) 12%,var(--chat-outline)); }
    .ticket-hero::after { content:""; position:absolute; inset:auto -15% -85% 38%; height:150px; border-radius:50%; background:color-mix(in srgb,var(--chat-secondary) 9%,transparent); filter:blur(28px); pointer-events:none; z-index:-1; }
    .hero-orbit { position:relative; flex:0 0 auto; width:54px; height:54px; }
    .hero-orbit::before { content:""; position:absolute; inset:-5px; border:1px solid color-mix(in srgb,var(--chat-primary) 18%,transparent); border-radius:18px; }
    .hero-avatar { width:54px; height:54px; display:grid; place-items:center; border-radius:17px; background:var(--chat-primary-container); color:var(--chat-on-primary-container); box-shadow:inset 0 1px 0 color-mix(in srgb,white 22%,transparent),0 8px 18px color-mix(in srgb,var(--chat-primary) 14%,transparent); }
    .hero-avatar :global(svg) { width:28px; height:28px; }
    .hero-dot { position:absolute; right:-4px; bottom:-3px; width:10px; height:10px; border-radius:50%; background:var(--chat-primary); border:3px solid var(--chat-surface-high); box-shadow:0 0 0 2px color-mix(in srgb,var(--chat-primary) 15%,transparent); }
    .hero-copy { min-width:0; flex:1; }
    .hero-kicker { display:block; margin-bottom:3px; color:var(--chat-primary); font-size:.61rem; font-weight:800; letter-spacing:.09em; }
    .hero-copy h2 { margin:0; color:var(--chat-on-surface); font-size:1.12rem; line-height:1.15; font-weight:800; letter-spacing:-.025em; }
    .hero-copy p { margin:5px 0 0; max-width:58ch; color:var(--chat-on-variant); font-size:.76rem; line-height:1.4; }
    .hero-signal { display:flex; align-items:flex-end; gap:3px; height:20px; margin-left:auto; padding:0 3px; opacity:.7; }
    .hero-signal span { width:3px; border-radius:999px; background:var(--chat-primary); }
    .hero-signal span:nth-child(1){height:7px;opacity:.42}.hero-signal span:nth-child(2){height:12px;opacity:.66}.hero-signal span:nth-child(3){height:17px}

    .ticket-content { display:grid; gap:16px; padding:20px; }
    .section-heading { display:flex; align-items:flex-start; justify-content:space-between; gap:12px; }
    .section-heading>div { display:grid; gap:3px; }
    .section-heading strong { color:var(--chat-on-surface); font-size:.9rem; font-weight:800; }
    .section-heading span:not(.step) { color:var(--chat-on-variant); font-size:.74rem; line-height:1.35; }
    .step { flex:0 0 auto; padding:4px 8px; border-radius:999px; background:var(--chat-surface-highest); color:var(--chat-on-variant); font-size:.61rem; font-weight:800; letter-spacing:.04em; }

    .reason-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:8px; }
    .reason-option { position:relative; display:flex; align-items:center; gap:10px; min-width:0; padding:11px; border:1px solid var(--chat-outline); border-radius:16px; background:var(--chat-surface-high); color:var(--chat-on-surface); text-align:left; cursor:pointer; font:inherit; transition:transform .16s ease,border-color .16s ease,background .16s ease,box-shadow .16s ease; }
    .reason-option:hover { transform:translateY(-1px); border-color:color-mix(in srgb,var(--chat-primary) 36%,var(--chat-outline)); background:var(--chat-surface-highest); }
    .reason-option.chosen { border-color:color-mix(in srgb,var(--chat-primary) 58%,var(--chat-outline)); background:color-mix(in srgb,var(--chat-primary-container) 74%,var(--chat-surface-high)); box-shadow:0 7px 18px color-mix(in srgb,var(--chat-primary) 8%,transparent); }
    .reason-icon { flex:0 0 auto; width:34px; height:34px; display:grid; place-items:center; border-radius:11px; background:var(--chat-surface-highest); color:var(--chat-primary); }
    .chosen .reason-icon { background:var(--chat-primary-container); color:var(--chat-on-primary-container); }
    .reason-icon :global(svg){width:18px;height:18px}.reason-symbol{font-size:.73rem;font-weight:850;letter-spacing:-.04em}.reason-copy{min-width:0;display:grid;gap:2px}.reason-copy strong{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-size:.77rem;font-weight:750}.reason-copy small{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:var(--chat-on-variant);font-size:.64rem}.check{flex:0 0 auto;width:8px;height:8px;margin-left:auto;border-radius:50%;background:transparent;border:1.5px solid var(--chat-outline)}.chosen .check{background:var(--chat-primary);border-color:var(--chat-primary);box-shadow:0 0 0 3px color-mix(in srgb,var(--chat-primary) 18%,transparent)}

    .details-heading { margin-top:2px; padding-top:4px; border-top:1px solid color-mix(in srgb,var(--chat-outline) 55%,transparent); }
    .field-grid{display:grid;gap:13px}.field{display:grid;gap:6px}.field label{color:var(--chat-on-variant);font-size:.72rem;font-weight:700;letter-spacing:.02em}.input-shell{position:relative}.field input,.field textarea{width:100%;box-sizing:border-box;border-radius:13px;border:1px solid var(--chat-outline);background:var(--md-sys-color-surface,var(--chat-surface));color:var(--chat-on-surface);padding:11px 12px;font:inherit;font-size:.87rem;outline:none;transition:border-color .16s ease,box-shadow .16s ease,background .16s ease}.field input{padding-right:62px}.field input:focus,.field textarea:focus{border-color:var(--chat-primary);background:var(--chat-surface-high);box-shadow:0 0 0 3px color-mix(in srgb,var(--chat-primary) 18%,transparent)}.field textarea{resize:vertical;min-height:116px;line-height:1.45}.field input::placeholder,.field textarea::placeholder{color:var(--chat-on-variant);opacity:.68}.field-count{position:absolute;right:11px;top:50%;transform:translateY(-50%);color:var(--chat-on-variant);opacity:.65;font-size:.61rem;font-variant-numeric:tabular-nums}.field-bottom{display:flex;justify-content:space-between;gap:8px;color:var(--chat-on-variant);opacity:.7;font-size:.61rem}

    .ticket-footer{display:flex;align-items:center;justify-content:space-between;gap:14px;padding:14px 20px 18px;border-top:1px solid var(--chat-outline);background:color-mix(in srgb,var(--chat-surface-high) 70%,transparent)}.footer-info{display:flex;align-items:center;gap:9px;min-width:0}.connected-dot{flex:0 0 auto;width:8px;height:8px;border-radius:50%;background:var(--chat-primary);box-shadow:0 0 0 4px color-mix(in srgb,var(--chat-primary) 13%,transparent)}.footer-info div{display:grid;gap:1px;min-width:0}.footer-info strong{color:var(--chat-on-surface);font-size:.68rem;font-weight:750}.footer-info span:not(.connected-dot){color:var(--chat-on-variant);font-size:.61rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.footer-actions{display:flex;align-items:center;gap:10px}.last-step{display:none}

    .state-card{display:grid;gap:10px;justify-items:center;text-align:center;padding:28px 20px}.state-icon-wrap{width:48px;height:48px;border-radius:14px;display:grid;place-items:center;background:var(--chat-primary-container);color:var(--chat-on-primary-container);margin-bottom:2px}.state-icon-wrap :global(svg){width:24px;height:24px}.state-title{margin:0;font-size:1rem;font-weight:700;color:var(--chat-on-surface)}.state-card .muted{max-width:28ch}.spinner{width:22px;height:22px;border-radius:999px;border:2px solid var(--chat-outline);border-top-color:var(--chat-primary);animation:spin .7s linear infinite}@keyframes spin{to{transform:rotate(360deg)}}
    .list{list-style:none;margin:0;padding:0;display:grid;gap:8px}.thread-row{width:100%;display:flex;align-items:flex-start;gap:12px;text-align:left;border-radius:16px;border:1px solid var(--chat-outline);background:var(--chat-surface);padding:12px 14px;color:inherit;cursor:pointer;transition:border-color .15s ease,background .15s ease}.thread-row:hover{border-color:color-mix(in srgb,var(--chat-primary) 40%,var(--chat-outline));background:var(--chat-surface-high)}.thread-row.unread{border-color:color-mix(in srgb,var(--chat-primary) 50%,transparent);background:var(--chat-primary-container);color:var(--chat-on-primary-container)}.thread-icon{flex-shrink:0;width:36px;height:36px;border-radius:12px;display:grid;place-items:center;background:var(--chat-surface-highest);color:var(--chat-primary)}.thread-row.unread .thread-icon{background:color-mix(in srgb,var(--chat-on-primary-container) 12%,transparent);color:var(--chat-on-primary-container)}.thread-icon :global(svg){width:18px;height:18px}.thread-main{flex:1;min-width:0;display:grid;gap:4px}.thread-top{display:flex;justify-content:space-between;align-items:center;gap:10px}.thread-subject{font-size:.94rem;font-weight:700;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;min-width:0}.preview{margin:0;font-size:.84rem;line-height:1.35;color:var(--chat-on-variant);overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.thread-row.unread .preview{color:inherit;opacity:.85}.thread-meta{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-top:2px}.chip{font-size:.68rem;font-weight:650;padding:2px 8px;border-radius:999px;background:var(--chat-surface-highest);color:var(--chat-on-variant)}.thread-row.unread .chip{background:color-mix(in srgb,var(--chat-on-primary-container) 14%,transparent);color:inherit}.time{font-size:.72rem;color:var(--chat-on-variant);opacity:.9}.pill{flex-shrink:0;font-size:.65rem;font-weight:750;letter-spacing:.03em;text-transform:uppercase;padding:3px 8px;border-radius:999px;border:1px solid transparent}.tone-new{background:color-mix(in srgb,var(--chat-primary) 22%,transparent);color:var(--chat-primary);border-color:color-mix(in srgb,var(--chat-primary) 35%,transparent)}.tone-progress{background:color-mix(in srgb,#f9a825 22%,transparent);color:#f9a825;border-color:color-mix(in srgb,#f9a825 35%,transparent)}.tone-done{background:color-mix(in srgb,#4caf50 22%,transparent);color:#81c784;border-color:color-mix(in srgb,#4caf50 32%,transparent)}.tone-closed{background:var(--chat-surface-highest);color:var(--chat-on-variant);border-color:var(--chat-outline)}.dot{display:inline-block;width:7px;height:7px;border-radius:999px;margin-right:6px;vertical-align:middle;background:var(--chat-primary);box-shadow:0 0 0 3px color-mix(in srgb,var(--chat-primary) 28%,transparent)}

    @media (max-width:620px){.screen{padding:10px 12px 28px}.ticket-hero{min-height:92px;padding:15px}.hero-avatar,.hero-orbit{width:48px;height:48px}.hero-copy h2{font-size:1rem}.hero-copy p{font-size:.71rem}.ticket-content{padding:15px}.ticket-footer{padding:13px 15px 15px}}
    @media (max-width:480px){.reason-grid{grid-template-columns:1fr}.hero-signal{display:none}.ticket-footer{align-items:flex-start;flex-direction:column}.footer-actions{width:100%;justify-content:flex-end}.footer-actions :global(button){width:100%}.footer-info{max-width:100%}.field-bottom span:first-child{max-width:72%}}
</style>
