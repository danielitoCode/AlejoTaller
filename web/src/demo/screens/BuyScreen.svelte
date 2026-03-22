<script lang="ts">
  import { Button, Card } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { cartItems, getProduct } from '../data'
  import { INTERNAL_ROUTES } from '../routes'

  export let navController: NavController

  let items = cartItems.map((item) => ({ product: getProduct(item.productId), quantity: item.quantity }))

  $: total = items.reduce((sum, item) => sum + item.product.price * item.quantity, 0)

  function updateQuantity(productId: string, delta: number) {
    items = items
      .map((item) =>
        item.product.id === productId ? { ...item, quantity: Math.max(0, item.quantity + delta) } : item
      )
      .filter((item) => item.quantity > 0)
  }
</script>

<section class="stack-screen">
  <h2>Tu carrito</h2>

  {#if items.length === 0}
    <p class="body-copy">No hay productos en el carrito.</p>
  {:else}
    <div class="cart-list">
      {#each items as item}
        <Card variant="filled" class="cart-card">
          <strong>{item.product.name}</strong>
          <div class="cart-copy">
            <span>Precio por unidad:</span>
            <strong>${item.product.price.toFixed(2)}</strong>
          </div>
          <div class="cart-copy">
            <span>Cantidad:</span>
            <strong>{item.quantity}</strong>
          </div>
          <div class="cart-actions">
            <Button variant="filled" size="m" onclick={() => updateQuantity(item.product.id, -1)}>-1</Button>
            <Button variant="outlined" size="m" onclick={() => updateQuantity(item.product.id, 1)}>+1</Button>
            <Button variant="filled" size="m" onclick={() => updateQuantity(item.product.id, -item.quantity)}>
              Quitar
            </Button>
          </div>
        </Card>
      {/each}
    </div>

    <div class="cart-footer">
      <strong>Total: ${total.toFixed(2)}</strong>
      <Button variant="filled" size="m" onclick={() => navController.navigate(INTERNAL_ROUTES.buyConfirm)}>
        Continuar
      </Button>
    </div>
  {/if}
</section>
