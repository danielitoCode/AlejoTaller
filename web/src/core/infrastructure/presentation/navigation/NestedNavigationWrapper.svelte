<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import { get } from "svelte/store";
    import { fade } from "svelte/transition";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import { composable } from "../../../../lib/navigation/composable";
    import { rememberNavController } from "../../../../lib/navigation/rememberNavController";
    import { Button, Icon, NavigationRailItem } from "m3-svelte";
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
    import { authContainer } from "../../../feature/auth/di/auth.container";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import { profileStore } from "../../../feature/auth/presentation/viewmodel/profile.store";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import { cartStore } from "../../../feature/sale/presentation/viewmodel/cart.store";
    import { BuyState } from "../../../feature/sale/domain/entity/enums";
    import InternalProductScreen from "../../../feature/product/presentation/screens/InternalProductScreen.svelte";
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
    import GuestAuthOverlay from "../../../feature/auth/presentation/components/GuestAuthOverlay.svelte";
    import {
        buy, buyConfirm, dashboard, product, productDetail, profile, reservation,
        reservationDetail, settings as settingsRoute, support, supportDetail, agent
    } from "./nested.router";
    import { buildHomeHash, parseDeepLinkHash } from "./deeplink";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string; email?: string; provider?: string }>;

    const internalNavController = rememberNavController(dashboard.path);
    const APP_VERSION = (import.meta as any).env?.VITE_APP_VERSION ?? "0.3.0";
    let resolvedUser: { name?: string; prefs?: Record<string, unknown> } | null = null;
    let fabOpen = false;
    let guestAuthOverlayOpen = false;
    let suppressHashSync = false;

    function resolveAvatarUrl(user: any): string {
        if (!user) return "";
        const sources = [user, user.prefs ?? {}];
        for (const src of sources) {
            for (const k of ["avatarUrl", "photo_url", "photoUrl", "avatar", "picture"]) {
                if (typeof src?.[k] === "string" && src[k].trim()) return src[k].trim();
            }
        }
        return "";
    }

    const items = [
        { label: "Productos", path: dashboard.path, icon: storefrontIcon },
        { label: "Su compra", path: buy.path, icon: shoppingCartIcon },
        { label: "Reservas", path: reservation.path, icon: qrCodeIcon },
        { label: "Soporte", path: support.path, icon: supportIcon },
        { label: "Asistente", path: agent.path, icon: smartToyIcon },
        { label: "Perfil", path: profile.path, icon: personIcon },
        { label: "Ajustes", path: settingsRoute.path, icon: settingsIcon }
    ];

    const internalStackStore = internalNavController._getStackStore();
    $: internalStack = $internalStackStore;
    $: currentEntry = internalStack.at(-1);
    $: currentPath = currentEntry?.route ?? dashboard.path;
    $: currentRouteKey = `${currentPath}:${JSON.stringify(currentEntry?.args ?? {})}`;
    $: routeUsesStageScroll = ![dashboard.path, product.path, productDetail.path, agent.path].includes(currentPath);
    $: cartCount = ($cartStore.items ?? []).reduce((sum: number, item: any) => sum + (item.quantity || 0), 0);
    $: isGuestSession = $sessionStore.isGuest;
    $: pendingSales = isGuestSession ? 0 : ($saleStore.items ?? []).filter((s: any) => s.verified === BuyState.UNVERIFIED).length;
    $: supportUnread = isGuestSession ? 0 : ($supportInboxStore.items ?? []).reduce((acc: number, m: any) => acc + (m.unreadUser ?? 0), 0);
    $: visibleItems = isGuestSession
        ? items.filter((item) => item.path === dashboard.path || item.path === agent.path)
        : items;
    $: navItems = visibleItems.map((item) => ({
        ...item,
        badge: item.path === buy.path ? cartCount
            : item.path === reservation.path ? pendingSales
            : item.path === support.path ? supportUnread
            : 0
    }));
    $: logoutLabel = isGuestSession ? "Salir" : "Cerrar sesión";
    $: profileDraft = $profileStore;
    $: navDisplayName = isGuestSession ? "Invitado" : (profileDraft.name?.trim() || resolvedUser?.name || "Usuario");
    $: navAvatarUrl = isGuestSession ? "" : (profileDraft.avatarUrl?.trim() || resolveAvatarUrl(resolvedUser) || "");
    $: navRoleLabel = isGuestSession ? "" : "Cliente";

    function isItemActive(path: string): boolean {
        return currentPath === path;
    }

    function go(path: string) {
        if (isGuestSession && path !== dashboard.path && path !== product.path && path !== productDetail.path && path !== agent.path) {
            guestAuthOverlayOpen = true;
            fabOpen = false;
            return;
        }
        fabOpen = false;
        if (currentPath !== path) internalNavController.navigate(path);
    }

    async function logout() {
        try {
            if (!isGuestSession) {
                await authContainer.useCases.sessions.close();
            }
        } catch (e) {
            console.error(e);
        }
        sessionStore.reset();
        profileStore.reset({ userId: "", email: "", name: "", phone: "", bio: "", avatarUrl: "" });
        navController.navigate("/login");
    }

    function handleRequestLogin() {
        guestAuthOverlayOpen = false;
        navController.navigate("/login");
    }

    async function hydrate() {
        try {
            if ($sessionStore.isGuest) {
                resolvedUser = null;
                return;
            }
            const user = await sessionStore.getCurrentUser();
            resolvedUser = user as any;
            if (user) profileStore.hydrateFromUser(user as any);
        } catch {
            resolvedUser = null;
        }
    }

    function syncHashFromStack() {
        if (suppressHashSync) return;
        const args = (currentEntry?.args ?? {}) as Record<string, string>;
        const nextHash = buildHomeHash(currentPath as any, args);
        if (typeof window !== "undefined" && window.location.hash !== nextHash) {
            window.history.replaceState({}, "", nextHash);
        }
    }

    function applyNestedFromHash() {
        if (typeof window === "undefined") return;
        const parsed = parseDeepLinkHash(window.location.hash);
        if (!parsed || parsed.top !== "home") return;
        const target = parsed.nested ?? dashboard.path;
        if (currentPath !== target) {
            suppressHashSync = true;
            internalNavController.navigate(target, parsed.args);
            queueMicrotask(() => { suppressHashSync = false; });
        }
    }

    onMount(() => {
        void hydrate();
        applyNestedFromHash();
        const onHash = () => applyNestedFromHash();
        window.addEventListener("hashchange", onHash);
        return () => window.removeEventListener("hashchange", onHash);
    });

    $: if (currentPath) syncHashFromStack();

    onDestroy(() => { fabOpen = false; });
</script>

<div class="nested-shell">
    <aside class="rail-wrap" aria-label="Navegación principal">
        <div class="brand">
            <img src="/alejoicon_clean.svg" alt="AlejoTaller" class="brand-logo" />
            <div class="brand-meta">
                <h2>Taller Alejo</h2>
                <p class="app-version">v{APP_VERSION}</p>
            </div>
        </div>
        <nav class="rail">
            {#each navItems as item (item.path)}
                <div class="rail-item-wrap" class:is-active={isItemActive(item.path)}>
                    <NavigationRailItem
                        icon={item.icon}
                        label={item.label}
                        selected={isItemActive(item.path)}
                        onclick={() => go(item.path)}
                    />
                    {#if item.badge > 0}<span class="rail-badge">{item.badge}</span>{/if}
                </div>
            {/each}
        </nav>
        <div class="panel-footer">
            <div class="user-chip">
                {#if isGuestSession}
                    <div class="user-avatar guest">?</div>
                    <div class="user-chip-meta"><p class="user-chip-name">Invitado</p></div>
                {:else if navAvatarUrl}
                    <img class="user-avatar" src={navAvatarUrl} alt="" />
                    <div class="user-chip-meta">
                        <p class="user-chip-name">{navDisplayName}</p>
                        {#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}
                    </div>
                {:else}
                    <div class="user-avatar">{navDisplayName?.slice(0, 1).toUpperCase() || "U"}</div>
                    <div class="user-chip-meta">
                        <p class="user-chip-name">{navDisplayName}</p>
                        {#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}
                    </div>
                {/if}
            </div>
            {#if isGuestSession}
                <Button variant="filled" size="s" iconType="left" onclick={handleRequestLogin}>
                    <Icon icon={loginIcon} /> Iniciar sesión
                </Button>
            {/if}
            <button type="button" class="logout-native" onclick={logout}>
                <Icon icon={logoutIcon} /><span>{logoutLabel}</span>
            </button>
        </div>
    </aside>

    <main class="content">
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
        {#if !isGuestSession}
            <SaleVerificationAlert navController={internalNavController} />
        {/if}
        <GuestAuthOverlay open={guestAuthOverlayOpen} on:login={handleRequestLogin} on:close={() => (guestAuthOverlayOpen = false)} />
    </main>

    {#if fabOpen}
        <button class="drawer-scrim" type="button" aria-label="Cerrar menú" onclick={() => (fabOpen = false)}></button>
        <aside class="drawer-panel" aria-hidden={!fabOpen} aria-label="Navegación">
            <header class="drawer-head">
                <div class="brand">
                    <img src="/alejoicon_clean.svg" alt="" class="brand-logo" />
                    <div class="brand-meta">
                        <h2>Taller Alejo</h2>
                        <p class="app-version">v{APP_VERSION}</p>
                    </div>
                </div>
                <button type="button" class="drawer-close" onclick={() => (fabOpen = false)} aria-label="Cerrar">
                    <Icon icon={closeIcon} />
                </button>
            </header>
            <nav class="drawer-rail">
                {#each navItems as item (item.path)}
                    <button type="button" class="drawer-item" class:is-active={isItemActive(item.path)} onclick={() => go(item.path)}>
                        <Icon icon={item.icon} /><span>{item.label}</span>
                        {#if item.badge > 0}<span class="drawer-badge">{item.badge}</span>{/if}
                    </button>
                {/each}
            </nav>
            <div class="drawer-footer">
                <div class="user-chip">
                    {#if isGuestSession}
                        <div class="user-avatar guest">?</div>
                        <div class="user-chip-meta"><p class="user-chip-name">Invitado</p></div>
                    {:else if navAvatarUrl}
                        <img class="user-avatar" src={navAvatarUrl} alt="" />
                        <div class="user-chip-meta">
                            <p class="user-chip-name">{navDisplayName}</p>
                            {#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}
                        </div>
                    {:else}
                        <div class="user-avatar">{navDisplayName?.slice(0, 1).toUpperCase() || "U"}</div>
                        <div class="user-chip-meta">
                            <p class="user-chip-name">{navDisplayName}</p>
                            {#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}
                        </div>
                    {/if}
                </div>
                <button type="button" class="logout-native" onclick={logout}>
                    <Icon icon={logoutIcon} /><span>{logoutLabel}</span>
                </button>
            </div>
        </aside>
    {/if}

    <button
        type="button"
        class="hamburger-fab"
        aria-label={fabOpen ? "Cerrar menú" : "Abrir menú"}
        aria-expanded={fabOpen}
        onclick={() => (fabOpen = !fabOpen)}
    >
        <Icon icon={fabOpen ? closeIcon : menuIcon} />
    </button>
</div>

<style>
.nested-shell{display:grid;grid-template-columns:88px minmax(0,1fr);height:100%;min-height:0;overflow:hidden;background:var(--md-sys-color-surface)}
.rail-wrap{display:flex;flex-direction:column;min-height:0;border-right:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 55%,transparent);background:var(--md-sys-color-surface-container-low);padding:10px 8px 12px;gap:8px}
.brand{display:flex;align-items:center;gap:8px;padding:4px}.brand-logo{width:36px;height:36px;border-radius:10px;object-fit:cover}
.brand-meta h2{margin:0;font-size:.95rem;font-weight:700}.app-version{margin:0;font-size:.65rem;opacity:.85}
.rail{flex:1;min-height:0;overflow-y:auto;display:flex;flex-direction:column;gap:2px}
.rail-item-wrap{position:relative}
.rail-item-wrap.is-active :global(button),.rail-item-wrap.is-active :global(.item){background:color-mix(in srgb,var(--md-sys-color-primary) 22%,transparent)!important;color:var(--md-sys-color-primary)!important;font-weight:700!important;border-radius:14px}
.rail-item-wrap.is-active::before{content:"";position:absolute;left:2px;top:8px;bottom:8px;width:3px;border-radius:999px;background:var(--md-sys-color-primary)}
.rail-badge,.drawer-badge{min-width:18px;height:18px;padding:0 5px;border-radius:999px;background:var(--md-sys-color-error);color:var(--md-sys-color-on-error);font-size:.65rem;font-weight:700;display:grid;place-items:center}
.panel-footer{flex-shrink:0;display:flex;flex-direction:column;gap:8px;padding-top:8px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent)}
.user-chip{display:flex;align-items:center;gap:8px;min-width:0}
.user-avatar{width:36px;height:36px;border-radius:12px;object-fit:cover;background:var(--md-sys-color-primary-container);display:grid;place-items:center;font-weight:700;flex-shrink:0}
.user-chip-name{margin:0;font-size:.78rem;font-weight:600}.user-chip-role{margin:1px 0 0;font-size:.65rem;opacity:.85}
.logout-native{width:100%;height:34px;display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:0 12px;border-radius:10px;border:1px solid color-mix(in srgb,#c62828 28%,transparent);background:color-mix(in srgb,#ef5350 10%,transparent);color:#ef9a9a;font:inherit;font-size:.72rem;font-weight:600;cursor:pointer}
.content{min-height:0;min-width:0;display:grid;grid-template-rows:minmax(0,1fr);overflow:hidden}
.route-stage{min-height:0;height:100%;display:grid;overflow:hidden}.route-stage-scroll{overflow:auto}
.hamburger-fab{display:none;position:fixed;bottom:max(16px,env(safe-area-inset-bottom));left:16px;z-index:40;width:48px;height:48px;border-radius:14px;border:0;background:var(--md-sys-color-primary);color:var(--md-sys-color-on-primary);align-items:center;justify-content:center;cursor:pointer}
.drawer-scrim{position:fixed;inset:0;z-index:50;background:rgba(0,0,0,.4);border:0}
.drawer-panel{position:fixed;top:0;left:0;bottom:0;z-index:51;width:min(280px,86vw);background:var(--md-sys-color-surface-container);display:flex;flex-direction:column;padding:12px;gap:8px;box-shadow:4px 0 24px rgba(0,0,0,.2)}
.drawer-head{display:flex;align-items:center;justify-content:space-between}.drawer-close{border:0;background:transparent;cursor:pointer;padding:6px}
.drawer-rail{flex:1;overflow-y:auto;display:flex;flex-direction:column;gap:4px}
.drawer-item{display:flex;align-items:center;gap:12px;padding:12px 14px;border:0;border-radius:12px;background:transparent;font:inherit;cursor:pointer;text-align:left}
.drawer-item.is-active{background:color-mix(in srgb,var(--md-sys-color-primary) 18%,transparent);color:var(--md-sys-color-primary);font-weight:700}
.drawer-footer{padding-top:8px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent);display:flex;flex-direction:column;gap:8px}
@media(max-width:900px){.nested-shell{grid-template-columns:1fr}.rail-wrap{display:none}.hamburger-fab{display:inline-flex}}
</style>
