<script lang="ts">
    import { onMount } from "svelte";
    import {Button, Card, Icon} from "m3-svelte";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
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
                if (saleId) {
                    await saleStore.syncAll().catch(() => null);
                }
            } finally {
                loading = false;
            }
        };

        void hydrate();

        const handleOnline = () => {
            void hydrate();
        };
        window.addEventListener("online", handleOnline);
        return () => {
            window.removeEventListener("online", handleOnline);
        };
    });

    function saleStatusMeta(state: BuyState) {
        if (state === BuyState.VERIFIED) return { label: "Tu pedido esta listo", icon: checkCircleIcon, tone: "ready" };
        if (state === BuyState.DELETED) return { label: "Cancelada", icon: cancelIcon, tone: "cancelled" };
        return { label: "Reservada, esperando confirmacion", icon: scheduleIcon, tone: "pending" };
    }

    function productName(productId: string): string {
        return $productStore.items.find((product) => product.id === productId)?.name ?? productId;
    }

    function deliverySummary() {
        if (!sale?.deliveryType) return "Aun no se definio la entrega para este pedido.";
        if (sale.deliveryType === DeliveryType.PICKUP) return "Recoger en tienda";
        return "Entrega a domicilio";
    }
</script>

<section class="screen">
    <Button variant="text" size="s" iconType="left" onclick={() => navController.popBackStack()}>
        <Icon icon={arrowBackIcon} />
        Volver
    </Button>

    {#if loading}
        <Card variant="filled">
            <p>Cargando detalle de la reserva...</p>
        </Card>
    {:else if sale}
        {@const meta = saleStatusMeta(sale.verified)}
        <div class="hero">
            <p class="eyebrow">Estado de pedido</p>
            <h1>Pedido #{sale.id.slice(0, 8)}</h1>
            <p class="sub">Monto total ${sale.amount.toFixed(2)} CUP</p>
        </div>

        <div class="status-card {meta.tone}">
            <Icon icon={meta.icon} />
            <span>{meta.label}</span>
        </div>

        <div class="layout">
            <Card variant="filled" class="qr-card">
                <div class="qr-header">
                    <Icon icon={qrCodeIcon} />
                    <span>QR de pedido</span>
                </div>
                <QRCodeDisplay {sale} />
                <p class="subtle">ID: {sale.id}</p>
                <p class="subtle">Fecha: {new Date(sale.date).toLocaleString()}</p>
                <p class="hint">Incluye productos para escaneo rápido</p>
            </Card>

            <Card variant="outlined" class="detail-card">
                <h2>Productos</h2>
                <div class="items">
                    {#each sale.products as item}
                        <div class="item-row">
                            <div>
                                <strong>{productName(item.productId)}</strong>
                                <span>Cant. {item.quantity}</span>
                            </div>
                            <b>{item.price.toFixed(2)} CUP</b>
                        </div>
                    {/each}
                </div>
            </Card>
        </div>

        <Card variant="elevated" class="delivery-card">
            <h2>Entrega seleccionada</h2>
            <div class="delivery-summary">
                <Icon icon={sale.deliveryType === DeliveryType.DELIVERY ? localShippingIcon : storeIcon} />
                <div>
                    <strong>{deliverySummary()}</strong>
                    {#if sale.deliveryType === DeliveryType.DELIVERY && sale.deliveryAddress}
                        <span>{sale.deliveryAddress.mainStreet}, No. {sale.deliveryAddress.houseNumber}, {sale.deliveryAddress.municipality}, {sale.deliveryAddress.province}</span>
                        <span>Telefono: {sale.deliveryAddress.phone}</span>
                        {#if sale.deliveryAddress.betweenStreets}
                            <span>Entre: {sale.deliveryAddress.betweenStreets}</span>
                        {/if}
                        {#if sale.deliveryAddress.referenceName}
                            <span>Preguntar por: {sale.deliveryAddress.referenceName}</span>
                        {/if}
                    {:else if sale.deliveryType === DeliveryType.PICKUP}
                        <span>Tu pedido se recogera directamente en el taller.</span>
                    {:else}
                        <span>Las reservas antiguas pueden no traer esta informacion completa.</span>
                    {/if}
                </div>
            </div>
        </Card>
    {:else}
        <Card variant="outlined">
            <p>No se encontro la reserva solicitada.</p>
            <Button variant="text" size="m" onclick={() => navController.popBackStack()}>Volver a reservas</Button>
        </Card>
    {/if}
</section>

<style>
    .screen {
        display: grid;
        gap: 18px;
        align-content: start;
        padding-bottom: 8px;
    }
    .eyebrow,
    h1,
    h2,
    p {
        margin: 0;
    }
    .eyebrow {
        color: var(--md-sys-color-primary);
        text-transform: uppercase;
        font-size: 0.78rem;
        font-weight: 800;
        letter-spacing: 0.08em;
    }
    .sub,
    .subtle {
        color: var(--md-sys-color-on-surface-variant);
    }
    .hero {
        padding: 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }
    .status-card {
        display: flex;
        gap: 10px;
        align-items: center;
        padding: 12px 16px;
        border-radius: 20px;
        font-weight: 800;
    }
    .status-card.pending {
        background: rgba(255, 151, 60, 0.14);
        color: #d97706;
    }
    .status-card.ready {
        background: color-mix(in srgb, var(--md-sys-color-primary) 14%, transparent);
        color: var(--md-sys-color-primary);
    }
    .status-card.cancelled {
        background: color-mix(in srgb, var(--md-sys-color-error) 14%, transparent);
        color: var(--md-sys-color-error);
    }
    .layout {
        display: grid;
        grid-template-columns: minmax(280px, 0.72fr) minmax(0, 1fr);
        gap: 16px;
    }
    .qr-card,
    .detail-card,
    .delivery-card {
        display: grid;
        gap: 14px;
        align-content: start;
        border-radius: 28px;
    }
    .qr-header {
        display: flex;
        gap: 8px;
        align-items: center;
        font-weight: 600;
    }
    .hint {
        font-size: 0.75rem;
        color: var(--md-sys-color-outline-variant);
        text-align: center;
        margin: 0;
        font-style: italic;
    }
    .items {
        display: grid;
        gap: 10px;
    }
    .item-row {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        padding: 12px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 85%, transparent);
    }
    .item-row div {
        display: grid;
        gap: 4px;
    }
    .item-row span {
        color: var(--md-sys-color-on-surface-variant);
    }
    .delivery-summary {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 12px;
        align-items: start;
        padding: 12px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 85%, transparent);
    }
    .delivery-summary div {
        display: grid;
        gap: 4px;
    }
    .delivery-summary span {
        color: var(--md-sys-color-on-surface-variant);
    }
    @media (max-width: 900px) {
        .layout {
            grid-template-columns: 1fr;
        }
    }
</style>
