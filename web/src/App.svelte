<script lang="ts">
  import { Button, Card, TextField } from 'm3-svelte'
  import {
    Bell,
    Gauge,
    Grid2x2,
    House,
    Package,
    Search,
    Settings,
    ShoppingBag,
    TriangleAlert,
    Wifi,
  } from 'lucide-svelte'

  type Category = {
    id: string
    name: string
  }

  type Product = {
    id: string
    name: string
    categoryId: string
    price: number
    stock: number
    accent: string
  }

  type Metric = {
    label: string
    value: string
    detail: string
  }

  const categories: Category[] = [
    { id: 'all', name: 'Todas' },
    { id: 'audio', name: 'Audio' },
    { id: 'energia', name: 'Energia' },
    { id: 'accesorios', name: 'Accesorios' },
    { id: 'ofertas', name: 'Ofertas' },
  ]

  const products: Product[] = [
    { id: 'p1', name: 'EchoFlow Mini', categoryId: 'audio', price: 149.9, stock: 12, accent: 'aurora' },
    { id: 'p2', name: 'PowerCore 90W', categoryId: 'energia', price: 89.5, stock: 7, accent: 'forest' },
    { id: 'p3', name: 'Clip Mount Pro', categoryId: 'accesorios', price: 24.99, stock: 19, accent: 'mist' },
    { id: 'p4', name: 'Studio Beam', categoryId: 'ofertas', price: 199, stock: 4, accent: 'ember' },
  ]

  const metrics: Metric[] = [
    { label: 'Ventas del dia', value: '$1,248', detail: '18 operaciones sincronizadas' },
    { label: 'Pendientes offline', value: '03', detail: 'Listas para enviar a Appwrite' },
    { label: 'Alertas', value: '02', detail: 'Una promo y un soporte abierto' },
  ]

  let query = ''
  let selectedCategoryId = 'all'

  const visibleProducts = $derived.by(() =>
    products.filter((product) => {
      const matchCategory =
        selectedCategoryId === 'all' || product.categoryId === selectedCategoryId
      const matchQuery = product.name.toLowerCase().includes(query.trim().toLowerCase())

      return matchCategory && matchQuery
    })
  )
</script>

<svelte:head>
  <title>Taller Alejo Dashboard</title>
  <meta
    name="description"
    content="Dashboard web alineado visualmente con Taller Alejo en Android y Material 3."
  />
</svelte:head>

<main class="app-shell">
  <section class="mobile-frame">
    <header class="topbar">
      <div>
        <p class="eyebrow">Taller Alejo</p>
        <h1>Panel de catalogo y operaciones</h1>
      </div>

      <button class="icon-button" type="button" aria-label="Ver alertas">
        <Bell size={20} />
      </button>
    </header>

    <Card variant="filled" class="promo-card">
      <div class="promo-copy">
        <span class="promo-badge">Promocion activa</span>
        <h2>Hasta 50% en dispositivos seleccionados</h2>
        <p>Replica el banner destacado de Android con una CTA principal y resumen operativo.</p>

        <div class="promo-actions">
          <Button variant="filled" size="m">Ver promociones</Button>
          <Button variant="text" size="m">Detalles</Button>
        </div>
      </div>

      <div class="promo-visual" aria-hidden="true">
        <div class="promo-orb promo-orb-a"></div>
        <div class="promo-orb promo-orb-b"></div>
        <div class="promo-device"></div>
      </div>
    </Card>

    <section class="dashboard-grid">
      <Card variant="filled" class="catalog-card">
        <div class="section-heading">
          <div>
            <p class="section-label">Catalogo</p>
            <h3>Busqueda y categorias</h3>
          </div>
          <span class="supporting">Patron equivalente a `ProductScreen`</span>
        </div>

        <div class="search-wrap">
          <Search class="search-icon" size={18} />
          <TextField label="Buscar producto" bind:value={query} />
        </div>

        <div class="chip-row" role="tablist" aria-label="Categorias">
          {#each categories as category}
            <button
              class:selected={category.id === selectedCategoryId}
              class="chip"
              type="button"
              on:click={() => (selectedCategoryId = category.id)}
            >
              {category.name}
            </button>
          {/each}
        </div>

        <div class="product-grid">
          {#each visibleProducts as product}
            <article class="product-card">
              <div class={`product-media ${product.accent}`}>
                <div class="media-price">${product.price.toFixed(2)}</div>
              </div>
              <div class="product-meta">
                <strong>{product.name}</strong>
                <span>Stock {product.stock}</span>
              </div>
            </article>
          {/each}

          {#if visibleProducts.length === 0}
            <div class="empty-state">
              <Search size={28} />
              <strong>Sin resultados</strong>
              <p>Prueba con otro termino o selecciona otra categoria.</p>
            </div>
          {/if}
        </div>
      </Card>

      <div class="side-column">
        <Card variant="outlined" class="metric-card">
          <div class="section-heading compact">
            <div>
              <p class="section-label">Estado</p>
              <h3>Resumen operativo</h3>
            </div>
            <span class="status-pill online">
              <Wifi size={14} />
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

        <Card variant="filled" class="queue-card">
          <div class="section-heading compact">
            <div>
              <p class="section-label">Sincronizacion</p>
              <h3>Paridad con Android</h3>
            </div>
            <Gauge size={18} />
          </div>

          <ul class="check-list">
            <li>
              <span class="dot success"></span>
              Appwrite en `web` corresponde a Appwrite en Android.
            </li>
            <li>
              <span class="dot success"></span>
              Dexie cubre la capa offline-first donde Android usa Room.
            </li>
            <li>
              <span class="dot warning"></span>
              Faltaba superficie UI conectada a esas capas.
            </li>
          </ul>
        </Card>

        <Card variant="outlined" class="alerts-card">
          <div class="section-heading compact">
            <div>
              <p class="section-label">Prioridades</p>
              <h3>Brechas actuales</h3>
            </div>
            <TriangleAlert size={18} />
          </div>

          <ul class="check-list">
            <li>
              <span class="dot warning"></span>
              La web no tenia navegacion inferior ni jerarquia tipo app.
            </li>
            <li>
              <span class="dot warning"></span>
              La demo anterior no representaba productos, promos ni estados.
            </li>
            <li>
              <span class="dot warning"></span>
              El tono visual estaba mas cerca de landing de libreria que de panel de taller.
            </li>
          </ul>
        </Card>
      </div>
    </section>

    <nav class="bottom-nav" aria-label="Navegacion principal">
      <button class="nav-item active" type="button">
        <House size={20} />
        <span>Inicio</span>
      </button>
      <button class="nav-item" type="button">
        <Grid2x2 size={20} />
        <span>Catalogo</span>
      </button>
      <button class="nav-item" type="button">
        <ShoppingBag size={20} />
        <span>Ventas</span>
      </button>
      <button class="nav-item" type="button">
        <Package size={20} />
        <span>Stock</span>
      </button>
      <button class="nav-item" type="button">
        <Settings size={20} />
        <span>Ajustes</span>
      </button>
    </nav>
  </section>
</main>
