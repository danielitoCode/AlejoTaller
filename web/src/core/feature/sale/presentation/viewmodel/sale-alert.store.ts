import { writable, derived } from 'svelte/store';
import type { Sale } from '../../domain/entity/Sale';

export interface SaleVerificationAlert {
    saleId: string;
    decision: 'confirmed' | 'rejected';
    timestamp: string;
    amount?: number;
    productCount?: number;
}

interface SaleAlertState {
    alerts: SaleVerificationAlert[];
    loading: boolean;
    error: string | null;
}

const initialState: SaleAlertState = {
    alerts: [],
    loading: false,
    error: null,
};

function createSaleAlertStore() {
    const { subscribe, update } = writable<SaleAlertState>(initialState);

    function addAlert(alert: SaleVerificationAlert): void {
        update((state) => ({
            ...state,
            alerts: [alert, ...state.alerts],
        }));
    }

    function removeAlert(saleId: string): void {
        update((state) => ({
            ...state,
            alerts: state.alerts.filter((a) => a.saleId !== saleId),
        }));
    }

    function clearAlerts(): void {
        update(() => initialState);
    }

    return {
        subscribe,
        addAlert,
        removeAlert,
        clearAlerts,
    };
}

export const saleAlertStore = createSaleAlertStore();

/**
 * Derivado que obtiene las alertas activas (sin consumir todas)
 */
export const activeAlerts = derived(saleAlertStore, ($store) => $store.alerts);

/**
 * Derivado que obtiene si hay alertas sin leer
 */
export const hasUnreadAlerts = derived(saleAlertStore, ($store) => $store.alerts.length > 0);

