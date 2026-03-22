<script lang="ts">
    import type { AnimationSpec } from "./AnimationsSpec";
    import { fadeIn, fadeOut } from "./transitions";

    export let visible: boolean;

    export let enter: AnimationSpec = fadeIn();
    export let exit: AnimationSpec = fadeOut();

    let shouldRender = visible;
    let classes = "";

    function applyEnter() {
        classes = `
            ${enter.base}
            ${enter.from}
        `;

        requestAnimationFrame(() => {
            classes = `
                ${enter.base}
                ${enter.to}
            `;
        });
    }

    function applyExit() {
        classes = `
            ${exit.base}
            ${exit.from}
        `;

        requestAnimationFrame(() => {
            classes = `
                ${exit.base}
                ${exit.to}
            `;
        });

        setTimeout(() => {
            shouldRender = false;
        }, exit.duration);
    }

    $: {
        if (visible) {
            shouldRender = true;
            applyEnter();
        } else if (shouldRender) {
            applyExit();
        }
    }
</script>

{#if shouldRender}
    <div class={classes}>
        <slot />
    </div>
{/if}