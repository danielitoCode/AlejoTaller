<script lang="ts">
    import { Button, Card, Icon } from "m3-svelte";
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

    let isDropdownOpen = false;

    const currencyOptions = [
        { value: "CUP", label: "CUP" },
        { value: "USD", label: "USD" }
    ] as const;

    function selectCurrency(currency: "CUP" | "USD") {
        exchangeStore.setCurrency(currency);
        isDropdownOpen = false;

        if (currency === "USD" && !$exchangeStore.exchange && !$exchangeStore.loading) void exchangeStore.refresh();
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
                <div class="currency-menu">
                    <button
                            class="currency-trigger"
                            type="button"
                            aria-haspopup="listbox"
                            aria-expanded={isDropdownOpen}
                            on:click={() => (isDropdownOpen = !isDropdownOpen)}
                    >
                        <span>{$exchangeStore.selectedCurrency}</span>
                        <span class="chevron" aria-hidden="true">⌄</span>
                    </button>

                    {#if isDropdownOpen}
                        <div class="currency-options" role="listbox" aria-label="Seleccionar moneda">
                            {#each currencyOptions as option}
                                <button
                                        class:active={$exchangeStore.selectedCurrency === option.value}
                                        role="option"
                                        aria-selected={$exchangeStore.selectedCurrency === option.value}
                                        type="button"
                                        on:click={() => selectCurrency(option.value)}
                                >
                                    {option.label}
                                </button>
                            {/each}
                        </div>
                    {/if}
                </div>
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

    .currency-menu {
        position: relative;
    }

    .currency-trigger {
        min-width: 86px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        background: var(--md-sys-color-surface-container);
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: space-between;
        gap: 10px;
        padding: 9px 12px;
        font-size: 0.82rem;
        font-weight: 800;
    }

    .chevron {
        color: var(--md-sys-color-on-surface-variant);
        font-size: 0.9rem;
        line-height: 1;
    }

    .currency-options {
        position: absolute;
        top: calc(100% + 6px);
        right: 0;
        z-index: 12;
        min-width: 100%;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        background: var(--md-sys-color-surface-container-high);
        box-shadow: 0 14px 34px color-mix(in srgb, black 18%, transparent);
        display: grid;
        gap: 4px;
        padding: 6px;
    }

    .currency-options button {
        border: 0;
        border-radius: 12px;
        background: transparent;
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        font-size: 0.8rem;
        font-weight: 800;
        padding: 9px 12px;
        text-align: left;
    }

    .currency-options button:hover,
    .currency-options button.active {
        background: var(--md-sys-color-primary-container);
        color: var(--md-sys-color-on-primary-container);
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