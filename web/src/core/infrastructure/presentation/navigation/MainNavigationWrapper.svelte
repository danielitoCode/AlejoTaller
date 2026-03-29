<script lang="ts">
    import { onDestroy, onMount } from "svelte";
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {home, login, register, splash, welcome} from "./router";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import Splash from "../../../feature/auth/presentation/routes/Splash.svelte";
    import Login from "../../../feature/auth/presentation/routes/Login.svelte";
    import Register from "../../../feature/auth/presentation/routes/Register.svelte";
    import WelcomeScreen from "../routes/WelcomeScreen.svelte";
    import NestedNavigationWrapper from "./NestedNavigationWrapper.svelte";
    import { buildTopLevelHash, parseDeepLinkHash } from "./deeplink";
    const navController = rememberNavController(splash.path);
    const stackStore = navController._getStackStore();

    let suppressHashSync = false;

    $: mainStack = $stackStore;
    $: currentEntry = mainStack.at(-1);
    $: currentPath = currentEntry?.route ?? splash.path;

    function applyHash(hash: string) {
        const parsed = parseDeepLinkHash(hash);
        if (!parsed) return;

        if (parsed.top === home.path) {
            const currentArgs = (currentEntry?.args ?? {}) as Record<string, string>;
            navController.resetTo(home.path, {
                ...currentArgs,
                ...parsed.args
            });
            return;
        }

        navController.resetTo(parsed.top, parsed.args);
    }

    function handleHashChange() {
        suppressHashSync = true;
        applyHash(window.location.hash);
        queueMicrotask(() => {
            suppressHashSync = false;
        });
    }

    onMount(() => {
        if (window.location.hash) {
            applyHash(window.location.hash);
        } else {
            window.history.replaceState({}, "", buildTopLevelHash(splash.path));
        }

        window.addEventListener("hashchange", handleHashChange);
    });

    onDestroy(() => {
        window.removeEventListener("hashchange", handleHashChange);
    });

    $: if (!suppressHashSync && typeof window !== "undefined" && currentPath && currentPath !== home.path) {
        const nextHash = buildTopLevelHash(currentPath as typeof splash.path | typeof welcome.path | typeof login.path | typeof register.path);
        if (window.location.hash !== nextHash) {
            window.history.replaceState({}, "", nextHash);
        }
    }

</script>

<NavHost
        navController={navController}
        routes={[
                composable(splash, () => Splash),
                composable(welcome, () => WelcomeScreen),
                composable(login, () => Login),
                composable(register, () => Register),
                composable(home, () => NestedNavigationWrapper)
            ]}
/>
