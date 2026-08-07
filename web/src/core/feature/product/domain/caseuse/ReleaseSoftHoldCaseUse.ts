import type { Sale } from "../../../sale/domain/entity/Sale";
import type { ProductRepository } from "../repository/product.repository";

/**
 * Rollback de soft-hold: reserved -= quantity por línea.
 * Usado cuando el pedido se cancela (cliente) o se rechaza (operador / DELETED).
 * Idempotente a nivel de llamada: clamp reserved >= 0.
 */
export class ReleaseSoftHoldCaseUse {
    constructor(private readonly repository: ProductRepository) {}

    async execute(sale: Sale): Promise<string[]> {
        const touched: string[] = [];

        for (const item of sale.products ?? []) {
            const qty = Math.max(0, Math.floor(item.quantity));
            if (qty === 0 || !item.productId) continue;

            const product = await this.repository.getById(item.productId);
            if (!product) {
                console.warn(
                    `[ReleaseSoftHold] producto ausente productId=${item.productId} saleId=${sale.id}`
                );
                continue;
            }

            const nextReserved = Math.max(0, (product.reserved ?? 0) - qty);

            if (import.meta.env.DEV) {
                console.info(
                    `[ReleaseSoftHold] product=${item.productId} reserved ${product.reserved ?? 0} → ${nextReserved}`
                );
            }

            await this.repository.update(item.productId, { reserved: nextReserved });
            touched.push(item.productId);
        }

        return [...new Set(touched)];
    }
}
