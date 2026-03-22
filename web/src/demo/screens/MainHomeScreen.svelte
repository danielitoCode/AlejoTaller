<script lang="ts">
  import { onMount } from 'svelte'
  import { Button, Card, FAB } from 'm3-svelte'
  import type { NavController } from '../../lib/navigation/NavController'
  import type { NavBackStackEntry } from '../../lib/navigation/NavBackStackEntry'
  import NavHost from '../../lib/navigation/NavHost.svelte'
  import { rememberNavController } from '../../lib/navigation/rememberNavController'
  import { INTERNAL_ROUTES, MAIN_ROUTES } from '../routes'
  import HomeScreen from './HomeScreen.svelte'
  import ProductDetailScreen from './ProductDetailScreen.svelte'
  import PromotionDetailScreen from './PromotionDetailScreen.svelte'
  import BuyScreen from './BuyScreen.svelte'
  import BuyConfirmScreen from './BuyConfirmScreen.svelte'
  import ReservationsScreen from './ReservationsScreen.svelte'
  import ProfileScreen from './ProfileScreen.svelte'
  import SettingsScreen from './SettingsScreen.svelte'

  export let navController: NavController

  const internalNavController = rememberNavController(INTERNAL_ROUTES.home)
  const internalRoutes = [
    { route: { path: INTERNAL_ROUTES.home }, component: HomeScreen },
    { route: { path: INTERNAL_ROUTES.productDetail }, component: ProductDetailScreen },
    { route: { path: INTERNAL_ROUTES.promotionDetail }, component: PromotionDetailScreen },
    { route: { path: INTERNAL_ROUTES.buy }, component: BuyScreen },
    { route: { path: INTERNAL_ROUTES.buyConfirm }, component: BuyConfirmScreen },
    { route: { path: INTERNAL_ROUTES.reservations }, component: ReservationsScreen },
    { route: { path: INTERNAL_ROUTES.profile }, component: ProfileScreen },
    { route: { path: INTERNAL_ROUTES.settings }, component: SettingsScreen },
  ]

  const fabItems = [
    { label: 'Productos', path: INTERNAL_ROUTES.home, icon: 'P' },
    { label: 'Su compra', path: INTERNAL_ROUTES.buy, icon: 'C', badge: '2' },
    { label: 'Reservas', path: INTERNAL_ROUTES.reservations, icon: 'R' },
    { label: 'Perfil', path: INTERNAL_ROUTES.profile, icon: 'U' },
    { label: 'Ajustes', path: INTERNAL_ROUTES.settings, icon: 'A' },
  ]

  let fabOpen = false
  let viewportWidth = 0

  const stackStore = internalNavController._getStackStore()
  $: internalStack = $stackStore
  $: currentEntry = internalStack.at(-1) as NavBackStackEntry<{ productId?: string; promotionId?: string }> | undefined
  $: currentPath = currentEntry?.route ?? INTERNAL_ROUTES.home
  $: isDesktopListDetail = viewportWidth >= 980
  $: showSplitLayout =
    isDesktopListDetail &&
    (currentPath === INTERNAL_ROUTES.home ||
      currentPath === INTERNAL_ROUTES.productDetail ||
      currentPath === INTERNAL_ROUTES.promotionDetail)

  function syncViewport() {
    viewportWidth = window.innerWidth
  }

  onMount(() => {
    syncViewport()
    window.addEventListener('resize', syncViewport)
    return () => window.removeEventListener('resize', syncViewport)
  })

  function navigateInternal(path: string) {
    fabOpen = false
    if (path === currentPath) return
    internalNavController.navigate(path)
  }

  function logout() {
    fabOpen = false
    navController.navigate(MAIN_ROUTES.landing)
  }
</script>

<section class="internal-shell">
  <div class="internal-topbar">
    <div>
      <p class="eyebrow">Panel interno</p>
      <h2>Taller Alejo</h2>
    </div>
    <span class="status-pill online">
      <span class="pill-dot"></span>
      Sincronizado
    </span>
  </div>

  {#if showSplitLayout}
    <div class="list-detail-shell">
      <div class="list-pane">
        <HomeScreen navController={internalNavController} />
      </div>

      <div class="detail-pane">
        {#if currentPath === INTERNAL_ROUTES.productDetail}
          <ProductDetailScreen
            navController={internalNavController}
            navBackStackEntry={currentEntry as NavBackStackEntry<{ productId?: string }>}
            embedded={true}
          />
        {:else if currentPath === INTERNAL_ROUTES.promotionDetail}
          <PromotionDetailScreen
            navController={internalNavController}
            navBackStackEntry={currentEntry as NavBackStackEntry<{ promotionId?: string }>}
            embedded={true}
          />
        {:else}
          <Card variant="outlined" class="detail-placeholder-card">
            <div class="detail-placeholder-art"></div>
            <h3>Selecciona un producto</h3>
            <p class="body-copy">
              El layout adaptativo replica el placeholder del detalle en escritorio antes de abrir
              producto u oferta.
            </p>
          </Card>
        {/if}
      </div>
    </div>
  {:else}
    <NavHost navController={internalNavController} routes={internalRoutes} />
  {/if}

  {#if viewportWidth < 760}
    <div class="mobile-dock">
      <Button variant="text" size="s" onclick={() => navigateInternal(INTERNAL_ROUTES.home)}>
        Productos
      </Button>
      <Button variant="text" size="s" onclick={() => navigateInternal(INTERNAL_ROUTES.buy)}>
        Compra
      </Button>
      <Button variant="text" size="s" onclick={() => navigateInternal(INTERNAL_ROUTES.reservations)}>
        Reservas
      </Button>
    </div>
  {/if}

  {#if fabOpen}
    <button class="fab-scrim" type="button" aria-label="Cerrar menu" onclick={() => (fabOpen = false)}></button>
  {/if}

  <div class="fab-stack">
    {#if fabOpen}
      <div class="fab-menu">
        {#each fabItems as item}
          <div class="fab-item-row">
            <button class="fab-label" type="button" onclick={() => navigateInternal(item.path)}>
              {item.label}
            </button>
            <button class="fab-mini" type="button" onclick={() => navigateInternal(item.path)}>
              <span>{item.icon}</span>
              {#if item.badge}
                <span class="mini-badge">{item.badge}</span>
              {/if}
            </button>
          </div>
        {/each}
        <div class="fab-item-row">
          <button class="fab-label" type="button" onclick={logout}>Cerrar sesion</button>
          <button class="fab-mini" type="button" onclick={logout}>
            <span>X</span>
          </button>
        </div>
      </div>
    {/if}

    <FAB color="primary" text={fabOpen ? 'Cerrar' : 'Menu'} onclick={() => (fabOpen = !fabOpen)} />
  </div>
</section>
