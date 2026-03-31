package com.elitec.shared.sale.feature.sale.domain.caseUse

import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllSalesCaseUse(
    private val repository: SaleRepository
) {
    operator fun invoke(): Flow<List<Sale>> = repository.observeAll()
}
