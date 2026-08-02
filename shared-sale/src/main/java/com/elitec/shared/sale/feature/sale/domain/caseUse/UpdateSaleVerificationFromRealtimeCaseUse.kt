package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.SaleType
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository

/**
 * Cierra la venta desde el operador: VERIFIED o DELETED.
 *
 * SALE_POLICY al confirmar:
 * - NORMAL → amount del pedido
 * - DISCOUNT → [amountOverride] efectivo (debe ser >= 0 y < amount de lista)
 * - GIFT → amount = 0
 */
class UpdateSaleVerificationFromRealtimeCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(
        saleId: String,
        isSuccess: Boolean,
        saleType: SaleType? = null,
        amountOverride: Double? = null
    ): Result<Unit> = runCatching {
        val currentSale = repository.getById(saleId)
        val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
        if (currentSale.verified == nextState) return@runCatching

        val resolvedType = if (isSuccess) {
            saleType ?: currentSale.saleType ?: SaleType.NORMAL
        } else {
            currentSale.saleType
        }

        val resolvedAmount = when {
            !isSuccess -> currentSale.amount
            resolvedType == SaleType.GIFT -> 0.0
            resolvedType == SaleType.DISCOUNT -> {
                val effective = amountOverride
                    ?: error("DISCOUNT requiere importe efectivo (amountOverride)")
                require(effective >= 0.0) { "El importe con descuento no puede ser negativo" }
                require(effective < currentSale.amount || currentSale.amount <= 0.0) {
                    "El importe con descuento debe ser menor al precio de lista (${currentSale.amount})"
                }
                effective
            }
            else -> currentSale.amount // NORMAL
        }

        repository.save(
            currentSale.copy(
                verified = nextState,
                saleType = resolvedType,
                amount = resolvedAmount
            )
        )
    }
}
