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
    const APP_VERSION = (import.meta as any).env?.VITE_APP_VERSION ?? "0.3.0";
    let currentUser: Promise<any> | null = null;
    let resolvedUser: { name?: string; prefs?: Record<string, unknown> } | null = null;

    function resolveAvatarUrl(user: any): string {
        if (!user?.prefs) return "";
        const pr = user.prefs as Record<string, unknown>;
        for (const k of ["avatarUrl", "photo_url", "photoUrl", "avatar"]) {
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

    let stopSupportRt: (() => void) | null = null;
    let fabOpen = false;
    let suppressHashSync = false;
    let hashSyncReady = false;
    let adminChoicePending = false;
    let adminRedirecting = false;
    let guestAuthOverlayOpen = false;

    $: {
        if (typeof document !== "undefined") {
            document.body.style.overflow = fabOpen ? "hidden" : "";
        }
    }

    function clearSessionBoundState({ clearCart = false }: { clearCart?: boolean } = {}) {
        stopSupportRt?.();
        stopSupportRt = null;
        supportInboxStore.stopRealtime();
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

    function startSupportBadgePipeline() {
        supportInboxStore.syncMine().catch(() => {});
        if (!stopSupportRt) {
            stopSupportRt = supportInboxStore.startRealtime();
        }
    }

    function forceVisitorMode(user?: any) {
        sessionStore.setGuestSession();
        authFlowStore.setSuccess({
            userId: resolveUserId(user),
            email: null,
            provider: "guest"
        });
    }

    function handleSaleVerificationOpen(event: Event) {
        if (isGuestSession) return;
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
                : targetRoute === supportDetail.path && (parsed.args?.id || parsed.args?.threadId)
                ? { id: parsed.args.id ?? parsed.args.threadId }
                : undefined;
        const currentArgs = currentEntry?.args as Record<string, string> | undefined;

        if (import.meta.env.DEV && targetRoute === productDetail.path) {
            logProductFlow(parsed.args?.productId || "?", "resolve-start");
        }

        if (currentPath !== targetRoute || JSON.stringify(currentArgs ?? {}) !== JSON.stringify(targetArgs ?? {})) {
            if (import.meta.env.DEV) {
                logNavRoute(targetRoute, targetArgs);
            }
            const stackSize = get(internalStackStore).length;
            const isColdBootDeepLink = stackSize <= 1 && currentPath === dashboard.path;
            if (isColdBootDeepLink && targetRoute !== dashboard.path) {
                internalNavController.navigate(targetRoute, targetArgs);
            } else {
                internalNavController.resetTo(targetRoute, targetArgs);
            }
        }
    }

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

    async function logout() {
        try {
            await authContainer.useCases.sessions.closeSession.execute();
        } finally {
            clearSessionBoundState({ clearCart: true });
            navController.resetTo("login");
        }
    }

    function handleRequestLogin() {
        if (import.meta.env.DEV) {
            logNavAuthCheck(false, false, "redirect-login");
        }
        guestAuthOverlayOpen = false;
        clearSessionBoundState({ clearCart: true });
        navController.resetTo("login");
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
        window.removeEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.addEventListener("hashchange", applyInternalHash);
        window.addEventListener("request-guest-login", handleRequestLogin);

        suppressHashSync = true;
        if (window.location.hash) {
            if (import.meta.env.DEV) {
                logNavRoute("nested-onMount", { hash: window.location.hash });
            }
            applyInternalHash();
        }

        queueMicrotask(() => {
            suppressHashSync = false;
            hashSyncReady = true;
        });

        currentUser = authContainer.useCases.accounts.getCurrentUser()
            .then((user) => {
                if (!hasClearAuthenticatedProfile(user)) {
                    forceVisitorMode(user);
                    if (import.meta.env.DEV) {
                        logNavAuthCheck(false, true, "force-visitor-unclear-profile");
                    }
                    const parsedHash = parseDeepLinkHash(window.location.hash);
                    const hasProductDeeplink = parsedHash?.top === "home" && (
                        parsedHash.nested === productDetail.path || !!parsedHash.args?.productId
                    );
                    if (!hasProductDeeplink) {
                        internalNavController.resetTo(dashboard.path);
                    }
                    resolvedUser = { name: "Visitante" };
                    return { name: "Visitante", email: "" };
                }

                sessionStore.setAuthenticatedSession();
                resolvedUser = user;
                startSupportBadgePipeline();
                promotionStore.syncAll().catch(() => {
                    toastStore.error("Error al sincronizar promociones");
                });
                saleStore.syncAll().catch(() => {
                    toastStore.error("Error al sincronizar reservas");
                });
                if (shouldOfferAdminChoice(user)) {
                    const choice = getStoredAdminChoice();
                    if (choice === "admin") {
                        continueToAdmin();
                    } else if (choice !== "client") {
                        adminChoicePending = true;
                    }
                }
                return user;
            })
            .catch(() => {
                forceVisitorMode();
                resolvedUser = { name: "Visitante" };
                if (import.meta.env.DEV) {
                    logNavAuthCheck(false, true, "force-visitor-no-session");
                }
                return { name: "Visitante", email: "" };
            });

        productStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar productos");
        });
        categoryStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar categorias");
        });

        if (!get(sessionStore).isGuest) {
            promotionStore.syncAll().catch(() => {
                toastStore.error("Error al sincronizar promociones");
            });
            saleStore.syncAll().catch(() => {
                toastStore.error("Error al sincronizar reservas");
            });
            startSupportBadgePipeline();
        }
    });

    onDestroy(() => {
        window.removeEventListener("sale-verification-open", handleSaleVerificationOpen as EventListener);
        window.removeEventListener("hashchange", applyInternalHash);
        window.removeEventListener("request-guest-login", handleRequestLogin);
        stopSupportRt?.();
        stopSupportRt = null;
        supportInboxStore.stopRealtime();
        clearSessionBoundState();
        logger.info("[InternalNavigation] disposed");
    });

    $: if (hashSyncReady && !suppressHashSync && typeof window !== "undefined") {
        const args = currentEntry?.args as Record<string, string> | undefined;
        const nextHash = buildHomeHash(
            currentPath as typeof dashboard.path | typeof buy.path | typeof buyConfirm.path | typeof reservation.path | typeof reservationDetail.path | typeof profile.path | typeof settingsRoute.path | typeof productDetail.path | typeof support.path | typeof supportDetail.path | typeof agent.path,
            currentPath === reservationDetail.path ? { reservationId: args?.id } :
            currentPath === productDetail.path ? { productId: args?.productId } :
            currentPath === supportDetail.path ? { id: args?.id } :
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
                    <img src="/alejoicon_clean.svg" alt="Logo AlejoTaller" class="brand-logo" />
                    <div class="brand-meta">
                        <h2>Taller Alejo</h2>
                        <p class="app-version">v{APP_VERSION}</p>
                    </div>
                </div>
            </header>
            <div class="panel-promo">
                <PromotionChrome variant="nav" />
            </div>
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
                <div class="user-chip">
                    {#if isGuestSession}
                        <span class="user-avatar guest" aria-hidden="true">
                            <Icon icon={personIcon} />
                        </span>
                        <div class="user-chip-meta">
                            <p class="user-chip-name">Invitado</p>
                        </div>
                    {:else}
                        {#await currentUser ?? Promise.resolve({ name: "Usuario" })}
                            <span class="user-avatar placeholder" aria-hidden="true">…</span>
                            <div class="user-chip-meta">
                                <p class="user-chip-name">Cargando…</p>
                            </div>
                        {:then user}
                            {@const avatar = resolveAvatarUrl(user)}
                            {#if avatar}
                                <img class="user-avatar" src={avatar} alt="" />
                            {:else}
                                <span class="user-avatar initials" aria-hidden="true">{userInitials(user.name)}</span>
                            {/if}
                            <div class="user-chip-meta">
                                <p class="user-chip-name">{user.name || "Usuario"}</p>
                                <p class="user-chip-role">Cliente</p>
                            </div>
                        {:catch}
                            <span class="user-avatar guest" aria-hidden="true">
                                <Icon icon={personIcon} />
                            </span>
                            <div class="user-chip-meta">
                                <p class="user-chip-name">Usuario</p>
                            </div>
                        {/await}
                    {/if}
                </div>
                {#if isGuestSession}
                    <Button class="login-btn" variant="filled" size="m" iconType="left" onclick={handleRequestLogin}>
                        <Icon icon={loginIcon} />
                        Iniciar sesión
                    </Button>
                    <Button class="logout-btn" variant="tonal" size="m" iconType="left" onclick={logout}>
                        <Icon icon={logoutIcon} />
                        {logoutLabel}
                    </Button>
                {:else}
                    <Button class="logout-btn" variant="tonal" size="m" iconType="left" onclick={logout}>
                        <Icon icon={logoutIcon} />
                        {logoutLabel}
                    </Button>
                {/if}
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
                    <Icon icon={loginIcon} />
                    Iniciar sesión
                </Button>
            {/if}
        </div>
        {#key currentRouteKey}
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
                        composable(reservationDetail, () => InternalReservationDetailScreen),
                        composable(support, () => SupportInbox),
                        composable(supportDetail, () => SupportDetail),
                        composable(agent, () => AgentChat)
                    ]}
                />
            </div>
        {/key}
        {#if !isGuestSession}
            <SaleVerificationAlert navController={internalNavController} />
        {/if}
        {#if adminChoicePending}
            <AdminRoleChoiceCard
                busy={adminRedirecting}
                on:stayClient={continueAsClient}
                on:goAdmin={continueToAdmin}
            />
        {/if}
        <GuestAuthOverlay
            open={guestAuthOverlayOpen}
            on:login={handleRequestLogin}
            on:close={() => (guestAuthOverlayOpen = false)}
        />
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
                                {logoutLabel}
                            </Button>
                            <button class="fab-mini logout-mini" type="button" on:click={logout} aria-label={logoutLabel}>
                                <Icon icon={logoutIcon} />
                            </button>
                        </div>
                    </div>
                {/if}
                <div class="fab-main-wrap">
                    {#if !isGuestSession && cartCount > 0}
                        <span class="main-badge">{cartCount}</span>
                    {/if}
                    <FAB size="normal" icon={fabOpen ? closeIcon : menuIcon} onclick={() => (fabOpen = !fabOpen)} />
                </div>
            </div>
        </div>
        <div class="promo-float-host compact-only">
            <PromotionChrome variant="float" />
        </div>
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
.rail-wrap :global(.item.active .icon),.rail-wrap :global(.item.active svg){color:var(--md-sys-color-primary)}
.rail-item-wrap{position:relative;overflow:visible}
.rail-badge{position:absolute;top:6px;right:10px;min-width:20px;height:20px;padding:0 5px;border-radius:999px;display:flex;align-items:center;justify-content:center;background:#d92d20;color:#fff;font-size:.7rem;font-weight:800;pointer-events:none;border:2px solid color-mix(in srgb,var(--md-sys-color-surface-container) 90%,transparent);z-index:5}
.panel-footer{padding:12px 14px 16px;display:grid;gap:12px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 45%,transparent);background:color-mix(in srgb,var(--md-sys-color-surface-container-high) 55%,transparent)}
.user-chip{display:flex;align-items:center;gap:12px;padding:6px 4px;min-width:0}
.user-avatar{width:42px;height:42px;border-radius:14px;object-fit:cover;flex-shrink:0;display:grid;place-items:center;font-size:.85rem;font-weight:700;background:var(--md-sys-color-primary-container);color:var(--md-sys-color-on-primary-container)}
.user-avatar.guest,.user-avatar.placeholder{background:color-mix(in srgb,var(--md-sys-color-surface-container-highest) 90%,transparent);color:var(--md-sys-color-on-surface-variant)}
.user-chip-meta{min-width:0;flex:1}
.user-chip-name{margin:0;font-size:.92rem;font-weight:650;color:var(--md-sys-color-on-surface);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.user-chip-role{margin:2px 0 0;font-size:.72rem;font-weight:500;color:var(--md-sys-color-on-surface-variant);opacity:.9}
.panel-footer :global(button){width:100%;border-radius:14px;font-weight:650}
.panel-footer :global(.logout-btn.m3-container),.panel-footer :global(button.logout-btn){padding:14px 18px!important;min-height:48px;background:color-mix(in srgb,var(--md-sys-color-error) 14%,transparent)!important;color:var(--md-sys-color-error)!important;border:1px solid color-mix(in srgb,var(--md-sys-color-error) 35%,transparent)!important}
.panel-footer :global(.logout-btn:hover),.panel-footer :global(button.logout-btn:hover){background:color-mix(in srgb,var(--md-sys-color-error) 22%,transparent)!important}
.panel-footer :global(.login-btn){min-height:48px;padding:14px 18px!important}
.content{min-height:0;min-width:0;display:grid;grid-template-rows:auto minmax(0,1fr);overflow:hidden}
.route-stage{min-height:0;height:100%;display:grid;overflow:hidden}
.route-stage>:global(*){min-height:0;height:100%;max-height:100%}
.route-stage.route-stage-scroll{overflow-x:hidden;overflow-y:auto;overscroll-behavior:contain;padding-right:4px}
.top-mobile{display:none}
.fab-layer,.promo-float-host{display:none}
@media(max-width:840px){
.nested-shell{grid-template-columns:1fr}
.expanded-only{display:none}
.content{padding:8px 8px max(96px,calc(env(safe-area-inset-bottom)+12px))}
.top-mobile{display:flex;align-items:center;justify-content:space-between;gap:12px;margin-bottom:6px}
.compact-only,.promo-float-host{display:block}
.fab-layer{display:block;position:fixed;inset:0;pointer-events:none;z-index:1000}
.fab-scrim{position:fixed;inset:0;border:0;background:color-mix(in srgb,var(--md-sys-color-scrim) 54%,transparent);pointer-events:auto;z-index:1000}
.fab-stack{position:fixed;right:max(16px,env(safe-area-inset-right));bottom:max(24px,calc(env(safe-area-inset-bottom)+24px));display:grid;justify-items:end;gap:14px;pointer-events:none;z-index:1001}
.fab-menu{display:grid;gap:12px;pointer-events:auto;z-index:1002}
.fab-item-row{display:flex;align-items:center;gap:10px}
.fab-mini{position:relative;width:40px;height:40px;border:none;border-radius:999px;display:grid;place-items:center;cursor:pointer;background:var(--md-sys-color-primary-container);color:var(--md-sys-color-on-primary-container)}
.fab-mini.active{background:var(--md-sys-color-primary);color:var(--md-sys-color-on-primary)}
.logout-mini{background:var(--md-sys-color-error-container);color:var(--md-sys-color-on-error-container)}
.mini-badge,.main-badge{position:absolute;top:-6px;right:-6px;width:24px;height:24px;border-radius:999px;display:flex;align-items:center;justify-content:center;background:#d92d20;color:var(--md-sys-color-on-error);font-size:.72rem;font-weight:800;border:2px solid var(--md-sys-color-surface);z-index:5}
.fab-main-wrap{position:relative;pointer-events:auto;z-index:1003}
}
.rail-wrap :global(.rail::-webkit-scrollbar),.route-stage::-webkit-scrollbar{width:8px}
.rail-wrap :global(.rail::-webkit-scrollbar-thumb),.route-stage::-webkit-scrollbar-thumb{background:color-mix(in srgb,var(--md-sys-color-outline) 28%,transparent);border-radius:999px;border:2px solid transparent;background-clip:padding-box}
.rail-wrap :global(.rail::-webkit-scrollbar-track),.route-stage::-webkit-scrollbar-track{background:transparent}
</style>
