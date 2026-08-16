/**
 * CreateSaleCaseUse — thin delegate to repository.create.
 * Validación de negocio (stock/amount) vive en RegisterNewSaleCaseUse + CheckAProductExistence
 * (SALE_POLICY / soft-hold). Aquí solo se prueba el contrato de persistencia.
 */
import { describe, it, expect, beforeEach, vi } from "vitest";
import { CreateSaleCaseUse } from "../../../../../../core/feature/sale/domain/caseuse/CreateSaleCaseUse";
import type { SaleRepository } from "../../../../../../core/feature/sale/domain/repository/SaleRepository";
import { BuyState, Currency } from "../../../../../../core/feature/sale/domain/entity/enums";
import type { Sale } from "../../../../../../core/feature/sale/domain/entity/Sale";

describe("CreateSaleCaseUse", () => {
    let mockRepository: {
        create: ReturnType<typeof vi.fn>;
        update: ReturnType<typeof vi.fn>;
    };
    let useCase: CreateSaleCaseUse;

    function createValidSale(): Sale {
        return {
            id: "sale-1",
            date: new Date().toISOString(),
            amount: 250,
            currency: Currency.CUP,
            verified: BuyState.UNVERIFIED,
            products: [
                { productId: "prod-1", productName: "Product 1", price: 100, quantity: 2 },
                { productId: "prod-2", productName: "Product 2", price: 50, quantity: 1 }
            ],
            userId: "user-123",
            deliveryType: null
        };
    }

    beforeEach(() => {
        mockRepository = {
            create: vi.fn().mockResolvedValue(createValidSale()),
            update: vi.fn()
        };
        useCase = new CreateSaleCaseUse(mockRepository as unknown as SaleRepository);
        vi.clearAllMocks();
        mockRepository.create.mockResolvedValue(createValidSale());
    });

    it("delegates to repository.create with the sale payload", async () => {
        const sale = createValidSale();
        await useCase.execute(sale);
        expect(mockRepository.create).toHaveBeenCalledWith(sale);
    });

    it("returns the created sale from the repository", async () => {
        const sale = createValidSale();
        const created = { ...sale, id: "persisted-1" };
        mockRepository.create.mockResolvedValue(created);
        await expect(useCase.execute(sale)).resolves.toEqual(created);
    });

    it("propagates repository persistence errors", async () => {
        mockRepository.create.mockRejectedValue(new Error("DB constraint violation"));
        await expect(useCase.execute(createValidSale())).rejects.toThrow("DB constraint violation");
    });
});
