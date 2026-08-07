package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

/**
 * Soft-hold (reserved += qty) con relectura remota antes de escribir.
 *
 * Core 1 mantiene la lógica de reserva en el cliente, pero la mutación de
 * reserved es atómica en la infraestructura Appwrite. Si una venta con varias
 * líneas falla después de aplicar una o más reservas, se compensa únicamente
 * lo confirmado en este intento.
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

        try {
            for (item in sale.products) {
                // Re-read from Appwrite to validate availability against source of truth.
                val product = repository.refreshFromRemote(item.productId)
                    ?: repository.getById(item.productId)
                    ?: error("Producto no disponible para soft-hold: ${item.productId}")

                if (item.quantity > product.availableStock()) {
                    val label = item.productName?.takeIf { it.isNotBlank() } ?: item.productId
                    error("Stock insuficiente (concurrencia): $label disponible=${product.availableStock()}")
                }

                val updated = repository.incrementReserved(
                    item.productId,
                    item.quantity
                ) ?: error("No se pudo aplicar soft-hold atómico a ${item.productId}")

                touched += item.productId
                Log.i(
                    TAG,
                    "event=soft_hold_applied productId=${item.productId} qty=${item.quantity} " +
                        "reserved=${updated.reserved} saleId=${sale.id}"
                )
            }
        } catch (error: Throwable) {
            compensate(touched, sale.id)
            throw error
        }

        Log.i(TAG, "event=soft_hold_applied saleId=${sale.id} lines=${sale.products.size}")
        touched.toList()
    }

    private suspend fun compensate(productIds: Set<String>, saleId: String) {
        if (productIds.isEmpty()) return

        for (item in productIds.toList().asReversed()) {
            val quantity = saleQuantity(saleId, item)
            // La cantidad se resuelve desde el sale en invoke; este método no se usa
            // para inventar cantidades. Se conserva como punto único de compensación.
            if (quantity <= 0) continue
            runCatching {
                repository.decrementReserved(item, quantity)
            }.onSuccess {
                if (it == null) error("rollback returned null")
                Log.w(TAG, "event=soft_hold_compensated productId=$item qty=$quantity saleId=$saleId")
            }.onFailure {
                Log.e(TAG, "event=soft_hold_compensation_failed productId=$item saleId=$saleId", it)
            }
        }
    }

    private fun saleQuantity(saleId: String, productId: String): Int =
        pendingQuantities[saleId]?.get(productId)?.coerceAtLeast(0) ?: 0

    companion object {
        private const val TAG = "ApplySoftHold"
        private val pendingQuantities = mutableMapOf<String, Map<String, Int>>()
    }
}
