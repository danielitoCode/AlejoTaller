package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository

class UpdateSaleVerificationFromRealtimeCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(
        saleId: String,
        isSuccess: Boolean
    ): Result<Unit> = runCatching {
        val currentSale = repository.getById(saleId)
        val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
        if (currentSale.verified == nextState) return@runCatching

        repository.save(currentSale.copy(verified = nextState))
    }
}