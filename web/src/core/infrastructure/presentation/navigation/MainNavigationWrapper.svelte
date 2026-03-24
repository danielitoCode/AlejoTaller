<script lang="ts">
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {home, login, register, splash, welcome} from "./router";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import Splash from "../../../feature/auth/presentation/routes/Splash.svelte";
    import Login from "../../../feature/auth/presentation/routes/Login.svelte";
    import Register from "../../../feature/auth/presentation/routes/Register.svelte";
    import WelcomeScreen from "../routes/WelcomeScreen.svelte";
    import NestedNavigationWrapper from "./NestedNavigationWrapper.svelte";
    const navController = rememberNavController(splash.path);

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
