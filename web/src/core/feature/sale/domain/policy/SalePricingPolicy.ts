import type { Product } from "../../../product/domain/entity/Product"
import type { Promotion } from "../../../notification/domain/entity/Promotion"
import { effectivePrice } from "../../../notification/domain/policy/PromotionPolicy"
import type { SaleItem } from "../entity/Sale"

/**
 * Política B — al crear la venta se congela el precio unitario efectivo.
 * product.price (lista) no se muta; unitPrice del ítem es el snapshot de cobro.
 */
export function snapshotSaleItemFromProduct(
    product: Pick<Product, "id" | "name" | "price">,
    quantity: number,
    promos: Promotion[],
    nowEpochMillis: number = Date.now(),
    convertUnit: (usdUnit: number) => number = (n) => n
): SaleItem {
    const listUsd = Number(product.price)
    const listSafe = Number.isFinite(listUsd) && listUsd >= 0 ? listUsd : 0
    const unitUsd = effectivePrice(listSafe, product.id, promos, nowEpochMillis)
    const unit = convertUnit(unitUsd)
    const list = convertUnit(listSafe)
    const qty = Math.max(0, Math.floor(quantity))

    return {
        productId: product.id,
        productName: product.name ?? null,
        quantity: qty,
        unitPrice: unit,
        listUnitPrice: list,
        price: unit,
    }
}

export function saleAmountFromItems(
    items: Pick<SaleItem, "quantity" | "unitPrice" | "price">[]
): number {
    return items.reduce((sum, item) => {
        const unit = Number(item.unitPrice ?? item.price ?? 0)
        const qty = Number(item.quantity) || 0
        const safeUnit = Number.isFinite(unit) ? unit : 0
        return sum + safeUnit * qty
    }, 0)
}
