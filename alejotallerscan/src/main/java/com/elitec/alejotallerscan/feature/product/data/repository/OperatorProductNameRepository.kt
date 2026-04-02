package com.elitec.alejotallerscan.feature.product.data.repository

interface OperatorProductNameRepository {
    suspend fun getProductName(productId: String): String?
}
