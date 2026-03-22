<script lang="ts">
    import type { NavController } from "./NavController";
    import AnimatedVisibility from "../motion/AnimatedVisibility.svelte";

    export let navController: NavController;

    export let routes: {
        route: { path: string };
        component: any;
    }[];

    // 🔹 Store interno (estable)
    const stackStore = navController._getStackStore();

    $: stack = $stackStore;
    $: currentEntry = stack.at(-1);

    $: active =
        routes.find(r => r.route.path === currentEntry?.route);
</script>

<div>
    <AnimatedVisibility visible={true}>
        {#if active}
            <svelte:component
                    this={active.component}
                    navController={navController}
                    navBackStackEntry={currentEntry}
            />
        {/if}
    </AnimatedVisibility>
</div>