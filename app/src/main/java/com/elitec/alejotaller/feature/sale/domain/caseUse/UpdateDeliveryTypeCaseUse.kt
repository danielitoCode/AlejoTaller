package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository

class UpdateDeliveryTypeCaseUse(
    private val repository: SaleRepository
) {
    suspend operator fun invoke(
        saleId: String,
        deliveryType: DeliveryType
    ): Result<Unit> = runCatching {
        val currentSale = repository.getById(saleId)
        val updatedSale = currentSale.copy(deliveryType = deliveryType)
        repository.save(updatedSale)
    }
}