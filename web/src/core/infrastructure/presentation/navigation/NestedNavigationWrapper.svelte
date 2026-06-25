<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import {fade} from "svelte/transition";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {Button, FAB, Icon, NavigationRail, NavigationRailItem} from "m3-svelte";
    import menuIcon from "@ktibow/iconset-material-symbols/menu-rounded";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";
    import storefrontIcon from "@ktibow/iconset-material-symbols/storefront-rounded";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import qrCodeIcon from "@ktibow/iconset-material-symbols/qr-code-rounded";
    import personIcon from "@ktibow/iconset-material-symbols/person-rounded";
    import settingsIcon from "@ktibow/iconset-material-symbols/settings-rounded";
    import logoutIcon from "@ktibow/iconset-material-symbols/logout-rounded";
    import {authContainer} from "../../../feature/auth/di/auth.container";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {categoryStore} from "../../../feature/category/presentation/viewmodel/category.store";
    import {productStore} from "../../../feature/product/presentation/viewmodel/product.store";
    import {promotionStore} from "../../../feature/notification/presentation/viewmodel/promotion.store";
    import {saleStore} from "../../../feature/sale/presentation/viewmodel/sale.store";
    import {saleAlertStore} from "../../../feature/sale/presentation/viewmodel/sale-alert.store";
    import {cartStore} from "../../../feature/sale/presentation/viewmodel/cart.store";
    import {BuyState} from "../../../feature/sale/domain/entity/enums";
    import InternalProductScreen from "../../../feature/product/presentation/screens/InternalProductScreen.svelte";
    import InternalBuyScreen from "../routes/InternalBuyScreen.svelte";
    import InternalBuyConfirmScreen from "../routes/InternalBuyConfirmScreen.svelte";
    import InternalReservationScreen from "../routes/InternalReservationScreen.svelte";
    import InternalReservationDetailScreen from "../routes/InternalReservationDetailScreen.svelte";
    import InternalProfileScreen from "../routes/InternalProfileScreen.svelte";
    import SettingsScreen from "../../../feature/settigns/presentation/routes/SettingsScreen.svelte";
    import SaleVerificationAlert from "../components/SaleVerificationAlert.svelte";
    import {toastStore} from "../viewmodel/toast.store";
    import {logger} from "../util/logger.service";
    import {authFlowStore} from "../../../feature/auth/presentation/viewmodel/auth-flow.store";
    import {
        buy,
        buyConfirm,
        dashboard,
        product,
        productDetail,
        profile,
        reservation,
        reservationDetail,
        settings as settingsRoute
    } from "./nested.router";
    import { buildHomeHash, parseDeepLinkHash } from "./deeplink";
    import { rememberPendingDeepLink } from "./pending-deeplink.store";
    import AdminRoleChoiceCard from "../../../feature/auth/presentation/components/AdminRoleChoiceCard.svelte";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../../../feature/auth/presentation/util/admin-redirect";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string; email?: string; provider?: string }>;

    const internalNavController = rememberNavController(dashboard.path);
    const userId = navBackStackEntry?.args?.id ?? "usuario";
    const currentUser = sessionStore.getCurrentUser();

    const items = [
        { label: "Productos", path: dashboard.path, icon: storefrontIcon, badge: 0 },
        { label: "Su compra", path: buy.path, icon: shoppingCartIcon, badge: 0 },
        { label: "Reservas", path: reservation.path, icon: qrCodeIcon, badge: 0 },
        { label: "Perfil", path: profile.path, icon: personIcon, badge: 0 },
        { label: "Ajustes", path: settingsRoute.path, icon: settingsIcon, badge: 0 }
    ];

    const internalStackStore = internalNavController._getStackStore();

    $: internalStack = $internalStackStore;
    $: currentEntry = internalStack.at(-1);
    $: currentPath = currentEntry?.route ?? dashboard.path;
    $: routeUsesStageScroll = ![dashboard.path, product.path, productDetail.path].includes(currentPath);
    $: cartCount = $cartStore.items.reduce((sum, item) => sum + item.quantity, 0);
    $: pendingSales = $saleStore.items.filter((sale) => sale.verified === BuyState.UNVERIFIED).length;
    $: navItems = items.map((item) => ({
        ...item,
        badge: item.path === buy.path ? cartCount : item.path === reservation.path ? pendingSales : 0
    }));

    let fabOpen = false;
    let suppressHashSync = false;
    let adminChoicePending = false;
    let adminRedirecting = false;

    $: {
        if (typeof document !== "undefined") {
            document.body.style.overflow = fabOpen ? "hidden" : "";
        }
    }

    function clearSessionBoundState({ clearCart = false }: { clearCart?: boolean } = {}) {
        saleStore.reset();
        saleAlertStore.clearAlerts();
        promotionStore.cleanup();
        productStore.reset();
        categoryStore.reset();
        sessionStore.reset();
        authFlowStore.reset();
        if (clearCart) {
            cartStore.clear();
        }
    }

    function handleSaleVerificationOpen(event: Event) {
        const saleId = (event as CustomEvent<{ saleId?: string }>).detail?.saleId;
        if (!saleId) return;
        suppressHashSync = true;
        internalNavController.resetTo(reservationDetail.path, { id: saleId });
        const nextHash = buildHomeHash(reservationDetail.path, { reservationId: saleId });
        if (window.location.hash !== nextHash) {
            window.location.hash = nextHash;
        }
        queueMicrotask(() => {
            suppressHashSync = false;
        });
    }

    function applyInternalHash() {
        const parsed = parseDeepLinkHash(window.location.hash);
        if (!parsed || parsed.top !== "home") return;
        const targetRoute = parsed.nested ?? dashboard.path;
        const targetArgs =
            targetRoute === reservationDetail.path && parsed.args?.reservationId
                ? { id: parsed.args.reservationId }
                : targetRoute === productDetail.path && parsed.args?.productId
                ? { productId: parsed.args.productId }
                : undefined;
        const currentArgs = currentEntry?.args as Record<string, string> | undefined;

        if (currentPath !== targetRoute || JSON.stringify(currentArgs ?? {}) !== JSON.stringify(targetArgs ?? {})) {
            internalNavController.resetTo(targetRoute, targetArgs);
        }
    }

    function go(path: string) {
        if (currentPath !== path) internalNavController.navigate(path);
        fabOpen = false;
    }

    function isItemActive(path: string): boolean {
        if (path === dashboard.path) return currentPath === dashboard.path || currentPath === product.path || currentPath === productDetail.path;
        if (path === buy.path) return currentPath === buy.path || currentPath === buyConfirm.path;
        if (path === reservation.path) return currentPath === reservation.path || currentPath === reservationDetail.path;
        return currentPath === path;
    }

    async function logout() {
        try {
            await authContainer.useCases.sessions.closeSession.execute();
        } finally {
            clearSessionBoundState({ clearCart: true });
            navController.resetTo("welcome");
        }
    }

    function continueAsClient() {
        rememberAdminChoice("client");
        adminChoicePending = false;
    }

    async function continueToAdmin() {
        adminRedirecting = true;
        rememberAdminChoice("admin");
        await goToAdminDashboard(
            async () => await authContainer.useCases.sessions.closeSession.execute()
        );
        adminRedirecting = false;
    }

    onMount(() => {
        window.addEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.addEventListener("hashchange", applyInternalHash);

        if (window.location.hash) {
            suppressHashSync = true;
            applyInternalHash();
            queueMicrotask(() => {
                suppressHashSync = false;
            });
        }

        authContainer.useCases.accounts.getCurrentUser()
            .then(async (user) => {
                if (!shouldOfferAdminChoice(user)) return;
                const choice = getStoredAdminChoice();
                if (choice === "admin") {
                    await continueToAdmin();
                    return;
                }
                if (choice !== "client") {
                    adminChoicePending = true;
                }
            })
            .catch(() => {
                rememberPendingDeepLink(window.location.hash);
                clearSessionBoundState({ clearCart: true });
                navController.resetTo("login");
            });

        productStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar productos");
        });
        categoryStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar categorias");
        });
        promotionStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar promociones");
        });
        saleStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar reservas");
        });
    });

    onDestroy(() => {
        window.removeEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.removeEventListener("hashchange", applyInternalHash);
        clearSessionBoundState();
        logger.info("[InternalNavigation] disposed");
    });

    $: if (!suppressHashSync && typeof window !== "undefined") {
        const args = currentEntry?.args as Record<string, string> | undefined;
        const nextHash = buildHomeHash(
            currentPath as typeof dashboard.path | typeof buy.path | typeof buyConfirm.path | typeof reservation.path | typeof reservationDetail.path | typeof profile.path | typeof settingsRoute.path | typeof productDetail.path,
            currentPath === reservationDetail.path ? { reservationId: args?.id } :
            currentPath === productDetail.path ? { productId: args?.productId } :
            undefined
        );
        if (window.location.hash !== nextHash) {
            window.history.replaceState({}, "", nextHash);
        }
    }
</script>

<section class="nested-shell">
    <aside class="panel-shell expanded-only">
        <div class="panel-card">
            <header class="panel-head">
                <div class="brand">
                    <img src="/alejoicon_clean.svg" alt="Logo" class="brand-logo" />
                    <div class="brand-meta">
                        <h2>Taller Alejo</h2>
                        {#await currentUser}
                            <p>Cargando cuenta...</p>
                        {:then user}
                            <p>{user.name}</p>
                        {:catch error}
                            <p>{error.message}</p>
                        {/await}
                    </div>
                </div>
            </header>

            <div class="rail-wrap">
                <NavigationRail open={true} collapse="no" alignment="top" iconType="left">
                    {#each navItems as item}
                        <div class="rail-item-wrap">
                            <NavigationRailItem
                                label={item.label}
                                icon={item.icon}
                                active={isItemActive(item.path)}
                                onclick={() => go(item.path)}
                            />
                            {#if item.badge > 0}
                                <span class="rail-badge" aria-label={`Pendientes ${item.badge}`}>{item.badge}</span>
                            {/if}
                        </div>
                    {/each}
                </NavigationRail>
            </div>

            <div class="panel-footer">
                <Button variant="tonal" size="m" iconType="left" onclick={logout}>
                    <Icon icon={logoutIcon} />
                    Cerrar sesion
                </Button>
            </div>
        </div>
    </aside>

    <main class="content">
        <div class="top-mobile compact-only">
            <div class="mobile-title">
                <strong>Taller Alejo</strong>
                <span>{userId}</span>
            </div>
        </div>

        {#key currentPath}
            <div
                class="route-stage"
                class:route-stage-scroll={routeUsesStageScroll}
                in:fade={{ duration: 180 }}
                out:fade={{ duration: 120 }}
            >
                <NavHost
                    navController={internalNavController}
                    routes={[
                        composable(dashboard, () => InternalProductScreen),
                        composable(product, () => InternalProductScreen),
                        composable(productDetail, () => InternalProductScreen),
                        composable(buy, () => InternalBuyScreen),
                        composable(buyConfirm, () => InternalBuyConfirmScreen),
                        composable(profile, () => InternalProfileScreen),
                        composable(settingsRoute, () => SettingsScreen),
                        composable(reservation, () => InternalReservationScreen),
                        composable(reservationDetail, () => InternalReservationDetailScreen)
                    ]}
                />
            </div>
        {/key}

        <SaleVerificationAlert navController={internalNavController} />

        {#if adminChoicePending}
            <AdminRoleChoiceCard
                busy={adminRedirecting}
                on:stayClient={continueAsClient}
                on:goAdmin={continueToAdmin}
            />
        {/if}

        <div class="fab-layer compact-only">
            {#if fabOpen}
                <button class="fab-scrim" type="button" aria-label="Cerrar menu" on:click={() => (fabOpen = false)}></button>
            {/if}

            <div class="fab-stack">
                {#if fabOpen}
                    <div class="fab-menu" aria-label="Navegacion rapida">
                        {#each navItems as item}
                            <div class="fab-item-row">
                                <Button class="fab-label" variant="elevated" size="m" onclick={() => go(item.path)}>
                                    {item.label}
                                </Button>

                                <button
                                    class="fab-mini"
                                    class:active={isItemActive(item.path)}
                                    type="button"
                                    on:click={() => go(item.path)}
                                    aria-current={isItemActive(item.path) ? "page" : undefined}
                                    aria-label={item.label}
                                >
                                    <Icon icon={item.icon} />
                                    {#if item.badge > 0}
                                        <span class="mini-badge">{item.badge}</span>
                                    {/if}
                                </button>
                            </div>
                        {/each}

                        <div class="fab-item-row">
                            <Button class="fab-label logout-label-mobile" variant="tonal" size="m" onclick={logout}>
                                Cerrar sesion
                            </Button>

                            <button class="fab-mini logout-mini" type="button" on:click={logout} aria-label="Cerrar sesion">
                                <Icon icon={logoutIcon} />
                            </button>
                        </div>
                    </div>
                {/if}

                <div class="fab-main-wrap">
                    {#if cartCount > 0}
                        <span class="main-badge">{cartCount}</span>
                    {/if}
                    <FAB
                        size="normal"
                        icon={fabOpen ? closeIcon : menuIcon}
                        onclick={() => (fabOpen = !fabOpen)}
                    />
                </div>
            </div>
        </div>
    </main>
</section>

<style>
    .nested-shell {
        height: 100dvh;
        display: grid;
        grid-template-columns: 320px minmax(0, 1fr);
        overflow: hidden;
        background: var(--md-sys-color-background);
    }

    .panel-shell {
        height: 100%;
        padding: 16px 0 16px 16px;
        min-width: 0;
    }

    .panel-card {
        height: 100%;
        display: grid;
        grid-template-rows: auto minmax(0, 1fr) auto;
        border-radius: 32px;
        overflow: hidden;
        border: 1px solid color-mix(
                in srgb,
                var(--md-sys-color-outline-variant) 75%,
                transparent
        );
        background:
                linear-gradient(
                        180deg,
                        color-mix(in srgb, var(--md-sys-color-surface-container) 92%, transparent),
                        var(--md-sys-color-surface-container-low)
                );
        box-shadow:
                0 10px 28px rgba(0, 0, 0, 0.08),
                0 30px 80px rgba(0, 0, 0, 0.10);
    }

    .panel-head {
        padding: 22px 18px 14px;

        background: linear-gradient(
                180deg,
                color-mix(in srgb, var(--md-sys-color-surface-container-high) 85%, transparent),
                transparent
        );

        border-bottom: 1px solid
        color-mix(in srgb, var(--md-sys-color-outline-variant) 50%, transparent);
    }

    .brand {
        display: flex;
        align-items: center;
        gap: 14px;
    }

    .brand-logo {
        width: 46px;
        height: 46px;

        padding: 6px;
        border-radius: 14px;

        background: var(--md-sys-color-surface-container-highest);

        box-shadow:
                0 6px 16px rgba(0, 0, 0, 0.10);
    }

    .brand-meta h2 {
        margin: 0;
        font-size: 1.05rem;
        font-weight: 800;
        letter-spacing: -0.02em;
    }

    .brand-meta p {
        margin: 0;
        font-size: 0.85rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    .rail-wrap {
        min-height: 0;
        overflow: hidden;

        padding: 10px 6px;

        position: relative;
    }

    .rail-wrap :global(.m3-container) {
        width: 100%;
    }

    .rail-wrap :global(.rail) {
        width: 100%;
        height: 100%;
        padding: 8px 0 18px;
        gap: 18px;
        background: transparent;
    }

    .rail-wrap :global(.rail.open),
    .rail-wrap :global(.m3-container:has(> .rail.open:not(.modal))) {
        width: 100%;
    }

    .rail-wrap:global(.items) {
        width: 100%;
        padding-right: 8px;
        overflow: visible;
    }

    .rail-wrap :global(.item) {
        border-radius: 14px;

        transition:
                background .2s ease,
                transform .15s ease;
    }

    .rail-wrap :global(.item:hover) {
        background: var(--md-sys-color-surface-container-highest);
        transform: translateX(2px);
    }

    .rail-wrap :global(.item.active) {
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
        font-weight: 700;
    }

    .rail-wrap :global(.icon),
    .rail-wrap :global(.label) {
        overflow: visible;
    }

    .rail-item-wrap {
        position: relative;
        overflow: visible;
    }

    .rail-badge {
        position: absolute;
        top: 8px;
        right: 14px;
        width: 24px;
        height: 24px;
        border-radius: 999px;
        display: flex;
        align-items: center;
        justify-content: center;
        place-items: center;
        background: #d92d20;
        color: var(--md-sys-color-on-error);
        font-size: 0.74rem;
        font-weight: 800;
        pointer-events: none;
        border: 2px solid var(--md-sys-color-surface-container);
        box-shadow: 0 8px 16px rgb(0 0 0 / 0.18);
        z-index: 5;
    }

    .panel-footer {
        padding: 14px 16px 18px;

        border-top: 1px solid
        color-mix(in srgb, var(--md-sys-color-outline-variant) 60%, transparent);

        background: color-mix(
                in srgb,
                var(--md-sys-color-surface-container-high) 70%,
                transparent
        );
    }

    .panel-footer :global(button) {
        width: 100%;

        border-radius: 14px;

        font-weight: 700;
    }

    .content {
        min-height: 0;
        min-width: 0;

        display: grid;
        grid-template-rows: auto minmax(0, 1fr);

        overflow: hidden;
    }

    .route-stage {
        min-height: 0;
        height: 100%;
        display: grid;
        align-content: start;
        overflow: hidden;
    }

    .route-stage.route-stage-scroll {
        overflow-x: hidden;
        overflow-y: auto;
        overscroll-behavior: contain;
        padding-right: 4px;
    }

    .top-mobile {
        display: none;
    }

    .mobile-title {
        display: grid;
        gap: 2px;
    }

    .mobile-title strong {
        font-size: 1.04rem;
    }

    .mobile-title span {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.8rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .fab-layer {
        display: none;
    }

    @media (max-width: 840px) {
        .nested-shell {
            grid-template-columns: 1fr;
        }

        .expanded-only {
            display: none;
        }

        .content {
            padding: 8px 8px max(96px, calc(env(safe-area-inset-bottom) + 12px));
        }

        /* Ensure route-stage scroller leaves space for FAB */
        .route-stage.route-stage-scroll {
            padding-bottom: calc(96px + env(safe-area-inset-bottom, 0px));
        }

        .top-mobile {
            display: grid;
            margin-bottom: 6px;
        }

        .compact-only {
            display: block;
        }

        .fab-layer {
            display: block;
            position: fixed;
            inset: 0;
            pointer-events: none;
            /* Keep the mobile FAB overlay above route content/cards. */
            z-index: 1000;
        }

        .fab-scrim {
            position: fixed;
            inset: 0;
            border: 0;
            background: color-mix(in srgb, var(--md-sys-color-scrim) 54%, transparent);
            backdrop-filter: blur(2px);
            -webkit-backdrop-filter: blur(2px);
            pointer-events: auto;
            z-index: 1000;
        }

        .fab-stack {
            position: fixed;
            right: max(16px, env(safe-area-inset-right));
            bottom: max(24px, calc(env(safe-area-inset-bottom) + 24px));
            display: grid;
            justify-items: end;
            gap: 14px;
            pointer-events: none;
            /* Elevate above route-stage scroll content and the expanded scrim. */
            z-index: 1001;
        }

        .fab-menu {
            display: grid;
            gap: 12px;
            pointer-events: auto;
            position: relative;
            z-index: 1002;
        }

        .fab-item-row {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .fab-label {
            min-width: 0;
        }

        .fab-label:global(.m3-container) {
            box-shadow: 0 14px 28px color-mix(in srgb, black 20%, transparent);
        }

        .fab-mini {
            position: relative;
            width: 40px;
            height: 40px;
            border: none;
            border-radius: 999px;
            display: grid;
            place-items: center;
            cursor: pointer;
            background: var(--md-sys-color-primary-container);
            color: var(--md-sys-color-on-primary-container);
            box-shadow: 0 14px 28px color-mix(in srgb, black 20%, transparent);
        }

        .fab-mini.active {
            background: var(--md-sys-color-primary);
            color: var(--md-sys-color-on-primary);
        }

        .logout-mini {
            background: var(--md-sys-color-error-container);
            color: var(--md-sys-color-on-error-container);
        }

        .logout-label-mobile:global(.m3-container) {
            background: var(--md-sys-color-error-container);
            color: var(--md-sys-color-on-error-container);
        }

        .mini-badge,
        .main-badge {
            position: absolute;
            top: -6px;
            right: -6px;
            width: 24px;
            height: 24px;
            border-radius: 999px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #d92d20;
            color: var(--md-sys-color-on-error);
            font-size: 0.72rem;
            font-weight: 800;
            border: 2px solid var(--md-sys-color-surface);
            box-shadow: 0 8px 16px rgb(0 0 0 / 0.18);
            z-index: 5;
        }

        .fab-main-wrap {
            position: relative;
            pointer-events: auto;
            z-index: 1003;
            filter: drop-shadow(0 16px 28px color-mix(in srgb, black 28%, transparent))
            drop-shadow(0 6px 12px color-mix(in srgb, black 18%, transparent));
        }
    }

    .rail-wrap :global(.rail::-webkit-scrollbar),
    .content::-webkit-scrollbar,
    .route-stage::-webkit-scrollbar {
        width: 10px;
        height: 10px;
    }

    .rail-wrap :global(.rail::-webkit-scrollbar-thumb),
    .content::-webkit-scrollbar-thumb,
    .route-stage::-webkit-scrollbar-thumb {
        background: color-mix(in srgb, var(--md-sys-color-outline) 30%, transparent);
        border-radius: 999px;
        border: 2px solid transparent;
        background-clip: padding-box;
    }

    .rail-wrap :global(.rail::-webkit-scrollbar-track),
    .content::-webkit-scrollbar-track,
    .route-stage::-webkit-scrollbar-track {
        background: transparent;
    }
</style>
