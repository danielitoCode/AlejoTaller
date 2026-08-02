package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.SaleType
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository

/**
 * Cierra la venta desde el operador: VERIFIED o DELETED.
 * Al confirmar, persiste [saleType] (SALE_POLICY) y ajusta amount si es GIFT.
 */
class UpdateSaleVerificationFromRealtimeCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(
        saleId: String,
        isSuccess: Boolean,
        saleType: SaleType? = null
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
            else -> currentSale.amount
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
