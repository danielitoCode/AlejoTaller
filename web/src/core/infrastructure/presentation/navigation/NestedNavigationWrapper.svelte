<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import { fade } from "svelte/transition";
    import type { NavBackStackEntry } from "../../../../lib/navigation/NavBackStackEntry";
    import type { NavController } from "../../../../lib/navigation/NavController";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import { composable } from "../../../../lib/navigation/composable";
    import { rememberNavController } from "../../../../lib/navigation/rememberNavController";
    import { authContainer } from "../../../feature/auth/di/auth.container";
    import { sessionStore } from "../../../feature/auth/presentation/viewmodel/session.store";
    import CategoryManagement from "../../../feature/category/presentation/routes/CategoryManagement.svelte";
    import ProductManagement from "../../../feature/product/presentation/routes/ProductManagement.svelte";
    import { categoryStore } from "../../../feature/category/presentation/viewmodel/category.store";
    import { productStore } from "../../../feature/product/presentation/viewmodel/product.store";
    import { promotionStore } from "../../../feature/notification/presentation/viewmodel/promotion.store";
    import PromoManagement from "../../../feature/notification/presentation/routes/PromoManagement.svelte";
    import SaleManagement from "../../../feature/sale/presentation/routes/SaleManagement.svelte";
    import { saleStore } from "../../../feature/sale/presentation/viewmodel/sale.store";
    import UserManagement from "../../../feature/auth/presentation/routes/UserManagement.svelte";
    import Icon from "../components/Icon.svelte";
    import DashboardHome from "../routes/DashboardHome.svelte";
    import SettingsManagement from "../routes/SettingsManagement.svelte";
    import ReservationManagement from "../routes/ReservationManagement.svelte";
    import { toastStore } from "../viewmodel/toast.store";
    import { logger } from "../util/logger.service";
    import RealtimeDock from "../components/RealtimeDock.svelte";
    import SupportInbox from "../../../feature/support/presentation/routes/SupportInbox.svelte";
    import { supportInboxStore } from "../../../feature/support/presentation/viewmodel/support-inbox.store";
    import { category, dashboard, product, promo, reservation, sales, settings, support, users } from "./nested.router";
    import { subscribePulseChannelAll } from "../../data/alset-pulse/pulse.realtime";
    import { pulseRefreshTargets } from "../../data/alset-pulse/pulse.refresh-targets";
    import { ENV } from "../../env";
    import SupportDetail from "../../../feature/support/presentation/routes/SupportDetail.svelte";
    import SaleDetail from "../../../feature/sale/presentation/routes/SaleDetail.svelte";
    import { supportDetail, salesDetail } from "./nested.router";
    import { get } from "svelte/store";
    import { BuyState } from "../../../feature/sale/domain/entity/enums";
    import {
        BadgeDollarSign,
        CalendarCheck2,
        Home,
        LogOut,
        Menu,
        MessageSquareText,
        Megaphone,
        Package,
        Settings,
        Tags,
        Users
    } from "lucide-svelte";

    export let navController: NavController;
    export let navBackStackEntry: NavBackStackEntry<{ id?: string }>;

    const internalNavController = rememberNavController(dashboard.path);
    const userId = navBackStackEntry?.args?.id ?? "usuario";

    const currentUser = sessionStore.getCurrentUser();

    const items = [
        { label: "Principal", path: dashboard.path, icon: Home },
        { label: "Mensajes", path: support.path, icon: MessageSquareText },
        { label: "Usuarios", path: users.path, icon: Users },
        { label: "Productos", path: product.path, icon: Package },
        { label: "Categorías", path: category.path, icon: Tags },
        { label: "Ventas", path: sales.path, icon: BadgeDollarSign },
        { label: "Promos", path: promo.path, icon: Megaphone },
        { label: "Reservas", path: reservation.path, icon: CalendarCheck2 },
        { label: "Ajustes", path: settings.path, icon: Settings }
    ];

    const internalStackStore = internalNavController._getStackStore();
    $: internalStack = $internalStackStore;
    $: currentPath = internalStack.at(-1)?.route ?? dashboard.path;

    let sidebarOpen = false;
    let stopPulseRefresh: (() => void) | null = null;
    let supportSyncTimer: number | null = null;
    let salesSyncTimer: number | null = null;
    let syncingSupport = false;
    let syncingSales = false;
    let queuedSupport = false;
    let queuedSales = false;

    function go(path: string) {
        if (currentPath !== path) internalNavController.navigate(path);
        sidebarOpen = false;
    }

    async function logout() {
        try {
            await authContainer.useCases.sessions.closeSession.execute();
        } finally {
            navController.navigate("welcome");
        }
    }

    onMount(() => {
        authContainer.useCases.accounts
            .getCurrentUser()
            .then((u) => {
                if (u.role !== "admin") {
                    navController.navigate("unauthorized", {
                        message: "Tu cuenta no está autorizada para usar el panel de gestión."
                    });
                }
            })
            .catch(() => {
                navController.navigate("login");
            });

        productStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar datos");
        });
        categoryStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar datos");
        });
        promotionStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar datos");
        });
        saleStore.syncAll().catch(() => {
            toastStore.error("Error al sincronizar ventas");
        });

        logger.info(
            `[Pusher] init key=${ENV.pusherKey ? ENV.pusherKey.slice(0, 6) + "…" : "N/A"} cluster=${ENV.pusherCluster ?? "N/A"} channel=${ENV.pusherSupportChannel ?? "support-inbox"}`
        );

        stopPulseRefresh = subscribePulseChannelAll((eventName, payload) => {
            try {
                const summary =
                    payload && typeof payload === "object"
                        ? JSON.stringify(payload).slice(0, 800)
                        : String(payload ?? "");
                logger.info(`[Pusher] event=${eventName} payload=${summary}`);
            } catch {
                logger.info(`[Pusher] event=${eventName}`);
            }

            let targets: ("support" | "sales")[] = [];
            try {
                targets = pulseRefreshTargets(eventName, payload);
            } catch (e: any) {
                logger.error(`[Pusher] error parsing refresh targets: ${e?.message ?? e}`, e?.stack);
                toastStore.error("Evento realtime inválido");
                return;
            }

            logger.info(`[Pusher] targets=${targets.length ? targets.join(",") : "none"}`);
            if (targets.length === 0) return;

            toastStore.info(
                targets.length === 2
                    ? "Evento realtime: sincronizando todo…"
                    : targets[0] === "support"
                      ? "Evento realtime: sincronizando mensajes…"
                      : "Evento realtime: sincronizando ventas…",
                1200
            );

            if (targets.includes("support")) scheduleSupportSync();
            if (targets.includes("sales")) scheduleSalesSync();
        });
    });

    onDestroy(() => {
        if (supportSyncTimer) window.clearTimeout(supportSyncTimer);
        if (salesSyncTimer) window.clearTimeout(salesSyncTimer);
        supportSyncTimer = null;
        salesSyncTimer = null;
        stopPulseRefresh?.();
        stopPulseRefresh = null;
    });

    function scheduleSupportSync() {
        if (supportSyncTimer) window.clearTimeout(supportSyncTimer);
        supportSyncTimer = window.setTimeout(() => {
            supportSyncTimer = null;
            syncSupportInbox();
        }, 220);
    }

    function scheduleSalesSync() {
        if (salesSyncTimer) window.clearTimeout(salesSyncTimer);
        salesSyncTimer = window.setTimeout(() => {
            salesSyncTimer = null;
            syncSales();
        }, 220);
    }

    async function syncSupportInbox() {
        const beforeItems = get(supportInboxStore).items;
        const beforePending = beforeItems.filter((m) => m.status === "nuevo").length;
        if (syncingSupport) {
            queuedSupport = true;
            return;
        }
        syncingSupport = true;
        queuedSupport = false;
        toastStore.info("Actualizando mensajes…", 1200);
        try {
            await supportInboxStore.syncAll();
            const afterItems = get(supportInboxStore).items;
            const afterPending = afterItems.filter((m) => m.status === "nuevo").length;
            const delta = Math.max(0, afterPending - beforePending);
            toastStore.success(delta > 0 ? `Nuevo mensaje (+${delta})` : "Mensajes actualizados", 1100);
        } catch (e: any) {
            logger.error(e?.message ?? e, e?.stack);
            toastStore.error("No se pudieron actualizar los mensajes");
        } finally {
            syncingSupport = false;
            if (queuedSupport) syncSupportInbox();
        }
    }

    async function syncSales() {
        const beforeItems = get(saleStore).items;
        const beforePending = beforeItems.filter((s) => s.verified === BuyState.UNVERIFIED).length;
        if (syncingSales) {
            queuedSales = true;
            return;
        }
        syncingSales = true;
        queuedSales = false;
        toastStore.info("Actualizando ventas...", 1200);
        try {
            await saleStore.syncAll();
            const afterItems = get(saleStore).items;
            const afterPending = afterItems.filter((s) => s.verified === BuyState.UNVERIFIED).length;
            const delta = Math.max(0, afterPending - beforePending);
            toastStore.success(delta > 0 ? `Nueva venta (+${delta})` : "Ventas actualizadas", 1100);
        } catch (e: any) {
            logger.error(e?.message ?? e, e?.stack);
            toastStore.error("No se pudieron actualizar las ventas");
        } finally {
            syncingSales = false;
            if (queuedSales) syncSales();
        }
    }
</script>

<section class="nested-shell">
    <aside class="sidebar {sidebarOpen ? 'open' : ''}">
        <header class="sidebar-head">
            <div class="brand">
                <img src="/alejoicon_clean.svg" alt="Logo" class="brand-logo" />
                <div class="brand-meta">
                    <h2>Business Dashboard</h2>
                    {#await currentUser}
                        <p>Loading user...</p>
                    {:then user}
                        <p>{user.name}</p>
                    {:catch error}
                        <p>{error.message}</p>
                    {/await}
                </div>
            </div>
        </header>

        <nav class="sidebar-nav" aria-label="Menú">
            {#each items as item}
                <button
                    class:selected={currentPath === item.path}
                    on:click={() => go(item.path)}
                    aria-current={currentPath === item.path ? "page" : undefined}
                    title={item.label}
                >
                    <Icon icon={item.icon} size={18} className="nav-ico" ariaLabel={item.label} />
                    <span class="nav-label">{item.label}</span>
                </button>
            {/each}
        </nav>

        <button class="logout" on:click={logout} aria-label="Cerrar sesión" title="Cerrar sesión">
            <Icon icon={LogOut} size={18} className="nav-ico" ariaLabel="Cerrar sesión" />
            <span class="logout-label">Cerrar sesión</span>
        </button>
    </aside>

    <main class="content">
        <div class="top-mobile">
            <button
                class="menu-toggle"
                type="button"
                aria-label={sidebarOpen ? "Cerrar menú" : "Abrir menú"}
                on:click={() => (sidebarOpen = !sidebarOpen)}
            >
                <Icon icon={Menu} size={20} className="menu-ico" ariaLabel="Menú" />
            </button>
            <strong>Panel de gestión</strong>
            <span class="ghost" aria-hidden="true">{userId}</span>
        </div>

        <RealtimeDock navController={internalNavController} />

        {#key currentPath}
            <div class="route-stage" in:fade={{ duration: 180 }} out:fade={{ duration: 120 }}>
                <NavHost
                    navController={internalNavController}
                    routes={[
                        composable(dashboard, () => DashboardHome),
                        composable(support, () => SupportInbox),
                        composable(supportDetail, () => SupportDetail),
                        composable(users, () => UserManagement),
                        composable(product, () => ProductManagement),
                        composable(category, () => CategoryManagement),
                        composable(sales, () => SaleManagement),
                        composable(salesDetail, () => SaleDetail),
                        composable(promo, () => PromoManagement),
                        composable(settings, () => SettingsManagement),
                        composable(reservation, () => ReservationManagement)
                    ]}
                />
            </div>
        {/key}
    </main>

    {#if sidebarOpen}
        <button class="scrim" aria-label="Cerrar menú" on:click={() => (sidebarOpen = false)}></button>
    {/if}
</section>

<style>
    .nested-shell {
        height: 100dvh;
        display: grid;
        grid-template-columns: 280px 1fr;
        background: var(--md-sys-color-background);
        color: var(--md-sys-color-on-background);
        overflow: hidden;
    }

    .sidebar {
        border-right: 1px solid var(--md-sys-color-outline-variant);
        background: var(--md-sys-color-surface);
        padding: 14px;
        height: 100%;
        overflow: hidden;
        display: flex;
        flex-direction: column;
        gap: 12px;
    }

    .brand {
        display: flex;
        gap: 12px;
        align-items: center;
    }

    .brand-logo {
        width: 44px;
        height: 44px;
        object-fit: contain;
        opacity: 0.95;
    }

    .brand-meta {
        min-width: 0;
        display: grid;
        gap: 2px;
    }

    .sidebar-head h2 {
        margin: 0;
        font-size: 1.05rem;
        line-height: 1.15;
    }

    .sidebar-head p {
        margin: 0;
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.92rem;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
    }

    .sidebar-nav {
        display: grid;
        gap: 8px;
        align-content: start;
        overflow: auto;
        min-height: 0;
        padding-right: 6px;
    }

    .sidebar-nav button,
    .logout {
        text-align: left;
        border: 1px solid var(--md-sys-color-outline-variant);
        background: transparent;
        color: var(--md-sys-color-on-surface);
        border-radius: 12px;
        padding: 10px 12px;
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        gap: 10px;
        font-weight: 650;
        transition: background-color 160ms ease, border-color 160ms ease, transform 120ms ease;
    }

    .nav-ico {
        opacity: 0.92;
    }

    .sidebar-nav button:hover {
        background: color-mix(in srgb, var(--md-sys-color-surface-variant) 40%, transparent);
    }

    .sidebar-nav button:active {
        transform: translateY(1px);
    }

    .sidebar-nav button.selected {
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        border-color: var(--md-sys-color-primary);
    }

    .sidebar-nav button.selected .nav-ico {
        opacity: 1;
    }

    .logout {
        margin-top: auto;
        justify-content: center;
        color: var(--md-sys-color-on-error-container);
        background: color-mix(in srgb, var(--md-sys-color-error-container) 72%, transparent);
        border-color: color-mix(in srgb, var(--md-sys-color-error) 22%, transparent);
    }

    .content {
        height: 100%;
        padding: 16px;
        min-width: 0;
        overflow: auto;
    }

    .route-stage {
        min-height: 100%;
        display: grid;
        align-content: start;
    }

    .top-mobile {
        display: none;
    }

    .scrim {
        display: none;
    }

    @media (max-width: 860px) {
        .nested-shell {
            grid-template-columns: 1fr;
        }

        .sidebar {
            position: fixed;
            inset: 0 auto 0 0;
            width: min(84vw, 320px);
            z-index: 40;
            transform: translateX(-105%);
            transition: transform 180ms ease;
            box-shadow: 0 20px 35px rgba(0, 0, 0, 0.25);
        }

        .sidebar.open {
            transform: translateX(0);
        }

        .content {
            height: 100dvh;
            padding: 12px;
        }

        .top-mobile {
            display: grid;
            grid-template-columns: auto 1fr auto;
            gap: 10px;
            align-items: center;
            margin-bottom: 12px;
        }

        .menu-toggle {
            width: 42px;
            height: 42px;
            border-radius: 12px;
            border: 1px solid var(--md-sys-color-outline-variant);
            background: var(--md-sys-color-surface);
            display: grid;
            place-items: center;
        }

        .menu-ico {
            opacity: 0.9;
        }

        .ghost {
            opacity: 0;
            pointer-events: none;
            user-select: none;
        }

        .scrim {
            display: block;
            position: fixed;
            inset: 0;
            background: color-mix(in srgb, black 30%, transparent);
            z-index: 20;
            border: 0;
        }
    }

    /* Tablet: rail compacto (solo iconos) */
    @media (min-width: 861px) and (max-width: 1100px) {
        .nested-shell {
            grid-template-columns: 84px 1fr;
        }

        .sidebar {
            padding: 12px 10px;
        }

        .brand {
            justify-content: center;
        }

        .brand-logo {
            width: 46px;
            height: 46px;
        }

        .brand-meta {
            display: none;
        }

        .sidebar-nav button,
        .logout {
            justify-content: center;
            padding: 10px;
            border-radius: 14px;
        }

        .sidebar-nav button .nav-label,
        .logout .logout-label {
            display: none;
        }

        .logout {
            width: 100%;
        }
    }

    .sidebar-nav::-webkit-scrollbar,
    .content::-webkit-scrollbar {
        width: 10px;
        height: 10px;
    }

    .sidebar-nav::-webkit-scrollbar-thumb,
    .content::-webkit-scrollbar-thumb {
        background: color-mix(in srgb, var(--md-sys-color-outline) 30%, transparent);
        border-radius: 999px;
        border: 2px solid transparent;
        background-clip: padding-box;
    }

    .sidebar-nav::-webkit-scrollbar-track,
    .content::-webkit-scrollbar-track {
        background: transparent;
    }
</style>



