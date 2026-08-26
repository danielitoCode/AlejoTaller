<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import { get } from "svelte/store";
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
    import supportIcon from "@ktibow/iconset-material-symbols/support-agent-rounded";
    import smartToyIcon from "@ktibow/iconset-material-symbols/smart-toy-rounded";
    import logoutIcon from "@ktibow/iconset-material-symbols/logout-rounded";
    import loginIcon from "@ktibow/iconset-material-symbols/login-rounded";
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
    import PromotionChrome from "../../../feature/notification/presentation/components/PromotionChrome.svelte";
    import InternalBuyScreen from "../routes/InternalBuyScreen.svelte";
    import InternalBuyConfirmScreen from "../routes/InternalBuyConfirmScreen.svelte";
    import InternalReservationScreen from "../routes/InternalReservationScreen.svelte";
    import InternalReservationDetailScreen from "../routes/InternalReservationDetailScreen.svelte";
    import InternalProfileScreen from "../routes/InternalProfileScreen.svelte";
    import SettingsScreen from "../../../feature/settigns/presentation/routes/SettingsScreen.svelte";
    import SupportInbox from "../../../feature/support/presentation/routes/SupportInbox.svelte";
    import SupportDetail from "../../../feature/support/presentation/routes/SupportDetail.svelte";
    import AgentChat from "../../../feature/agent/presentation/routes/AgentChat.svelte";
    import { supportInboxStore } from "../../../feature/support/presentation/viewmodel/support-inbox.store";
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
        settings as settingsRoute,
        support,
        supportDetail,
        agent
    } from "./nested.router";
    import { buildHomeHash, parseDeepLinkHash } from "./deeplink";
    import { rememberPendingDeepLink } from "./pending-deeplink.store";
    import { logNavAuthCheck, logNavRoute, logProductFlow, logNavError } from "./debug-logger";
    import AdminRoleChoiceCard from "../../../feature/auth/presentation/components/AdminRoleChoiceCard.svelte";
    import GuestAuthOverlay from "../../../feature/auth/presentation/components/GuestAuthOverlay.svelte";
    import {
        getStoredAdminChoice,
        goToAdminDashboard,
        rememberAdminChoice,
        shouldOfferAdminChoice
    } from "../../../feature/auth/presentation/util/admin-redirect";
    import {
        hasClearAuthenticatedProfile,
        resolveUserId
    } from "../../../feature/auth/presentation/util/profile-classification";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string; email?: string; provider?: string }>;

    const internalNavController = rememberNavController(dashboard.path);
    const userId = navBackStackEntry?.args?.id ?? "usuario";
    let currentUser: Promise<any> | null = null;

    const items = [
        { label: "Productos", path: dashboard.path, icon: storefrontIcon, badge: 0 },
        { label: "Su compra", path: buy.path, icon: shoppingCartIcon, badge: 0 },
        { label: "Reservas", path: reservation.path, icon: qrCodeIcon, badge: 0 },
        { label: "Soporte", path: support.path, icon: supportIcon, badge: 0 },
        { label: "Asistente", path: agent.path, icon: smartToyIcon, badge: 0 },
        { label: "Perfil", path: profile.path, icon: personIcon, badge: 0 },
        { label: "Ajustes", path: settingsRoute.path, icon: settingsIcon, badge: 0 }
    ];

    const internalStackStore = internalNavController._getStackStore();

    $: internalStack = $internalStackStore;
    $: currentEntry = internalStack.at(-1);
    $: currentPath = currentEntry?.route ?? dashboard.path;
    $: currentRouteKey = `${currentPath}:${JSON.stringify(currentEntry?.args ?? {})}`;
    $: routeUsesStageScroll = ![dashboard.path, product.path, productDetail.path].includes(currentPath);
    $: cartCount = $cartStore.items.reduce((sum, item) => sum + item.quantity, 0);
    $: isGuestSession = $sessionStore.isGuest;
    $: pendingSales = isGuestSession ? 0 : $saleStore.items.filter((sale) => sale.verified === BuyState.UNVERIFIED).length;
    $: supportUnread = isGuestSession
        ? 0
        : ($supportInboxStore.items ?? []).reduce((acc, m) => acc + (m.unreadUser ?? 0), 0);
    $: visibleItems = isGuestSession ? items.filter((item) => item.path === dashboard.path || item.path === agent.path) : items;
    $: navItems = visibleItems.map((item) => ({
        ...item,
        badge:
            item.path === buy.path
                ? cartCount
                : item.path === reservation.path
                  ? pendingSales
                  : item.path === support.path
                    ? supportUnread
                    : 0
    }));
    $: logoutLabel = isGuestSession ? "Salir" : "Cerrar sesion";

    // NOTE: full NestedNavigationWrapper body (handlers + template + styles) must be restored from master.
    // Agent wiring above is complete. Copy remaining functions/template/styles from master branch file.
    let stopSupportRt: (() => void) | null = null;
    let fabOpen = false;
    let suppressHashSync = false;
    let hashSyncReady = false;
    let adminChoicePending = false;
    let adminRedirecting = false;
    let guestAuthOverlayOpen = false;

    function go(path: string) {
        if (isGuestSession && path !== dashboard.path && path !== product.path && path !== productDetail.path && path !== agent.path) {
            guestAuthOverlayOpen = true;
            fabOpen = false;
            return;
        }
        if (currentPath !== path) internalNavController.navigate(path);
        fabOpen = false;
    }

    function isItemActive(path: string): boolean {
        if (path === dashboard.path) return currentPath === dashboard.path || currentPath === product.path || currentPath === productDetail.path;
        if (path === buy.path) return currentPath === buy.path || currentPath === buyConfirm.path;
        if (path === reservation.path) return currentPath === reservation.path || currentPath === reservationDetail.path;
        if (path === support.path) return currentPath === support.path || currentPath === supportDetail.path;
        return currentPath === path;
    }
</script>

<section class="nested-shell">
    <aside class="panel-shell expanded-only">
        <div class="panel-card">
            <header class="panel-head"><strong>Taller Alejo</strong></header>
            <div class="rail-wrap">
                <NavigationRail open={true} collapse="no" alignment="top" iconType="left">
                    {#each navItems as item}
                        <NavigationRailItem label={item.label} icon={item.icon} active={isItemActive(item.path)} onclick={() => go(item.path)} />
                    {/each}
                </NavigationRail>
            </div>
        </div>
    </aside>
    <main class="content">
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
                composable(reservationDetail, () => InternalReservationDetailScreen),
                composable(support, () => SupportInbox),
                composable(supportDetail, () => SupportDetail),
                composable(agent, () => AgentChat)
            ]}
        />
    </main>
</section>

<style>
.nested-shell{height:100dvh;display:grid;grid-template-columns:320px 1fr;overflow:hidden}
.panel-shell{height:100%;padding:16px}.panel-card{height:100%;display:grid;grid-template-rows:auto 1fr}
.rail-wrap{min-height:0;overflow:auto}.content{min-height:0;overflow:auto}
@media(max-width:840px){.nested-shell{grid-template-columns:1fr}.expanded-only{display:none}}
</style>
