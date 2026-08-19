package com.elitec.alejotallerscan.feature.product.domain.caseuse

import android.util.Log
import com.elitec.alejotallerscan.feature.finance.domain.entity.SaleFinanceWrite
import com.elitec.alejotallerscan.feature.finance.domain.repository.OperatorSaleFinanceRepository
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementWrite
import com.elitec.alejotallerscan.feature.inventory.domain.repository.OperatorStockMovementRepository
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockRepository
import com.elitec.shared.auth.feature.auth.domain.repositories.AccountRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import java.time.Instant

/**
 * WAREHOUSE_POLICY + Core 2 B2 — soft-hold y traza al decidir el operador.
 *
 * VERIFIED:
 *  - existence -= qty, reserved -= qty
 *  - stock_movements tipo salida_venta (balance_after, sale_id, user_id)
 *  - sale_finance_event (revenue, cogs = Σ last_unit_cost×qty, margin)
 *
 * DELETED:
 *  - reserved -= qty
 *  - sin movement salida_venta, sin finance
 *
 * Idempotencia: movements por sale_id+product_id; finance por sale_id.
 * Solo se invoca tras transición desde UNVERIFIED.
 */
class ApplyOperatorStockDecisionCaseUse(
    private val stockRepository: OperatorStockRepository,
    private val movementRepository: OperatorStockMovementRepository,
    private val financeRepository: OperatorSaleFinanceRepository,
    private val accountRepository: AccountRepository
) {
    suspend operator fun invoke(sale: Sale, confirmed: Boolean): Result<Unit> = runCatching {
        if (sale.products.isEmpty()) {
            Log.w(TAG, "event=operator_stock_skip_empty saleId=${sale.id}")
            return@runCatching
        }

        val operatorUserId = runCatching { accountRepository.getCurrentUserInfo().id }
            .getOrDefault("")
            .ifBlank { "operator" }

        val existingMovements = if (confirmed) {
            runCatching { movementRepository.listBySaleId(sale.id) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val alreadyMovedProductIds = existingMovements
            .filter { it.type == TYPE_SALIDA_VENTA }
            .map { it.productId }
            .toSet()

        var totalCogs = 0.0

        for (item in sale.products) {
            val qty = item.quantity.coerceAtLeast(0)
            if (qty == 0) continue

            val existenceDelta = if (confirmed) -qty else 0
            val reservedDelta = -qty

            val snapshot = stockRepository.applyDeltas(
                productId = item.productId,
                existenceDelta = existenceDelta,
                reservedDelta = reservedDelta
            )

            Log.i(
                TAG,
                "event=operator_stock_line saleId=${sale.id} productId=${item.productId} " +
                    "qty=$qty confirmed=$confirmed existenceAfter=${snapshot.existenceAfter} " +
                    "reservedAfter=${snapshot.reservedAfter} lastUnitCost=${snapshot.lastUnitCost}"
            )

            if (!confirmed) continue

            val unitCost = snapshot.lastUnitCost?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
            totalCogs += unitCost * qty

            if (item.productId in alreadyMovedProductIds) {
                Log.i(
                    TAG,
                    "event=operator_movement_idempotent saleId=${sale.id} productId=${item.productId}"
                )
                continue
            }

            movementRepository.create(
                StockMovementWrite(
                    productId = item.productId,
                    type = TYPE_SALIDA_VENTA,
                    quantity = qty,
                    balanceAfter = snapshot.existenceAfter,
                    reason = REASON_SALIDA_VENTA,
                    userId = operatorUserId,
                    saleId = sale.id
                )
            )
        }

        if (confirmed) {
            val revenue = sale.amount.coerceAtLeast(0.0)
            val cogs = totalCogs.coerceAtLeast(0.0)
            val margin = revenue - cogs
            financeRepository.createIdempotent(
                SaleFinanceWrite(
                    saleId = sale.id,
                    revenue = revenue,
                    cogs = cogs,
                    margin = margin,
                    userId = operatorUserId,
                    atIso = Instant.now().toString(),
                    currency = sale.currency.name
                )
            )
            Log.i(
                TAG,
                "event=operator_finance_done saleId=${sale.id} revenue=$revenue cogs=$cogs margin=$margin"
            )
        }
    }

    companion object {
        private const val TAG = "ApplyOperatorStock"
        const val TYPE_SALIDA_VENTA = "salida_venta"
        const val REASON_SALIDA_VENTA = "confirmacion_venta"
    }
}
