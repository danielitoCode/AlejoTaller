<script lang="ts">
  import { Button, Card } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import type { NavBackStackEntry } from '../../lib/navigation/NavBackStackEntry'
  import { getProduct } from '../data'
  import { INTERNAL_ROUTES } from '../routes'

  export let navController: NavController
  export let navBackStackEntry: NavBackStackEntry<{ productId?: string }>
  export let embedded = false

  $: product = getProduct(navBackStackEntry?.args?.productId ?? '')
</script>

<section class:embedded-detail={embedded} class="detail-shell">
  <div class="detail-topbar">
    {#if !embedded}
      <button class="circle-action" type="button" onclick={() => navController.popBackStack()}>&lt;-</button>
    {/if}
    <div class="detail-actions">
      <button class="circle-action" type="button">Fav</button>
    </div>
  </div>

  <div class="detail-image-wrap">
    <div class={`detail-image ${product.accent}`}></div>
  </div>

  <Card variant="filled" class="detail-content-card">
    <div class="detail-heading">
      <h2>{product.name}</h2>
      <span class="offer-chip">
        <span class="offer-dot"></span>
        En oferta
      </span>
    </div>

    <div class="rating-row">
      <span class="rating-chip"><span class="rating-mark"></span>{product.rating}</span>
      <span class="rating-chip"><span class="rating-mark rating-mark-alt"></span>{product.recommendation}</span>
      <span class="rating-meta">{product.reviews}</span>
    </div>

    <p class="detail-description">{product.description}</p>
  </Card>

  {#if !embedded}
    <div class="price-bar">
      <div>
        <span class="price-old">${product.oldPrice.toFixed(2)}</span>
        <strong class="price-current">${product.price.toFixed(2)}</strong>
      </div>
      <Button variant="filled" size="m" onclick={() => navController.navigate(INTERNAL_ROUTES.buy)}>
        Agregar al carrito
      </Button>
    </div>
  {/if}
</section>
