<script lang="ts">
    import { Button, Card, Icon, Switch } from "m3-svelte";
    import refreshIcon from "@ktibow/iconset-material-symbols/refresh-rounded";
    import { exchangeStore } from "../viewmodels/exchanges.store";

    export let compact = false;

    $: usdRate = $exchangeStore.exchange?.usdReference;
    $: statusText = $exchangeStore.loading
        ? "Consultando tasa..."
        : $exchangeStore.error
            ? "Tasa no disponible"
            : usdRate
                ? `1 USD = ${usdRate.toFixed(2)} CUP`
                : "Consulta la tasa de elTOQUE";

    function onSwitchClick() {
        const nextCurrency = $exchangeStore.selectedCurrency === "USD" ? "CUP" : "USD";
        exchangeStore.setCurrency(nextCurrency);
        if (nextCurrency === "USD" && !$exchangeStore.exchange && !$exchangeStore.loading) void exchangeStore.refresh();
    }
</script>

<div class:compact>
    <Card variant="outlined">
        <div class="currency-switch">
            <div class="copy">
                <strong>Moneda</strong>
                <span>{statusText}</span>
            </div>
            <div class="control">
                <span class:active={$exchangeStore.selectedCurrency === "CUP"}>CUP</span>
                <label aria-label="Cambiar entre CUP y USD"><Switch checked={$exchangeStore.selectedCurrency === "USD"} onclick={onSwitchClick} /></label>
                <span class:active={$exchangeStore.selectedCurrency === "USD"}>USD</span>
                <Button variant="text" size="s" iconType="left" onclick={() => exchangeStore.refresh()} disabled={$exchangeStore.loading}>
                    <Icon icon={refreshIcon} />
                    {compact ? "" : "Actualizar"}
                </Button>
            </div>
        </div>
        {#if $exchangeStore.error && !compact}
            <p class="error">{$exchangeStore.error}</p>
        {/if}
        {#if $exchangeStore.exchange && !compact}
            <p class="source">Fuente referencial: elTOQUE. Actualizado: {new Date($exchangeStore.exchange.updatedAt).toLocaleString("es-CU")}</p>
        {/if}
    </Card>
</div>

<style>
    .compact :global(.m3-card) {
        border-radius: 18px;
    }

    .currency-switch {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 12px;
        padding: 12px;
    }

    .copy {
        display: grid;
        gap: 3px;
        min-width: 0;
    }

    .copy strong {
        color: var(--md-sys-color-on-surface);
        font-size: 0.9rem;
    }

    .copy span,
    .source,
    .error {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.78rem;
    }

    .control {
        display: flex;
        align-items: center;
        gap: 8px;
        white-space: nowrap;
    }

    .control span {
        font-size: 0.78rem;
        font-weight: 800;
        color: var(--md-sys-color-on-surface-variant);
    }

    .control span.active {
        color: var(--md-sys-color-primary);
    }

    .source,
    .error {
        margin: -4px 12px 12px;
    }

    .error {
        color: var(--md-sys-color-error);
    }

    @media (max-width: 520px) {
        .currency-switch {
            align-items: start;
            flex-direction: column;
        }
    }
</style>