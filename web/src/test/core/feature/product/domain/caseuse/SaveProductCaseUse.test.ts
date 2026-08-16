/**
 * SaveProductCaseUse — create product when categoryId is set; no-op if empty category.
 * Validación de precio/stock no está en este case use (panel / operador).
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { SaveProductCaseUse } from "../../../../../../core/feature/product/domain/caseuse/SaveProductCaseUse";
import type { ProductRepository } from "../../../../../../core/feature/product/domain/repository/product.repository";
import type { Product } from "../../../../../../core/feature/product/domain/entity/Product";

describe("SaveProductCaseUse", () => {
    let mockRepository: { create: ReturnType<typeof vi.fn> };
    let useCase: SaveProductCaseUse;

    function createValidProduct(overrides: Partial<Product> = {}): Product {
        return {
            id: "p1",
            name: "Producto",
            price: 100,
            existence: 10,
            reserved: 0,
            categoryId: "cat-1",
            ...overrides
        } as Product;
    }

    beforeEach(() => {
        mockRepository = {
            create: vi.fn().mockResolvedValue(undefined)
        };
        useCase = new SaveProductCaseUse(mockRepository as unknown as ProductRepository);
    });

    it("creates product when categoryId is present", async () => {
        const product = createValidProduct();
        await useCase.execute(product);
        expect(mockRepository.create).toHaveBeenCalledWith(product);
    });

    it("skips create when categoryId is empty", async () => {
        await useCase.execute(createValidProduct({ categoryId: "" }));
        expect(mockRepository.create).not.toHaveBeenCalled();
    });

    it("propagates repository errors", async () => {
        mockRepository.create.mockRejectedValue(new Error("write failed"));
        await expect(useCase.execute(createValidProduct())).rejects.toThrow("write failed");
    });
});
