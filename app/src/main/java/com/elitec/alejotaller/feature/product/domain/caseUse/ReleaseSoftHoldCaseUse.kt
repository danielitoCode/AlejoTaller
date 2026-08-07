package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

/**
 * Rollback soft-hold: reserved -= qty por línea (cancelación / rechazo).
 * Clamp reserved >= 0. Devuelve productIds tocados para señal stock:changed.
 */
class ReleaseSoftHoldCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale): Result<List<String>> = runCatching {
        val touched = linkedSetOf<String>()
        for (item in sale.products) {
            val qty = item.quantity.coerceAtLeast(0)
            if (qty == 0) continue
            repository.decrementReserved(item.productId, qty)
                ?: error("No se pudo liberar soft-hold de ${item.productId}")
            touched += item.productId
            Log.i(TAG, "event=soft_hold_released productId=${item.productId} qty=$qty saleId=${sale.id}")
        }
        touched.toList()
    }

    companion object {
        private const val TAG = "ReleaseSoftHold"
    }
}
