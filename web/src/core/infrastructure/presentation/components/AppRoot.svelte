<script lang="ts">
    type ModifierLike = {
        toStyle?: () => string;
    };

    export let modifier: ModifierLike | undefined = undefined;
</script>

<div
        class="compose-relative"
        style={`width:100vw;height:100vh;${modifier?.toStyle?.() ?? ""}`}
>
    <slot />
</div>
