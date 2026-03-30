<script lang="ts">
    import { Modifier } from "../modifier/Modifier";
    import type { NavController } from "./NavController";
    import AnimatedContent from "../../motion/AnimatedContent.svelte";
    import type { ContentTransition } from "../motion/ContentTransition";
    import { fade } from "../motion/contentTransitions";

    export let navController: NavController;

    export let routes: {
        route: { path: string };
        component: any;
    }[];

    // 🔹 Transición única, Compose-like
    export let transition: ContentTransition = fade(320);

    export let modifier: Modifier = Modifier.empty();

    // 🔹 Store interno (estable)
    const stackStore = navController._getStackStore();

    $: stack = $stackStore;
    $: currentEntry = stack.at(-1);

    $: active =
        routes.find(r => r.route.path === currentEntry?.route);
</script>

<div style={modifier.toStyle()}>
    <AnimatedContent
            targetState={currentEntry?.route}
            transition={transition}
    >
        {#if active}
            <svelte:component
                    this={active.component}
                    navController={navController}
                    navBackStackEntry={currentEntry}
            />
        {/if}
    </AnimatedContent>
</div>
