<script lang="ts">
    import { Button, ConnectedButtons, SplitButton } from "m3-svelte"

    let itemA = $state(false);
    let itemB = $state(false);
</script>

<Button variant="elevated" onclick={() => alert("!")}>Hello</Button>

<ConnectedButtons>
    <Button><input type="checkbox" bind:checked={itemA} />A</Button>
    <Button><input type="checkbox" bind:checked={itemB} />B</Button>
</ConnectedButtons>

<SplitButton variant="filled" onclick={() => alert("!")}>
    Hello
    {#snippet menu()}
        and more
    {/snippet}
</SplitButton>