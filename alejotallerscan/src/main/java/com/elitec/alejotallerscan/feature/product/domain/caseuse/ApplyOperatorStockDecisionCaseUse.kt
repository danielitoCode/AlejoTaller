package com.elitec.alejotallerscan.feature.product.domain.caseuse

import android.util.Log
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

/**
 * WAREHOUSE_POLICY — soft-hold en decisión del operador.
 *
 * VERIFIED: existence -= qty, reserved -= qty (consume físico + libera hold)
 * DELETED:  reserved -= qty (libera hold, no toca existence)
 *
 * Idempotente a nivel de flujo: solo se invoca tras transición desde UNVERIFIED.
 * StockMovement SALIDA_VENTA queda para colección dedicada (Core 1 opcional / Core 2).
 */
class ApplyOperatorStockDecisionCaseUse(
    private val stockRepository: OperatorStockRepository
) {
    suspend operator fun invoke(sale: Sale, confirmed: Boolean): Result<Unit> = runCatching {
        if (sale.products.isEmpty()) {
            Log.w(TAG, "event=operator_stock_skip_empty saleId=${sale.id}")
            return@runCatching
        }

        for (item in sale.products) {
            val qty = item.quantity.coerceAtLeast(0)
            if (qty == 0) continue

            val existenceDelta = if (confirmed) -qty else 0
            val reservedDelta = -qty

            val (existenceAfter, reservedAfter) = stockRepository.applyDeltas(
                productId = item.productId,
                existenceDelta = existenceDelta,
                reservedDelta = reservedDelta
            )

            Log.i(
                TAG,
                "event=operator_stock_line saleId=${sale.id} productId=${item.productId} " +
                    "qty=$qty confirmed=$confirmed existenceAfter=$existenceAfter reservedAfter=$reservedAfter"
            )
        }
    }

    companion object {
        private const val TAG = "ApplyOperatorStock"
    }
}
