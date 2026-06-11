import { derived, writable } from "svelte/store";
import { exchangeContainer } from "../../di/ExchangeContainer";
import type { CupExchange, DisplayCurrency } from "../../domain/entity/CupExchange";

interface ExchangeState {
    selectedCurrency: DisplayCurrency;
    exchange: CupExchange | null;
    loading: boolean;
    error: string | null;
}

const STORAGE_KEY = "alejo-taller-display-currency";

function readInitialCurrency(): DisplayCurrency {
    if (typeof window === "undefined") return "CUP";
    const stored = window.localStorage.getItem(STORAGE_KEY);
    return stored === "USD" ? "USD" : "CUP";
}

function createExchangeStore() {
    const { subscribe, set, update } = writable<ExchangeState>({
        selectedCurrency: readInitialCurrency(),
        exchange: null,
        loading: false,
        error: null
    });

    async function refresh(): Promise<void> {
        update((state) => ({ ...state, loading: true, error: null }));
        try {
            const exchange = await exchangeContainer.useCases.getToday.execute();
            update((state) => ({ ...state, exchange, loading: false, error: null }));
        } catch (error) {
            update((state) => ({
                ...state,
                loading: false,
                error: error instanceof Error ? error.message : "No se pudo consultar la tasa de cambio"
            }));
        }
    }

    function setCurrency(currency: DisplayCurrency): void {
        if (typeof window !== "undefined") window.localStorage.setItem(STORAGE_KEY, currency);
        update((state) => ({ ...state, selectedCurrency: currency }));
    }

    function toggleCurrency(): void {
        update((state) => {
            const selectedCurrency: DisplayCurrency = state.selectedCurrency === "CUP" ? "USD" : "CUP";
            if (typeof window !== "undefined") window.localStorage.setItem(STORAGE_KEY, selectedCurrency);
            return { ...state, selectedCurrency };
        });
    }

    return {
        subscribe,
        refresh,
        setCurrency,
        toggleCurrency,
        reset: () => set({ selectedCurrency: "CUP", exchange: null, loading: false, error: null })
    };
}

export const exchangeStore = createExchangeStore();

export const selectedCurrencyStore = derived(exchangeStore, ($state) => $state.selectedCurrency);

export function convertCupAmount(amountCup: number, state: ExchangeState): number {
    if (state.selectedCurrency === "USD") {
        const rate = state.exchange?.usdReference;
        return rate && rate > 0 ? amountCup / rate : amountCup;
    }
    return amountCup;
}

export function formatMoney(amountCup: number, state: ExchangeState): string {
    const currency = state.selectedCurrency;
    const amount = convertCupAmount(amountCup, state);
    return new Intl.NumberFormat("es-CU", {
        style: "currency",
        currency,
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount);
}