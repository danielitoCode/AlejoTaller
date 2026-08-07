import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("../../../../../../core/infrastructure/di/dexie.db", () => ({
    db: {
        products: {
            put: vi.fn(),
            get: vi.fn(),
            toArray: vi.fn(),
            clear: vi.fn(),
            bulkPut: vi.fn(),
            delete: vi.fn(),
            filter: vi.fn()
        }
    }
}));

import { db } from "../../../../../../core/infrastructure/di/dexie.db";
import { ProductOfflineFirstRepository } from "../../../../../../core/feature/product/data/repository/product.offline-first.repository";
import type ProductNetRepository from "../../../../../../core/feature/product/data/repository/product.net.repository";
import type { ProductDTO } from "../../../../../../core/feature/product/data/dto/ProductDTO";

describe("ProductOfflineFirstRepository atomic stock mutations (Core 1)", () => {
    let net: {
        getById: ReturnType<typeof vi.fn>;
        incrementReserved: ReturnType<typeof vi.fn>;
        decrementReserved: ReturnType<typeof vi.fn>;
    };
    let repository: ProductOfflineFirstRepository;

    const product = {
        $id: "p1",
        $createdAt: "2026-08-07T00:00:00.000Z",
        $updatedAt: "2026-08-07T00:00:00.000Z",
        name: "Coca",
        description: "",
        price: 5,
        photo_url: "",
        category_id: "c1",
        rating: 0,
        existence: 10,
        status: 10,
        reserved: 2
    } as unknown as ProductDTO;

    beforeEach(() => {
        vi.clearAllMocks();
        net = {
            getById: vi.fn().mockResolvedValue(product),
            incrementReserved: vi.fn().mockResolvedValue({ ...product, reserved: 5 }),
            decrementReserved: vi.fn().mockResolvedValue({ ...product, reserved: 0 })
        };
        repository = new ProductOfflineFirstRepository(net as unknown as ProductNetRepository);
    });

    it("reads remote existence, performs atomic increment, and caches only the response", async () => {
        const result = await repository.incrementReserved("p1", 3);

        expect(net.getById).toHaveBeenCalledWith("p1");
        expect(net.incrementReserved).toHaveBeenCalledWith("p1", 3, 10);
        expect(db.products.put).toHaveBeenCalledTimes(1);
        expect(result?.reserved).toBe(5);
    });

    it("does not fall back to Dexie when atomic increment fails", async () => {
        net.incrementReserved.mockRejectedValue(new Error("conflict"));

        const result = await repository.incrementReserved("p1", 3);

        expect(result).toBeNull();
        expect(db.products.put).not.toHaveBeenCalled();
    });

    it("performs release through atomic decrement and caches the authoritative response", async () => {
        const result = await repository.decrementReserved("p1", 3);

        expect(net.decrementReserved).toHaveBeenCalledWith("p1", 3);
        expect(db.products.put).toHaveBeenCalledTimes(1);
        expect(result?.reserved).toBe(0);
    });
});
