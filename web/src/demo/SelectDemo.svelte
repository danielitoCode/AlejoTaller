<script lang="ts">
    import {Select } from "m3-svelte"

    let value = $state("")
</script>
<Select
        label="Select"
        options={[
    { text: "A", value: "a" },
    { text: "B", value: "b" },
  ]}
        bind:value
/>