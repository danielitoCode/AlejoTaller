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

        console.info(`[stock-rt] refreshByIds start count=${ids.length} ids=${ids.join(",")}`);

        for (const id of ids) {
            try {
                const product = await this.repository.getById(id);
                if (product) {
                    updated.push(product);
                    console.info(
                        `[stock-rt] refreshByIds OK id=${id} existence=${product.existence} ` +
                            `reserved=${product.reserved ?? 0} available=${product.existence - (product.reserved ?? 0)}`
                    );
                } else {
                    console.warn(`[stock-rt] refreshByIds null id=${id}`);
                }
            } catch (e) {
                console.warn(
                    `[stock-rt] refreshByIds FAIL id=${id}: ${e instanceof Error ? e.message : String(e)}`
                );
            }
        }

        console.info(
            `[stock-rt] refreshByIds done refreshed=${updated.length}/${ids.length}`
        );

        return updated;
    }
}
