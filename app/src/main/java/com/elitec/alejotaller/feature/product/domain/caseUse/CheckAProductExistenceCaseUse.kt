package com.elitec.alejotaller.feature.product.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem

/**
 * Soft-check de disponibilidad al crear un pedido (paridad con web).
 * No descuenta stock; el descuento definitivo ocurre en el operador al confirmar.
 */
class CheckAProductExistenceCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> = runCatching {
        if (sale.products.isEmpty()) {
            error("No se puede ejecutar una compra vacía")
        }
        checkSaleListExistence(sale.products)
    }

    private suspend fun checkSaleListExistence(saleList: List<SaleItem>) {
        for (item in saleList) {
            val available = checkExistence(item.quantity, item.productId)
            if (!available) {
                val label = item.productName?.takeIf { it.isNotBlank() } ?: item.productId
                error("No hay disponibilidad en la tienda para el producto: $label")
            }
        }
    }

    private suspend fun checkExistence(quantityOfSale: Int, productId: String): Boolean {
        val product = repository.getById(productId)
            ?: error("El producto no se encuentra disponible")
        return quantityOfSale <= product.existence
    }
}
