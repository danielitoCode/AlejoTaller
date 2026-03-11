package com.elitec.alejotaller.feature.product.domain.caseUse

import android.nfc.FormatException
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

class GetProductByIdCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(id: String): Result<Product> = runCatching {
        repository.getById(id) ?: throw FormatException("Not id saved")
    }
}