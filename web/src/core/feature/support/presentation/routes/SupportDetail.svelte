<script lang="ts">
    import { onMount } from "svelte";
    import type { NavBackStackEntry } from "../../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../../lib/navigation/NavController";
    import Icon from "../../../../infrastructure/presentation/components/Icon.svelte";
    import { toastStore } from "../../../../infrastructure/presentation/viewmodel/toast.store";
    import { logger } from "../../../../infrastructure/presentation/util/logger.service";
    import { supportInboxStore } from "../viewmodel/support-inbox.store";
    import type { SupportMessage, SupportStatus } from "../../domain/entity/SupportMessage";
    import { ArrowLeft, Clock, Mail, MessageSquareText } from "lucide-svelte";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    const messageId = navBackStackEntry?.args?.id ?? "";
    let loading = false;
    let message: SupportMessage | null = null;

    $: message = messageId ? $supportInboxStore.items.find((m) => m.id === messageId) ?? null : null;

    onMount(() => {
        if (!messageId) return;
        if (message) return;
        loading = true;
        supportInboxStore
            .syncAll()
            .catch((e) => {
                logger.error(e?.message ?? e, e?.stack);
                toastStore.error("No se pudo cargar el mensaje.");
            })
            .finally(() => (loading = false));
    });

    function back() {
        navController.popBackStack();
    }

    async function setStatus(next: SupportStatus) {
        if (!messageId) return;
        try {
            await supportInboxStore.setStatus(messageId, next);
            toastStore.success("Estado actualizado", 1200);
        } catch (e: any) {
            logger.error(e?.message ?? e, e?.stack);
            toastStore.error("No se pudo actualizar el estado.");
        }
    }
</script>

<section class="mgmt-container">
    <header class="mgmt-page-head">
        <div class="mgmt-page-title">
            <button class="mgmt-btn ghost" type="button" on:click={back}>
                <Icon icon={ArrowLeft} size={18} ariaLabel="Volver" />
                Volver
            </button>
            <div>
                <h1 class="mgmt-h1">Detalle de mensaje</h1>
                <p class="mgmt-muted">Soporte y comunicaciones</p>
            </div>
        </div>
    </header>

    {#if !messageId}
        <div class="mgmt-card">
            <p class="mgmt-muted">Falta el id del mensaje.</p>
        </div>
    {:else if loading}
        <div class="mgmt-card">
            <p class="mgmt-muted">Cargando...</p>
        </div>
    {:else if !message}
        <div class="mgmt-card">
            <p class="mgmt-muted">No se encontró el mensaje.</p>
        </div>
    {:else}
        <div class="detail-card">
            <div class="detail-head">
                <div class="detail-title">
                    <div class="detail-ico">
                        <Icon icon={MessageSquareText} size={18} ariaLabel="Mensaje" />
                    </div>
                    <div>
                        <h2 class="detail-h2">{message.subject || "Sin asunto"}</h2>
                        <div class="meta">
                            <span class="meta-item">
                                <Icon icon={Mail} size={14} ariaLabel="Email" />
                                {message.fromEmail}
                            </span>
                            <span class="meta-item">
                                <Icon icon={Clock} size={14} ariaLabel="Fecha" />
                                {new Date(message.createdAtIso).toLocaleString()}
                            </span>
                        </div>
                    </div>
                </div>

                <div class="status-actions">
                    <span class="pill {message.status}">{message.status}</span>
                    <div class="btns">
                        <button class="mgmt-btn sm" type="button" on:click={() => setStatus("nuevo")}>
                            Nuevo
                        </button>
                        <button class="mgmt-btn sm ghost" type="button" on:click={() => setStatus("en_proceso")}>
                            En proceso
                        </button>
                        <button class="mgmt-btn sm ghost" type="button" on:click={() => setStatus("resuelto")}>
                            Resuelto
                        </button>
                    </div>
                </div>
            </div>

            <div class="detail-body">
                <p class="body">{message.body}</p>
            </div>
        </div>
    {/if}
</section>

<style>
    .detail-card {
        border-radius: 22px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
        box-shadow: 0 18px 44px color-mix(in srgb, black 35%, transparent);
        overflow: hidden;
    }

    .detail-head {
        padding: 16px;
        display: grid;
        gap: 14px;
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
    }

    .detail-title {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 12px;
        align-items: start;
    }

    .detail-ico {
        width: 38px;
        height: 38px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 32%, transparent);
    }

    .detail-h2 {
        margin: 0;
        font-size: 1.2rem;
        letter-spacing: -0.01em;
        font-weight: 950;
    }

    .meta {
        margin-top: 6px;
        display: inline-flex;
        gap: 12px;
        flex-wrap: wrap;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 72%, transparent);
        font-size: 0.88rem;
    }

    .meta-item {
        display: inline-flex;
        gap: 6px;
        align-items: center;
    }

    .status-actions {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        align-items: center;
        flex-wrap: wrap;
    }

    .btns {
        display: inline-flex;
        gap: 8px;
        flex-wrap: wrap;
    }

    .mgmt-btn.sm {
        padding: 8px 12px;
        border-radius: 14px;
        font-weight: 900;
    }

    .detail-body {
        padding: 16px;
    }

    .body {
        margin: 0;
        white-space: pre-wrap;
        line-height: 1.55;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 92%, transparent);
    }

    .pill {
        font-size: 0.72rem;
        font-weight: 900;
        padding: 5px 10px;
        border-radius: 999px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 35%, transparent);
    }

    .pill.nuevo {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
    }

    .pill.en_proceso {
        border-color: color-mix(in srgb, #f59e0b 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #f59e0b 14%, transparent);
    }

    .pill.resuelto {
        border-color: color-mix(in srgb, #22c55e 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #22c55e 12%, transparent);
    }
</style>



