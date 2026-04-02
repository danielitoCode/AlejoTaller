package com.elitec.alejotallerscan.feature.product.domain.caseuse

import com.elitec.alejotallerscan.feature.product.data.repository.OperatorProductNameRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale

class EnrichSaleProductsCaseUse(
    private val repository: OperatorProductNameRepository
) {
    suspend operator fun invoke(sale: Sale): Sale {
        val enrichedProducts = sale.products.map { item ->
            if (!item.productName.isNullOrBlank()) {
                item
            } else {
                val productName = runCatching { repository.getProductName(item.productId) }.getOrNull()
                item.copy(productName = productName ?: item.productName)
            }
        }
        return sale.copy(products = enrichedProducts)
    }
}
