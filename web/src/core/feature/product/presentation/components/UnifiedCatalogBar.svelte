<script lang="ts">
    import { onDestroy } from "svelte";
    import { exchangeStore } from "../../../exchange/presentation/viewmodels/exchanges.store";
    import type { Category } from "../../../category/domain/entity/Category";

    export let categories: Category[] = [];
    export let query = "";
    export let selectedCategoryId: string | null = null;
    export let minPrice: number | null = null;
    export let maxPrice: number | null = null;
    export let onQueryChanged: (query: string) => void = () => {};
    export let onCategorySelected: (categoryId: string | null) => void = () => {};
    export let onPriceRangeChanged: (min: number | null, max: number | null) => void = () => {};

    let filtersOpen = false;
    let draftMin = "";
    let draftMax = "";
    let barEl: HTMLElement | null = null;
    let panelTop = 0;
    let panelRight = 16;
    let isMobileLayout = false;

    $: currency = $exchangeStore.selectedCurrency;
    $: exchangeRate = $exchangeStore.exchange?.usdReference ?? null;
    $: activeFilterCount =
        (query.trim() ? 1 : 0) +
        (selectedCategoryId ? 1 : 0) +
        (minPrice !== null || maxPrice !== null ? 1 : 0);
    $: hasPriceFilter = minPrice !== null || maxPrice !== null;

    $: parsedDraftMin = parseDraftNumber(draftMin);
    $: parsedDraftMax = parseDraftNumber(draftMax);
    $: priceNegative =
        (parsedDraftMin !== null && parsedDraftMin < 0) ||
        (parsedDraftMax !== null && parsedDraftMax < 0);
    $: priceOrderInvalid =
        parsedDraftMin !== null &&
        parsedDraftMax !== null &&
        parsedDraftMin > parsedDraftMax;
    $: cupPriceRangeUnavailable =
        currency === "CUP" &&
        (draftMin.trim() !== "" || draftMax.trim() !== "") &&
        (!exchangeRate || exchangeRate <= 0);
    $: priceApplyDisabled =
        priceNegative || priceOrderInvalid || cupPriceRangeUnavailable;

    function parseDraftNumber(value: string): number | null {
        const trimmed = value.trim();
        if (!trimmed) return null;
        const n = Number(trimmed);
        return Number.isFinite(n) ? n : null;
    }

    function displayAmount(value: number | null): string {
        if (value === null) return "";
        if (currency === "USD" || !exchangeRate) return String(Math.round(value));
        return String(Math.round(value * exchangeRate));
    }

    function toUsd(value: number): number | null {
        if (!Number.isFinite(value) || value < 0) return null;
        if (currency === "CUP") {
            if (!exchangeRate || exchangeRate <= 0) return null;
            return value / exchangeRate;
        }
        return value;
    }

    function measurePanelAnchor() {
        if (typeof window === "undefined") return;
        isMobileLayout = window.matchMedia("(max-width: 840px)").matches;
        if (!barEl) return;
        const rect = barEl.getBoundingClientRect();
        panelTop = Math.round(rect.bottom + 10);
        panelRight = Math.max(12, Math.round(window.innerWidth - rect.right));
    }

    function openFilters() {
        draftMin = displayAmount(minPrice);
        draftMax = displayAmount(maxPrice);
        measurePanelAnchor();
        filtersOpen = true;
    }

    function closeFilters() {
        filtersOpen = false;
    }

    /** Categoría: aplica al instante y se recuerda al cerrar el panel. */
    function selectCategory(categoryId: string | null) {
        onCategorySelected(categoryId);
    }

    function applyPriceRange() {
        if (priceApplyDisabled) return;

        const nextMin =
            parsedDraftMin !== null ? toUsd(parsedDraftMin) : null;
        const nextMax =
            parsedDraftMax !== null ? toUsd(parsedDraftMax) : null;

        // Campos vacíos = sin límite en ese extremo
        const validMin =
            draftMin.trim() === ""
                ? null
                : nextMin !== null && nextMin >= 0
                  ? nextMin
                  : null;
        const validMax =
            draftMax.trim() === ""
                ? null
                : nextMax !== null && nextMax >= 0
                  ? nextMax
                  : null;

        if (validMin !== null && validMax !== null && validMin > validMax) return;

        onPriceRangeChanged(validMin, validMax);
    }

    function resetAllFilters() {
        onQueryChanged("");
        onCategorySelected(null);
        onPriceRangeChanged(null, null);
        draftMin = "";
        draftMax = "";
    }

    function categorySummary(): string {
        if (selectedCategoryId) {
            return (
                categories.find((c) => c.id === selectedCategoryId)?.name ??
                "Filtro"
            );
        }
        if (hasPriceFilter) return "Filtros";
        return "Todas";
    }

    function priceSummary(): string {
        if (minPrice === null && maxPrice === null) return "Precio";
        const min = minPrice !== null ? displayAmount(minPrice) : "0";
        const max = maxPrice !== null ? displayAmount(maxPrice) : "∞";
        return `${min}–${max}`;
    }

    function handleViewportChange() {
        if (!filtersOpen) return;
        measurePanelAnchor();
    }

    if (typeof window !== "undefined") {
        window.addEventListener("resize", handleViewportChange);
        window.addEventListener("scroll", handleViewportChange, true);
    }

    onDestroy(() => {
        if (typeof window !== "undefined") {
            window.removeEventListener("resize", handleViewportChange);
            window.removeEventListener("scroll", handleViewportChange, true);
        }
    });
</script>

<div class="catalog-search">
    <div class="bar" bind:this={barEl}>
        <div class="search-field">
            <span class="search-icon" aria-hidden="true">⌕</span>
            <input
                type="search"
                value={query}
                placeholder="Buscar productos..."
                aria-label="Buscar productos"
                on:input={(event) =>
                    onQueryChanged((event.currentTarget as HTMLInputElement).value)}
            />
            {#if query}
                <button
                    class="clear"
                    type="button"
                    aria-label="Borrar búsqueda"
                    on:click={() => onQueryChanged("")}>×</button
                >
            {/if}
        </div>

        <button
            class="desktop-segment"
            type="button"
            on:click={openFilters}
            aria-label="Seleccionar categoría"
        >
            <span class="segment-label">Categoría</span>
            <strong>{categorySummary()}</strong>
        </button>

        <button
            class="desktop-segment price-segment"
            type="button"
            on:click={openFilters}
            aria-label="Filtrar por precio"
        >
            <span class="segment-label">Precio</span>
            <strong>{priceSummary()}</strong>
        </button>

        <div class="currency-wrap">
            <span class="segment-label currency-label">Moneda</span>
            <button
                class="currency-button"
                type="button"
                on:click={() => exchangeStore.toggleCurrency()}
                aria-label={`Cambiar moneda. Actual: ${currency}`}
            >
                {currency}<span aria-hidden="true">⌄</span>
            </button>
        </div>

        <button
            class="filter-button"
            type="button"
            on:click={openFilters}
            aria-label="Abrir filtros"
        >
            <span aria-hidden="true">⚙</span>
            {#if activeFilterCount > 0}<small>{activeFilterCount}</small>{/if}
        </button>

        <span class="search-submit" aria-hidden="true">⌕</span>
    </div>

    {#if filtersOpen}
        <button
            class="filter-backdrop"
            type="button"
            aria-label="Cerrar filtros"
            on:click={closeFilters}
        ></button>
        <section
            class="filter-panel"
            class:mobile-sheet={isMobileLayout}
            role="dialog"
            aria-modal="true"
            aria-label="Filtros de productos"
            style={isMobileLayout
                ? undefined
                : `top: ${panelTop}px; right: ${panelRight}px;`}
        >
            <div class="panel-header">
                <div>
                    <span class="eyebrow">Catálogo</span>
                    <h2>Filtros</h2>
                </div>
                <button
                    type="button"
                    class="close"
                    aria-label="Cerrar filtros"
                    on:click={closeFilters}>×</button
                >
            </div>

            <div class="panel-body">
                <!-- Categoría: aplicación inmediata -->
                <div class="filter-group">
                    <label>Categoría</label>
                    <p class="hint">Se aplica al seleccionar</p>
                    <div class="category-options" role="listbox" aria-label="Categorías">
                        <button
                            type="button"
                            role="option"
                            aria-selected={selectedCategoryId === null}
                            class:active={selectedCategoryId === null}
                            on:click={() => selectCategory(null)}
                        >Todas</button
                        >
                        {#each categories as category}
                            <button
                                type="button"
                                role="option"
                                aria-selected={selectedCategoryId === category.id}
                                class:active={selectedCategoryId === category.id}
                                on:click={() => selectCategory(category.id)}
                            >{category.name}</button
                            >
                        {/each}
                    </div>
                </div>

                <!-- Precio: requiere aplicar rango -->
                <div class="filter-group">
                    <label>Precio en {currency}</label>
                    <div class="price-inputs">
                        <label>
                            <span>Desde</span>
                            <input
                                type="number"
                                min="0"
                                step="any"
                                inputmode="decimal"
                                value={draftMin}
                                placeholder="Sin mínimo"
                                aria-invalid={priceNegative || priceOrderInvalid}
                                on:input={(event) =>
                                    (draftMin = (event.currentTarget as HTMLInputElement).value)}
                            />
                        </label>
                        <label>
                            <span>Hasta</span>
                            <input
                                type="number"
                                min="0"
                                step="any"
                                inputmode="decimal"
                                value={draftMax}
                                placeholder="Sin máximo"
                                aria-invalid={priceNegative || priceOrderInvalid}
                                on:input={(event) =>
                                    (draftMax = (event.currentTarget as HTMLInputElement).value)}
                            />
                        </label>
                    </div>

                    {#if priceNegative}
                        <small class="exchange-warning">Los precios no pueden ser negativos.</small>
                    {:else if priceOrderInvalid}
                        <small class="exchange-warning">«Desde» debe ser menor o igual que «Hasta».</small>
                    {:else if cupPriceRangeUnavailable}
                        <small class="exchange-warning">Necesitamos la tasa de cambio para filtrar por precio en CUP.</small>
                    {/if}

                    <button
                        type="button"
                        class="apply-price"
                        disabled={priceApplyDisabled}
                        on:click={applyPriceRange}
                    >Aplicar rango de precios</button
                    >
                </div>
            </div>

            <div class="panel-actions">
                <button type="button" class="reset" on:click={resetAllFilters}>
                    Resetear filtros
                </button>
            </div>
        </section>
    {/if}
</div>

<style>
    .catalog-search { position: relative; width: 100%; z-index: 30; }
    .bar {
        min-height: 64px;
        display: flex;
        align-items: center;
        width: 100%;
        box-sizing: border-box;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 32px;
        background: var(--md-sys-color-surface);
        box-shadow: 0 8px 24px color-mix(in srgb, black 8%, transparent);
        overflow: hidden;
    }
    .search-field {
        flex: 1.35 1 280px;
        min-width: 0;
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 0 18px 0 20px;
    }
    .search-icon { font-size: 25px; line-height: 1; color: var(--md-sys-color-on-surface-variant); }
    .search-field input {
        width: 100%;
        min-width: 0;
        border: 0;
        outline: 0;
        background: transparent;
        color: var(--md-sys-color-on-surface);
        font: inherit;
        font-size: 0.92rem;
    }
    .search-field input::placeholder {
        color: var(--md-sys-color-on-surface-variant);
        opacity: 0.85;
    }
    .clear, .close {
        border: 0;
        background: transparent;
        cursor: pointer;
        color: var(--md-sys-color-on-surface-variant);
    }
    .clear { font-size: 22px; padding: 4px; }
    .desktop-segment, .currency-wrap {
        min-height: 42px;
        border: 0;
        border-left: 1px solid var(--md-sys-color-outline-variant);
        background: transparent;
        padding: 4px 18px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        text-align: left;
    }
    .desktop-segment { cursor: pointer; min-width: 135px; }
    .desktop-segment:hover { background: var(--md-sys-color-surface-container); }
    .price-segment { min-width: 145px; }
    .segment-label, .eyebrow {
        font-size: 0.68rem;
        font-weight: 700;
        color: var(--md-sys-color-on-surface-variant);
    }
    .desktop-segment strong {
        font-size: 0.8rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 150px;
    }
    .currency-wrap { min-width: 74px; align-items: flex-start; }
    .currency-button {
        border: 0;
        background: transparent;
        padding: 2px 0;
        cursor: pointer;
        color: var(--md-sys-color-on-surface);
        font-size: 0.8rem;
        font-weight: 800;
        display: flex;
        gap: 6px;
        align-items: center;
    }
    .filter-button { display: none; }
    .search-submit {
        width: 48px;
        height: 48px;
        margin-right: 7px;
        flex: 0 0 auto;
        border-radius: 50%;
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        display: grid;
        place-items: center;
        font-size: 25px;
    }

    .filter-backdrop {
        position: fixed;
        inset: 0;
        width: 100%;
        height: 100%;
        border: 0;
        padding: 0;
        background: color-mix(in srgb, black 38%, transparent);
        backdrop-filter: blur(3px);
        z-index: 1200;
        cursor: default;
    }

    .filter-panel {
        position: fixed;
        width: min(460px, calc(100vw - 24px));
        max-height: min(70vh, calc(100dvh - 24px));
        display: flex;
        flex-direction: column;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 24px;
        background: var(--md-sys-color-surface);
        box-shadow: 0 24px 70px color-mix(in srgb, black 22%, transparent);
        z-index: 1201;
        overflow: hidden;
    }

    .filter-panel.mobile-sheet {
        left: 12px;
        right: 12px;
        top: auto;
        bottom: max(12px, env(safe-area-inset-bottom));
        width: auto;
        max-height: min(72vh, calc(100dvh - 24px));
    }

    .panel-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 20px 22px 12px;
        flex-shrink: 0;
    }
    .panel-header h2 { margin: 3px 0 0; font-size: 1.2rem; }
    .close { font-size: 28px; }

    .panel-body {
        display: grid;
        gap: 28px;
        padding: 10px 22px 20px;
        overflow: auto;
        min-height: 0;
        flex: 1 1 auto;
    }

    .filter-group > label {
        display: block;
        margin-bottom: 4px;
        font-size: 0.82rem;
        font-weight: 800;
    }

    .hint {
        margin: 0 0 10px;
        font-size: 0.72rem;
        color: var(--md-sys-color-on-surface-variant);
    }

    .category-options {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
    }

    .category-options button {
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 18px;
        padding: 8px 13px;
        background: var(--md-sys-color-surface);
        color: var(--md-sys-color-on-surface);
        cursor: pointer;
        font-size: 0.8rem;
        font-weight: 600;
        transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
    }

    .category-options button:hover {
        background: var(--md-sys-color-surface-container);
    }

    .category-options button.active {
        background: var(--md-sys-color-primary-container);
        border-color: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary-container);
        font-weight: 800;
        box-shadow: 0 0 0 1px color-mix(in srgb, var(--md-sys-color-primary) 40%, transparent);
    }

    .price-inputs {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 10px;
        margin-top: 8px;
    }

    .price-inputs label { display: grid; gap: 6px; }
    .price-inputs span {
        font-size: 0.72rem;
        color: var(--md-sys-color-on-surface-variant);
    }
    .price-inputs input {
        width: 100%;
        box-sizing: border-box;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 12px;
        padding: 11px 12px;
        background: var(--md-sys-color-surface-container-low);
        color: var(--md-sys-color-on-surface);
        outline: none;
    }
    .price-inputs input:focus { border-color: var(--md-sys-color-primary); }
    .price-inputs input[aria-invalid="true"] {
        border-color: var(--md-sys-color-error);
    }

    .exchange-warning {
        color: var(--md-sys-color-error);
        font-size: 0.72rem;
        display: block;
        margin-top: 8px;
    }

    .apply-price {
        margin-top: 12px;
        width: 100%;
        border: 0;
        border-radius: 16px;
        padding: 11px 16px;
        cursor: pointer;
        font-weight: 750;
        font-size: 0.88rem;
        background: var(--md-sys-color-primary);
        color: var(--md-sys-color-on-primary);
        transition: opacity 0.15s ease;
    }
    .apply-price:disabled {
        cursor: not-allowed;
        opacity: 0.45;
    }
    .apply-price:not(:disabled):hover {
        filter: brightness(1.06);
    }

    .panel-actions {
        border-top: 1px solid var(--md-sys-color-outline-variant);
        display: flex;
        justify-content: stretch;
        padding: 14px 22px;
        flex-shrink: 0;
    }

    .reset {
        width: 100%;
        border: 1px solid var(--md-sys-color-outline-variant);
        border-radius: 16px;
        padding: 11px 16px;
        cursor: pointer;
        font-weight: 700;
        font-size: 0.88rem;
        background: transparent;
        color: var(--md-sys-color-on-surface-variant);
        transition: background 0.15s ease, border-color 0.15s ease;
    }
    .reset:hover {
        background: var(--md-sys-color-surface-container);
        border-color: color-mix(in srgb, var(--md-sys-color-outline) 50%, transparent);
        color: var(--md-sys-color-on-surface);
    }

    @media (max-width: 840px) {
        .bar { min-height: 58px; border-radius: 29px; }
        .search-field { padding-left: 16px; padding-right: 8px; }
        .desktop-segment { display: none; }
        .currency-wrap {
            border-left: 0;
            min-width: 56px;
            padding: 0 5px;
            align-items: center;
        }
        .currency-label { display: none; }
        .filter-button {
            display: grid;
            position: relative;
            place-items: center;
            width: 42px;
            height: 42px;
            margin-right: 3px;
            border: 0;
            border-radius: 50%;
            background: transparent;
            color: var(--md-sys-color-on-surface);
            cursor: pointer;
            font-size: 17px;
        }
        .filter-button small {
            position: absolute;
            top: 2px;
            right: 0;
            min-width: 16px;
            height: 16px;
            border-radius: 9px;
            display: grid;
            place-items: center;
            background: var(--md-sys-color-primary);
            color: var(--md-sys-color-on-primary);
            font-size: 0.6rem;
        }
        .search-submit { display: none; }
    }

    @media (max-width: 480px) {
        .bar { min-height: 54px; }
        .search-field { padding-left: 13px; }
        .search-icon { font-size: 22px; }
        .search-field input { font-size: 0.84rem; }
        .currency-wrap { min-width: 50px; }
        .currency-button { font-size: 0.75rem; }
        .filter-panel.mobile-sheet { left: 8px; right: 8px; }
        .panel-header { padding: 18px 18px 10px; }
        .panel-body { padding: 8px 18px 16px; }
        .panel-actions { padding: 12px 18px; }
    }
</style>
