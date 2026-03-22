<script lang="ts">
    import type {ContentTransition} from "./ContentTransition";
    import {fade} from "./contentTransitions";

    export let targetState: any;
    export let transition: ContentTransition = fade();

    let currentKey = targetState;
    let exiting = false;

    let timeoutId: ReturnType<typeof setTimeout> | null = null;

    $: if (targetState !== currentKey) {
        exiting = true;

        if (timeoutId) clearTimeout(timeoutId);

        timeoutId = setTimeout(() => {
            currentKey = targetState;
            exiting = false;
            timeoutId = null;
        }, transition.duration);
    }
</script>

<div class="relative w-full h-full overflow-hidden">
    <div
            class={`w-full h-full transition-all ${
      exiting ? transition.exit : transition.enter
    }`}
    >
        <slot />
    </div>
</div>