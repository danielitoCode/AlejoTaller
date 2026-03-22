<script lang="ts">
  import { Card } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import type { NavBackStackEntry } from '../../lib/navigation/NavBackStackEntry'
  import { getPromotion } from '../data'

  export let navController: NavController
  export let navBackStackEntry: NavBackStackEntry<{ promotionId?: string }>
  export let embedded = false

  $: promotion = getPromotion(navBackStackEntry?.args?.promotionId ?? '')
</script>

<section class:embedded-detail={embedded} class="detail-shell promotion-detail-shell">
  {#if !embedded}
    <div class="detail-topbar">
      <button class="circle-action" type="button" onclick={() => navController.popBackStack()}>&lt;-</button>
      <div class="detail-topbar-title">Oferta especial</div>
    </div>
  {/if}

  <div class="detail-image-wrap">
    <div class={`detail-image ${promotion.accent} promotion-hero`}>
      <span class="promo-badge detail-promo-badge">Oferta especial</span>
    </div>
  </div>

  <Card variant="filled" class="detail-content-card">
    <div class="promotion-copy-stack">
      <h2>{promotion.title}</h2>
      <p class="detail-description">{promotion.message}</p>
      <div class="price-row">
        <span class="price-old">{promotion.oldPrice}</span>
        <strong class="price-current">{promotion.currentPrice}</strong>
      </div>
      <span class="promo-validity">Vigente por tiempo limitado</span>
    </div>
  </Card>
</section>
