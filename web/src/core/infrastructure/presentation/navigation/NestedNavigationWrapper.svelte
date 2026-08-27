<script lang="ts">
    import {onDestroy, onMount} from "svelte";
    import { get } from "svelte/store";
    import {fade} from "svelte/transition";
    import type {NavBackStackEntry} from "../../../../lib/navigation/NavBackStackEntry";
    import type {NavController} from "../../../../lib/navigation/NavController";
    import {NavigationRail, NavigationRailItem, Icon, Button} from "m3-svelte";
    import homeIcon from "@ktibow/iconset-material-symbols/home-rounded";
    import inventoryIcon from "@ktibow/iconset-material-symbols/inventory-2-rounded";
    import shoppingCartIcon from "@ktibow/iconset-material-symbols/shopping-cart-rounded";
    import personIcon from "@ktibow/iconset-material-symbols/person-rounded";
    import supportIcon from "@ktibow/iconset-material-symbols/support-agent-rounded";
    import smartToyIcon from "@ktibow/iconset-material-symbols/smart-toy-rounded";
    import logoutIcon from "@ktibow/iconset-material-symbols/logout-rounded";
    import menuIcon from "@ktibow/iconset-material-symbols/menu-rounded";
    import closeIcon from "@ktibow/iconset-material-symbols/close-rounded";
    import appLogo from "../../../../assets/app-logo.webp";
    import {sessionStore} from "../../../feature/auth/presentation/viewmodel/session.store";
    import {profileStore} from "../../../feature/auth/presentation/viewmodel/profile.store";
    import {authContainer} from "../../../feature/auth/di/auth.container";
    import {cartStore} from "../../../feature/sale/presentation/viewmodel/cart.store";
    import {isGuestSession as checkGuest} from "../../../feature/auth/presentation/util/gest-session";
    import InternalHomeScreen from "../routes/InternalHomeScreen.svelte";
    import ProductScreen from "../../../feature/product/presentation/screens/ProductScreen.svelte";
    import InternalBuyScreen from "../routes/InternalBuyScreen.svelte";
    import InternalProfileScreen from "../routes/InternalProfileScreen.svelte";
    import SupportInbox from "../../../feature/support/presentation/routes/SupportInbox.svelte";
    import AgentChat from "../../../feature/agent/presentation/routes/AgentChat.svelte";
    import InternalReservationScreen from "../routes/InternalReservationScreen.svelte";
    import InternalReservationDetailScreen from "../routes/InternalReservationDetailScreen.svelte";
    import InternalBuyConfirmScreen from "../routes/InternalBuyConfirmScreen.svelte";
    import ProductDetailScreen from "../../../feature/product/presentation/screens/ProductDetailScreen.svelte";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry;

    const APP_VERSION = "1.0.0";

    let fabOpen = false;
    let currentPath = "/home";
    let resolvedUser: any = null;

    $: session = $sessionStore;
    $: profileDraft = $profileStore;
    $: cart = $cartStore;
    $: isGuestSession = checkGuest(resolvedUser ?? session?.user);

    function resolveAvatarUrl(user: any): string {
        if (!user) return "";
        const prefs = user.prefs ?? user;
        for (const k of ["avatarUrl", "photo_url", "photoUrl", "avatar", "picture"]) {
            const v = prefs?.[k] ?? user?.[k];
            if (typeof v === "string" && v.trim()) return v.trim();
        }
        return "";
    }

    $: navAvatarUrl = isGuestSession ? "" : (profileDraft.avatarUrl?.trim() || resolveAvatarUrl(resolvedUser) || "");
    $: navDisplayName = isGuestSession
        ? "Invitado"
        : (profileDraft.name?.trim() || resolvedUser?.name || session?.user?.name || "Usuario");
    $: navRoleLabel = isGuestSession
        ? ""
        : (typeof resolvedUser?.prefs?.role === "string"
            ? resolvedUser.prefs.role
            : (Array.isArray((resolvedUser as any)?.labels) && (resolvedUser as any).labels[0]
                ? (resolvedUser as any).labels[0]
                : (profileDraft as any)?.role || ""));
    $: logoutLabel = isGuestSession ? "Salir" : "Cerrar sesión";

    const navItems = [
        { path: "/home", label: "Inicio", icon: homeIcon, badge: 0 },
        { path: "/products", label: "Productos", icon: inventoryIcon, badge: 0 },
        { path: "/cart", label: "Carrito", icon: shoppingCartIcon, badge: 0 },
        { path: "/profile", label: "Perfil", icon: personIcon, badge: 0 },
        { path: "/support", label: "Soporte", icon: supportIcon, badge: 0 },
        { path: "/agent", label: "Asistente", icon: smartToyIcon, badge: 0 },
    ];

    $: cartCount = cart?.items?.reduce((n: number, i: any) => n + (i.quantity || 0), 0) || 0;
    $: itemsWithBadge = navItems.map((item) =>
        item.path === "/cart" ? { ...item, badge: cartCount } : item
    );

    function isItemActive(path: string): boolean {
        if (!currentPath) return false;
        if (path === "/home") return currentPath === "/home" || currentPath === "/";
        return currentPath === path || currentPath.startsWith(path + "/");
    }

    const routeUsesStageScroll = (path: string) => {
        if (!path) return true;
        if (path === "/agent" || path.startsWith("/agent/")) return false;
        return true;
    };

    function go(path: string) {
        fabOpen = false;
        currentPath = path;
        navController.navigate(path);
    }

    async function logout() {
        try {
            await authContainer.useCases.sessions.close();
            sessionStore.clear();
            profileStore.reset({
                userId: "",
                email: "",
                name: "",
                phone: "",
                bio: "",
                avatarUrl: "",
            });
            navController.navigate("/login");
        } catch (e) {
            console.error(e);
        }
    }

    async function hydrateUser() {
        try {
            const user = await sessionStore.getCurrentUser();
            resolvedUser = user;
            if (user && !checkGuest(user)) {
                profileStore.hydrateFromUser(user);
            }
        } catch {
            resolvedUser = session?.user ?? null;
        }
    }

    onMount(() => {
        currentPath = navBackStackEntry?.destination?.route ?? "/home";
        void hydrateUser();
        const unsub = navController.subscribe?.((entry: any) => {
            currentPath = entry?.destination?.route ?? currentPath;
        });
        return () => {
            if (typeof unsub === "function") unsub();
        };
    });

    onDestroy(() => {
        fabOpen = false;
    });
</script>

<div class="nested-shell">
    <aside class="rail-wrap" aria-label="Navegación principal">
        <div class="brand">
            <img src={appLogo} alt="AlejoTaller" class="brand-logo" />
            <div class="brand-text">
                <span class="brand-title">AlejoTaller</span>
                <span class="brand-version">v{APP_VERSION}</span>
            </div>
        </div>

        <nav class="rail">
            {#each itemsWithBadge as item (item.path)}
                <div class="rail-item-wrap" class:is-active={isItemActive(item.path)}>
                    <NavigationRailItem
                        icon={item.icon}
                        label={item.label}
                        selected={isItemActive(item.path)}
                        onclick={() => go(item.path)}
                    />
                    {#if item.badge > 0}
                        <span class="rail-badge">{item.badge}</span>
                    {/if}
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
            <button type="button" class="logout-native" onclick={logout}>
                <Icon icon={logoutIcon} />
                <span>{logoutLabel}</span>
            </button>
        </div>
    </aside>

    <div class="content">
        <div class="route-stage" class:route-stage-scroll={routeUsesStageScroll(currentPath)} in:fade={{ duration: 180 }} out:fade={{ duration: 120 }}>
            {#if currentPath === "/home" || currentPath === "/"}
                <InternalHomeScreen {navController} {navBackStackEntry} />
            {:else if currentPath === "/products" || currentPath.startsWith("/products/")}
                <ProductScreen {navController} {navBackStackEntry} />
            {:else if currentPath === "/cart"}
                <InternalBuyScreen {navController} {navBackStackEntry} />
            {:else if currentPath === "/profile"}
                <InternalProfileScreen {navController} {navBackStackEntry} />
            {:else if currentPath === "/support" || currentPath.startsWith("/support/")}
                <SupportInbox {navController} {navBackStackEntry} />
            {:else if currentPath === "/agent" || currentPath.startsWith("/agent/")}
                <AgentChat {navController} />
            {:else if currentPath === "/reservations"}
                <InternalReservationScreen {navController} {navBackStackEntry} />
            {:else if currentPath.startsWith("/reservations/")}
                <InternalReservationDetailScreen {navController} {navBackStackEntry} />
            {:else if currentPath === "/buy/confirm"}
                <InternalBuyConfirmScreen {navController} {navBackStackEntry} />
            {:else if currentPath.startsWith("/product/")}
                <ProductDetailScreen {navController} {navBackStackEntry} />
            {:else}
                <InternalHomeScreen {navController} {navBackStackEntry} />
            {/if}
        </div>
    </div>

    {#if fabOpen}
        <button class="drawer-scrim" type="button" aria-label="Cerrar menú" onclick={() => (fabOpen = false)}></button>
        <aside class="drawer-panel" aria-hidden={!fabOpen} aria-label="Navegación">
            <header class="drawer-head">
                <div class="brand drawer-brand">
                    <img src={appLogo} alt="" class="brand-logo" />
                    <div class="brand-text">
                        <span class="brand-title">AlejoTaller</span>
                        <span class="brand-version">v{APP_VERSION}</span>
                    </div>
                </div>
                <button type="button" class="drawer-close" onclick={() => (fabOpen = false)} aria-label="Cerrar"><Icon icon={closeIcon} /></button>
            </header>
            <nav class="drawer-rail">
                {#each itemsWithBadge as item (item.path)}
                    <button type="button" class="drawer-item" class:is-active={isItemActive(item.path)} onclick={() => go(item.path)}>
                        <Icon icon={item.icon} />
                        <span>{item.label}</span>
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
                        <div class="user-chip-meta"><p class="user-chip-name">{navDisplayName}</p>{#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}</div>
                    {:else}
                        <div class="user-avatar">{navDisplayName?.slice(0, 1).toUpperCase() || "U"}</div>
                        <div class="user-chip-meta"><p class="user-chip-name">{navDisplayName}</p>{#if navRoleLabel}<p class="user-chip-role">{navRoleLabel}</p>{/if}</div>
                    {/if}
                </div>
                <button type="button" class="logout-native" onclick={logout}><Icon icon={logoutIcon} /><span>{logoutLabel}</span></button>
            </div>
        </aside>
    {/if}

    <button type="button" class="hamburger-fab" aria-label={fabOpen ? "Cerrar menú" : "Abrir menú"} aria-expanded={fabOpen} onclick={() => (fabOpen = !fabOpen)}>
        <Icon icon={fabOpen ? closeIcon : menuIcon} />
    </button>
</div>

<style>
.nested-shell{display:grid;grid-template-columns:88px minmax(0,1fr);height:100%;min-height:0;overflow:hidden;background:var(--md-sys-color-surface)}
.rail-wrap{display:flex;flex-direction:column;min-height:0;border-right:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 55%,transparent);background:var(--md-sys-color-surface-container-low);padding:10px 8px 12px;gap:8px}
.brand{display:flex;align-items:center;gap:8px;padding:4px 4px 8px;flex-shrink:0}
.brand-logo{width:36px;height:36px;border-radius:10px;object-fit:cover;flex-shrink:0}
.brand-text{display:flex;flex-direction:column;min-width:0;line-height:1.15}
.brand-title{font-size:.95rem;font-weight:700;letter-spacing:-.02em;color:var(--md-sys-color-on-surface);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.brand-version{font-size:.65rem;color:var(--md-sys-color-on-surface-variant);opacity:.85}
.rail{flex:1;min-height:0;overflow-y:auto;display:flex;flex-direction:column;gap:2px;padding:4px 0}
.rail-item-wrap{position:relative}
.rail-item-wrap :global(.item),.rail-item-wrap :global(button),.rail-item-wrap :global([role="button"]){background:transparent!important;border-radius:14px!important;min-height:48px}
.rail-item-wrap.is-active :global(.item),.rail-item-wrap.is-active :global(button),.rail-item-wrap.is-active :global([role="button"]){background:color-mix(in srgb,var(--md-sys-color-primary) 22%,transparent)!important;color:var(--md-sys-color-primary)!important;font-weight:700!important;border-radius:14px}
.rail-item-wrap.is-active::before{content:"";position:absolute;left:2px;top:8px;bottom:8px;width:3px;border-radius:999px;background:var(--md-sys-color-primary);z-index:2;pointer-events:none}
.rail-badge{position:absolute;top:4px;right:6px;min-width:18px;height:18px;padding:0 5px;border-radius:999px;background:var(--md-sys-color-error);color:var(--md-sys-color-on-error);font-size:.65rem;font-weight:700;display:grid;place-items:center;z-index:3;pointer-events:none}
.panel-footer{flex-shrink:0;display:flex;flex-direction:column;gap:8px;padding-top:8px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent)}
.user-chip{display:flex;align-items:center;gap:8px;min-width:0}
.user-avatar{width:36px;height:36px;border-radius:12px;object-fit:cover;background:var(--md-sys-color-primary-container);color:var(--md-sys-color-on-primary-container);display:grid;place-items:center;font-size:.85rem;font-weight:700;flex-shrink:0}
.user-avatar.guest{opacity:.7}
.user-chip-meta{min-width:0;flex:1}
.user-chip-name{margin:0;font-size:.78rem;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;color:var(--md-sys-color-on-surface)}
.user-chip-role{margin:1px 0 0;font-size:.65rem;color:var(--md-sys-color-on-surface-variant);white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.logout-native{width:100%;height:34px;min-height:34px;max-height:34px;display:inline-flex;align-items:center;justify-content:center;gap:6px;padding:0 12px;border-radius:10px;border:1px solid color-mix(in srgb,#c62828 28%,transparent);background:color-mix(in srgb,#ef5350 10%,transparent);color:#ef9a9a;font:inherit;font-size:.72rem;font-weight:600;letter-spacing:.01em;line-height:1;cursor:pointer;transition:background .15s ease,border-color .15s ease,color .15s ease;box-sizing:border-box}
.logout-native:hover{background:color-mix(in srgb,#ef5350 20%,transparent);border-color:color-mix(in srgb,#c62828 42%,transparent);color:#ffcdd2}
.logout-native :global(svg){width:16px;height:16px;flex-shrink:0}
.content{min-height:0;min-width:0;display:grid;grid-template-rows:auto minmax(0,1fr);overflow:hidden}
.route-stage{min-height:0;height:100%;display:grid;overflow:hidden}
.route-stage-scroll{overflow:auto}
.hamburger-fab{display:none;position:fixed;bottom:max(16px,env(safe-area-inset-bottom));left:16px;z-index:40;width:48px;height:48px;border-radius:14px;border:0;background:var(--md-sys-color-primary);color:var(--md-sys-color-on-primary);box-shadow:0 4px 14px color-mix(in srgb,var(--md-sys-color-primary) 35%,transparent);align-items:center;justify-content:center;cursor:pointer}
.drawer-scrim{position:fixed;inset:0;z-index:50;background:rgba(0,0,0,.4);border:0;cursor:pointer}
.drawer-panel{position:fixed;top:0;left:0;bottom:0;z-index:51;width:min(280px,86vw);background:var(--md-sys-color-surface-container);display:flex;flex-direction:column;padding:12px;gap:8px;box-shadow:4px 0 24px rgba(0,0,0,.2)}
.drawer-head{display:flex;align-items:center;justify-content:space-between;gap:8px}
.drawer-close{border:0;background:transparent;color:var(--md-sys-color-on-surface);cursor:pointer;padding:6px;border-radius:10px}
.drawer-rail{flex:1;overflow-y:auto;display:flex;flex-direction:column;gap:4px}
.drawer-item{display:flex;align-items:center;gap:12px;padding:12px 14px;border:0;border-radius:12px;background:transparent;color:var(--md-sys-color-on-surface);font:inherit;font-weight:500;cursor:pointer;text-align:left;position:relative}
.drawer-item.is-active{background:color-mix(in srgb,var(--md-sys-color-primary) 18%,transparent);color:var(--md-sys-color-primary);font-weight:700}
.drawer-badge{margin-left:auto;min-width:20px;height:20px;padding:0 6px;border-radius:999px;background:var(--md-sys-color-error);color:var(--md-sys-color-on-error);font-size:.7rem;font-weight:700;display:grid;place-items:center}
.drawer-footer{padding-top:8px;border-top:1px solid color-mix(in srgb,var(--md-sys-color-outline-variant) 40%,transparent);display:flex;flex-direction:column;gap:8px}
@media (max-width:900px){.nested-shell{grid-template-columns:1fr}.rail-wrap{display:none}.hamburger-fab{display:inline-flex}}
.rail-wrap :global(.rail::-webkit-scrollbar),.route-stage::-webkit-scrollbar{width:8px}
.rail-wrap :global(.rail::-webkit-scrollbar-thumb),.route-stage::-webkit-scrollbar-thumb{background:color-mix(in srgb,var(--md-sys-color-outline) 28%,transparent);border-radius:999px;border:2px solid transparent;background-clip:padding-box}
.rail-wrap :global(.rail::-webkit-scrollbar-track),.route-stage::-webkit-scrollbar-track{background:transparent}
</style>
