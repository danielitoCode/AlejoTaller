<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {home, login, register, splash, welcome, welcomeUpdate} from "./router";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import Splash from "../../../feature/auth/presentation/routes/Splash.svelte";
    import Login from "../../../feature/auth/presentation/routes/Login.svelte";
    import Register from "../../../feature/auth/presentation/routes/Register.svelte";
    import WelcomeUpdateScreen from "../../../feature/auth/presentation/routes/WelcomeUpdateScreen.svelte";
    import WelcomeScreen from "../routes/WelcomeScreen.svelte";
    import NestedNavigationWrapper from "./NestedNavigationWrapper.svelte";
    import { buildTopLevelHash, parseDeepLinkHash } from "./deeplink";
    import { rememberPendingDeepLink } from "./pending-deeplink.store";
    import { logNavAuthCheck, logNavRoute, logNavError } from "./debug-logger";

    /** Raw hash captured before Svelte mounts, so the router never clears it */
    export let initialDeepLink: string | null = null;

    const navController = rememberNavController(splash.path);
    const stackStore = navController._getStackStore();

    let suppressHashSync = true;

    $: mainStack = $stackStore;
    $: currentEntry = mainStack.at(-1);
    $: currentPath = currentEntry?.route ?? splash.path;

    function applyHash(hash: string) {
        const parsed = parseDeepLinkHash(hash);
        if (!parsed) return;

        if (import.meta.env.DEV) {
            logNavRoute(parsed.top, { nested: parsed.nested, args: parsed.args });
        }

        if (parsed.top === home.path) {
            window.history.replaceState({}, "", hash);
            const currentArgs = (currentEntry?.args ?? {}) as Record<string, string>;
            navController.resetTo(home.path, {
                ...currentArgs,
                ...parsed.args
            });
            return;
        }

        navController.resetTo(parsed.top, parsed.args);
    }

    function isProductDeepLinkHash(hash: string): boolean {
        const parsed = parseDeepLinkHash(hash);
        return parsed?.top === home.path && (parsed.nested === "product-detail" || !!parsed.args?.productId);
    }

    function maybeRememberPendingDeepLinkForAuthRedirect(path: string) {
        if (
            (path === welcome.path ||
                path === welcomeUpdate.path ||
                path === login.path) &&
            isProductDeepLinkHash(window.location.hash)
        ) {
            rememberPendingDeepLink(window.location.hash);
        }
    }

    function handleHashChange() {
        suppressHashSync = true;
        applyHash(window.location.hash);
        queueMicrotask(() => {
            suppressHashSync = false;
        });
    }

    onMount(() => {
        const hashToApply = initialDeepLink ?? window.location.hash;
        if (import.meta.env.DEV) {
            if (initialDeepLink) {
                logNavAuthCheck(false, false, "continue");
            }
        }

        if (hashToApply) {
            const parsed = parseDeepLinkHash(hashToApply);
            if (parsed && parsed.top === home.path) {
                const isActionableImmediately = false; // Splash owns all home deeplinks
                if (isActionableImmediately) {
                    window.history.replaceState({}, "", hashToApply);
                }
            } else {
                applyHash(hashToApply);
            }
        } else {
            window.history.replaceState({}, "", buildTopLevelHash(splash.path));
        }

        queueMicrotask(() => {
            suppressHashSync = false;
        });

        window.addEventListener("hashchange", handleHashChange);
    });

    onDestroy(() => {
        window.removeEventListener("hashchange", handleHashChange);
    });

    $: if (!suppressHashSync && typeof window !== "undefined" && currentPath && currentPath !== home.path) {
        maybeRememberPendingDeepLinkForAuthRedirect(currentPath);
        if (!window.location.hash.startsWith("#/home/")) {
            const nextHash = buildTopLevelHash(
                currentPath as
                    | typeof splash.path
                    | typeof welcome.path
                    | typeof welcomeUpdate.path
                    | typeof login.path
                    | typeof register.path
            );
            if (window.location.hash !== nextHash) {
                window.history.replaceState({}, "", nextHash);
            }
        }
    }

</script>

<NavHost
        navController={navController}
        routes={[
                composable(splash, () => Splash),
                composable(welcome, () => WelcomeScreen),
                composable(welcomeUpdate, () => WelcomeUpdateScreen),
                composable(login, () => Login),
                composable(register, () => Register),
                composable(home, () => NestedNavigationWrapper)
            ]}
/>
