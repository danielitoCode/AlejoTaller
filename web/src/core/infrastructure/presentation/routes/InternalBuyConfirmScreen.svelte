<script lang="ts">
    import {Button, Card, CircularProgressEstimate, Dialog, Icon, TextFieldOutlined} from "m3-svelte";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import paymentsIcon from "@ktibow/iconset-material-symbols/payments-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import creditCardIcon from "@ktibow/iconset-material-symbols/credit-card-heart-rounded";
    import accountBalanceIcon from "@ktibow/iconset-material-symbols/account-balance-rounded";
    import arrowBackIcon from "@ktibow/iconset-material-symbols/arrow-back-rounded";
    import localMallIcon from "@ktibow/iconset-material-symbols/local-mall-rounded";
    import storefrontIcon from "@ktibow/iconset-material-symbols/storefront-rounded";
    import localShippingIcon from "@ktibow/iconset-material-symbols/local-shipping-rounded";
    import homePinIcon from "@ktibow/iconset-material-symbols/home-pin-rounded";
    import editLocationIcon from "@ktibow/iconset-material-symbols/edit-location-alt-rounded";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {BuyState, DeliveryType} from "../../../feature/sale/domain/entity/enums";
    import {cartStore} from "../../../feature/sale/presentation/viewmodel/cart.store";
    import {saleStore} from "../../../feature/sale/presentation/viewmodel/sale.store";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {toastStore} from "../viewmodel/toast.store";
    import {buy, reservationDetail} from "../navigation/nested.router";
    import type {DeliveryAddress} from "../../../feature/sale/domain/entity/Sale";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;
    $: void navBackStackEntry;

    type PaymentMethod = "ULTRAPAY" | "TRANSFERMOVIL" | null;
    type DeliveryDraft = {
        province: string;
        municipality: string;
        mainStreet: string;
        betweenStreets: string;
        phone: string;
        houseNumber: string;
        referenceName: string;
    };

    let selectedMethod: PaymentMethod = null;
    let selectedDeliveryType: DeliveryType | null = null;
    let isSubmitting = false;
    let isAddressDialogOpen = false;
    let address: DeliveryDraft = {
        province: "",
        municipality: "",
        mainStreet: "",
        betweenStreets: "",
        phone: "",
        houseNumber: "",
        referenceName: ""
    };

    $: items = $cartStore.items;
    $: totalAmount = $cartStore.items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);
    $: needsAddress = selectedDeliveryType === DeliveryType.DELIVERY;
    $: missingDeliveryType = !selectedDeliveryType;
    $: invalidAddress = needsAddress && !isAddressValid(address);
    $: addressSummary = [
        address.mainStreet.trim(),
        address.houseNumber.trim() ? `No. ${address.houseNumber.trim()}` : "",
        address.municipality.trim(),
        address.province.trim()
    ]
        .filter(Boolean)
        .join(", ");

    function isAddressValid(value: DeliveryDraft): boolean {
        return Boolean(
            value.province.trim() &&
            value.municipality.trim() &&
            value.mainStreet.trim() &&
            value.phone.trim() &&
            value.houseNumber.trim()
        );
    }

    function toDeliveryAddress(value: DeliveryDraft): DeliveryAddress {
        return {
            province: value.province.trim(),
            municipality: value.municipality.trim(),
            mainStreet: value.mainStreet.trim(),
            betweenStreets: value.betweenStreets.trim() || null,
            phone: value.phone.trim(),
            houseNumber: value.houseNumber.trim(),
            referenceName: value.referenceName.trim() || null
        };
    }

    function selectPickup() {
        selectedDeliveryType = DeliveryType.PICKUP;
        isAddressDialogOpen = false;
    }

    function selectDelivery() {
        selectedDeliveryType = DeliveryType.DELIVERY;
        isAddressDialogOpen = true;
    }

    async function submitPurchase() {
        if (!items.length || isSubmitting) return;
        if (!selectedDeliveryType) {
            toastStore.warning("Selecciona si tu pedido es para recoger o a domicilio");
            return;
        }
        if (needsAddress && !isAddressValid(address)) {
            isAddressDialogOpen = true;
            toastStore.warning("Completa los datos obligatorios de la direccion de entrega");
            return;
        }

        isSubmitting = true;

        const currentUser = await sessionStore.getCurrentUser().catch(() => null);
        if (!currentUser?.$id) {
            isSubmitting = false;
            toastStore.error("No se pudo resolver la sesion actual");
            return;
        }

        try {
            const created = await saleStore.create({
                id: crypto.randomUUID(),
                date: new Date().toISOString(),
                amount: totalAmount,
                verified: BuyState.UNVERIFIED,
                userId: currentUser.$id,
                deliveryType: selectedDeliveryType,
                deliveryAddress: needsAddress ? toDeliveryAddress(address) : null,
                products: items.map((item) => ({
                    productId: item.product.id,
                    productName: item.product.name,
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
    {#if isSubmitting}
        <div class="processing-overlay" aria-live="polite" aria-busy="true">
            <Card variant="filled" class="processing-card">
                <div class="processing-content">
                    <CircularProgressEstimate sToHalfway={2} title="Procesando..." />
                    <div class="processing-copy">
                        <strong>Procesando...</strong>
                        <span>Estamos registrando tu solicitud.</span>
                    </div>
                </div>
            </Card>
        </div>
    {/if}

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

        <Card variant="filled" class="payment-card">
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
                <p>Si no eliges un metodo, la compra se registra como reserva pendiente.</p>
            </div>

            <section class="delivery-panel">
                <div class="section-title delivery-title">
                    <Icon icon={selectedDeliveryType === DeliveryType.DELIVERY ? accountBalanceIcon : shoppingCartIcon} />
                    <h2>Entrega del pedido</h2>
                </div>

                <div class="delivery-note" class:warning={missingDeliveryType}>
                    <div class="delivery-note__icon">
                        <Icon icon={selectedDeliveryType ? homePinIcon : editLocationIcon} />
                    </div>
                    <div class="delivery-note__copy">
                        <strong>{selectedDeliveryType ? "Entrega seleccionada" : "Debes elegir como recibiras el pedido"}</strong>
                        <p>
                            {#if selectedDeliveryType === DeliveryType.PICKUP}
                                Recogeras la compra en el taller cuando quede confirmada.
                            {:else if selectedDeliveryType === DeliveryType.DELIVERY}
                                El vendedor recibira tu direccion junto con la solicitud del pedido.
                            {:else}
                                Marca una opcion para continuar: recoger en tienda o entrega a domicilio.
                            {/if}
                        </p>
                    </div>
                </div>

                <div class="delivery-grid">
                    <Button
                            variant="outlined"
                            size="xl"
                            iconType="left"
                            onclick={selectPickup}
                    >
                        <span style="padding: 12px 0">
                            <Icon icon={storefrontIcon} />
                             Recoger en tienda
                        </span>
                    </Button>

                    <Button
                            variant="outlined"
                            size="xl"
                            iconType="left"
                            onclick={selectDelivery}
                    >
                        <span style="padding: 12px 0">
                              <Icon icon={localShippingIcon} />
                                Entrega a domicilio
                        </span>

                    </Button>

                </div>

                {#if needsAddress}
                    <div class="address-preview">
                        <div class="address-preview__copy">
                            <div class="address-preview__title">
                                <Icon icon={homePinIcon} />
                                <strong>Direccion de entrega</strong>
                            </div>
                            <p>
                                {#if addressSummary}
                                    {addressSummary}
                                {:else}
                                    Aun no has completado la direccion del pedido.
                                {/if}
                            </p>
                        </div>
                        <Button variant="tonal" size="s" iconType="left" onclick={() => (isAddressDialogOpen = true)}>

                            <Icon icon={editLocationIcon} />
                            {addressSummary ? "Editar direccion" : "Agregar direccion"}
                        </Button>
                    </div>
                {/if}

                <div class="actions">
                    <Button
                        variant="filled"
                        size="m"
                        iconType="left"
                        onclick={submitPurchase}
                        disabled={isSubmitting || !items.length}
                    >
                        <span style="padding: 12px 0">
                            <Icon icon={selectedMethod ? paymentsIcon : localMallIcon} />
                            {selectedMethod ? "Solicitar pedido y pagar" : "Reservar sin pago online"}
                        </span>
                    </Button>
                </div>
            </section>
        </Card>
    </div>
</section>

<Dialog bind:open={isAddressDialogOpen} headline="Direccion de entrega" icon={homePinIcon}>
    <div class="address-dialog">
        <p class="dialog-copy">
            Completa la informacion para que el vendedor reciba la entrega clara desde el primer momento.
        </p>
        <div class="address-form">
            <div class="field-shell">
                <TextFieldOutlined label="Provincia" bind:value={address.province} />
            </div>
            <div class="field-shell">
                <TextFieldOutlined label="Municipio" bind:value={address.municipality} />
            </div>
            <div class="field-shell">
                <TextFieldOutlined label="Calle principal" bind:value={address.mainStreet} />
            </div>
            <div class="field-shell">
                <TextFieldOutlined label="Telefono" bind:value={address.phone} type="tel" />
            </div>
            <div class="field-shell">
                <TextFieldOutlined label="Numero de la casa" bind:value={address.houseNumber} />
            </div>
            <div class="field-shell">
                <TextFieldOutlined label="Entre calles" bind:value={address.betweenStreets} />
            </div>
            <div class="field-shell field-shell--full">
                <TextFieldOutlined label="Preguntar por" bind:value={address.referenceName} />
            </div>
        </div>
        <p class="field-hint">Son obligatorios: provincia, municipio, calle principal, telefono y numero de la casa.</p>
    </div>
    {#snippet buttons()}
        <Button variant="text" onclick={() => (isAddressDialogOpen = false)}>Cerrar</Button>
        <Button variant="filled" disabled={!isAddressValid(address)} onclick={() => (isAddressDialogOpen = false)}>
            Guardar direccion
        </Button>
    {/snippet}
</Dialog>

<style>
    .screen {
        position: relative;
        display: grid;
        gap: 14px;
        align-content: start;
        padding-bottom: 12px;
    }
    .processing-overlay {
        position: fixed;
        inset: 0;
        z-index: 2100;
        display: grid;
        place-items: center;
        padding: 24px;
        background: color-mix(in srgb, var(--md-sys-color-scrim) 34%, transparent);
        backdrop-filter: blur(4px);
    }
    .processing-card {
        width: min(100%, 360px);
        border-radius: 24px;
        box-shadow: 0 18px 40px color-mix(in srgb, black 22%, transparent);
    }
    .processing-content {
        display: grid;
        justify-items: center;
        gap: 14px;
        padding: 24px 22px;
        text-align: center;
    }
    .processing-copy {
        display: grid;
        gap: 6px;
    }
    .processing-copy strong {
        font-size: 1rem;
        line-height: 1.2;
    }
    .processing-copy span {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.95rem;
        line-height: 1.4;
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
        padding: 16px 18px;
        border-radius: 28px;
        background: linear-gradient(180deg, var(--md-sys-color-surface-container-high) 0%, var(--md-sys-color-surface-container) 100%);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
    }
    .layout {
        display: grid;
        grid-template-columns: minmax(0, 1fr) minmax(320px, 0.95fr);
        gap: 14px;
    }
    .summary-card,
    .payment-card {
        display: grid;
        gap: 12px;
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
        background: color-mix(in srgb, var(--md-sys-color-surface-container-low) 86%, transparent);
        padding: 12px;
        display: grid;
        grid-template-columns: auto minmax(0, 1fr);
        gap: 12px;
        text-align: left;
        cursor: pointer;
        box-shadow: 0 10px 22px color-mix(in srgb, var(--md-sys-color-shadow) 10%, transparent);
        transition: transform 140ms ease, box-shadow 180ms ease, border-color 180ms ease, background-color 180ms ease;
    }
    .method:hover {
        transform: translateY(-1px);
        box-shadow: 0 14px 28px color-mix(in srgb, var(--md-sys-color-shadow) 15%, transparent);
    }
    .method.selected {
        border-color: var(--md-sys-color-primary);
        background: color-mix(in srgb, var(--md-sys-color-primary-container) 85%, transparent);
        box-shadow: 0 16px 30px color-mix(in srgb, var(--md-sys-color-primary) 18%, transparent);
    }
    .method-icon {
        width: 48px;
        height: 48px;
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
        padding: 10px 12px;
        border-radius: 18px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 88%, transparent);
    }
    .actions :global(button) {
        width: 100%;
    }
    .actions :global(.submit-button.m3-container) {
        width: 100%;
        min-height: 58px;
        padding-inline: 30px;
        border-radius: 999px;
        justify-content: center;
        box-shadow: 0 14px 28px color-mix(in srgb, var(--md-sys-color-primary) 28%, transparent);
    }
    .delivery-panel {
        display: grid;
        gap: 14px;
        padding: 16px;
        border-radius: 28px;
        background: linear-gradient(
            180deg,
            color-mix(in srgb, var(--md-sys-color-secondary-container) 42%, transparent) 0%,
            color-mix(in srgb, var(--md-sys-color-surface-container-high) 95%, transparent) 100%
        );
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 76%, transparent);
        box-shadow: 0 18px 36px color-mix(in srgb, var(--md-sys-color-shadow) 10%, transparent);
    }
    .delivery-title {
        margin-top: 0;
    }
    .delivery-note {
        display: grid;
        grid-template-columns: auto minmax(0, 1fr);
        gap: 12px;
        align-items: start;
        padding: 12px 14px;
        border-radius: 22px;
        background: color-mix(in srgb, var(--md-sys-color-secondary-container) 56%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 78%, transparent);
    }
    .delivery-note.warning {
        background: color-mix(in srgb, var(--md-sys-color-tertiary-container) 68%, transparent);
        border-color: color-mix(in srgb, var(--md-sys-color-tertiary) 44%, transparent);
    }
    .delivery-note__icon {
        width: 40px;
        height: 40px;
        border-radius: 14px;
        display: grid;
        place-items: center;
        background: color-mix(in srgb, var(--md-sys-color-surface) 72%, transparent);
        color: var(--md-sys-color-primary);
    }
    .delivery-note.warning .delivery-note__icon {
        color: var(--md-sys-color-tertiary);
    }
    .delivery-note__copy {
        display: grid;
        gap: 4px;
    }
    .delivery-note__copy p {
        color: var(--md-sys-color-on-surface-variant);
    }
    .delivery-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 12px;
        align-items: stretch;
    }
    .delivery-option {
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline) 48%, transparent);
        border-radius: 26px;
        background: linear-gradient(
            180deg,
            color-mix(in srgb, var(--md-sys-color-surface-bright) 98%, transparent) 0%,
            color-mix(in srgb, var(--md-sys-color-surface-container) 100%, transparent) 100%
        );
        padding: 16px;
        display: grid;
        grid-template-columns: auto minmax(0, 1fr);
        align-items: center;
        gap: 12px;
        text-align: left;
        cursor: pointer;
        box-shadow:
            inset 0 1px 0 color-mix(in srgb, white 10%, transparent),
            0 14px 28px color-mix(in srgb, var(--md-sys-color-shadow) 11%, transparent);
        transition: transform 140ms ease, box-shadow 180ms ease, border-color 180ms ease, background-color 180ms ease;
        min-height: 108px;
    }
    .delivery-option:hover {
        transform: translateY(-1px);
        box-shadow:
            inset 0 1px 0 color-mix(in srgb, white 14%, transparent),
            0 18px 32px color-mix(in srgb, var(--md-sys-color-shadow) 16%, transparent);
    }
    .delivery-option.selected {
        border-color: var(--md-sys-color-primary);
        background: linear-gradient(
            180deg,
            color-mix(in srgb, var(--md-sys-color-primary-container) 92%, transparent) 0%,
            color-mix(in srgb, var(--md-sys-color-secondary-container) 80%, transparent) 100%
        );
        box-shadow:
            inset 0 1px 0 color-mix(in srgb, white 12%, transparent),
            0 18px 34px color-mix(in srgb, var(--md-sys-color-primary) 20%, transparent);
    }
    .delivery-option__icon {
        width: 46px;
        height: 46px;
        border-radius: 18px;
        display: grid;
        place-items: center;
        color: white;
        flex-shrink: 0;
    }
    .delivery-option__icon.pickup {
        background: linear-gradient(135deg, color-mix(in srgb, var(--md-sys-color-secondary) 88%, black 10%), color-mix(in srgb, var(--md-sys-color-secondary-container) 84%, transparent));
    }
    .delivery-option__icon.delivery {
        background: linear-gradient(135deg, color-mix(in srgb, var(--md-sys-color-primary) 90%, black 8%), color-mix(in srgb, var(--md-sys-color-tertiary) 72%, transparent));
    }
    .delivery-option__copy {
        display: grid;
        gap: 5px;
    }
    .delivery-option__copy strong {
        font-size: 1rem;
        line-height: 1.3;
    }
    .delivery-option span,
    .field-hint {
        color: var(--md-sys-color-on-surface-variant);
    }
    .delivery-option span {
        font-size: 0.95rem;
        line-height: 1.4;
    }
    .address-preview {
        display: grid;
        grid-template-columns: minmax(0, 1fr) auto;
        gap: 12px;
        align-items: center;
        padding: 12px 14px;
        border-radius: 22px;
        background: color-mix(in srgb, var(--md-sys-color-surface-container-high) 92%, transparent);
        border: 1px solid color-mix(in srgb, var(--md-sys-color-outline-variant) 84%, transparent);
    }
    .address-preview__copy {
        display: grid;
        gap: 8px;
    }
    .address-preview__copy p {
        color: var(--md-sys-color-on-surface-variant);
    }
    .address-preview__title {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .address-dialog {
        display: grid;
        gap: 18px;
        min-width: min(38rem, 82vw);
    }
    .dialog-copy {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 1rem;
        line-height: 1.55;
    }
    .address-form {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 18px;
    }
    .field-shell {
        min-width: 0;
    }
    .field-shell--full {
        grid-column: 1 / -1;
    }
    .address-form :global(.field-shell .m3-container),
    .address-form :global(.field-shell .m3-field),
    .address-form :global(.field-shell input) {
        width: 100%;
    }
    .address-form :global(.field-shell .m3-container) {
        min-height: 60px;
    }
    .address-form :global(.field-shell input) {
        font-size: 1.02rem;
        line-height: 1.45;
    }
    .address-form :global(.field-shell label),
    .address-form :global(.field-shell .label) {
        font-size: 0.98rem;
    }
    .field-hint {
        font-size: 0.92rem;
        line-height: 1.45;
    }
    :global(dialog.m3-container) {
        padding: 1.75rem;
    }
    :global(dialog.m3-container .headline) {
        font-size: 1.55rem;
        line-height: 1.2;
        margin-bottom: 1.25rem;
    }
    :global(dialog.m3-container .buttons) {
        gap: 0.75rem;
        margin-top: 0.25rem;
    }
    :global(dialog.m3-container .buttons .m3-container) {
        min-height: 52px;
        padding-inline: 22px;
        border-radius: 999px;
    }
    @media (max-width: 900px) {
        .layout {
            grid-template-columns: 1fr;
        }
        .delivery-grid,
        .address-form {
            grid-template-columns: 1fr;
        }
        .address-preview {
            grid-template-columns: 1fr;
        }
        .address-dialog {
            min-width: min(100%, 30rem);
        }
    }
    @media (min-width: 901px) {
        .payment-card {
            gap: 10px;
        }
        .payment-card .section-title h2 {
            font-size: 1.6rem;
            line-height: 1.1;
        }
    }
</style>
