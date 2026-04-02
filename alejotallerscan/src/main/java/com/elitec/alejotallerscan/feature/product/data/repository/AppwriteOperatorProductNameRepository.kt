package com.elitec.alejotallerscan.feature.product.data.repository

import com.elitec.alejotallerscan.BuildConfig
import io.appwrite.services.Databases

class AppwriteOperatorProductNameRepository(
    private val databases: Databases
) : OperatorProductNameRepository {
    override suspend fun getProductName(productId: String): String? {
        val response = databases.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = productId
        )
        return response.data["name"] as? String
    }
}
