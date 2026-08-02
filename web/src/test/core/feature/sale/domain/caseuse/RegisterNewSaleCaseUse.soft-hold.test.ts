/**
 * Tests parciales Core 1 — SALE_POLICY + WAREHOUSE_POLICY (cliente web).
 * Cubre soft-hold en RegisterNewSaleCaseUse: reserved += qty, available, idempotencia.
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
        const products = new Map<string, Product>([["p1", { ...baseProduct }]]);

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
            update: vi.fn().mockImplementation(async (id: string, partial: Partial<Product>) => {
                const current = products.get(id);
                if (!current) throw new Error("missing");
                const next = { ...current, ...partial };
                products.set(id, next);
                return next;
            }),
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

    it("increments reserved by line quantity after creating UNVERIFIED sale", async () => {
        const result = await useCase.execute(createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 3 }] }));

        expect(saleRepo.create).toHaveBeenCalled();
        expect(productRepo.update).toHaveBeenCalledWith("p1", { reserved: 3 });
        expect(result.stockHoldApplied).toBe(true);
    });

    it("uses available = existence - reserved when validating soft-hold", async () => {
        vi.mocked(productRepo.getById).mockResolvedValue({
            ...baseProduct,
            existence: 10,
            reserved: 7
        });

        // available = 3; quantity 3 → ok
        await expect(
            useCase.execute(createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 3 }] }))
        ).resolves.toBeDefined();

        expect(productRepo.update).toHaveBeenCalledWith("p1", { reserved: 10 });
    });

    it("does not write reserved when quantity exceeds available", async () => {
        vi.mocked(productRepo.getById).mockResolvedValue({
            ...baseProduct,
            existence: 10,
            reserved: 8
        });

        // available = 2; quantity 5 → soft-hold falla (best-effort: sale ya creada)
        const result = await useCase.execute(
            createSale({ products: [{ productId: "p1", productName: "Coca", quantity: 5 }] })
        );

        expect(saleRepo.create).toHaveBeenCalled();
        expect(productRepo.update).not.toHaveBeenCalled();
        // soft-hold no aplicado → flag no marcado
        expect(result.stockHoldApplied).toBeFalsy();
    });

    it("skips soft-hold when stockHoldApplied is already true on domain sale", async () => {
        // El case use crea con stockHoldApplied: false explícito en create;
        // la idempotencia se valida en applySoftHold con el sale ya persistido.
        // Si el producto ya tiene reserved y se reintenta apply con flag true en el objeto
        // pasado a applySoftHold interno — aquí simulamos que create devuelve flag true.
        vi.mocked(saleRepo.create).mockResolvedValue({
            ...createSale(),
            id: "sale-1",
            stockHoldApplied: true
        } as Sale);

        const result = await useCase.execute(createSale());

        expect(productRepo.update).not.toHaveBeenCalled();
        expect(result.stockHoldApplied).toBe(true);
    });

    it("availableStock helper matches policy", () => {
        expect(availableStock({ ...baseProduct, existence: 10, reserved: 4 })).toBe(6);
        expect(availableStock({ ...baseProduct, existence: 2, reserved: 5 })).toBe(0);
    });

    it("telegram failure does not block sale + soft-hold", async () => {
        vi.mocked(telegram.notify).mockRejectedValue(new Error("Telegram down"));

        const result = await useCase.execute(createSale());

        expect(result.id).toBeTruthy();
        expect(productRepo.update).toHaveBeenCalledWith("p1", { reserved: 3 });
    });
});
