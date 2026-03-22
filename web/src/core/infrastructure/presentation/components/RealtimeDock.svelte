<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import { derived, get } from "svelte/store";
    import Icon from "./Icon.svelte";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import { supportInboxStore } from "../../../feature/support/presentation/viewmodel/support-inbox.store";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import type { SupportMessage } from "../../../feature/support/domain/entity/SupportMessage";
    import type { Sale } from "../../../feature/sale/domain/entity/Sale";
    import { BuyState } from "../../../feature/sale/domain/entity/enums";
    import { support, supportDetail, sales, salesDetail } from "../navigation/nested.router";
    import { BadgeDollarSign, ChevronRight, MessageSquareText, X } from "lucide-svelte";

    export let navController: NavController;

    let openPanel: "support" | "sales" | null = null;
    let dockEl: HTMLElement | null = null;

    const supportPending = derived(supportInboxStore, ($s) => $s.items.filter((m) => m.status === "nuevo").length);
    const salesPending = derived(saleStore, ($s) => $s.items.filter((s) => s.verified === BuyState.UNVERIFIED).length);

    const supportItems = derived(supportInboxStore, ($s) =>
        $s.items
            .slice()
            .sort((a, b) => b.createdAtIso.localeCompare(a.createdAtIso))
            .slice(0, 6)
    );

    const saleItems = derived(saleStore, ($s) =>
        $s.items
            .slice()
            .sort((a, b) => String(b.date ?? "").localeCompare(String(a.date ?? "")))
            .slice(0, 6)
    );

    let lastSupportPending = get(supportPending);
    let lastSalesPending = get(salesPending);
    let supportBump = 0;
    let salesBump = 0;

    const unsubSupport = supportPending.subscribe((n) => {
        if (n > lastSupportPending) supportBump++;
        lastSupportPending = n;
    });
    const unsubSales = salesPending.subscribe((n) => {
        if (n > lastSalesPending) salesBump++;
        lastSalesPending = n;
    });

    function gotoSupportList() {
        navController.navigate(support.path);
        openPanel = null;
    }

    function gotoSalesList() {
        navController.navigate(sales.path);
        openPanel = null;
    }

    function gotoSupportItem(item: SupportMessage) {
        navController.navigate(supportDetail.path, { id: item.id });
        openPanel = null;
    }

    function gotoSaleItem(item: Sale) {
        navController.navigate(salesDetail.path, { id: item.id });
        openPanel = null;
    }

    function toggle(panel: "support" | "sales") {
        openPanel = openPanel === panel ? null : panel;
    }

    function close() {
        openPanel = null;
    }

    function handleOutside(e: PointerEvent) {
        if (!dockEl) return;
        const target = e.target as Node | null;
        if (target && dockEl.contains(target)) return;
        close();
    }

    onMount(() => {
        document.addEventListener("pointerdown", handleOutside, true);
    });

    onDestroy(() => {
        document.removeEventListener("pointerdown", handleOutside, true);
        unsubSupport();
        unsubSales();
    });
</script>

<div class="dock" bind:this={dockEl} data-open={openPanel ? "true" : "false"}>
    <div class="dock-btns">
        {#key supportBump}
            <button
                class="dock-btn {openPanel === 'support' ? 'active' : ''}"
                type="button"
                aria-label="Mensajes"
                title="Mensajes"
                on:click={() => toggle("support")}
            >
                <Icon icon={MessageSquareText} size={18} ariaLabel="Mensajes" />
                {#if $supportPending > 0}
                    <span class="badge" aria-label="Mensajes pendientes">{$supportPending}</span>
                {/if}
            </button>
        {/key}

        {#key salesBump}
            <button
                class="dock-btn {openPanel === 'sales' ? 'active' : ''}"
                type="button"
                aria-label="Ventas"
                title="Ventas"
                on:click={() => toggle("sales")}
            >
                <Icon icon={BadgeDollarSign} size={18} ariaLabel="Ventas" />
                {#if $salesPending > 0}
                    <span class="badge" aria-label="Ventas pendientes">{$salesPending}</span>
                {/if}
            </button>
        {/key}
    </div>

    {#if openPanel === "support"}
        <div class="panel" role="dialog" aria-label="Mensajes recientes">
            <header class="panel-head">
                <strong>Mensajes</strong>
                <div class="panel-actions">
                    <button class="panel-link" type="button" on:click={gotoSupportList}>
                        Ver todos <Icon icon={ChevronRight} size={16} ariaLabel="Ver todos" />
                    </button>
                    <button class="panel-close" type="button" aria-label="Cerrar" on:click={close}>
                        <Icon icon={X} size={16} ariaLabel="Cerrar" />
                    </button>
                </div>
            </header>
            <div class="panel-body">
                {#if $supportItems.length === 0}
                    <div class="empty">Sin mensajes.</div>
                {:else}
                    {#each $supportItems as item (item.id)}
                        <button class="row" type="button" on:click={() => gotoSupportItem(item)}>
                            <div class="row-main">
                                <div class="row-top">
                                    <span class="row-title">{item.subject || "Sin asunto"}</span>
                                    <span class="pill {item.status}">{item.status}</span>
                                </div>
                                <div class="row-sub">
                                    <span class="muted">{item.fromName || item.fromEmail || "Usuario"}</span>
                                    <span class="dot">•</span>
                                    <span class="muted">{new Date(item.createdAtIso).toLocaleString()}</span>
                                </div>
                            </div>
                            <Icon icon={ChevronRight} size={16} ariaLabel="Abrir" className="chev" />
                        </button>
                    {/each}
                {/if}
            </div>
        </div>
    {/if}

    {#if openPanel === "sales"}
        <div class="panel" role="dialog" aria-label="Ventas recientes">
            <header class="panel-head">
                <strong>Ventas</strong>
                <div class="panel-actions">
                    <button class="panel-link" type="button" on:click={gotoSalesList}>
                        Ver todas <Icon icon={ChevronRight} size={16} ariaLabel="Ver todas" />
                    </button>
                    <button class="panel-close" type="button" aria-label="Cerrar" on:click={close}>
                        <Icon icon={X} size={16} ariaLabel="Cerrar" />
                    </button>
                </div>
            </header>
            <div class="panel-body">
                {#if $saleItems.length === 0}
                    <div class="empty">Sin ventas.</div>
                {:else}
                    {#each $saleItems as item (item.id)}
                        <button class="row" type="button" on:click={() => gotoSaleItem(item)}>
                            <div class="row-main">
                                <div class="row-top">
                                    <span class="row-title">Venta #{item.id.slice(0, 8)}</span>
                                    <span class="pill {item.verified === BuyState.UNVERIFIED ? 'unverified' : 'verified'}">
                                        {item.verified}
                                    </span>
                                </div>
                                <div class="row-sub">
                                    <span class="muted">${item.amount.toFixed(2)}</span>
                                    <span class="dot">•</span>
                                    <span class="muted">{new Date(item.date).toLocaleString()}</span>
                                </div>
                            </div>
                            <Icon icon={ChevronRight} size={16} ariaLabel="Abrir" className="chev" />
                        </button>
                    {/each}
                {/if}
            </div>
        </div>
    {/if}
</div>

<style>
    .dock {
        position: fixed;
        top: 14px;
        right: 16px;
        z-index: 60;
        display: grid;
        gap: 10px;
        pointer-events: none;
    }

    .dock-btns {
        display: flex;
        gap: 10px;
        justify-content: flex-end;
        pointer-events: auto;
    }

    .dock-btn {
        position: relative;
        height: 42px;
        width: 42px;
        border-radius: 14px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 90%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        color: var(--md-sys-color-on-surface);
        display: grid;
        place-items: center;
        box-shadow: 0 12px 30px color-mix(in srgb, black 35%, transparent);
        transition: transform 120ms ease, border-color 140ms ease, background 140ms ease;
        pointer-events: auto;
    }

    .dock-btn:hover {
        transform: translateY(-1px);
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, var(--md-sys-color-outline-variant));
    }

    .dock-btn.active {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 55%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary) 18%, var(--md-sys-color-surface) 60%);
    }

    .dock-btn:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 55%, white);
        outline-offset: 2px;
    }

    .dock-btn {
        animation: bump 240ms ease;
    }

    @keyframes bump {
        0% {
            transform: scale(1);
        }
        40% {
            transform: scale(1.06);
        }
        100% {
            transform: scale(1);
        }
    }

    .badge {
        position: absolute;
        top: -7px;
        right: -7px;
        min-width: 20px;
        height: 20px;
        padding: 0 6px;
        border-radius: 999px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-primary) 90%, black);
        color: var(--md-sys-color-on-primary);
        font-size: 0.72rem;
        font-weight: 900;
        display: grid;
        place-items: center;
        box-shadow: 0 12px 18px color-mix(in srgb, black 30%, transparent);
    }

    .panel {
        width: min(420px, calc(100vw - 28px));
        border-radius: 18px;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 80%, transparent);
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        backdrop-filter: blur(16px);
        -webkit-backdrop-filter: blur(16px);
        box-shadow: 0 18px 55px color-mix(in srgb, black 44%, transparent);
        overflow: hidden;
        pointer-events: auto;
    }

    .panel-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        padding: 12px 12px 10px 14px;
        border-bottom: 1px solid var(--md-sys-color-outline-variant);
    }

    .panel-actions {
        display: inline-flex;
        gap: 8px;
        align-items: center;
    }

    .panel-link {
        border: 0;
        background: transparent;
        color: color-mix(in srgb, var(--md-sys-color-primary) 85%, white);
        font-weight: 800;
        display: inline-flex;
        gap: 6px;
        align-items: center;
        padding: 6px 10px;
        border-radius: 12px;
    }

    .panel-link:hover {
        background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
    }

    .panel-close {
        width: 32px;
        height: 32px;
        border-radius: 12px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 65%, transparent);
        display: grid;
        place-items: center;
    }

    .panel-close:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 35%, var(--md-sys-color-outline-variant));
    }

    .panel-body {
        max-height: min(420px, calc(100dvh - 140px));
        overflow: auto;
        padding: 10px;
        display: grid;
        gap: 8px;
    }

    .row {
        width: 100%;
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 10px;
        align-items: center;
        text-align: left;
        border-radius: 16px;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: color-mix(in srgb, var(--md-sys-color-surface) 80%, transparent);
        padding: 10px 12px;
    }

    .row:hover {
        border-color: color-mix(in srgb, var(--md-sys-color-primary) 30%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, var(--md-sys-color-primary) 10%, var(--md-sys-color-surface) 78%);
    }

    .row:focus-visible {
        outline: 2px solid color-mix(in srgb, var(--md-sys-color-primary) 55%, white);
        outline-offset: 2px;
    }

    .row-main {
        min-width: 0;
        display: grid;
        gap: 4px;
    }

    .row-top {
        display: flex;
        gap: 10px;
        align-items: center;
        justify-content: space-between;
    }

    .row-title {
        font-weight: 900;
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
        color: color-mix(in srgb, var(--md-sys-color-on-surface) 70%, transparent);
        font-size: 0.84rem;
    }

    .muted {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 240px;
    }

    .dot {
        opacity: 0.7;
    }

    .pill {
        font-size: 0.72rem;
        font-weight: 900;
        padding: 4px 8px;
        border-radius: 999px;
        border: 1px solid var(--md-sys-color-outline-variant);
        text-transform: none;
        letter-spacing: 0.02em;
        white-space: nowrap;
        color: var(--md-sys-color-on-surface);
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

    .pill.resuelto,
    .pill.verified {
        border-color: color-mix(in srgb, #22c55e 35%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #22c55e 12%, transparent);
    }

    .pill.unverified {
        border-color: color-mix(in srgb, #a855f7 38%, var(--md-sys-color-outline-variant));
        background: color-mix(in srgb, #a855f7 12%, transparent);
    }

    .empty {
        padding: 16px;
        text-align: center;
        color: color-mix(in srgb, var(--md-sys-color-on-background) 70%, transparent);
    }

    @media (max-width: 900px) {
        .dock {
            top: 62px;
        }
    }
</style>
