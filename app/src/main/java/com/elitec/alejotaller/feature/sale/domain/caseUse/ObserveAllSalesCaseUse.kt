package com.elitec.alejotaller.feature.sale.domain.caseUse

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllSalesCaseUse(
    private val repository: SaleRepository
) {
    operator fun invoke(): Flow<List<Sale>> = repository.observeAll()
}