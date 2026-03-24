<script lang="ts">
    import { Button, Card, Icon } from "m3-svelte";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import storeIcon from "@ktibow/iconset-material-symbols/store-rounded";
    import scheduleIcon from "@ktibow/iconset-material-symbols/schedule-rounded";
    import checkCircleIcon from "@ktibow/iconset-material-symbols/check-circle-rounded";
    import cancelIcon from "@ktibow/iconset-material-symbols/cancel-rounded";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import { productStore } from "../../../feature/product/presentation/viewmodel/product.store";
    import { BuyState, DeliveryType } from "../../../feature/sale/domain/entity/enums";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import { toastStore } from "../viewmodel/toast.store";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    $: saleId = navBackStackEntry?.args?.id ?? "";
    $: sale = $saleStore.items.find((item) => item.id === saleId) ?? null;

    function saleStatusMeta(state: BuyState) {
        if (state === BuyState.VERIFIED) return { label: "Tu pedido esta listo", icon: checkCircleIcon, tone: "ready" };
        if (state === BuyState.DELETED) return { label: "Cancelada", icon: cancelIcon, tone: "cancelled" };
        return { label: "Reservada, esperando confirmacion", icon: scheduleIcon, tone: "pending" };
    }

    function productName(productId: string): string {
        return $productStore.items.find((product) => product.id === productId)?.name ?? productId;
    }

    async function setDeliveryType(deliveryType: DeliveryType) {
        if (!sale) return;
        try {
            await saleStore.updateDeliveryType(sale.id, deliveryType);
            toastStore.success("Preferencia de entrega guardada");
        } catch (error) {
            toastStore.error(error instanceof Error ? error.message : "No se pudo guardar la entrega");
        }
    }
</script>

<section class="screen">
    <Button variant="text" size="s" iconType="left" onclick={() => navController.popBackStack()}>
        <Icon icon={arrowBackIcon} />
        Volver
    </Button>

    {#if sale}
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
                <div class="qr-box">
                    <Icon icon={qrCodeIcon} />
                    <span>QR de pedido</span>
                </div>
                <p class="subtle">ID: {sale.id}</p>
                <p class="subtle">Fecha: {new Date(sale.date).toLocaleString()}</p>
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

        {#if sale.verified === BuyState.VERIFIED}
            <Card variant="elevated" class="delivery-card">
                <h2>Como prefieres recibirlo?</h2>
                <div class="delivery-options">
                    <button
                        class:selected={sale.deliveryType === DeliveryType.PICKUP}
                        class="delivery-option"
                        type="button"
                        on:click={() => setDeliveryType(DeliveryType.PICKUP)}
                    >
                        <Icon icon={storeIcon} />
                        <strong>Recoger</strong>
                        <span>Paso por el taller</span>
                    </button>
                    <button
                        class:selected={sale.deliveryType === DeliveryType.DELIVERY}
                        class="delivery-option"
                        type="button"
                        on:click={() => setDeliveryType(DeliveryType.DELIVERY)}
                    >
                        <Icon icon={localShippingIcon} />
                        <strong>Domicilio</strong>
                        <span>Me lo traen</span>
                    </button>
                </div>
            </Card>
        {/if}
    {:else}
        <Card variant="outlined">
            <p>No se encontro la reserva solicitada.</p>
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
    .qr-box {
        min-height: 180px;
        border-radius: 24px;
        background: white;
        color: #111827;
        display: grid;
        place-items: center;
        gap: 8px;
    }
    .qr-box :global(svg) {
        width: 72px;
        height: 72px;
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
    .delivery-options {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 12px;
    }
    .delivery-option {
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 18px;
        background: transparent;
        padding: 14px;
        display: grid;
        gap: 6px;
        justify-items: center;
        text-align: center;
        cursor: pointer;
    }
    .delivery-option.selected {
        border-color: var(--md-sys-color-primary);
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 85%, transparent);
    }
    .delivery-option span {
        color: var(--md-sys-color-on-surface-variant);
    }
    @media (max-width: 900px) {
        .layout,
        .delivery-options {
            grid-template-columns: 1fr;
        }
    }
</style>
