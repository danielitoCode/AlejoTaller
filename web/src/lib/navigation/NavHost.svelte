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

<div class="nav-host">
    <AnimatedVisibility visible={true}>
        {#if active}
            <div class="nav-host-content">
                <svelte:component
                    this={active.component}
                    navController={navController}
                    navBackStackEntry={currentEntry}
                />
            </div>
        {/if}
    </AnimatedVisibility>
</div>

<style>
    .nav-host {
        width: 100%;
        height: 100%;
        min-height: 0;
    }

    .nav-host :global(.animated-visibility) {
        width: 100%;
        height: 100%;
        min-height: 0;
    }

    .nav-host-content {
        width: 100%;
        height: 100%;
        min-height: 0;
    }
</style>
