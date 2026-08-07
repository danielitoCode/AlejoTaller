import { describe, expect, it, vi } from "vitest";

import { ReleaseSoftHoldCaseUse } from "../../../../../../core/feature/product/domain/caseuse/ReleaseSoftHoldCaseUse";
import type { ProductRepository } from "../../../../../../core/feature/product/domain/repository/product.repository";
import type { Sale } from "../../../../../../core/feature/sale/domain/entity/Sale";
import type { Product } from "../../../../../../core/feature/product/domain/entity/Product";

describe("ReleaseSoftHoldCaseUse atomic mutation (Core 1)", () => {
    const product: Product = {
        id: "p1",
        name: "Coca",
        description: "",
        existence: 10,
        reserved: 4,
        price: 5,
        photoUrl: "",
        categoryId: "c1"
    };

    const sale = {
        id: "sale-1",
        products: [{ productId: "p1", productName: "Coca", quantity: 3 }]
    } as Sale;

    it("delegates release to atomic decrement and never uses read-modify-write update", async () => {
        const repository = {
            decrementReserved: vi.fn().mockResolvedValue({ ...product, reserved: 1 }),
            getById: vi.fn(),
            update: vi.fn()
        } as unknown as ProductRepository;

        const useCase = new ReleaseSoftHoldCaseUse(repository);
        const result = await useCase.execute(sale);

        expect(repository.decrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(repository.getById).not.toHaveBeenCalled();
        expect(repository.update).not.toHaveBeenCalled();
        expect(result).toEqual(["p1"]);
    });

    it("does not signal a product when the atomic decrement fails", async () => {
        const repository = {
            decrementReserved: vi.fn().mockResolvedValue(null),
            getById: vi.fn(),
            update: vi.fn()
        } as unknown as ProductRepository;

        const useCase = new ReleaseSoftHoldCaseUse(repository);

        await expect(useCase.execute(sale)).resolves.toEqual([]);
        expect(repository.decrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(repository.update).not.toHaveBeenCalled();
    });
});
