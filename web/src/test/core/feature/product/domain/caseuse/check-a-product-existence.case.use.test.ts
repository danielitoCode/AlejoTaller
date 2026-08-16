import { beforeEach, describe, expect, it, vi } from "vitest";

import { CheckAProductExistenceCaseUse } from "../../../../../../core/feature/product/domain/caseuse/CheckAProductExistenceCaseUse";
import type { ProductRepository } from "../../../../../../core/feature/product/domain/repository/product.repository";
import type { Sale } from "../../../../../../core/feature/sale/domain/entity/Sale";
import type { Product } from "../../../../../../core/feature/product/domain/entity/Product";

describe("CheckAProductExistenceCaseUse", () => {
    let repository: ProductRepository;
    let useCase: CheckAProductExistenceCaseUse;

    beforeEach(() => {
        repository = {
            getById: vi.fn()
        } as unknown as ProductRepository;

        useCase = new CheckAProductExistenceCaseUse(repository);
    });

    function createSale(quantity = 2): Sale {
        return {
            currency: "USD",
            products: [
                {
                    productId: "p1",
                    productName: "Coca Cola",
                    quantity
                }
            ]
        } as Sale;
    }

    function createProduct(existence = 10, reserved = 0): Product {
        return {
            id: "p1",
            existence,
            reserved
        } as Product;
    }


    it("passes when all products have enough available stock", async () => {
        vi.mocked(repository.getById).mockResolvedValue(
            createProduct(10, 0)
        );

        await expect(useCase.execute(createSale(5)))
            .resolves
            .toBeUndefined();

        await expect(repository.getById).toHaveBeenCalledTimes(1);
        await expect(repository.getById).toHaveBeenCalledWith("p1");
    });

    it("passes when available = existence - reserved covers quantity", async () => {
        // existence 10, reserved 4 → available 6
        vi.mocked(repository.getById).mockResolvedValue(
            createProduct(10, 4)
        );

        await expect(useCase.execute(createSale(6)))
            .resolves
            .toBeUndefined();
    });

    it("throws when reserved reduces available below quantity", async () => {
        // existence 10, reserved 6 → available 4
        vi.mocked(repository.getById).mockResolvedValue(
            createProduct(10, 6)
        );

        await expect(useCase.execute(createSale(5)))
            .rejects
            .toThrow(
                "No hay disponibilidad en la tienda para el producto: Coca Cola"
            );
    });

    it("throws when stock is insufficient", async () => {
        vi.mocked(repository.getById).mockResolvedValue(
            createProduct(3, 0)
        );

        await expect(useCase.execute(createSale(5)))
            .rejects
            .toThrow(
                "No hay disponibilidad en la tienda para el producto: Coca Cola"
            );
    });

    it("throws when product does not exist", async () => {
        vi.mocked(repository.getById).mockResolvedValue(null);

        await expect(useCase.execute(createSale()))
            .rejects
            .toThrow("El producto no se encuentra disponible");
    });

    it("stops checking after the first unavailable product", async () => {
        const sale = {
            currency: "USD",
            products: [
                {
                    productId: "p1",
                    productName: "Producto 1",
                    quantity: 2
                },
                {
                    productId: "p2",
                    productName: "Producto 2",
                    quantity: 10
                },
                {
                    productId: "p3",
                    productName: "Producto 3",
                    quantity: 1
                }
            ]
        } as Sale;

        vi.mocked(repository.getById)
            .mockResolvedValueOnce({
                id: "p1",
                existence: 10,
                reserved: 0
            } as Product)
            .mockResolvedValueOnce({
                id: "p2",
                existence: 2,
                reserved: 0
            } as Product);

        await expect(useCase.execute(sale))
            .rejects
            .toThrow(
                "No hay disponibilidad en la tienda para el producto: Producto 2"
            );

        expect(repository.getById).toHaveBeenCalledTimes(2);
        expect(repository.getById).not.toHaveBeenCalledWith("p3");
    });

    it("checks every product when all exist", async () => {
        const sale = {
            currency: "USD",
            products: [
                {
                    productId: "p1",
                    productName: "Producto 1",
                    quantity: 1
                },
                {
                    productId: "p2",
                    productName: "Producto 2",
                    quantity: 2
                },
                {
                    productId: "p3",
                    productName: "Producto 3",
                    quantity: 3
                }
            ]
        } as Sale;

        vi.mocked(repository.getById).mockImplementation(async (id) => ({
            id,
            existence: 100,
            reserved: 0
        } as Product));

        await expect(useCase.execute(sale))
            .resolves
            .toBeUndefined();

        expect(repository.getById).toHaveBeenCalledTimes(3);
        expect(repository.getById).toHaveBeenNthCalledWith(1, "p1");
        expect(repository.getById).toHaveBeenNthCalledWith(2, "p2");
        expect(repository.getById).toHaveBeenNthCalledWith(3, "p3");
    });
});
