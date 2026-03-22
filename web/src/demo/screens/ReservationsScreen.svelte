<script lang="ts">
  import { Button, Card } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { reservations } from '../data'
  import { INTERNAL_ROUTES } from '../routes'

  export let navController: NavController

  const statusClassByValue = {
    Pendiente: 'is-pending',
    Listo: 'is-ready',
    Cancelada: 'is-cancelled',
  } as const
</script>

<section class="stack-screen">
  <div class="section-heading">
    <div>
      <h2>Mis reservas</h2>
    </div>
  </div>

  {#if reservations.length === 0}
    <div class="reservations-empty">
      <div class="empty-bag">Bag</div>
      <strong>Aun no tienes compras</strong>
      <p>Aqui apareceran tus pedidos una vez que hayas realizado alguna compra.</p>
      <Button variant="filled" size="m" onclick={() => navController.navigate(INTERNAL_ROUTES.home)}>
        Ver productos
      </Button>
    </div>
  {:else}
    <div class="reservation-list">
      {#each reservations as reservation}
        <Card variant="filled" class="reservation-card">
          <div class="reservation-main">
            <div>
              <span class="reservation-date">{reservation.date}</span>
              <h3>Pedido</h3>
              <p class="reservation-id">#{reservation.id}</p>
              <p class="reservation-amount">Monto total: {reservation.amount} CUP</p>
              <p class="body-copy">Entrega: {reservation.delivery}</p>
            </div>
            <span class={`reservation-status ${statusClassByValue[reservation.status]}`}>
              {reservation.status}
            </span>
          </div>
        </Card>
      {/each}
    </div>
  {/if}
</section>
