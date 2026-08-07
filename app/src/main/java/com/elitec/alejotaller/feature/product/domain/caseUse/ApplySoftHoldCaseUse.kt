package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

/**
 * Soft-hold (reserved += qty) con relectura remota antes de escribir (concurrencia).
 */
class ApplySoftHoldCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale): Result<List<String>> = runCatching {
        if (sale.stockHoldApplied) {
            Log.i(TAG, "event=soft_hold_skip_already_applied saleId=${sale.id}")
            return@runCatching emptyList()
        }
        val touched = linkedSetOf<String>()
        for (item in sale.products) {
            // Re-read from network to reduce race with other clients
            val product = repository.refreshFromRemote(item.productId)
                ?: repository.getById(item.productId)
                ?: error("Producto no disponible para soft-hold: ${item.productId}")

            if (item.quantity > product.availableStock()) {
                val label = item.productName?.takeIf { it.isNotBlank() } ?: item.productId
                error("Stock insuficiente (concurrencia): $label disponible=${product.availableStock()}")
            }
            repository.incrementReserved(item.productId, item.quantity)
                ?: error("No se pudo aplicar soft-hold a ${item.productId}")
            touched += item.productId
        }
        Log.i(TAG, "event=soft_hold_applied saleId=${sale.id} lines=${sale.products.size}")
        touched.toList()
    }

    companion object {
        private const val TAG = "ApplySoftHold"
    }
}
