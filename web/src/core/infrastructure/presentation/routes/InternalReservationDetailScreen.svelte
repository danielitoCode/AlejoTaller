<script lang="ts">
    import {onMount} from "svelte";
    import {Button, Card, Icon} from "m3-svelte";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import inventoryIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import paymentsIcon from "@ktibow/iconset-material-symbols/payments-rounded";
    import eventIcon from "@ktibow/iconset-material-symbols/event-rounded";
    import shieldCheckIcon from "@ktibow/iconset-material-symbols/shield-check-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {productStore} from "../../../feature/product/presentation/viewmodel/product.store";
    import {BuyState, DeliveryType} from "../../../feature/sale/domain/entity/enums";
    import {saleStore} from "../../../feature/sale/presentation/viewmodel/sale.store";
    import QRCodeDisplay from "../components/QRCodeDisplay.svelte";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    let loading = true;
    $: saleId = navBackStackEntry?.args?.id ?? "";
    $: sale = $saleStore.items.find((item) => item.id === saleId) ?? null;

    onMount(() => {
        const hydrate = async () => {
            loading = true;
            try {
                if (saleId) await saleStore.syncAll().catch(() => null);
            } finally {
                loading = false;
            }
        };

        void hydrate();
        const handleOnline = () => void hydrate();
        window.addEventListener("online", handleOnline);
        return () => window.removeEventListener("online", handleOnline);
    });

    function saleStatusMeta(state: BuyState) {
        if (state === BuyState.VERIFIED) return {label: "Tu pedido esta listo", icon: checkCircleIcon, tone: "ready"};
        if (state === BuyState.DELETED) return {label: "Reserva cancelada", icon: cancelIcon, tone: "cancelled"};
        return {label: "Esperando confirmacion de la tienda", icon: scheduleIcon, tone: "pending"};
    }

    function productName(productId: string): string {
        return $productStore.items.find((product) => product.id === productId)?.name ?? productId;
    }

    function deliverySummary() {
        if (!sale?.deliveryType) return "Aun no se definio la entrega para este pedido.";
        if (sale.deliveryType === DeliveryType.PICKUP) return "Recoger en tienda";
        return "Entrega a domicilio";
    }

    function formatDate(raw: string): string {
        try {
            return new Date(raw).toLocaleString("es-CU", {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch {
            return raw;
        }
    }

    function formatAmount(amount: number, currency?: string | null): string {
        const value = Number.isFinite(amount) ? amount : 0;
        return `${value.toLocaleString("es-CU", {minimumFractionDigits: 2, maximumFractionDigits: 2})} ${currency || "USD"}`;
    }
</script>

<section class="screen">
    <Button class="back-btn" variant="text" size="s" iconType="left" onclick={() => navController.popBackStack()}>
        <Icon icon={arrowBackIcon} />
        Volver a mis reservas
    </Button>

    {#if loading}
        <Card variant="filled" class="loading-card">
            <div class="loading-icon"><Icon icon={inventoryIcon} /></div>
            <div>
                <strong>Cargando tu reserva…</strong>
                <p>Sincronizando el estado y los detalles del pedido.</p>
            </div>
        </Card>
    {:else if sale}
        {@const meta = saleStatusMeta(sale.verified)}
        {@const itemCount = sale.products.reduce((sum, item) => sum + item.quantity, 0)}

        <header class="hero {meta.tone}">
            <div class="hero-copy">
                <div class="eyebrow-row">
                    <span class="eyebrow">Detalle de reserva</span>
                    <span class="order-chip">#{sale.id.slice(0, 8)}</span>
                </div>
                <h1>Tu solicitud de compra</h1>
                <p>Presenta el código de tu reserva en la tienda para identificar rápidamente tu pedido.</p>
            </div>
            <div class="hero-total">
                <span>Total reservado</span>
                <strong>{formatAmount(sale.amount, sale.currency ? String(sale.currency) : null)}</strong>
            </div>
        </header>

        <section class="status-banner {meta.tone}" aria-label="Estado de la reserva">
            <div class="status-icon"><Icon icon={meta.icon} /></div>
            <div class="status-copy">
                <strong>{meta.label}</strong>
                <span>
                    {#if meta.tone === "pending"}
                        La tienda debe revisar y aprobar esta solicitud.
                    {:else if meta.tone === "ready"}
                        La tienda ha aprobado tu solicitud. Ya puedes continuar con el proceso de entrega.
                    {:else}
                        Esta reserva ya no está activa.
                    {/if}
                </span>
            </div>
            <span class="status-label">{meta.tone === "pending" ? "Pendiente" : meta.tone === "ready" ? "Aprobada" : "Cancelada"}</span>
        </section>

        <div class="layout">
            <Card variant="elevated" class="qr-card">
                <div class="card-heading">
                    <div class="heading-icon qr"><Icon icon={qrCodeIcon} /></div>
                    <div>
                        <span class="section-kicker">Identificacion</span>
                        <h2>Código QR</h2>
                    </div>
                </div>

                <div class="qr-stage">
                    <div class="qr-frame">
                        <QRCodeDisplay {sale} />
                    </div>
                </div>

                <div class="qr-caption">
                    <strong>Escanea para reconocer tu pedido</strong>
                    <span>El operador puede usar este código para abrir los datos de la reserva.</span>
                </div>

                <div class="qr-meta">
                    <div><span>ID</span><strong>{sale.id}</strong></div>
                    <div><span>Creada</span><strong>{formatDate(sale.date)}</strong></div>
                </div>
            </Card>

            <div class="side-column">
                <Card variant="filled" class="detail-card">
                    <div class="card-heading compact">
                        <div class="heading-icon products"><Icon icon={inventoryIcon} /></div>
                        <div>
                            <span class="section-kicker">Contenido</span>
                            <h2>Productos</h2>
                        </div>
                        <span class="count-badge">{itemCount} {itemCount === 1 ? "unidad" : "unidades"}</span>
                    </div>

                    <div class="items">
                        {#each sale.products as item, i (item.productId + i)}
                            <div class="item-row">
                                <div class="item-index">{i + 1}</div>
                                <div class="item-copy">
                                    <strong>{productName(item.productId)}</strong>
                                    <span>{item.quantity} {item.quantity === 1 ? "unidad" : "unidades"}</span>
                                </div>
                                <strong class="item-price">{formatAmount(item.price, sale.currency ? String(sale.currency) : null)}</strong>
                            </div>
                        {/each}
                    </div>

                    <div class="total-row">
                        <div>
                            <span>Total de la reserva</span>
                            <small>{itemCount} unidades</small>
                        </div>
                        <strong>{formatAmount(sale.amount, sale.currency ? String(sale.currency) : null)}</strong>
                    </div>
                </Card>

                <Card variant="outlined" class="info-card">
                    <div class="info-row">
                        <div class="info-icon"><Icon icon={eventIcon} /></div>
                        <div><span>Fecha de solicitud</span><strong>{formatDate(sale.date)}</strong></div>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><Icon icon={paymentsIcon} /></div>
                        <div><span>Moneda</span><strong>{sale.currency ? String(sale.currency) : "USD"}</strong></div>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><Icon icon={shieldCheckIcon} /></div>
                        <div><span>Tipo de operación</span><strong>Reserva de compra</strong></div>
                    </div>
                </Card>
            </div>
        </div>

        <Card variant="filled" class="delivery-card">
            <div class="card-heading">
                <div class="heading-icon delivery"><Icon icon={sale.deliveryType === DeliveryType.DELIVERY ? localShippingIcon : storeIcon} /></div>
                <div>
                    <span class="section-kicker">Logística</span>
                    <h2>Entrega seleccionada</h2>
                </div>
            </div>

            <div class="delivery-body">
                <div class="delivery-main">
                    <div class="delivery-type-icon"><Icon icon={sale.deliveryType === DeliveryType.DELIVERY ? localShippingIcon : storeIcon} /></div>
                    <div>
                        <strong>{deliverySummary()}</strong>
                        <span>
                            {#if sale.deliveryType === DeliveryType.PICKUP}
                                Tu pedido se recogerá directamente en el taller cuando quede confirmado.
                            {:else if sale.deliveryType === DeliveryType.DELIVERY}
                                El vendedor recibirá la dirección asociada a esta solicitud.
                            {:else}
                                Las reservas antiguas pueden no tener información de entrega completa.
                            {/if}
                        </span>
                    </div>
                </div>

                {#if sale.deliveryType === DeliveryType.DELIVERY && sale.deliveryAddress}
                    <div class="address-grid">
                        <div><span>Dirección</span><strong>{sale.deliveryAddress.mainStreet}, No. {sale.deliveryAddress.houseNumber}</strong></div>
                        <div><span>Municipio / provincia</span><strong>{sale.deliveryAddress.municipality}, {sale.deliveryAddress.province}</strong></div>
                        <div><span>Teléfono</span><strong>{sale.deliveryAddress.phone}</strong></div>
                        {#if sale.deliveryAddress.betweenStreets}
                            <div><span>Entre calles</span><strong>{sale.deliveryAddress.betweenStreets}</strong></div>
                        {/if}
                        {#if sale.deliveryAddress.referenceName}
                            <div><span>Preguntar por</span><strong>{sale.deliveryAddress.referenceName}</strong></div>
                        {/if}
                    </div>
                {/if}
            </div>
        </Card>
    {:else}
        <Card variant="outlined" class="not-found">
            <div class="not-found-icon"><Icon icon={inventoryIcon} /></div>
            <div>
                <h2>No se encontró la reserva</h2>
                <p>Es posible que haya sido eliminada o que ya no esté disponible en este dispositivo.</p>
            </div>
            <Button variant="tonal" size="m" onclick={() => navController.popBackStack()}>Volver a reservas</Button>
        </Card>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 1rem;
        align-content: start;
        max-width: 1040px;
        width: 100%;
        margin-inline: auto;
        padding-bottom: 1.5rem;
    }

    h1, h2, p { margin: 0; }

    .back-btn { justify-self: start; }

    .hero {
        display: flex;
        justify-content: space-between;
        align-items: end;
        gap: 1.5rem;
        padding: 1.4rem 1.5rem;
        border-radius: 1.65rem;
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);
        background:
            radial-gradient(100% 130% at 100% 0%, color-mix(in srgb, var(--md-sys-color-primary) 15%, transparent), transparent 56%),
            linear-gradient(160deg, var(--md-sys-color-surface-container-high), var(--md-sys-color-surface-container));
        overflow: hidden;
        position: relative;
    }

    .hero::after {
        content: "";
        position: absolute;
        width: 12rem;
        height: 12rem;
        right: -6rem;
        bottom: -8rem;
        border-radius: 50%;
        background: color-mix(in srgb, var(--md-sys-color-primary) 8%, transparent);
        pointer-events: none;
    }

    .hero-copy { min-width: 0; position: relative; z-index: 1; }
    .eyebrow-row { display: flex; align-items: center; gap: .55rem; flex-wrap: wrap; }
    .eyebrow, .section-kicker {
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        letter-spacing: .09em;
        font-size: .68rem;
        font-weight: 800;
    }
    .order-chip {
        padding: .3rem .55rem;
        border-radius: 999px;
        background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent);
        color: var(--md-sys-color-primary);
        font-size: .7rem;
        font-weight: 800;
    }
    .hero h1 { margin-top: .25rem; font-size: clamp(1.5rem, 4vw, 2.05rem); letter-spacing: -.035em; }
    .hero p:not(.eyebrow) { margin-top: .45rem; color: var(--md-sys-color-on-surface-variant); max-width: 42rem; line-height: 1.45; }
    .hero-total {
        flex: 0 0 auto;
        min-width: 9rem;
        padding: .85rem 1rem;
        border-radius: 1.1rem;
        background: color-mix(in srgb, var(--md-sys-color-surface) 55%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 45%, transparent);
        position: relative;
        z-index: 1;
    }
    .hero-total span { display: block; color: var(--md-sys-color-on-surface-variant); font-size: .7rem; font-weight: 700; }
    .hero-total strong { display: block; margin-top: .2rem; font-size: 1.35rem; letter-spacing: -.025em; }

    .status-banner {
        display: grid;
        grid-template-columns: auto 1fr auto;
        gap: .8rem;
        align-items: center;
        padding: .8rem 1rem;
        border-radius: 1.25rem;
        border: 1px solid transparent;
    }
    .status-banner.pending { background: color-mix(in srgb, #f59e0b 10%, var(--md-sys-color-surface-container)); border-color: color-mix(in srgb, #f59e0b 25%, transparent); }
    .status-banner.ready { background: color-mix(in srgb, #62d879 10%, var(--md-sys-color-surface-container)); border-color: color-mix(in srgb, #62d879 24%, transparent); }
    .status-banner.cancelled { background: color-mix(in srgb, #ff7c7c 10%, var(--md-sys-color-surface-container)); border-color: color-mix(in srgb, #ff7c7c 24%, transparent); }
    .status-icon { display: grid; place-items: center; width: 2.6rem; height: 2.6rem; border-radius: .9rem; background: color-mix(in srgb, currentColor 12%, transparent); }
    .pending .status-icon { color: #e8a11a; } .ready .status-icon { color: #69d681; } .cancelled .status-icon { color: #ff9696; }
    .status-copy { min-width: 0; display: grid; gap: .12rem; }
    .status-copy strong { font-size: .92rem; }
    .status-copy span { color: var(--md-sys-color-on-surface-variant); font-size: .78rem; }
    .status-label { padding: .38rem .65rem; border-radius: 999px; background: color-mix(in srgb, currentColor 10%, transparent); font-size: .68rem; font-weight: 800; }

    .layout { display: grid; grid-template-columns: minmax(290px, .78fr) minmax(0, 1.22fr); gap: 1rem; align-items: start; }
    .side-column { display: grid; gap: 1rem; min-width: 0; }
    .qr-card, .detail-card, .info-card, .delivery-card { border-radius: 1.45rem; }
    .qr-card { display: grid; gap: 1rem; align-content: start; padding: 1.2rem; background: linear-gradient(155deg, color-mix(in srgb, var(--md-sys-color-primary) 7%, var(--md-sys-color-surface-container-high)), var(--md-sys-color-surface-container)); }

    .card-heading { display: flex; align-items: center; gap: .7rem; min-width: 0; }
    .card-heading.compact { align-items: center; }
    .card-heading h2 { font-size: 1rem; letter-spacing: -.015em; }
    .heading-icon { display: grid; place-items: center; width: 2.65rem; height: 2.65rem; flex: 0 0 auto; border-radius: .9rem; background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent); color: var(--md-sys-color-primary); }
    .heading-icon.products { color: var(--md-sys-color-secondary); background: color-mix(in srgb, var(--md-sys-color-secondary) 12%, transparent); }
    .heading-icon.delivery { color: var(--md-sys-color-tertiary); background: color-mix(in srgb, var(--md-sys-color-tertiary) 12%, transparent); }

    .qr-stage { display: grid; place-items: center; padding: .7rem; border-radius: 1.35rem; background: rgb(255 255 255 / .96); min-height: 275px; box-shadow: inset 0 0 0 1px rgb(0 0 0 / .05), 0 12px 28px rgb(0 0 0 / .12); }
    .qr-frame { width: min(100%, 250px); aspect-ratio: 1; display: grid; place-items: center; padding: .45rem; }
    .qr-frame :global(img), .qr-frame :global(svg), .qr-frame :global(canvas) { width: 100% !important; height: auto !important; max-width: 235px; }
    .qr-caption { display: grid; gap: .25rem; text-align: center; }
    .qr-caption strong { font-size: .88rem; }
    .qr-caption span { color: var(--md-sys-color-on-surface-variant); font-size: .74rem; line-height: 1.4; }
    .qr-meta { display: grid; grid-template-columns: 1fr 1fr; gap: .55rem; }
    .qr-meta div { display: grid; gap: .15rem; padding: .65rem .7rem; border-radius: .9rem; background: color-mix(in srgb, var(--md-sys-color-surface) 50%, transparent); border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 35%, transparent); min-width: 0; }
    .qr-meta span, .info-row span, .address-grid span { color: var(--md-sys-color-on-surface-variant); font-size: .67rem; font-weight: 700; }
    .qr-meta strong { font-size: .72rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .detail-card { display: grid; gap: 1rem; }
    .count-badge { margin-left: auto; padding: .35rem .65rem; border-radius: 999px; background: var(--md-sys-color-secondary-container); color: var(--md-sys-color-on-secondary-container); font-size: .68rem; font-weight: 800; white-space: nowrap; }
    .items { display: grid; gap: .55rem; }
    .item-row { display: grid; grid-template-columns: 2rem minmax(0, 1fr) auto; align-items: center; gap: .65rem; padding: .72rem; border-radius: 1rem; background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 82%, transparent); border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 30%, transparent); }
    .item-index { display: grid; place-items: center; width: 2rem; height: 2rem; border-radius: .7rem; background: color-mix(in srgb, var(--md-sys-color-primary) 10%, transparent); color: var(--md-sys-color-primary); font-size: .7rem; font-weight: 800; }
    .item-copy { min-width: 0; display: grid; gap: .15rem; }
    .item-copy strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: .84rem; }
    .item-copy span { color: var(--md-sys-color-on-surface-variant); font-size: .72rem; }
    .item-price { font-size: .8rem; white-space: nowrap; }
    .total-row { display: flex; justify-content: space-between; align-items: center; gap: 1rem; padding-top: .9rem; border-top: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 35%, transparent); }
    .total-row div { display: grid; gap: .12rem; }
    .total-row span { font-size: .82rem; font-weight: 700; }
    .total-row small { color: var(--md-sys-color-on-surface-variant); font-size: .68rem; }
    .total-row > strong { font-size: 1.1rem; color: var(--md-sys-color-primary); }

    .info-card { display: grid; grid-template-columns: repeat(3, 1fr); gap: .5rem; padding: 1rem; }
    .info-row { display: flex; gap: .55rem; align-items: center; min-width: 0; padding: .55rem; border-radius: .85rem; background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 65%, transparent); }
    .info-icon { display: grid; place-items: center; width: 2rem; height: 2rem; flex: 0 0 auto; border-radius: .65rem; color: var(--md-sys-color-primary); background: color-mix(in srgb, var(--md-sys-color-primary) 10%, transparent); }
    .info-row div:last-child { display: grid; gap: .1rem; min-width: 0; }
    .info-row strong { font-size: .73rem; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

    .delivery-card { display: grid; gap: 1rem; }
    .delivery-body { display: grid; gap: .8rem; }
    .delivery-main { display: flex; gap: .8rem; align-items: flex-start; padding: .9rem; border-radius: 1.05rem; background: color-mix(in srgb, var(--md-sys-color-tertiary) 8%, var(--md-sys-color-surface-container-high)); }
    .delivery-type-icon { display: grid; place-items: center; width: 2.7rem; height: 2.7rem; flex: 0 0 auto; border-radius: .9rem; color: var(--md-sys-color-tertiary); background: color-mix(in srgb, var(--md-sys-color-tertiary) 13%, transparent); }
    .delivery-main > div:last-child { display: grid; gap: .2rem; }
    .delivery-main strong { font-size: .9rem; }
    .delivery-main span { color: var(--md-sys-color-on-surface-variant); font-size: .76rem; line-height: 1.45; }
    .address-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .55rem; }
    .address-grid div { display: grid; gap: .15rem; padding: .7rem; border-radius: .9rem; background: var(--md-sys-color-surface-container-high); }
    .address-grid strong { font-size: .75rem; }

    .loading-card, .not-found { display: flex; align-items: center; gap: .9rem; padding: 1.2rem; border-radius: 1.35rem; }
    .loading-icon, .not-found-icon { display: grid; place-items: center; width: 2.8rem; height: 2.8rem; border-radius: .95rem; background: color-mix(in srgb, var(--md-sys-color-primary) 12%, transparent); color: var(--md-sys-color-primary); flex: 0 0 auto; }
    .loading-card p, .not-found p { margin-top: .2rem; color: var(--md-sys-color-on-surface-variant); font-size: .8rem; }
    .not-found { flex-wrap: wrap; }
    .not-found > div:nth-child(2) { flex: 1; min-width: 14rem; }

    @media (max-width: 900px) {
        .layout { grid-template-columns: 1fr; }
        .qr-card { max-width: none; }
        .info-card { grid-template-columns: 1fr; }
    }

    @media (max-width: 640px) {
        .screen { gap: .8rem; }
        .hero { flex-direction: column; align-items: stretch; padding: 1.15rem; border-radius: 1.35rem; }
        .hero-total { width: 100%; min-width: 0; }
        .status-banner { grid-template-columns: auto 1fr; }
        .status-label { grid-column: 2; justify-self: start; }
        .qr-stage { min-height: 235px; }
        .qr-meta { grid-template-columns: 1fr; }
        .address-grid { grid-template-columns: 1fr; }
        .item-row { grid-template-columns: 1.8rem minmax(0, 1fr); }
        .item-price { grid-column: 2; justify-self: start; margin-top: -.25rem; }
        .delivery-main { align-items: flex-start; }
    }

    @media (prefers-reduced-motion: reduce) {
        * { scroll-behavior: auto !important; }
    }
</style>
