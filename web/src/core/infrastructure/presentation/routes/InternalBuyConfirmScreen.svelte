<script lang="ts">
    import { Button, Card, Icon } from "m3-svelte";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import paymentsIcon from "@ktibow/iconset-material-symbols/payments-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import creditCardIcon from "@ktibow/iconset-material-symbols/credit-card-heart-rounded";
    import accountBalanceIcon from "@ktibow/iconset-material-symbols/account-balance-rounded";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import { BuyState } from "../../../feature/sale/domain/entity/enums";
    import { cartStore } from "../../../feature/sale/presentation/viewmodel/cart.store";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { toastStore } from "../viewmodel/toast.store";
    import { buy, reservationDetail } from "../navigation/nested.router";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;

    type PaymentMethod = "ULTRAPAY" | "TRANSFERMOVIL" | null;

    let selectedMethod: PaymentMethod = null;
    let isSubmitting = false;

    $: items = $cartStore.items;
    $: totalAmount = $cartStore.items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);

    async function submitPurchase() {
        if (!items.length || isSubmitting) return;

        const currentUser = await sessionStore.getCurrentUser().catch(() => null);
        if (!currentUser?.$id) {
            toastStore.error("No se pudo resolver la sesion actual");
            return;
        }

        isSubmitting = true;
        try {
            const created = await saleStore.create({
                id: crypto.randomUUID(),
                date: new Date().toISOString(),
                amount: totalAmount,
                verified: BuyState.UNVERIFIED,
                userId: currentUser.$id,
                deliveryType: null,
                products: items.map((item) => ({
                    productId: item.product.id,
                    quantity: item.quantity,
                    price: item.product.price
                }))
            });

            cartStore.clear();

            if (selectedMethod) {
                toastStore.info("Reserva creada. La pasarela web aun esta pendiente de integracion.");
            } else {
                toastStore.success("Reserva registrada correctamente");
            }

            navController.navigate(reservationDetail.path, { id: created.id });
        } catch (error) {
            toastStore.error(error instanceof Error ? error.message : "No se pudo crear la reserva");
        } finally {
            isSubmitting = false;
        }
    }
</script>

<section class="screen">
    <Button variant="text" size="s" iconType="left" onclick={() => navController.navigate(buy.path)}>
        <Icon icon={arrowBackIcon} />
        Volver
    </Button>

    <div class="title-row hero">
        <div>
            <p class="eyebrow">Confirmacion</p>
            <h1>Confirmar compra</h1>
            <p>Puedes reservar sin pago online o preparar un canal de pago experimental.</p>
        </div>
    </div>

    <div class="layout">
        <Card variant="filled" class="summary-card">
            <div class="section-title">
                <Icon icon={shoppingCartIcon} />
                <h2>Resumen</h2>
            </div>
            <div class="summary-list">
                {#each items as item}
                    <div class="summary-row">
                        <div>
                            <strong>{item.product.name}</strong>
                            <span>{item.quantity} x ${item.product.price.toFixed(2)}</span>
                        </div>
                        <b>${(item.product.price * item.quantity).toFixed(2)}</b>
                    </div>
                {/each}
            </div>
            <div class="total-row">
                <span>Total final</span>
                <strong>${totalAmount.toFixed(2)}</strong>
            </div>
        </Card>

        <Card variant="elevated" class="payment-card">
            <div class="section-title">
                <Icon icon={paymentsIcon} />
                <h2>Metodos de pago</h2>
            </div>

            <button class:selected={selectedMethod === "ULTRAPAY"} class="method" type="button" on:click={() => (selectedMethod = "ULTRAPAY")}>
                <div class="method-icon ultra">
                    <Icon icon={creditCardIcon} />
                </div>
                <div class="method-copy">
                    <strong>UltraPay</strong>
                    <span>Tarjeta virtual / SolucionesCuba</span>
                </div>
            </button>

            <button class:selected={selectedMethod === "TRANSFERMOVIL"} class="method" type="button" on:click={() => (selectedMethod = "TRANSFERMOVIL")}>
                <div class="method-icon transfer">
                    <Icon icon={accountBalanceIcon} />
                </div>
                <div class="method-copy">
                    <strong>Transfermovil</strong>
                    <span>Transferencia directa. La pasarela web aun no esta cableada.</span>
                </div>
            </button>

            <div class="reserve-note">
                <Icon icon={qrCodeIcon} />
                <p>Si no eliges un metodo, la compra se registra como reserva pendiente, igual que en Android.</p>
            </div>

            <div class="actions">
                <Button variant="filled" size="m" onclick={submitPurchase} disabled={isSubmitting || !items.length}>
                    {selectedMethod ? "Solicitar pedido y pagar" : "Reservar sin pago online"}
                </Button>
            </div>
        </Card>
    </div>
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
    .title-row p:last-child {
        color: var(--md-sys-color-on-surface-variant);
        margin-top: 6px;
    }
    .hero {
        padding: 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }
    .layout {
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(320px, 0.95fr);
        gap: 16px;
    }
    .summary-card,
    .payment-card {
        display: grid;
        gap: 14px;
        align-content: start;
        border-radius: 28px;
    }
    .section-title {
        display: flex;
        gap: 8px;
        align-items: center;
    }
    .section-title :global(svg) {
        width: 22px;
        height: 22px;
    }
    .summary-list {
        display: grid;
        gap: 10px;
    }
    .summary-row {
        display: flex;
        justify-content: space-between;
        gap: 12px;
        padding: 12px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface) 78%, transparent);
    }
    .summary-row div {
        display: grid;
        gap: 4px;
    }
    .summary-row span,
    .method-copy span,
    .reserve-note p {
        color: var(--md-sys-color-on-surface-variant);
    }
    .total-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 1.05rem;
        font-weight: 800;
    }
    .method {
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 22px;
        background: transparent;
        padding: 14px;
        display: grid;
        grid-template-columns: auto minmax(0, 1fr);
        gap: 12px;
        text-align: left;
        cursor: pointer;
    }
    .method.selected {
        border-color: var(--md-sys-color-primary);
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 85%, transparent);
    }
    .method-icon {
        width: 52px;
        height: 52px;
        border-radius: 18px;
        display: grid;
        place-items: center;
    }
    .method-icon.ultra {
        background: linear-gradient(135deg, rgba(73, 83, 93, 0.9), rgba(73, 83, 93, 0.45));
        color: white;
    }
    .method-icon.transfer {
        background: linear-gradient(135deg, rgba(96, 109, 255, 0.75), rgba(50, 255, 114, 0.45));
        color: white;
    }
    .method-copy {
        display: grid;
        gap: 4px;
    }
    .reserve-note {
        display: grid;
        grid-template-columns: auto 1fr;
        gap: 10px;
        align-items: start;
        padding: 12px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 88%, transparent);
    }
    .actions :global(button) {
        width: 100%;
    }
    @media (max-width: 900px) {
        .layout {
            grid-template-columns: 1fr;
        }
    }
</style>
