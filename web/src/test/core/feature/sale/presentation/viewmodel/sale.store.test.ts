/**
 * saleStore — suscripción Appwrite Realtime (SALE_POLICY: cliente ve decisión del operador).
 * Se mantiene RT mientras hay sesión autenticada (no solo UNVERIFIED).
 */
import { beforeEach, describe, expect, it, vi } from "vitest";
import { get, writable, type Writable } from "svelte/store";
import { BuyState, Currency, DeliveryType } from "../../../../../../core/feature/sale/domain/entity/enums";
import type { Sale } from "../../../../../../core/feature/sale/domain/entity/Sale";

type SessionState = {
    loading: boolean;
    error: string | null;
    lastAction: string | null;
    isGuest: boolean;
};

const mocks = vi.hoisted(() => {
    const unsubscribeFn = vi.fn();
    return {
        unsubscribeFn,
        getByUser: vi.fn(),
        createSale: vi.fn(),
        updateVerified: vi.fn(),
        updateDeliveryType: vi.fn(),
        getCurrentUser: vi.fn(),
        startAppwriteSaleRealtime: vi.fn(() => unsubscribeFn),
        stopAppwriteSaleRealtime: vi.fn(),
        startStockRealtime: vi.fn(),
        sessionState: null as unknown as Writable<SessionState>
    };
});

mocks.sessionState = writable<SessionState>({
    loading: false,
    error: null,
    lastAction: null,
    isGuest: false
});

vi.mock("../../../../../../core/feature/sale/di/sale.container", () => ({
    saleContainer: {
        repositories: {
            offlineFirst: {
                getByUser: mocks.getByUser
            }
        },
        useCases: {
            getAll: { execute: vi.fn() },
            create: { execute: mocks.createSale },
            updateVerified: { execute: mocks.updateVerified },
            updateDeliveryType: { execute: mocks.updateDeliveryType },
            applyRealtimeSnapshot: { execute: vi.fn() }
        }
    }
}));

vi.mock("../../../../../../core/infrastructure/data/appwrite/appwrite-sale-realtime", () => ({
    startAppwriteSaleRealtime: mocks.startAppwriteSaleRealtime,
    stopAppwriteSaleRealtime: mocks.stopAppwriteSaleRealtime
}));

vi.mock("../../../../../../core/feature/auth/presentation/viewmodel/session.store", () => {
    const store = mocks.sessionState;
    return {
        sessionStore: {
            subscribe: (fn: (v: SessionState) => void) => store.subscribe(fn),
            getCurrentUser: mocks.getCurrentUser
        }
    };
});

vi.mock("../../../../../../core/feature/product/presentation/viewmodel/product.store", () => ({
    productStore: {
        startStockRealtime: mocks.startStockRealtime
    }
}));

vi.mock("../../../../../../core/feature/sale/presentation/viewmodel/sale-alert.store", () => ({
    saleAlertStore: { addAlert: vi.fn() }
}));

vi.mock("../../../../../../core/infrastructure/presentation/util/logger.service", () => ({
    logger: { log: vi.fn(), info: vi.fn(), warn: vi.fn(), error: vi.fn() }
}));

vi.mock("../../../../../../core/infrastructure/presentation/viewmodel/toast.store", () => ({
    toastStore: { success: vi.fn(), error: vi.fn() }
}));

function buildSale(overrides: Partial<Sale> = {}): Sale {
    return {
        id: "sale-1",
        date: "2026-03-29",
        amount: 120,
        currency: Currency.CUP,
        verified: BuyState.UNVERIFIED,
        products: [],
        userId: "user-1",
        deliveryType: null,
        ...overrides
    };
}

async function loadSaleStore() {
    const module = await import("../../../../../../core/feature/sale/presentation/viewmodel/sale.store");
    return module.saleStore;
}

describe("saleStore Appwrite realtime (SALE_POLICY)", () => {
    beforeEach(() => {
        vi.resetModules();
        mocks.unsubscribeFn.mockReset();
        mocks.getByUser.mockReset();
        mocks.createSale.mockReset();
        mocks.updateVerified.mockReset();
        mocks.updateDeliveryType.mockReset();
        mocks.getCurrentUser.mockReset();
        mocks.startAppwriteSaleRealtime.mockClear();
        mocks.stopAppwriteSaleRealtime.mockClear();
        mocks.startStockRealtime.mockClear();
        mocks.startAppwriteSaleRealtime.mockReturnValue(mocks.unsubscribeFn);

        mocks.sessionState.set({
            loading: false,
            error: null,
            lastAction: null,
            isGuest: false
        });
        mocks.getCurrentUser.mockResolvedValue({ $id: "user-1" });
    });

    it("subscribes to Appwrite sale RT when user is authenticated", async () => {
        mocks.getByUser.mockResolvedValue([
            buildSale(),
            buildSale({ id: "sale-2", verified: BuyState.VERIFIED })
        ]);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();

        expect(mocks.getByUser).toHaveBeenCalledWith("user-1");
        expect(mocks.startAppwriteSaleRealtime).toHaveBeenCalled();
        expect(get(saleStore.unverifiedCount)).toBe(1);
    });

    it("keeps Appwrite RT after verifying last pending (session-based subscription)", async () => {
        mocks.getByUser.mockResolvedValue([buildSale()]);
        mocks.updateVerified.mockResolvedValue(buildSale({ verified: BuyState.VERIFIED }));

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();
        await saleStore.setVerified("sale-1", BuyState.VERIFIED);

        expect(mocks.startAppwriteSaleRealtime).toHaveBeenCalled();
        expect(get(saleStore.unverifiedCount)).toBe(0);
    });

    it("stops Appwrite RT on reset", async () => {
        mocks.getByUser.mockResolvedValue([buildSale()]);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();
        saleStore.reset();

        expect(mocks.stopAppwriteSaleRealtime).toHaveBeenCalled();
        expect(get(saleStore.hasData)).toBe(false);
    });

    it("does not open RT channel when there is no authenticated user", async () => {
        mocks.getCurrentUser.mockResolvedValue(null);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();

        expect(mocks.getByUser).not.toHaveBeenCalled();
        expect(mocks.startAppwriteSaleRealtime).not.toHaveBeenCalled();
        expect(mocks.stopAppwriteSaleRealtime).toHaveBeenCalled();
    });

    it("updates delivery type without tearing down RT", async () => {
        mocks.getByUser.mockResolvedValue([buildSale()]);
        mocks.updateDeliveryType.mockResolvedValue(buildSale({ deliveryType: DeliveryType.PICKUP }));

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();
        await saleStore.updateDeliveryType("sale-1", DeliveryType.PICKUP);

        expect(mocks.updateDeliveryType).toHaveBeenCalledWith("sale-1", DeliveryType.PICKUP);
        expect(get(saleStore.unverifiedCount)).toBe(1);
        expect(mocks.unsubscribeFn).not.toHaveBeenCalled();
    });
});
