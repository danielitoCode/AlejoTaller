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
    import {profileStore} from "../../../feature/auth/presentation/viewmodel/profile.store";
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
        buy, buyConfirm, dashboard, product, productDetail, profile, reservation,
        reservationDetail, settings as settingsRoute, support, supportDetail, agent
    } from "./nested.router";
    import { buildHomeHash, parseDeepLinkHash } from "./deeplink";
    import { logNavAuthCheck, logNavRoute, logProductFlow } from "./debug-logger";
    import AdminRoleChoiceCard from "../../../feature/auth/presentation/components/AdminRoleChoiceCard.svelte";
    import GuestAuthOverlay from "../../../feature/auth/presentation/components/GuestAuthOverlay.svelte";
    import {
        getStoredAdminChoice, goToAdminDashboard, rememberAdminChoice, shouldOfferAdminChoice
    } from "../../../feature/auth/presentation/util/admin-redirect";
    import {
        hasClearAuthenticatedProfile, resolveUserId
    } from "../../../feature/auth/presentation/util/profile-classification";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string; email?: string; provider?: string }>;

    const internalNavController = rememberNavController(dashboard.path);
    const userId = navBackStackEntry?.args?.id ?? "usuario";
    const APP_VERSION = (import.meta as any).env?.VITE_APP_VERSION ?? "0.3.0";
    let currentUser: Promise<any> | null = null;
    let resolvedUser: { name?: string; prefs?: Record<string, unknown> } | null = null;

    function resolveAvatarUrl(user: any): string {
        if (!user) return "";
        for (const k of ["avatarUrl", "photo_url", "photoUrl", "avatar", "picture"]) {
            if (typeof user[k] === "string" && (user[k] as string).trim()) return (user[k] as string).trim();
        }
        const pr = (user.prefs ?? {}) as Record<string, unknown>;
        for (const k of ["avatarUrl", "photo_url", "photoUrl", "avatar", "picture"]) {
            if (typeof pr[k] === "string" && (pr[k] as string).trim()) return (pr[k] as string).trim();
        }
        return "";
    }

    function userInitials(name: string | undefined): string {
        const n = (name || "U").trim();
        const parts = n.split(/\s+/).filter(Boolean);
        if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
        return n.slice(0, 2).toUpperCase();
    }

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
    $: routeUsesStageScroll = ![dashboard.path, product.path, productDetail.path, agent.path].includes(currentPath);
    $: cartCount = $cartStore.items.reduce((sum, item) => sum + item.quantity, 0);
    $: isGuestSession = $sessionStore.isGuest;
    $: pendingSales = isGuestSession ? 0 : $saleStore.items.filter((sale) => sale.verified === BuyState.UNVERIFIED).length;
    $: supportUnread = isGuestSession ? 0 : ($supportInboxStore.items ?? []).reduce((acc, m) => acc + (m.unreadUser ?? 0), 0);
    $: visibleItems = isGuestSession ? items.filter((item) => item.path === dashboard.path || item.path === agent.path) : items;
    $: navItems = visibleItems.map((item) => ({
        ...item,
        badge: item.path === buy.path ? cartCount : item.path === reservation.path ? pendingSales : item.path === support.path ? supportUnread : 0
    }));
    $: logoutLabel = isGuestSession ? "Salir" : "Cerrar sesion";
    $: profileDraft = $profileStore;
    $: navDisplayName = isGuestSession ? "Invitado" : (profileDraft.name?.trim() || resolvedUser?.name || "Usuario");
    $: navAvatarUrl = isGuestSession ? "" : (profileDraft.avatarUrl?.trim() || resolveAvatarUrl(resolvedUser) || "");
    $: navRoleLabel = isGuestSession ? "" : "Cliente";

    let stopSupportRt: (() => void) | null = null;
    let fabOpen = false;
    let suppressHashSync = false;
    let hashSyncReady = false;
    let adminChoicePending = false;
    let adminRedirecting = false;
    let guestAuthOverlayOpen = false;

    $: { if (typeof document !== "undefined") document.body.style.overflow = fabOpen ? "hidden" : ""; }

    function clearSessionBoundState({ clearCart = false }: { clearCart?: boolean } = {}) {
        stopSupportRt?.(); stopSupportRt = null;
        supportInboxStore.stopRealtime(); saleStore.reset(); saleAlertStore.clearAlerts();
        promotionStore.cleanup(); productStore.reset(); categoryStore.reset();
        sessionStore.reset(); authFlowStore.reset();
        if (clearCart) cartStore.clear();
    }

    function startSupportBadgePipeline() {
        supportInboxStore.syncMine().catch(() => {});
        if (!stopSupportRt) stopSupportRt = supportInboxStore.startRealtime();
    }

    function forceVisitorMode(user?: any) {
        sessionStore.setGuestSession();
        authFlowStore.setSuccess({ userId: resolveUserId(user), email: null, provider: "guest" });
    }

    function handleSaleVerificationOpen(event: Event) {
        if (isGuestSession) return;
        const saleId = (event as CustomEvent<{ saleId?: string }>).detail?.saleId;
        if (!saleId) return;
        suppressHashSync = true;
        internalNavController.resetTo(reservationDetail.path, { id: saleId });
        const nextHash = buildHomeHash(reservationDetail.path, { reservationId: saleId });
        if (window.location.hash !== nextHash) window.location.hash = nextHash;
        queueMicrotask(() => { suppressHashSync = false; });
    }

    function applyInternalHash() {
        const parsed = parseDeepLinkHash(window.location.hash);
        if (!parsed || parsed.top !== "home") return;
        const targetRoute = parsed.nested ?? dashboard.path;
        const targetArgs =
            targetRoute === reservationDetail.path && parsed.args?.reservationId ? { id: parsed.args.reservationId }
            : targetRoute === productDetail.path && parsed.args?.productId ? { productId: parsed.args.productId }
            : targetRoute === supportDetail.path && (parsed.args?.id || parsed.args?.threadId) ? { id: parsed.args.id ?? parsed.args.threadId }
            : undefined;
        const currentArgs = currentEntry?.args as Record<string, string> | undefined;
        if (import.meta.env.DEV && targetRoute === productDetail.path) logProductFlow(parsed.args?.productId || "?", "resolve-start");
        if (currentPath !== targetRoute || JSON.stringify(currentArgs ?? {}) !== JSON.stringify(targetArgs ?? {})) {
            if (import.meta.env.DEV) logNavRoute(targetRoute, targetArgs);
            const stackSize = get(internalStackStore).length;
            const isColdBootDeepLink = stackSize <= 1 && currentPath === dashboard.path;
            if (isColdBootDeepLink && targetRoute !== dashboard.path) internalNavController.navigate(targetRoute, targetArgs);
            else internalNavController.resetTo(targetRoute, targetArgs);
        }
    }

    function go(path: string) {
        if (isGuestSession && path !== dashboard.path && path !== product.path && path !== productDetail.path && path !== agent.path) {
            guestAuthOverlayOpen = true; fabOpen = false; return;
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

    async function logout() {
        try { await authContainer.useCases.sessions.closeSession.execute(); }
        finally { clearSessionBoundState({ clearCart: true }); navController.resetTo("login"); }
    }

    function handleRequestLogin() {
        if (import.meta.env.DEV) logNavAuthCheck(false, false, "redirect-login");
        guestAuthOverlayOpen = false;
        clearSessionBoundState({ clearCart: true });
        navController.resetTo("login");
    }

    function continueAsClient() { rememberAdminChoice("client"); adminChoicePending = false; }

    async function continueToAdmin() {
        adminRedirecting = true;
        rememberAdminChoice("admin");
        await goToAdminDashboard(async () => await authContainer.useCases.sessions.closeSession.execute());
        adminRedirecting = false;
    }

    onMount(() => {
        window.removeEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.addEventListener("hashchange", applyInternalHash);
        window.addEventListener("request-guest-login", handleRequestLogin);
        suppressHashSync = true;
        if (window.location.hash) {
            if (import.meta.env.DEV) logNavRoute("nested-onMount", { hash: window.location.hash });
            applyInternalHash();
        }
        queueMicrotask(() => { suppressHashSync = false; hashSyncReady = true; });

        currentUser = authContainer.useCases.accounts.getCurrentUser()
            .then((user) => {
                if (!hasClearAuthenticatedProfile(user)) {
                    forceVisitorMode(user);
                    if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-unclear-profile");
                    const parsedHash = parseDeepLinkHash(window.location.hash);
                    const hasProductDeeplink = parsedHash?.top === "home" && (parsedHash.nested === productDetail.path || !!parsedHash.args?.productId);
                    if (!hasProductDeeplink) internalNavController.resetTo(dashboard.path);
                    resolvedUser = { name: "Visitante" };
                    return { name: "Visitante", email: "" };
                }
                sessionStore.setAuthenticatedSession();
                resolvedUser = user;
                profileStore.hydrateFromUser(user as any);
                startSupportBadgePipeline();
                promotionStore.syncAll().catch(() => toastStore.error("Error al sincronizar promociones"));
                saleStore.syncAll().catch(() => toastStore.error("Error al sincronizar reservas"));
                if (shouldOfferAdminChoice(user)) {
                    const choice = getStoredAdminChoice();
                    if (choice === "admin") continueToAdmin();
                    else if (choice !== "client") adminChoicePending = true;
                }
                return user;
            })
            .catch(() => {
                forceVisitorMode();
                resolvedUser = { name: "Visitante" };
                if (import.meta.env.DEV) logNavAuthCheck(false, true, "force-visitor-no-session");
                return { name: "Visitante", email: "" };
            });

        productStore.syncAll().catch(() => toastStore.error("Error al sincronizar productos"));
        categoryStore.syncAll().catch(() => toastStore.error("Error al sincronizar categorias"));
        if (!get(sessionStore).isGuest) {
            promotionStore.syncAll().catch(() => toastStore.error("Error al sincronizar promociones"));
            saleStore.syncAll().catch(() => toastStore.error("Error al sincronizar reservas"));
            startSupportBadgePipeline();
        }
    });

    onDestroy(() => {
        window.removeEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.removeEventListener("hashchange", applyInternalHash);
        window.removeEventListener("request-guest-login", handleRequestLogin);
        stopSupportRt?.(); stopSupportRt = null;
        supportInboxStore.stopRealtime();
        clearSessionBoundState();
        logger.info("[InternalNavigation] disposed");
    });

    $: if (hashSyncReady && !suppressHashSync && typeof window !== "undefined") {
        const args = currentEntry?.args as Record<string, string> | undefined;
        const nextHash = buildHomeHash(
            currentPath as any,
            currentPath === reservationDetail.path ? { reservationId: args?.id } :
            currentPath === productDetail.path ? { productId: args?.productId } :
            currentPath === supportDetail.path ? { id: args?.id } : undefined
        );
        if (window.location.hash !== nextHash) window.history.replaceState({}, "", nextHash);
    }
</script>

<section class="nested-shell">
    <aside class="panel-shell expanded-only">
        <div class="panel-card">
            <header class="panel-head">
                <div class="brand">
                    <img src="/alejoicon_clean.svg" alt="Logo AlejoTaller" class="brand-logo" />
                    <div class="brand-meta">
                        <h2>Taller Alejo</h2>
                        <p class="app-version">v{APP_VERSION}</p>
                    </div>
                </div>
            </header>
            <div class="panel-promo"><PromotionChrome variant="nav" /></div>
            <div class="rail-wrap">
                <NavigationRail open={true} collapse="no" alignment="top" iconType="left">
                    {#each navItems as item}
                        <div class="rail-item-wrap" class:is-active={isItemActive(item.path)}>
                            <NavigationRailItem label={item.label} icon={item.icon} active={isItemActive(item.path)} onclick={() => go(item.path)} />
                            {#if item.badge > 0}<span class="rail-badge" aria-label={`Pendientes ${item.badge}`}>{item.badge}</span>{/if}
                        </div>
                    {/each}
                </NavigationRail>
            </div>
            <div class="panel-footer">
                <div class="user-chip">
                    {#if isGuestSession}
                        <span class="user-avatar guest" aria-hidden="true"><Icon icon={personIcon} /></span>
                        <div class="user-chip-meta"><p class="user-chip-name">Invitado</p></div>
                    {:else if navAvatarUrl}
                        <img class="user-avatar" src={navAvatarUrl} alt="" />
                        <div class="user-chip-meta">
                            <p class="user-chip-name">{navDisplayName}</p>
                            <p class="user-chip-role">{navRoleLabel}</p>
                        </div>
                    {:else}
                        <span class="user-avatar initials" aria-hidden="true">{userInitials(navDisplayName)}</span>
                        <div class="user-chip-meta">
                            <p class="user-chip-name">{navDisplayName}</p>
                            <p class="user-chip-role">{navRoleLabel}</p>
                        </div>
                    {/if}
                </div>
                {#if isGuestSession}
                    <Button class="login-btn" variant="filled" size="m" iconType="left" onclick={handleRequestLogin}>
                        <Icon icon={loginIcon} /> Iniciar sesión
                    </Button>
                {/if}
                <button type="button" class="logout-native" onclick={logout}>
                    <Icon icon={logoutIcon} /><span>{logoutLabel}</span>
                </button>
            </div>
        </div>
    </aside>
    <main class="content">
        <div class="top-mobile compact-only">
            <div class="mobile-title">
                <strong>Taller Alejo</strong>
                <span>{isGuestSession ? "Visitante" : userId}</span>
            </div>
            {#if isGuestSession}
                <Button variant="filled" size="s" iconType="left" onclick={handleRequestLogin}>
                    <Icon icon={loginIcon} /> Iniciar sesión
                </Button>
            {/if}
        </div>
        {#key currentRouteKey}
            <div class="route-stage" class:route-stage-scroll={routeUsesStageScroll} in:fade={{ duration: 180 }} out:fade={{ duration: 120 }}>
                <NavHost navController={internalNavController} routes={[
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
                ]} />
            </div>
        {/key}
        {#if !isGuestSession}<SaleVerificationAlert navController={internalNavController} />{/if}
        {#if adminChoicePending}
            <AdminRoleChoiceCard busy={adminRedirecting} on:stayClient={continueAsClient} on:goAdmin={continueToAdmin} />
        {/if}
        <GuestAuthOverlay open={guestAuthOverlayOpen} on:login={handleRequestLogin} on:close={() => (guestAuthOverlayOpen = false)} />
        <div class="mobile-nav compact-only" class:open={fabOpen}>
            {#if fabOpen}
                <button class="drawer-scrim" type="button" aria-label="Cerrar menú" onclick={() => (fabOpen = false)}></button>
            {/if}
            <aside class="drawer-panel" aria-hidden={!fabOpen} aria-label="Navegación">
                <header class="drawer-head">
                    <div class="brand drawer-brand">
                        <img src="/alejoicon_clean.svg" alt="" class="brand-logo" />
                        <div class="brand-meta">
                            <h2>Taller Alejo</h2>
                            <p class="app-version">v{APP_VERSION}</p>
                        </div>
                    </div>
                    <button type="button" class="drawer-close" onclick={() => (fabOpen = false)} aria-label="Cerrar"><Icon icon={closeIcon} /></button>
                </header>
                <nav class="drawer-rail">
                    {#each navItems as item}
                        <button type="button" class="drawer-item" class:is-active={isItemActive(item.path)} onclick={() => go(item.path)}>
                            <Icon icon={item.icon} /><span>{item.label}</span>
                            {#if item.badge > 0}<span class="drawer-badge">{item.badge}</span>{/if}
                        </button>
                    {/each}
                </nav>
                <div class="drawer-footer">
                    <div class="user-chip">
                        {#if isGuestSession}
                            <span class="user-avatar guest" aria-hidden="true"><Icon icon={personIcon} /></span>
                            <div class="user-chip-meta"><p class="user-chip-name">Invitado</p></div>
                        {:else if navAvatarUrl}
                            <img class="user-avatar" src={navAvatarUrl} alt="" />
                            <div class="user-chip-meta"><p class="user-chip-name">{navDisplayName}</p><p class="user-chip-role">{navRoleLabel}</p></div>
                        {:else}
                            <span class="user-avatar initials" aria-hidden="true">{userInitials(navDisplayName)}</span>
                            <div class="user-chip-meta"><p class="user-chip-name">{navDisplayName}</p><p class="user-chip-role">{navRoleLabel}</p></div>
                        {/if}
                    </div>
                    <button type="button" class="logout-native" onclick={logout}><Icon icon={logoutIcon} /><span>{logoutLabel}</span></button>
                </div>
            </aside>
            <button type="button" class="hamburger-fab" aria-label={fabOpen ? "Cerrar menú" : "Abrir menú"} aria-expanded={fabOpen} onclick={() => (fabOpen = !fabOpen)}>
                {#if !isGuestSession && cartCount > 0 && !fabOpen}<span class="main-badge">{cartCount}</span>{/if}
                <Icon icon={fabOpen ? closeIcon : menuIcon} />
            </button>
        </div>
        <div class="promo-float-host compact-only"><PromotionChrome variant="float" /></div>
    </main>
</section>

<style>
.nested-shell{height:100dvh;display:grid;grid-template-columns:300px minmax(0,1fr);overflow:hidden;background:var(--md-sys-color-background)}
.panel-shell{height:100%;padding:14px 0 14px 14px;min-width:0}
.panel-card{height:100%;display:grid;grid-template-rows:auto auto minmax(0,1fr) auto;border-radius:28px;overflow:hidden;border:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 55%,transparent);background:color-mix(in srgb,var(--md-sys-color-surface-container) 88%,transparent);box-shadow:0 8px 28px rgba(0,0,0,.12)}
.panel-head{padding:20px 18px 12px;border-bottom:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent)}
.brand{display:flex;align-items:center;gap:14px}
.brand-logo{width:52px;height:52px;padding:7px;border-radius:16px;background:var(--md-sys-color-surface-container-highest);box-shadow:0 4px 14px rgba(0,0,0,.14);flex-shrink:0;object-fit:contain}
.brand-meta h2{margin:0;font-size:1.28rem;font-weight:800;letter-spacing:-.03em;line-height:1.15;color:var(--md-sys-color-on-surface)}
.brand-meta .app-version{margin:4px 0 0;font-size:.72rem;font-weight:500;letter-spacing:.04em;text-transform:uppercase;color:var(--md-sys-color-on-surface-variant);opacity:.85}
.panel-promo{padding:10px 16px 8px;flex-shrink:0}
.rail-wrap{min-height:0;overflow:hidden;padding:6px 10px 10px;position:relative}
.rail-wrap :global(.m3-container){width:100%}
.rail-wrap :global(.rail){width:100%;height:100%;padding:4px 0 12px;gap:4px;background:transparent!important}
.rail-wrap :global(.item){border-radius:14px;background:transparent!important;transition:background .18s ease,color .18s ease;margin:0 2px}
.rail-wrap :global(.item:hover){background:color-mix(in srgb,var(--md-sys-color-on-surface) 6%,transparent)!important}
.rail-wrap :global(.item.active){background:color-mix(in srgb,var(--md-sys-color-primary) 18%,transparent)!important;color:var(--md-sys-color-primary)!important;font-weight:700;box-shadow:inset 3px 0 0 0 var(--md-sys-color-primary)}
.rail-item-wrap{position:relative;overflow:visible}
.rail-item-wrap.is-active :global(.item),.rail-item-wrap.is-active :global(button),.rail-item-wrap.is-active :global([role="button"]){background:color-mix(in srgb,var(--md-sys-color-primary) 22%,transparent)!important;color:var(--md-sys-color-primary)!important;font-weight:700!important;border-radius:14px}
.rail-item-wrap.is-active::before{content:"";position:absolute;left:2px;top:8px;bottom:8px;width:3px;border-radius:999px;background:var(--md-sys-color-primary);z-index:2;pointer-events:none}
.rail-badge{position:absolute;top:6px;right:10px;min-width:20px;height:20px;padding:0 5px;border-radius:999px;display:flex;align-items:center;justify-content:center;background:#d92d20;color:#fff;font-size:.7rem;font-weight:800;pointer-events:none;border:2px solid color-mix(in srgb,var(--md-sys-color-surface-container) 90%,transparent);z-index:5}
.panel-footer{padding:10px 14px 14px;display:grid;gap:10px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 45%,transparent);background:color-mix(in srgb,var(--md-sys-color-surface-container-high) 55%,transparent)}
.user-chip{display:flex;align-items:center;gap:12px;padding:6px 4px;min-width:0}
.user-avatar{width:42px;height:42px;border-radius:14px;object-fit:cover;flex-shrink:0;display:grid;place-items:center;font-size:.85rem;font-weight:700;background:var(--md-sys-color-primary-container);color:var(--md-sys-color-on-primary-container)}
.user-avatar.guest,.user-avatar.placeholder{background:color-mix(in srgb,var(--md-sys-color-surface-container-highest) 90%,transparent);color:var(--md-sys-color-on-surface-variant)}
.user-chip-meta{min-width:0;flex:1}
.user-chip-name{margin:0;font-size:.92rem;font-weight:650;color:var(--md-sys-color-on-surface);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.user-chip-role{margin:2px 0 0;font-size:.72rem;font-weight:500;color:var(--md-sys-color-on-surface-variant);opacity:.9}
.panel-footer :global(button){width:100%;border-radius:14px;font-weight:650}
.logout-native{width:100%;height:34px;min-height:34px;max-height:34px;display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:0 12px;border-radius:10px;border:1px solid color-mix(in srgb,#c62828 28%,transparent);background:color-mix(in srgb,#ef5350 10%,transparent);color:#ef9a9a;font:inherit;font-size:.72rem;font-weight:600;letter-spacing:.01em;line-height:1;cursor:pointer;transition:background .15s ease,border-color .15s ease,color .15s ease;box-sizing:border-box}
.logout-native:hover{background:color-mix(in srgb,#ef5350 20%,transparent);border-color:color-mix(in srgb,#c62828 42%,transparent);color:#ffcdd2}
.logout-native :global(svg){width:16px;height:16px;flex-shrink:0}
.content{min-height:0;min-width:0;display:grid;grid-template-rows:auto minmax(0,1fr);overflow:hidden}
.route-stage{min-height:0;height:100%;display:grid;overflow:hidden}
.route-stage>:global(*){min-height:0;height:100%;max-height:100%}
.route-stage.route-stage-scroll{overflow-x:hidden;overflow-y:auto;overscroll-behavior:contain;padding-right:4px}
.top-mobile{display:none}.mobile-nav,.promo-float-host{display:none}
@media(max-width:840px){
.nested-shell{grid-template-columns:1fr}
.expanded-only{display:none}
.content{padding:8px 8px max(96px,calc(env(safe-area-inset-bottom)+12px))}
.top-mobile{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:6px}
.compact-only,.promo-float-host,.mobile-nav{display:block}
.drawer-scrim{position:fixed;inset:0;border:0;background:color-mix(in srgb,var(--md-sys-color-scrim,#000) 52%,transparent);z-index:1200;cursor:pointer}
.drawer-panel{position:fixed;top:0;left:0;bottom:0;width:min(300px,86vw);z-index:1201;display:grid;grid-template-rows:auto minmax(0,1fr) auto;background:var(--md-sys-color-surface-container,#1c1c1c);border-right:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 50%,transparent);box-shadow:12px 0 40px rgba(0,0,0,.35);transform:translateX(-105%);transition:transform .22s ease;padding:env(safe-area-inset-top,0) 0 env(safe-area-inset-bottom,0)}
.mobile-nav.open .drawer-panel{transform:translateX(0)}
.drawer-head{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:16px 14px 12px;border-bottom:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent)}
.drawer-close{width:40px;height:40px;border:0;border-radius:12px;background:color-mix(in srgb,var(--md-sys-color-on-surface) 8%,transparent);color:var(--md-sys-color-on-surface);display:grid;place-items:center;cursor:pointer}
.drawer-rail{overflow-y:auto;padding:10px;display:grid;gap:4px;align-content:start}
.drawer-item{display:flex;align-items:center;gap:12px;width:100%;min-height:48px;padding:0 14px;border:0;border-radius:14px;background:transparent;color:var(--md-sys-color-on-surface);font:inherit;font-size:.95rem;font-weight:600;cursor:pointer;text-align:left}
.drawer-item:hover{background:color-mix(in srgb,var(--md-sys-color-on-surface) 6%,transparent)}
.drawer-item.is-active{background:color-mix(in srgb,var(--md-sys-color-primary) 20%,transparent);color:var(--md-sys-color-primary);font-weight:700;box-shadow:inset 3px 0 0 0 var(--md-sys-color-primary)}
.drawer-badge{margin-left:auto;min-width:22px;height:22px;padding:0 6px;border-radius:999px;background:#d92d20;color:#fff;font-size:.72rem;font-weight:800;display:grid;place-items:center}
.drawer-footer{padding:12px 14px 16px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent);display:grid;gap:12px}
.hamburger-fab{position:fixed;right:max(16px,env(safe-area-inset-right));bottom:max(24px,calc(env(safe-area-inset-bottom)+20px));z-index:1190;width:56px;height:56px;border:0;border-radius:16px;background:var(--md-sys-color-primary-container,#2e7d32);color:var(--md-sys-color-on-primary-container,#fff);display:grid;place-items:center;cursor:pointer;box-shadow:0 8px 24px rgba(0,0,0,.28)}
.hamburger-fab .main-badge{position:absolute;top:-4px;right:-4px;min-width:22px;height:22px;border-radius:999px;background:#d92d20;color:#fff;font-size:.7rem;font-weight:800;display:grid;place-items:center;border:2px solid var(--md-sys-color-surface,#121)}
}
.rail-wrap :global(.rail::-webkit-scrollbar),.route-stage::-webkit-scrollbar{width:8px}
.rail-wrap :global(.rail::-webkit-scrollbar-thumb),.route-stage::-webkit-scrollbar-thumb{background:color-mix(in srgb,var(--md-sys-color-outline) 28%,transparent);border-radius:999px;border:2px solid transparent;background-clip:padding-box}
.rail-wrap :global(.rail::-webkit-scrollbar-track),.route-stage::-webkit-scrollbar-track{background:transparent}
</style>
