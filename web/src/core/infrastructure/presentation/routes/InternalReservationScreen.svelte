<script lang="ts">
    import { onMount } from "svelte";
    import { Button, Card, Icon } from "m3-svelte";
    import shoppingBagIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import arrowOutwardIcon from "@ktibow/iconset-material-symbols/arrow-outward-rounded";
    import storefrontIcon from "@ktibow/iconset-material-symbols/storefront-rounded";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { BuyState, DeliveryType, type Currency } from "../../../feature/sale/domain/entity/enums";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import { dashboard, reservationDetail } from "../navigation/nested.router";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navBackStackEntry;

    let currentUserId = "";
    let bootstrapping = true;

    async function hydrateReservations() {
        bootstrapping = true;
        try {
            const user = await sessionStore.getCurrentUser().catch(() => null);
            currentUserId = user?.$id ?? "";
            await saleStore.syncAll().catch(() => null);
        } finally {
            bootstrapping = false;
        }
    }

    onMount(() => {
        void hydrateReservations();
        const handleOnline = () => void hydrateReservations();
        window.addEventListener("online", handleOnline);
        return () => window.removeEventListener("online", handleOnline);
    });

    function saleStatusMeta(state: BuyState) {
        if (state === BuyState.VERIFIED) return { label: "Aprobada", icon: checkCircleIcon, tone: "ready" };
        if (state === BuyState.DELETED) return { label: "Rechazada", icon: cancelIcon, tone: "cancelled" };
        return { label: "En revisión", icon: scheduleIcon, tone: "pending" };
    }

    function deliveryMeta(type?: DeliveryType | null) {
        if (type === DeliveryType.DELIVERY) return { label: "A domicilio", icon: localShippingIcon };
        if (type === DeliveryType.PICKUP) return { label: "Recogida en tienda", icon: storeIcon };
        return { label: "Entrega pendiente", icon: scheduleIcon };
    }

    function itemsSummary(products: Array<{ productName?: string | null; productId: string; quantity: number }>) {
        const totalUnits = products.reduce((sum, item) => sum + item.quantity, 0);
        const names = products.slice(0, 2).map((item) => item.productName?.trim() || item.productId).join(" · ");
        return { totalUnits, preview: names ? `${names}${products.length > 2 ? ` +${products.length - 2}` : ""}` : "Sin productos" };
    }

    function formatSaleMoney(amount: number, currency?: Currency | string | null) {
        const code = (currency && String(currency)) || "USD";
        const value = Number.isFinite(amount) ? amount : 0;
        return `${value.toLocaleString("es-CU", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${code}`;
    }

    function formatSaleDate(raw: string) {
        try {
            return new Date(raw).toLocaleString("es-CU", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });
        } catch { return String(raw); }
    }

    $: items = $saleStore.items.filter((sale) => !currentUserId || sale.userId === currentUserId).slice().sort((a, b) => String(b.date).localeCompare(String(a.date)));
    $: pendingCount = items.filter((sale) => sale.verified === BuyState.UNVERIFIED).length;
    $: readyCount = items.filter((sale) => sale.verified === BuyState.VERIFIED).length;
    $: cancelledCount = items.filter((sale) => sale.verified === BuyState.DELETED).length;
</script>

<section class="screen">
    <header class="hero anim-in">
        <div class="hero-copy">
            <div class="eyebrow-row"><span class="eyebrow-icon"><Icon icon={shoppingBagIcon} /></span><span>Historial de compras</span></div>
            <h1>Mis reservas</h1>
            <p class="support">Consulta tus solicitudes de compra y sigue en un vistazo si la tienda las está revisando, aprobando o rechazando.</p>
        </div>
        {#if items.length}
            <div class="hero-summary" aria-label="Resumen de reservas">
                <div class="summary-total"><strong>{items.length}</strong><span>reservas</span></div>
                <div class="summary-divider"></div>
                <div class="summary-state pending"><i></i><strong>{pendingCount}</strong><span>en revisión</span></div>
                <div class="summary-state ready"><i></i><strong>{readyCount}</strong><span>aprobadas</span></div>
                {#if cancelledCount}<div class="summary-state cancelled"><i></i><strong>{cancelledCount}</strong><span>rechazadas</span></div>{/if}
            </div>
        {/if}
    </header>

    {#if bootstrapping}
        <Card variant="filled" class="empty-state anim-in"><div class="skeleton-pulse"></div><div><h2>Cargando reservas…</h2><p>Sincronizando tus solicitudes de compra.</p></div></Card>
    {:else if !items.length}
        <Card variant="outlined" class="empty-state anim-in">
            <div class="empty-icon"><Icon icon={shoppingBagIcon} /></div>
            <div><h2>Aún no tienes reservas</h2><p>Aquí aparecerán tus solicitudes cuando realices una compra.</p></div>
            <Button class="action-btn" variant="filled" size="m" onclick={() => navController.navigate(dashboard.path)}><Icon icon={storefrontIcon} />Ver productos</Button>
        </Card>
    {:else}
        <div class="list" aria-label="Lista de reservas">
            {#each items as sale, i (sale.id)}
                {@const meta = saleStatusMeta(sale.verified)}
                {@const delivery = deliveryMeta(sale.deliveryType)}
                {@const summary = itemsSummary(sale.products)}
                <button class="sale-card {meta.tone} anim-card" type="button" style={`--i:${i}`} aria-label={`Abrir reserva ${sale.id.slice(0, 8)}, estado ${meta.label}`} onclick={() => navController.navigate(reservationDetail.path, { id: sale.id })}>
                    <span class="card-accent" aria-hidden="true"></span>
                    <div class="sale-copy">
                        <div class="card-head">
                            <div class="title-block"><span class="card-kicker">Solicitud de compra</span><strong>#{sale.id.slice(0, 8)}</strong></div>
                            <span class="status-badge {meta.tone}"><Icon icon={meta.icon} /><span>{meta.label}</span></span>
                        </div>
                        <div class="card-divider"></div>
                        <div class="card-main">
                            <div class="amount-block"><span>Total reservado</span><strong>{formatSaleMoney(sale.amount, sale.currency)}</strong></div>
                            <span class="open-chip"><Icon icon={arrowOutwardIcon} /></span>
                        </div>
                        <div class="detail-strip">
                            <div class="detail-item"><span class="detail-icon"><Icon icon={delivery.icon} /></span><div><small>Entrega</small><strong>{delivery.label}</strong></div></div>
                            <div class="detail-item"><span class="detail-icon"><Icon icon={shoppingBagIcon} /></span><div><small>Productos</small><strong>{summary.totalUnits} {summary.totalUnits === 1 ? "artículo" : "artículos"}</strong></div></div>
                        </div>
                        <div class="meta-row">
                            <div><small>Fecha de solicitud</small><strong>{formatSaleDate(sale.date)}</strong></div>
                            <div class="products-meta"><small>Resumen</small><strong>{summary.preview}</strong></div>
                            {#if sale.currency}<span class="currency-badge">{String(sale.currency)}</span>{/if}
                        </div>
                    </div>
                </button>
            {/each}
        </div>
    {/if}
</section>

<style>
    .screen {
        --surface: var(--m3c-surface-container, var(--md-sys-color-surface-container));
        --surface-high: var(--m3c-surface-container-high, var(--md-sys-color-surface-container-high));
        --surface-highest: var(--m3c-surface-container-highest, var(--md-sys-color-surface-container-highest));
        --on: var(--m3c-on-surface, var(--md-sys-color-on-surface));
        --on-variant: var(--m3c-on-surface-variant, var(--md-sys-color-on-surface-variant));
        --primary: var(--m3c-primary, var(--md-sys-color-primary));
        --primary-container: var(--m3c-primary-container, var(--md-sys-color-primary-container));
        --on-primary-container: var(--m3c-on-primary-container, var(--md-sys-color-on-primary-container));
        --outline: var(--m3c-outline-variant, var(--md-sys-color-outline-variant));
        --success: #62b875; --warning: #e8a82f; --error: #d66b72;
        display:grid; gap:1rem; align-content:start; width:min(100%,1080px); margin:0 auto; padding:10px 18px 38px; box-sizing:border-box; color:var(--on);
    }
    .hero { position:relative; display:flex; justify-content:space-between; align-items:stretch; gap:1.2rem; padding:1.2rem; border:1px solid color-mix(in srgb,var(--outline) 68%,transparent); border-radius:1.55rem; overflow:hidden; background:radial-gradient(90% 130% at 100% 0%,color-mix(in srgb,var(--primary) 14%,transparent),transparent 58%),linear-gradient(145deg,var(--surface-highest),var(--surface)); box-shadow:0 10px 30px rgb(0 0 0/.08); }
    .hero::after { content:""; position:absolute; width:180px; height:180px; right:-90px; bottom:-105px; border-radius:50%; background:color-mix(in srgb,var(--primary) 9%,transparent); }
    .hero-copy { position:relative; z-index:1; flex:1 1 25rem; min-width:0; }
    .eyebrow-row { display:flex; align-items:center; gap:.45rem; color:var(--primary); margin-bottom:.42rem; font-size:.68rem; font-weight:850; letter-spacing:.1em; text-transform:uppercase; }
    .eyebrow-icon { display:grid; place-items:center; width:24px; height:24px; border-radius:8px; background:var(--primary-container); color:var(--on-primary-container); }
    .eyebrow-icon :global(svg){width:15px;height:15px}.hero h1{margin:0;font-size:clamp(1.55rem,3.5vw,2rem);line-height:1.08;letter-spacing:-.04em}.support{max-width:620px;margin:.45rem 0 0;color:var(--on-variant);font-size:.84rem;line-height:1.5}
    .hero-summary{position:relative;z-index:1;display:flex;align-items:center;align-self:center;flex-wrap:wrap;gap:.5rem;min-width:min(100%,340px);padding:.6rem;border:1px solid color-mix(in srgb,var(--outline) 48%,transparent);border-radius:1.15rem;background:color-mix(in srgb,var(--surface) 72%,transparent);backdrop-filter:blur(12px)}
    .summary-total,.summary-state{display:grid;gap:.05rem;align-content:center;min-width:58px}.summary-total{padding-inline:.35rem}.summary-total strong{font-size:1.35rem;line-height:1}.summary-total span,.summary-state span{color:var(--on-variant);font-size:.61rem;font-weight:700;white-space:nowrap}.summary-divider{width:1px;height:34px;background:var(--outline);opacity:.65}.summary-state{grid-template-columns:auto auto;column-gap:.35rem;padding:.4rem;border-radius:.75rem}.summary-state i{width:6px;height:6px;border-radius:50%;grid-row:1/span 2;align-self:center}.summary-state strong{font-size:.88rem}.summary-state.pending i{background:var(--warning)}.summary-state.ready i{background:var(--success)}.summary-state.cancelled i{background:var(--error)}
    .list{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.85rem}.sale-card{position:relative;display:block;width:100%;min-width:0;padding:0;border:1px solid color-mix(in srgb,var(--outline) 58%,transparent);border-radius:1.35rem;overflow:hidden;color:var(--on);background:var(--surface);text-align:left;cursor:pointer;box-shadow:0 8px 20px rgb(0 0 0/.07);transition:transform .2s cubic-bezier(.22,1,.36,1),box-shadow .2s,border-color .2s}.sale-card:hover{transform:translateY(-3px);border-color:color-mix(in srgb,var(--primary) 28%,var(--outline));box-shadow:0 16px 34px rgb(0 0 0/.12)}.sale-card:active{transform:translateY(-1px) scale(.997)}.sale-card:focus-visible{outline:2px solid var(--primary);outline-offset:3px}.sale-card.anim-card{animation:card-in .42s cubic-bezier(.22,1,.36,1) both;animation-delay:calc(var(--i,0)*45ms)}
    .card-accent{position:absolute;inset:0 auto 0 0;width:4px;background:var(--outline)}.pending .card-accent{background:var(--warning)}.ready .card-accent{background:var(--success)}.cancelled .card-accent{background:var(--error)}
    .sale-copy{display:grid;gap:.85rem;min-width:0;padding:1.05rem 1.05rem 1rem 1.2rem;background:radial-gradient(85% 110% at 100% 0%,color-mix(in srgb,var(--primary) 6%,transparent),transparent 58%),var(--surface)}.pending .sale-copy{background:radial-gradient(85% 110% at 100% 0%,color-mix(in srgb,var(--warning) 11%,transparent),transparent 58%),var(--surface)}.ready .sale-copy{background:radial-gradient(85% 110% at 100% 0%,color-mix(in srgb,var(--success) 10%,transparent),transparent 58%),var(--surface)}.cancelled .sale-copy{background:radial-gradient(85% 110% at 100% 0%,color-mix(in srgb,var(--error) 9%,transparent),transparent 58%),var(--surface)}
    .card-head,.card-main,.meta-row{display:flex;align-items:center;justify-content:space-between;gap:.8rem;min-width:0}.title-block{display:grid;gap:.15rem;min-width:0}.card-kicker,.amount-block>span,.detail-item small,.meta-row small{color:var(--on-variant);font-size:.62rem;font-weight:700;letter-spacing:.045em}.card-kicker{font-size:.6rem;text-transform:uppercase;letter-spacing:.08em;opacity:.8}.title-block strong{font-size:.98rem;line-height:1.1;letter-spacing:-.02em;overflow:hidden;text-overflow:ellipsis}.status-badge{display:inline-flex;align-items:center;gap:.34rem;flex:0 0 auto;min-height:30px;padding:.28rem .62rem;border-radius:999px;font-size:.68rem;font-weight:800;border:1px solid transparent}.status-badge :global(svg){width:15px;height:15px}.status-badge.pending{color:var(--warning);background:color-mix(in srgb,var(--warning) 13%,var(--surface));border-color:color-mix(in srgb,var(--warning) 25%,transparent)}.status-badge.ready{color:var(--success);background:color-mix(in srgb,var(--success) 13%,var(--surface));border-color:color-mix(in srgb,var(--success) 25%,transparent)}.status-badge.cancelled{color:var(--error);background:color-mix(in srgb,var(--error) 12%,var(--surface));border-color:color-mix(in srgb,var(--error) 24%,transparent)}
    .card-divider{height:1px;background:color-mix(in srgb,var(--outline) 55%,transparent)}.card-main{align-items:flex-end}.amount-block{display:grid;gap:.25rem;min-width:0}.amount-block>span{text-transform:uppercase;letter-spacing:.07em}.amount-block strong{font-size:clamp(1.35rem,2.8vw,1.7rem);line-height:1;letter-spacing:-.04em;font-weight:850;overflow-wrap:anywhere}.open-chip{display:grid;place-items:center;flex:0 0 auto;width:34px;height:34px;border-radius:50%;color:var(--on-primary-container);background:var(--primary-container);transition:transform .2s}.open-chip :global(svg){width:17px;height:17px}.sale-card:hover .open-chip{transform:translate(2px,-2px)}
    .detail-strip{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:.5rem}.detail-item{display:flex;align-items:center;gap:.55rem;min-width:0;padding:.62rem .68rem;border-radius:.9rem;background:color-mix(in srgb,var(--surface-high) 75%,transparent);border:1px solid color-mix(in srgb,var(--outline) 35%,transparent)}.detail-icon{display:grid;place-items:center;flex:0 0 auto;width:29px;height:29px;border-radius:9px;color:var(--primary);background:color-mix(in srgb,var(--primary-container) 70%,transparent)}.detail-icon :global(svg){width:16px;height:16px}.detail-item>div{display:grid;gap:.08rem;min-width:0}.detail-item strong{font-size:.7rem;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.meta-row{align-items:flex-end;gap:.75rem}.meta-row>div{display:grid;gap:.12rem;min-width:0;flex:1 1 0}.meta-row strong{color:var(--on-variant);font-size:.68rem;line-height:1.35;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.currency-badge{flex:0 0 auto;padding:.25rem .45rem;border-radius:.5rem;color:var(--on-variant);background:var(--surface-highest);font-size:.58rem;font-weight:800;letter-spacing:.05em}
    .empty-state{display:grid;justify-items:center;gap:.7rem;padding:2.4rem 1.4rem;text-align:center;border-radius:1.4rem}.empty-state h2{margin:0;font-size:1rem}.empty-state p{margin:0;color:var(--on-variant);font-size:.8rem}.empty-icon{display:grid;place-items:center;width:56px;height:56px;border-radius:18px;color:var(--on-primary-container);background:var(--primary-container)}.empty-icon :global(svg){width:28px;height:28px}.action-btn{margin-top:.25rem}.skeleton-pulse{width:54px;height:54px;border-radius:18px;background:var(--surface-highest);animation:pulse 1.25s ease-in-out infinite}.anim-in{animation:fade-up .4s ease both}
    @keyframes fade-up{from{opacity:0;transform:translateY(8px)}to{opacity:1;transform:translateY(0)}}@keyframes card-in{from{opacity:0;transform:translateY(12px) scale(.99)}to{opacity:1;transform:translateY(0) scale(1)}}@keyframes pulse{0%,100%{opacity:.55;transform:scale(.96)}50%{opacity:1;transform:scale(1)}}
    @media(max-width:860px){.list{grid-template-columns:1fr}.hero{align-items:flex-start;flex-direction:column}.hero-summary{width:100%;min-width:0;box-sizing:border-box}}
    @media(max-width:560px){.screen{padding:8px 12px 30px;gap:.8rem}.hero{padding:1rem;border-radius:1.3rem}.hero h1{font-size:1.45rem}.support{font-size:.78rem}.hero-summary{display:grid;grid-template-columns:auto 1px repeat(2,minmax(0,1fr));gap:.4rem}.summary-divider{height:30px}.summary-state{min-width:0}.summary-state span{white-space:normal}.status-badge{min-height:28px;padding-inline:.52rem}.status-badge span{display:none}.amount-block strong{font-size:1.3rem}.detail-strip{grid-template-columns:1fr}.meta-row{align-items:flex-start;flex-wrap:wrap}.products-meta{flex-basis:100%}}
    @media(prefers-reduced-motion:reduce){.anim-in,.anim-card,.skeleton-pulse{animation:none!important}.sale-card,.open-chip{transition:none!important}}
</style>
