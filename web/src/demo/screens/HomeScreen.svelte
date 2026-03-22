<script lang="ts">
  import { Button, Card, TextField } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import { categories, metrics, products, promotions } from '../data'
  import { INTERNAL_ROUTES } from '../routes'

  export let navController: NavController

  let query = ''
  let selectedCategoryId = 'all'
  let bannerVisible = true
  let activePromotionIndex = 0

  $: activePromotion = promotions[activePromotionIndex] ?? promotions[0]
  $: visibleProducts = products.filter((product) => {
    const matchCategory =
      selectedCategoryId === 'all' || product.categoryId === selectedCategoryId
    const matchQuery = product.name.toLowerCase().includes(query.trim().toLowerCase())

    return matchCategory && matchQuery
  })

  function nextPromotion() {
    activePromotionIndex = (activePromotionIndex + 1) % promotions.length
  }
</script>

<section class="main-grid">
  <div class="catalog-stack">
    {#if bannerVisible}
      <Card variant="filled" class="promo-card">
        <div class="promo-copy">
          <h2>{activePromotion.title}</h2>
          <div class="promo-badge promo-inline-badge">{activePromotion.message}</div>
          <div class="action-row">
            <Button
              variant="filled"
              size="m"
              onclick={() => navController.navigate(INTERNAL_ROUTES.promotionDetail, { promotionId: activePromotion.id })}
            >
              Ver oferta
            </Button>
            <Button variant="text" size="m" onclick={nextPromotion}>Siguiente</Button>
          </div>
        </div>

        <div class={`promo-visual ${activePromotion.accent}`} aria-hidden="true">
          <button class="banner-dismiss" type="button" onclick={() => (bannerVisible = false)}>
            Cerrar
          </button>
          <div class="promo-orb promo-orb-a"></div>
          <div class="promo-orb promo-orb-b"></div>
          <div class="promo-device"></div>
        </div>
      </Card>
    {/if}

    <Card variant="filled" class="catalog-card">
      <TextField label="Buscar producto" bind:value={query} />

      <div class="chip-row" role="tablist" aria-label="Categorias">
        {#each categories as category}
          <button
            class:selected={category.id === selectedCategoryId}
            class="chip"
            type="button"
            onclick={() => (selectedCategoryId = category.id)}
          >
            {category.name}
          </button>
        {/each}
      </div>

      <div class="section-heading product-heading">
        <div>
          <p class="section-label">Categorias</p>
          <h3>Productos</h3>
        </div>
        <span class="section-token">Ver todo</span>
      </div>

      <div class="product-grid">
        {#each visibleProducts as product}
          <article class="product-card">
            <button
              class="product-hit"
              type="button"
              onclick={() => navController.navigate(INTERNAL_ROUTES.productDetail, { productId: product.id })}
            >
              <div class={`product-media ${product.accent}`}>
                <div class="heart-mark">♡</div>
                <div class="media-price">${product.price.toFixed(2)}</div>
              </div>
              <div class="product-meta">
                <strong>{product.name}</strong>
                <span>Stock {product.stock}</span>
              </div>
            </button>
          </article>
        {/each}

        {#if visibleProducts.length === 0}
          <div class="empty-state">
            <span class="empty-icon" aria-hidden="true">⌕</span>
            <strong>Sin resultados</strong>
            <p>Prueba con otro termino o categoria.</p>
          </div>
        {/if}
      </div>
    </Card>
  </div>

  <div class="side-column">
    <Card variant="outlined" class="metric-card">
      <div class="section-heading compact">
        <div>
          <p class="section-label">Estado</p>
          <h3>Resumen operativo</h3>
        </div>
        <span class="status-pill online">
          <span class="pill-dot"></span>
          En linea
        </span>
      </div>

      <div class="metric-list">
        {#each metrics as metric}
          <div class="metric-row">
            <div>
              <strong>{metric.label}</strong>
              <p>{metric.detail}</p>
            </div>
            <span>{metric.value}</span>
          </div>
        {/each}
      </div>
    </Card>
  </div>
</section>
