<script lang="ts">
    import { Menu, Switch } from "m3-svelte"
    import {MenuItem} from "m3-svelte"
    import iconCircle from "@ktibow/iconset-material-symbols/circles"
    import iconSquare from "@ktibow/iconset-material-symbols/square"
    import iconTriangle from "@ktibow/iconset-material-symbols/triangle-circle-outline-rounded"

    let icons = $state(false);
</script>
<Menu>
    <MenuItem icon={iconCircle}>Alpha</MenuItem>
    <MenuItem icon={iconSquare}>Beta</MenuItem>
    <MenuItem icon={iconTriangle}>Charlie</MenuItem>
</Menu>

<label>
    <Switch bind:checked={icons} />
    {icons ? "Icons" : "No icons"}
</label>

<Menu>
    <MenuItem icon={icons ? iconCircle : undefined} onclick={() => {}}>Alpha</MenuItem>
    <MenuItem icon={icons ? iconSquare : undefined} onclick={() => {}}>Beta</MenuItem>
    <MenuItem icon={icons ? iconTriangle : undefined} disabled onclick={() => {}}>Charlie</MenuItem>
</Menu>