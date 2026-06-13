<script lang="ts">
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

        if (!$exchangeStore.exchange && !$exchangeStore.loading) void exchangeStore.hydrateCachedToday();
    }
</script>

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

<style>
    .currency-menu {
        max-width: min-content;
        position: relative;
    }

    .currency-trigger {
        min-width: 92px;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        background: var(--md-sys-color-surface-container-high);
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        display: inline-flex;
        align-items: center;
        justify-content: space-between;
        gap: 12px;
        padding: 9px 12px;
        font-size: 0.82rem;
        font-weight: 800;
        box-shadow: 0 8px 18px color-mix(in srgb, black 8%, transparent);
    }

    .currency-trigger:hover {
        background: var(--md-sys-color-surface-container-highest);
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
        z-index: 100;
        min-width: 100%;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        background-color: #1f211e; /* Solid dark theme background fallback */
        background: var(--md-sys-color-surface-container-highest, #1f211e);
        box-shadow: 0 14px 34px rgba(0, 0, 0, 0.45);
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
</style>