<script>
    import { ListItem, Icon } from "m3-svelte";
    import iconPlus from "@ktibow/iconset-material-symbols/add";
</script>

<ListItem headline="User">
    {#snippet leading()}
        <Icon icon={iconPlus} />
    {/snippet}
</ListItem>