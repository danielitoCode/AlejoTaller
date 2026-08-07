import type { Product } from "../entity/Product";
import type { ProductRepository } from "../repository/product.repository";

/**
 * Refresco selectivo offline-first: solo los productIds señalados por pulse/stock.
 * Fuente de verdad Appwrite → cache Dexie vía repository.getById.
 */
export class RefreshProductsByIdsCaseUse {
    constructor(private readonly repository: ProductRepository) {}

    async execute(productIds: string[]): Promise<Product[]> {
        const ids = [...new Set(productIds.filter(Boolean))];
        const updated: Product[] = [];

        for (const id of ids) {
            try {
                const product = await this.repository.getById(id);
                if (product) updated.push(product);
            } catch (e) {
                console.warn(
                    `[RefreshProductsByIds] falló id=${id}: ${e instanceof Error ? e.message : String(e)}`
                );
            }
        }

        if (import.meta.env.DEV) {
            console.info(
                `[RefreshProductsByIds] refreshed ${updated.length}/${ids.length} products`
            );
        }

        return updated;
    }
}
