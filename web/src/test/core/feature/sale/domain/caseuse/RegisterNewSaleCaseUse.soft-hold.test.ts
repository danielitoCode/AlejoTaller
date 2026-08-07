/**
 * Tests Core 1 — SALE_POLICY + WAREHOUSE_POLICY (cliente web).
 * Cubre validación de disponibilidad, delegación del soft-hold a la operación
 * atómica del repositorio, compensación multi-producto, idempotencia y best-effort.
 */
import { beforeEach, describe, expect, it, vi } from "vitest";

import { RegisterNewSaleCaseUse } from "../../../../../../core/feature/sale/domain/caseuse/RegisterNewSaleCaseUse";
import type { SaleRepository } from "../../../../../../core/feature/sale/domain/repository/SaleRepository";
import type { SaleNotificationUserProvider } from "../../../../../../core/feature/sale/domain/repository/SaleNotificationUserProvider";
import type { TelegramNotificator } from "../../../../../../core/feature/sale/domain/repository/TelegramNotificator";
import type { ProductRepository } from "../../../../../../core/feature/product/domain/repository/product.repository";
import type { Sale } from "../../../../../../core/feature/sale/domain/entity/Sale";
import type { Product } from "../../../../../../core/feature/product/domain/entity/Product";
import { availableStock } from "../../../../../../core/feature/product/domain/entity/Product";

describe("RegisterNewSaleCaseUse soft-hold (WAREHOUSE_POLICY)", () => {
    let saleRepo: SaleRepository;
    let productRepo: ProductRepository;
    let notificationUserProvider: SaleNotificationUserProvider;
    let telegram: TelegramNotificator;
    let useCase: RegisterNewSaleCaseUse;

    const baseProduct: Product = {
        id: "p1",
        name: "Coca",
        description: "",
        existence: 10,
        reserved: 0,
        price: 5,
        photoUrl: "",
        categoryId: "c1"
    };

    function createSale(overrides: Partial<Sale> = {}): Sale {
        return {
            id: "sale-1",
            date: new Date().toISOString(),
            amount: 10,
            verified: "UNVERIFIED",
            products: [
                {
                    productId: "p1",
                    productName: "Coca",
                    quantity: 3
                }
            ],
            currency: "USD",
            userId: "user-1",
            stockHoldApplied: false,
            ...overrides
        } as Sale;
    }

    beforeEach(() => {
        const products = new Map<string, Product>([
            ["p1", { ...baseProduct }],
            ["p2", { ...baseProduct, id: "p2", name: "Agua" }]
        ]);

        saleRepo = {
            create: vi.fn().mockImplementation(async (sale: Sale) => ({
                ...sale,
                id: sale.id || "sale-created"
            })),
            updateVerified: vi.fn(),
            getById: vi.fn(),
            getAll: vi.fn()
        } as unknown as SaleRepository;

        productRepo = {
            getById: vi.fn().mockImplementation(async (id: string) => products.get(id) ?? null),
            refreshFromRemote: vi.fn().mockImplementation(async (id: string) => products.get(id) ?? null),
            incrementReserved: vi.fn().mockImplementation(async (id: string, quantity: number) => {
                const current = products.get(id);
                if (!current) return null;
                const updated = { ...current, reserved: current.reserved + quantity };
                products.set(id, updated);
                return updated;
            }),
            decrementReserved: vi.fn().mockImplementation(async (id: string, quantity: number) => {
                const current = products.get(id);
                if (!current) return null;
                const updated = { ...current, reserved: Math.max(0, current.reserved - quantity) };
                products.set(id, updated);
                return updated;
            }),
            update: vi.fn(),
            getAll: vi.fn(),
            getByCategory: vi.fn(),
            create: vi.fn(),
            delete: vi.fn()
        } as unknown as ProductRepository;

        notificationUserProvider = {
            getCurrentUser: vi.fn().mockResolvedValue({ name: "U", email: "u@t.dev", phone: "" })
        } as unknown as SaleNotificationUserProvider;

        telegram = {
            notify: vi.fn().mockResolvedValue(undefined)
        } as unknown as TelegramNotificator;

        useCase = new RegisterNewSaleCaseUse(
            saleRepo,
            notificationUserProvider,
            telegram,
            productRepo
        );
    });

    it("increments reserved through the atomic repository operation after creating UNVERIFIED sale", async () => {
        const result = await useCase.execute(createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 3 }] }));

        expect(saleRepo.create).toHaveBeenCalled();
        expect(productRepo.refreshFromRemote).toHaveBeenCalledWith("p1");
        expect(productRepo.incrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(productRepo.update).not.toHaveBeenCalled();
        expect(result.stockHoldApplied).toBe(true);
    });

    it("uses available = existence - reserved when validating soft-hold", async () => {
        vi.mocked(productRepo.refreshFromRemote!).mockResolvedValue({
            ...baseProduct,
            existence: 10,
            reserved: 7
        });

        await expect(
            useCase.execute(createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 3 }] }))
        ).resolves.toBeDefined();

        expect(productRepo.incrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(productRepo.update).not.toHaveBeenCalled();
    });

    it("does not mutate reserved when quantity exceeds available", async () => {
        vi.mocked(productRepo.refreshFromRemote!).mockResolvedValue({
            ...baseProduct,
            existence: 10,
            reserved: 8
        });

        const result = await useCase.execute(
            createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 5 }] })
        );

        expect(saleRepo.create).toHaveBeenCalled();
        expect(productRepo.incrementReserved).not.toHaveBeenCalled();
        expect(productRepo.update).not.toHaveBeenCalled();
        expect(result.stockHoldApplied).toBeFalsy();
    });

    it("fails the soft-hold when the atomic repository mutation is rejected", async () => {
        vi.mocked(productRepo.incrementReserved).mockResolvedValue(null);

        const result = await useCase.execute(createSale());

        expect(productRepo.incrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(result.stockHoldApplied).toBe(false);
        expect((result as Sale & { softHoldError?: string }).softHoldError).toContain("Soft-hold atómico rechazado");
    });

    it("compensates confirmed previous lines when a later product hold fails", async () => {
        vi.mocked(productRepo.incrementReserved).mockImplementation(async (id: string, quantity: number) => {
            if (id === "p2") return null;
            return { ...baseProduct, id, reserved: quantity };
        });

        const result = await useCase.execute(
            createSale({
                products: [
                    { productId: "p1", productName: "Coca", quantity: 2 },
                    { productId: "p2", productName: "Agua", quantity: 4 }
                ]
            })
        );

        expect(result.stockHoldApplied).toBe(false);
        expect(productRepo.incrementReserved).toHaveBeenNthCalledWith(1, "p1", 2);
        expect(productRepo.incrementReserved).toHaveBeenNthCalledWith(2, "p2", 4);
        expect(productRepo.decrementReserved).toHaveBeenCalledWith("p1", 2);
        expect(productRepo.decrementReserved).toHaveBeenCalledTimes(1);
    });

    it("skips soft-hold when stockHoldApplied is already true on persisted sale", async () => {
        vi.mocked(saleRepo.create).mockResolvedValue({
            ...createSale(),
            id: "sale-1",
            stockHoldApplied: true
        } as Sale);

        const result = await useCase.execute(createSale());

        expect(productRepo.incrementReserved).not.toHaveBeenCalled();
        expect(result.stockHoldApplied).toBe(true);
    });

    it("availableStock helper matches policy", () => {
        expect(availableStock({ ...baseProduct, existence: 10, reserved: 4 })).toBe(6);
        expect(availableStock({ ...baseProduct, existence: 2, reserved: 5 })).toBe(0);
    });

    it("telegram failure does not block sale + atomic soft-hold", async () => {
        vi.mocked(telegram.notify).mockRejectedValue(new Error("Telegram down"));

        const result = await useCase.execute(createSale());

        expect(result.id).toBeTruthy();
        expect(productRepo.incrementReserved).toHaveBeenCalledWith("p1", 3);
    });
});
