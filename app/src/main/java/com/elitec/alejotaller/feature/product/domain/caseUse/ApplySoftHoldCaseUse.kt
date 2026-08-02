package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

/**
 * Aplica soft-hold (reserved += qty) por cada línea del pedido UNVERIFIED.
 * Best-effort por producto; falla si algún ítem no tiene available suficiente.
 */
class ApplySoftHoldCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale): Result<Unit> = runCatching {
        if (sale.stockHoldApplied) {
            Log.i(TAG, "event=soft_hold_skip_already_applied saleId=${sale.id}")
            return@runCatching
        }
        for (item in sale.products) {
            val product = repository.getById(item.productId)
                ?: error("Producto no disponible para soft-hold: ${item.productId}")
            if (item.quantity > product.availableStock()) {
                val label = item.productName?.takeIf { it.isNotBlank() } ?: item.productId
                error("Stock insuficiente para soft-hold: $label")
            }
            repository.incrementReserved(item.productId, item.quantity)
                ?: error("No se pudo aplicar soft-hold a ${item.productId}")
        }
        Log.i(TAG, "event=soft_hold_applied saleId=${sale.id} lines=${sale.products.size}")
    }

    companion object {
        private const val TAG = "ApplySoftHold"
    }
}
