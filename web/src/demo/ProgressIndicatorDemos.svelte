<script lang="ts">
    import { LinearProgress, LinearProgressEstimate, WavyLinearProgress, WavyLinearProgressEstimate, CircularProgress, CircularProgressEstimate } from "m3-svelte"
</script>
<LinearProgress percent={60} title="Loading data" />
<LinearProgressEstimate sToHalfway={2} title="Loading data" />
<WavyLinearProgress percent={60} title="Loading data" />
<WavyLinearProgressEstimate sToHalfway={2} title="Loading data" />
<CircularProgress percent={60} title="Loading data" />
<CircularProgressEstimate sToHalfway={2} title="Loading data" />