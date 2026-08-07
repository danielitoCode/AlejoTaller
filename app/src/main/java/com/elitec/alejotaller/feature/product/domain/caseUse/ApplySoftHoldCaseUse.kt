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

        val touched = linkedMapOf<String, Int>()

        try {
            for (item in sale.products) {
                // Una reserva es una mutación crítica: si Appwrite no responde,
                // no se autoriza la decisión usando la copia local de Room.
                val product = repository.refreshFromRemote(item.productId)
                    ?: error("Producto no disponible desde Appwrite para soft-hold: ${item.productId}")

                if (item.quantity > product.availableStock()) {
                    val label = item.productName?.takeIf { it.isNotBlank() } ?: item.productId
                    error("Stock insuficiente (concurrencia): $label disponible=${product.availableStock()}")
                }

                val updated = repository.incrementReserved(
                    item.productId,
                    item.quantity
                ) ?: error("No se pudo aplicar soft-hold atómico a ${item.productId}")

                touched.merge(item.productId, item.quantity, Int::plus)
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
        touched.keys.toList()
    }

    private suspend fun compensate(quantities: Map<String, Int>, saleId: String) {
        for ((productId, quantity) in quantities.entries.toList().asReversed()) {
            runCatching {
                repository.decrementReserved(productId, quantity)
            }.onSuccess {
                if (it == null) error("rollback returned null")
                Log.w(
                    TAG,
                    "event=soft_hold_compensated productId=$productId qty=$quantity saleId=$saleId"
                )
            }.onFailure {
                Log.e(
                    TAG,
                    "event=soft_hold_compensation_failed productId=$productId saleId=$saleId",
                    it
                )
            }
        }
    }

    companion object {
        private const val TAG = "ApplySoftHold"
    }
}
