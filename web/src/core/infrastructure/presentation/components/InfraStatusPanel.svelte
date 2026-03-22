<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import Icon from "./Icon.svelte";
    import LoadingSpinner from "./LoadingSpinner.svelte";
    import Sparkline from "./Sparkline.svelte";
    import { infraStatusStore } from "../viewmodel/infra-status.store";
    import { ENV } from "../../env";
    import { ExternalLink, ShieldCheck, TriangleAlert } from "lucide-svelte";

    let timer: number | null = null;

    function startPolling() {
        stopPolling();
        infraStatusStore.sync().catch(() => {});
        timer = window.setInterval(() => {
            infraStatusStore.sync().catch(() => {});
        }, 60_000);
    }

    function stopPolling() {
        if (timer) window.clearInterval(timer);
        timer = null;
    }

    onMount(() => {
        if ((ENV.infraStatusUrl || "").trim()) startPolling();
    });

    onDestroy(() => stopPolling());

    function badgeClass(ok: boolean | undefined): string {
        if (ok === true) return "badge ok";
        if (ok === false) return "badge bad";
        return "badge unk";
    }

    function safeHref(href?: string): string | null {
        if (!href) return null;
        const h = href.trim();
        if (!h) return null;
        if (h.startsWith("http://") || h.startsWith("https://")) return h;
        return null;
    }
</script>

{#if !$infraStatusStore.isConfigured}
    <section class="mgmt-card infra" aria-label="Infraestructura">
        <div class="infra-head">
            <div>
                <h2 class="mgmt-card-title" style="margin:0">Infraestructura</h2>
                <p class="mgmt-muted">Configura `VITE_INFRA_STATUS_URL` para ver el estado de Appwrite, Render y Cloudflare.</p>
            </div>
        </div>
    </section>
{:else}
    <section class="mgmt-card infra" aria-label="Infraestructura">
        <div class="infra-head">
            <div>
                <h2 class="mgmt-card-title" style="margin:0">Infraestructura</h2>
                <p class="mgmt-muted">Estado de servicios y despliegues (sin abrir plataformas externas).</p>
            </div>

            <div class="infra-actions">
                {#if $infraStatusStore.loading}
                    <span class="pill">
                        <LoadingSpinner size={16} label="Actualizando" subtle />
                        Actualizando…
                    </span>
                {/if}
                <button class="btn" type="button" on:click={() => infraStatusStore.sync()} disabled={$infraStatusStore.loading}>
                    {#if $infraStatusStore.loading}Actualizando…{:else}Refrescar{/if}
                </button>
            </div>
        </div>

        {#if $infraStatusStore.error}
            <div class="err">
                <Icon icon={TriangleAlert} size={18} ariaLabel="Error" />
                <span>{$infraStatusStore.error}</span>
            </div>
        {/if}

        <div class="infra-grid">
            <article class="card">
                <div class="top">
                    <div class="left">
                        <div class={badgeClass($infraStatusStore.data?.appwrite?.ok)}>
                            <Icon icon={ShieldCheck} size={16} ariaLabel="Appwrite" />
                        </div>
                        <div class="meta">
                            <div class="title">Appwrite</div>
                            <div class="sub">
                                {$infraStatusStore.data?.appwrite?.details ?? ($infraStatusStore.data?.appwrite?.ok ? "Operativo" : "Sin respuesta")}
                            </div>
                        </div>
                    </div>
                    {#if safeHref($infraStatusStore.data?.links?.appwrite) }
                        <a class="link" href={safeHref($infraStatusStore.data?.links?.appwrite) as string} target="_blank" rel="noreferrer">
                            <Icon icon={ExternalLink} size={16} ariaLabel="Abrir" />
                        </a>
                    {/if}
                </div>

                <div class="bottom">
                    <div class="metric">
                        <span class="label">Latencia</span>
                        <span class="value">{Math.round($infraStatusStore.data?.appwrite?.latencyMs ?? 0) || "—"} ms</span>
                    </div>
                    <Sparkline points={$infraStatusStore.history.appwrite} />
                </div>
            </article>

            <article class="card">
                <div class="top">
                    <div class="left">
                        <div class={badgeClass($infraStatusStore.data?.render?.ok)}>
                            <Icon icon={ShieldCheck} size={16} ariaLabel="Render" />
                        </div>
                        <div class="meta">
                            <div class="title">Render</div>
                            <div class="sub">
                                {$infraStatusStore.data?.render?.details ?? ($infraStatusStore.data?.render?.ok ? "Operativo" : "Sin respuesta")}
                            </div>
                        </div>
                    </div>
                    {#if safeHref($infraStatusStore.data?.links?.render)}
                        <a class="link" href={safeHref($infraStatusStore.data?.links?.render) as string} target="_blank" rel="noreferrer">
                            <Icon icon={ExternalLink} size={16} ariaLabel="Abrir" />
                        </a>
                    {/if}
                </div>

                <div class="bottom">
                    <div class="metric">
                        <span class="label">Latencia</span>
                        <span class="value">{Math.round($infraStatusStore.data?.render?.latencyMs ?? 0) || "—"} ms</span>
                    </div>
                    <Sparkline points={$infraStatusStore.history.render} />
                </div>
            </article>

            <article class="card">
                <div class="top">
                    <div class="left">
                        <div class={badgeClass($infraStatusStore.data?.cloudflare?.ok)}>
                            <Icon icon={ShieldCheck} size={16} ariaLabel="Cloudflare" />
                        </div>
                        <div class="meta">
                            <div class="title">Cloudflare</div>
                            <div class="sub">
                                {$infraStatusStore.data?.cloudflare?.details ?? ($infraStatusStore.data?.cloudflare?.ok ? "Operativo" : "Sin respuesta")}
                            </div>
                        </div>
                    </div>
                    {#if safeHref($infraStatusStore.data?.links?.cloudflare)}
                        <a class="link" href={safeHref($infraStatusStore.data?.links?.cloudflare) as string} target="_blank" rel="noreferrer">
                            <Icon icon={ExternalLink} size={16} ariaLabel="Abrir" />
                        </a>
                    {/if}
                </div>

                <div class="bottom">
                    <div class="metric">
                        <span class="label">Latencia</span>
                        <span class="value">{Math.round($infraStatusStore.data?.cloudflare?.latencyMs ?? 0) || "—"} ms</span>
                    </div>
                    <Sparkline points={$infraStatusStore.history.cloudflare} />
                </div>
            </article>
        </div>
    </section>
{/if}

<style>
    .infra {
        margin-top: 14px;
    }

    .infra-head {
        display: flex;
        justify-content: space-between;
        align-items: start;
        gap: 12px;
        margin-bottom: 12px;
    }

    .infra-actions {
        display: inline-flex;
        gap: 10px;
        align-items: center;
        flex-wrap: wrap;
        justify-content: end;
    }

    .pill {
        display: inline-flex;
        gap: 8px;
        align-items: center;
        padding: 8px 10px;
        border-radius: 999px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 25%, transparent);
        color: var(--md-sys-color-on-surface);
        font-weight: 750;
        font-size: 0.9rem;
    }

    .btn {
        height: 40px;
        border-radius: 14px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: transparent;
        color: inherit;
        cursor: pointer;
        font-weight: 800;
        padding: 0 12px;
    }

    .btn:disabled {
        opacity: 0.6;
        cursor: not-allowed;
    }

    .infra-grid {
        display: grid;
        grid-template-columns: repeat(3, minmax(0, 1fr));
        gap: 12px;
    }

    .card {
        border-radius: 18px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 92%, transparent);
        padding: 12px;
        display: grid;
        gap: 12px;
        overflow: hidden;
    }

    .top {
        display: flex;
        justify-content: space-between;
        gap: 10px;
        align-items: start;
    }

    .left {
        display: inline-flex;
        gap: 10px;
        align-items: center;
        min-width: 0;
    }

    .meta {
        min-width: 0;
        display: grid;
        gap: 2px;
    }

    .title {
        font-weight: 950;
        letter-spacing: -0.01em;
    }

    .sub {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.9rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .link {
        width: 38px;
        height: 38px;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: transparent;
        display: grid;
        place-items: center;
        color: inherit;
    }

    .link:hover {
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 32%, transparent);
    }

    .badge {
        width: 38px;
        height: 38px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 28%, transparent);
        color: var(--md-sys-color-on-surface);
        flex: 0 0 auto;
    }

    .badge.ok {
        border-color: color-mix(in srgb, #22c55e 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #22c55e 12%, transparent);
    }

    .badge.bad {
        border-color: color-mix(in srgb, var(--md-sys-color-error) 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-error) 10%, transparent);
    }

    .badge.unk {
        opacity: 0.85;
    }

    .bottom {
        display: grid;
        gap: 10px;
    }

    .metric {
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        gap: 10px;
        font-variant-numeric: tabular-nums;
    }

    .label {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.88rem;
        font-weight: 750;
    }

    .value {
        font-weight: 950;
        letter-spacing: -0.01em;
    }

    .err {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        padding: 10px 12px;
        border-radius: 14px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-error) 30%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-error-container) 65%, transparent);
        color: var(--md-sys-color-on-error-container);
        margin-bottom: 12px;
        font-weight: 750;
    }

    @media (max-width: 980px) {
        .infra-grid {
            grid-template-columns: 1fr;
        }
    }
</style>

