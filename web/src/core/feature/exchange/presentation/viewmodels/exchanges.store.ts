import { derived, writable } from "svelte/store";
import { exchangeContainer } from "../../di/ExchangeContainer";
import type { CupExchange, DisplayCurrency } from "../../domain/entity/CupExchange";
import { logger } from "../../../../infrastructure/presentation/util/logger.service";

interface ExchangeState {
    selectedCurrency: DisplayCurrency;
    exchange: CupExchange | null;
    loading: boolean;
    error: string | null;
}

const STORAGE_KEY = "alejo-taller-display-currency";
const LAST_REFRESH_DATE_KEY = "alejo-taller-exchange-last-refresh-date";

function readInitialCurrency(): DisplayCurrency {
    if (typeof window === "undefined") return "CUP";
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return stored === "USD" ? "USD" : "CUP";
}

function todayCacheKey(): string {
    return new Date().toISOString().slice(0, 10);
}

function markRefreshedToday(): void {
    if (typeof window !== "undefined") {
        window.localStorage.setItem(LAST_REFRESH_DATE_KEY, todayCacheKey());
    }
}

function wasRefreshedToday(): boolean {
    return typeof window !== "undefined" && window.localStorage.getItem(LAST_REFRESH_DATE_KEY) === todayCacheKey();
}

function createExchangeStore() {
    const { subscribe, set, update } = writable<ExchangeState>({
        selectedCurrency: readInitialCurrency(),
        exchange: null,
        loading: false,
        error: null
    });

    async function hydrateCachedToday(): Promise<boolean> {
        try {
            logger.info("[ExchangesStore] Buscando tasa de hoy en cache...");
            const exchange = await exchangeContainer.useCases.getCachedToday.execute();
            if (!exchange) {
                logger.warn("[ExchangesStore] No hay tasa de hoy guardada en la base de datos.");
                return false;
            }
            logger.info(`[ExchangesStore] Tasa cargada de cache: 1 USD = ${exchange.usdReference} CUP (Actualizado: ${exchange.updatedAt})`);
            update((state) => ({ ...state, exchange, error: null }));
            return true;
        } catch (error) {
            logger.error(`[ExchangesStore] Error al hidratar cache: ${error instanceof Error ? error.message : String(error)}`);
            return false;
        }
    }

    async function refresh(): Promise<void> {
        logger.info("[ExchangesStore] Solicitando tasa de cambio informal hoy desde API...");
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const exchange = await exchangeContainer.useCases.getToday.execute();
            markRefreshedToday();
            logger.info(`[ExchangesStore] Tasa consultada de API exitosamente: 1 USD = ${exchange.usdReference} CUP`);
            update((state) => ({ ...state, exchange, loading: false, error: null }));
        } catch (error) {
            const msg = error instanceof Error ? error.message : "No se pudo consultar la tasa de cambio";
            logger.error(`[ExchangesStore] Fallo consulta de tasa: ${msg}`);
            update((state) => ({
                ...state,
                loading: false,
                error: msg
            }));
        }
    }

    async function refreshForSplash(): Promise<void> {
        logger.info("[ExchangesStore] refreshForSplash invocado.");
        if (wasRefreshedToday() && await hydrateCachedToday()) {
            logger.info("[ExchangesStore] Tasa ya refrescada hoy. Usando cache.");
            return;
        }
        await refresh();
    }

    function setCurrency(currency: DisplayCurrency): void {
        logger.info(`[ExchangesStore] Moneda de visualizacion cambiada a: ${currency}`);
        if (typeof window !== "undefined") window.localStorage.setItem(STORAGE_KEY, currency);
        update((state) => ({ ...state, selectedCurrency: currency }));
    }

    function toggleCurrency(): void {
        update((state) => {
            const selectedCurrency: DisplayCurrency = state.selectedCurrency === "CUP" ? "USD" : "CUP";
            logger.info(`[ExchangesStore] Moneda de visualizacion conmutada a: ${selectedCurrency}`);
            if (typeof window !== "undefined") window.localStorage.setItem(STORAGE_KEY, selectedCurrency);
            return { ...state, selectedCurrency };
        });
    }

    return {
        subscribe,
        refresh,
        refreshForSplash,
        hydrateCachedToday,
        setCurrency,
        toggleCurrency,
        reset: () => set({ selectedCurrency: "CUP", exchange: null, loading: false, error: null })
    };
}

export const exchangeStore = createExchangeStore();

export const selectedCurrencyStore = derived(exchangeStore, ($state) => $state.selectedCurrency);

export function convertProductAmount(amountUsd: number, state: ExchangeState): number {
    if (state.selectedCurrency === "CUP") {
        const rate = state.exchange?.usdReference;
        return rate && rate > 0 ? amountUsd * rate : amountUsd;
    }
    return amountUsd;
}

export function formatMoney(amountUsd: number, state: ExchangeState): string {
    const currency = state.selectedCurrency;
    const amount = convertProductAmount(amountUsd, state);
    return new Intl.NumberFormat("es-CU", {
        style: "currency",
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount);
}
