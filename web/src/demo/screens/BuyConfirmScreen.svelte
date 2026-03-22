<script lang="ts">
  import { Button, Card } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { cartItems, getProduct } from '../data'
  import { INTERNAL_ROUTES } from '../routes'

  export let navController: NavController

  let selectedMethod: 'ultrapay' | 'transfermovil' | null = null
  const items = cartItems.map((item) => ({ product: getProduct(item.productId), quantity: item.quantity }))
  const total = items.reduce((sum, item) => sum + item.product.price * item.quantity, 0)
</script>

<section class="stack-screen confirm-shell">
  <div class="confirm-header">
    <h2>Confirmar compra</h2>
    <p class="body-copy">
      Puedes reservar sin pago online o elegir un canal de pago experimental.
    </p>
  </div>

  <div class="confirm-layout">
    <div class="confirm-items">
      {#each items as item}
        <Card variant="filled" class="confirm-item-card">
          <div>
            <strong>{item.product.name}</strong>
            <p class="body-copy">Cantidad: {item.quantity}</p>
          </div>
          <strong>${(item.product.price * item.quantity).toFixed(2)}</strong>
        </Card>
      {/each}
    </div>

    <Card variant="filled" class="payment-card">
      <div class="section-heading">
        <div>
          <p class="section-label">Metodos de pago</p>
          <h3>Selecciona un canal</h3>
        </div>
      </div>

      <button
        class:selected={selectedMethod === 'ultrapay'}
        class="payment-option ultrapay"
        type="button"
        onclick={() => (selectedMethod = 'ultrapay')}
      >
        <div>
          <strong>UltraPay</strong>
          <p>Tarjeta virtual SolucionesCuba</p>
        </div>
        <span class="selection-dot"></span>
      </button>

      <button
        class:selected={selectedMethod === 'transfermovil'}
        class="payment-option transfermovil"
        type="button"
        onclick={() => (selectedMethod = 'transfermovil')}
      >
        <div>
          <strong>Transfermovil</strong>
          <p>Pasarela de pagos cubana</p>
        </div>
        <span class="selection-dot"></span>
      </button>

      <div class="cart-footer confirm-footer">
        <strong>Total final: ${total.toFixed(2)}</strong>
      </div>

      <div class="auth-buttons-column">
        <Button variant="filled" size="m" onclick={() => navController.navigate(INTERNAL_ROUTES.reservations)}>
          {selectedMethod === null ? 'Reservar sin pago online' : 'Solicitar pedido y pagar'}
        </Button>
        <Button variant="outlined" size="m" onclick={() => navController.popBackStack()}>
          Volver
        </Button>
      </div>
    </Card>
  </div>
</section>
