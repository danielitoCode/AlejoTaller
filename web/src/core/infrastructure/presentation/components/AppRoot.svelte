<script lang="ts">

    import {Modifier} from "../../../../lib/core/modifier/Modifier";

    export let modifier: Modifier = Modifier.empty();
</script>

<div
        class="compose-relative"
        style={`width:100vw;height:100vh;${modifier.toStyle()}`}
>
    <slot />
</div>