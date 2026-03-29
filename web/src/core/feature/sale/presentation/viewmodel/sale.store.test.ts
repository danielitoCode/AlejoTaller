import { beforeEach, describe, expect, it, vi } from "vitest";
import { get } from "svelte/store";
import { BuyState, DeliveryType } from "../../domain/entity/enums";
import type { Sale } from "../../domain/entity/Sale";

const mocks = vi.hoisted(() => {
    const unsubscribeFn = vi.fn();

    return {
        unsubscribeFn,
        getByUser: vi.fn(),
        createSale: vi.fn(),
        updateVerified: vi.fn(),
        updateDeliveryType: vi.fn(),
        getCurrentUser: vi.fn(),
        subscribeSaleVerification: vi.fn(() => unsubscribeFn),
        unsubscribeSaleVerification: vi.fn(),
        addAlert: vi.fn(),
        loggerLog: vi.fn(),
        loggerInfo: vi.fn(),
        loggerWarn: vi.fn(),
        loggerError: vi.fn()
    };
});

vi.mock("../../di/sale.container", () => ({
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
            updateDeliveryType: { execute: mocks.updateDeliveryType }
        }
    }
}));

vi.mock("../../../../infrastructure/data/alset-pulse/pulse.realtime", () => ({
    subscribeSaleVerification: mocks.subscribeSaleVerification,
    unsubscribeSaleVerification: mocks.unsubscribeSaleVerification
}));

vi.mock("../../../auth/presentation/viewmodel/session.store", () => ({
    sessionStore: {
        getCurrentUser: mocks.getCurrentUser
    }
}));

vi.mock("./sale-alert.store", () => ({
    saleAlertStore: {
        addAlert: mocks.addAlert
    }
}));

vi.mock("../../../../infrastructure/presentation/util/logger.service", () => ({
    logger: {
        log: mocks.loggerLog,
        info: mocks.loggerInfo,
        warn: mocks.loggerWarn,
        error: mocks.loggerError
    }
}));

function buildSale(overrides: Partial<Sale> = {}): Sale {
    return {
        id: "sale-1",
        date: "2026-03-29",
        amount: 120,
        verified: BuyState.UNVERIFIED,
        products: [],
        userId: "user-1",
        deliveryType: null,
        ...overrides
    };
}

async function loadSaleStore() {
    const module = await import("./sale.store");
    return module.saleStore;
}

describe("saleStore realtime subscription", () => {
    beforeEach(() => {
        vi.resetModules();
        mocks.unsubscribeFn.mockReset();
        mocks.getByUser.mockReset();
        mocks.createSale.mockReset();
        mocks.updateVerified.mockReset();
        mocks.updateDeliveryType.mockReset();
        mocks.getCurrentUser.mockReset();
        mocks.subscribeSaleVerification.mockClear();
        mocks.unsubscribeSaleVerification.mockClear();
        mocks.addAlert.mockClear();
        mocks.loggerLog.mockClear();
        mocks.loggerInfo.mockClear();
        mocks.loggerWarn.mockClear();
        mocks.loggerError.mockClear();

        mocks.getCurrentUser.mockResolvedValue({ $id: "user-1" });
        mocks.subscribeSaleVerification.mockReturnValue(mocks.unsubscribeFn);
    });

    it("se suscribe solo cuando el usuario actual tiene ventas pendientes", async () => {
        mocks.getByUser.mockResolvedValue([
            buildSale(),
            buildSale({ id: "sale-2", verified: BuyState.VERIFIED })
        ]);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();

        expect(mocks.getByUser).toHaveBeenCalledWith("user-1");
        expect(mocks.subscribeSaleVerification).toHaveBeenCalledTimes(1);
        expect(mocks.subscribeSaleVerification).toHaveBeenCalledWith("user-1", expect.any(Function));
        expect(get(saleStore.unverifiedCount)).toBe(1);
    });

    it("no se suscribe si no hay ventas pendientes y corta la suscripcion al verificar la ultima", async () => {
        mocks.getByUser.mockResolvedValue([buildSale()]);
        mocks.updateVerified.mockResolvedValue(buildSale({ verified: BuyState.VERIFIED }));

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();
        await saleStore.setVerified("sale-1", BuyState.VERIFIED);

        expect(mocks.subscribeSaleVerification).toHaveBeenCalledTimes(1);
        expect(mocks.unsubscribeFn).toHaveBeenCalledTimes(1);
        expect(mocks.unsubscribeSaleVerification).toHaveBeenCalledTimes(1);
        expect(get(saleStore.unverifiedCount)).toBe(0);
    });

    it("resetea la suscripcion realtime al limpiar el store", async () => {
        mocks.getByUser.mockResolvedValue([buildSale()]);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();
        saleStore.reset();

        expect(mocks.unsubscribeFn).toHaveBeenCalledTimes(1);
        expect(mocks.unsubscribeSaleVerification).toHaveBeenCalledTimes(1);
        expect(get(saleStore.hasData)).toBe(false);
    });

    it("no abre canal si no hay usuario autenticado", async () => {
        mocks.getCurrentUser.mockResolvedValue(null);

        const saleStore = await loadSaleStore();
        await saleStore.syncAll();

        expect(mocks.getByUser).not.toHaveBeenCalled();
        expect(mocks.subscribeSaleVerification).not.toHaveBeenCalled();
        expect(mocks.unsubscribeSaleVerification).toHaveBeenCalledTimes(1);
    });

    it("mantiene intacto el flujo de entrega sin tocar la suscripcion", async () => {
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
