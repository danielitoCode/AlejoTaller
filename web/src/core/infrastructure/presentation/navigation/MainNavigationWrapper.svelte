<script lang="ts">
    import {rememberNavController} from "../../../../lib/navigation/rememberNavController";
    import {home, login, register, splash, unauthorized, welcome} from "./router";
    import NavHost from "../../../../lib/navigation/NavHost.svelte";
    import {composable} from "../../../../lib/navigation/composable";
    import NestedNavigationWrapper from "./NestedNavigationWrapper.svelte";
    import Splash from "../../../feature/auth/presentation/routes/Splash.svelte";
    import Welcome from "../routes/Welcome.svelte";
    import Login from "../../../feature/auth/presentation/routes/Login.svelte";
    import Register from "../../../feature/auth/presentation/routes/Register.svelte";
    import Unauthorized from "../../../feature/auth/presentation/routes/Unauthorized.svelte";
    import {onMount} from "svelte";

    const navController = rememberNavController(splash.path);

</script>

<NavHost
        navController={navController}
        routes={[
                composable(splash, () => Splash),
                composable(welcome, () => Welcome),
                composable(login, () => Login),
                composable(register, () => Register),
                composable(unauthorized, () => Unauthorized),
                composable(home, () => NestedNavigationWrapper)
            ]}
/>

