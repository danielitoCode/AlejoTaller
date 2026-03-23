<script lang="ts">
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {home, login, register, splash, unauthorized, welcome} from "./router";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import WelcomeScreen from "../routes/WelcomeScreen.svelte";
    import Splash from "../../../feature/auth/presentation/routes/Splash.svelte";
    import Login from "../../../feature/auth/presentation/routes/Login.svelte";
    const navController = rememberNavController(splash.path);

</script>

<NavHost
        navController={navController}
        routes={[
                composable(splash, () => Splash),
                composable(welcome, () => WelcomeScreen),
                composable(login, () => Login),
                /*composable(register, () => Register),
                composable(unauthorized, () => Unauthorized),
                composable(home, () => NestedNavigationWrapper)*/
            ]}
/>

