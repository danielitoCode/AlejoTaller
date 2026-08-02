import type {ProductRepository} from "../repository/product.repository";
import type {Sale, SaleItem} from "../../../sale/domain/entity/Sale";
import {availableStock} from "../entity/Product";

/**
 * Soft-check de disponibilidad usando stock disponible (existence - reserved).
 * No descuenta stock; solo valida que hay cupo para el soft-hold.
 */
export class CheckAProductExistenceCaseUse {
    constructor(private readonly repository: ProductRepository) {}

    async execute(sale: Sale): Promise<void> {
        if (sale.currency.length === 0) {
            throw new Error("No se puede ejecutar una compra vacía");
        }

        await this.checkSaleListExistence(sale.products);
    }

    private async checkSaleListExistence(saleList: SaleItem[]): Promise<void> {
        for (const item of saleList) {
            const exists = await this.checkExistence(
                item.quantity,
                item.productId
            );

            if (!exists) {
                throw new Error(
                    `No hay disponibilidad en la tienda para el producto: ${item.productName}`
                );
            }
        }
    }

    private async checkExistence(
        quantityOfSale: number,
        productId: string
    ): Promise<boolean> {
        const product = await this.repository.getById(productId);

        if (product === null) {
            throw new Error("El producto no se encuentra disponible");
        }

        return quantityOfSale <= availableStock(product);
    }
}
