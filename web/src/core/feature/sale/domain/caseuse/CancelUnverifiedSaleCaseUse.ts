import type { Sale } from "../entity/Sale";
import type { SaleRepository } from "../repository/SaleRepository";
import type { ProductRepository } from "../../../product/domain/repository/product.repository";
import { BuyState } from "../entity/enums";
import { ReleaseSoftHoldCaseUse } from "../../../product/domain/caseuse/ReleaseSoftHoldCaseUse";
import { publishStockChanged } from "../../../../infrastructure/data/alset-pulse/stock-pulse";

/**
 * Cancela un pedido UNVERIFIED del cliente:
 * 1) Marca DELETED
 * 2) Libera soft-hold (reserved -= qty)
 * 3) Emite stock:changed para que otros clientes refresquen esos productos
 */
export class CancelUnverifiedSaleCaseUse {
    private readonly releaseHold: ReleaseSoftHoldCaseUse;

    constructor(
        private readonly saleRepository: SaleRepository,
        productRepository: ProductRepository
    ) {
        this.releaseHold = new ReleaseSoftHoldCaseUse(productRepository);
    }

    async execute(sale: Sale): Promise<Sale> {
        if (sale.verified !== BuyState.UNVERIFIED) {
            throw new Error("Solo se pueden cancelar pedidos pendientes (UNVERIFIED)");
        }

        const updated = await this.saleRepository.updateVerified(sale.id, BuyState.DELETED);

        let productIds: string[] = [];
        try {
            productIds = await this.releaseHold.execute(sale);
        } catch (e) {
            console.error(
                `[CancelUnverifiedSale] release soft-hold falló saleId=${sale.id}: ` +
                `${e instanceof Error ? e.message : String(e)}`
            );
        }

        void publishStockChanged({
            productIds,
            reason: "release",
            saleId: sale.id,
            timestamp: new Date().toISOString()
        });

        return { ...updated, stockHoldApplied: false };
    }
}
