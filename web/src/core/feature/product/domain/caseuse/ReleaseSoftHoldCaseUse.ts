import type { Sale } from "../../../sale/domain/entity/Sale";
import type { ProductRepository } from "../repository/product.repository";

/**
 * Rollback de soft-hold: reserved -= quantity por línea.
 *
 * Core 1: el decremento se ejecuta atómicamente en Appwrite con min=0 y
 * solamente la respuesta remota se persiste en Dexie.
 */
export class ReleaseSoftHoldCaseUse {
    constructor(private readonly repository: ProductRepository) {}

    async execute(sale: Sale): Promise<string[]> {
        const touched: string[] = [];

        for (const item of sale.products ?? []) {
            const qty = Math.max(0, Math.floor(item.quantity));
            if (qty === 0 || !item.productId) continue;

            const updated = await this.repository.decrementReserved(item.productId, qty);
            if (!updated) {
                console.warn(
                    `[ReleaseSoftHold] decremento atómico falló productId=${item.productId} saleId=${sale.id}`
                );
                continue;
            }

            if (import.meta.env.DEV) {
                console.info(
                    `[ReleaseSoftHold] atomic product=${item.productId} qty=${qty} reserved=${updated.reserved ?? 0}`
                );
            }

            touched.push(item.productId);
        }

        return [...new Set(touched)];
    }
}
