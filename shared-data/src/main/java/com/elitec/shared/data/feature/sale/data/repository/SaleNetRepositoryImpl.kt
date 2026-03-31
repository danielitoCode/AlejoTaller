package com.elitec.shared.data.feature.sale.data.repository

import com.elitec.shared.data.feature.sale.data.dto.SaleDto
import com.elitec.shared.data.feature.sale.data.mapper.toSaleDto
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.data.infraestructure.core.data.config.SaleRemoteConfig
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases

class SaleNetRepositoryImpl(
    private val netDB: Databases,
    private val config: SaleRemoteConfig
): SaleNetRepository {
    override suspend fun getAll(userId: String): List<SaleDto> {
        val response = netDB.listDocuments(
            databaseId = config.databaseId,
            collectionId = config.saleCollectionId,
            queries = listOf(Query.equal("user_id", userId))
        )
        return response.documents.map { document -> document.toSaleDto() }
    }

    override suspend fun getById(itemId: String): SaleDto {
        val response = netDB.getDocument(
            databaseId = config.databaseId,
            collectionId = config.saleCollectionId,
            documentId = itemId
        )
        return response.toSaleDto()
    }

    override suspend fun save(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        netDB.createDocument(
            databaseId = config.databaseId,
            collectionId = config.saleCollectionId,
            documentId = resolvedId,
            data = item.toAppwriteData()
        )
    }

    override suspend fun upsert(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        val payload = item.copy(id = resolvedId).toAppwriteData()
        runCatching {
            netDB.createDocument(
                databaseId = config.databaseId,
                collectionId = config.saleCollectionId,
                documentId = resolvedId,
                data = payload
            )
        }.recoverCatching { throwable ->
            val appwriteError = throwable as? AppwriteException
            if (appwriteError?.code == 409) {
                netDB.updateDocument(
                    databaseId = config.databaseId,
                    collectionId = config.saleCollectionId,
                    documentId = resolvedId,
                    data = payload
                )
            } else {
                throw throwable
            }
        }.getOrThrow()
    }
}

internal fun SaleDto.toAppwriteData(): Map<String, Any?> = mapOf(
    "date" to date.toString(),
    "amount" to amount,
    "verified" to verified,
    "products" to products.map { product ->
        mapOf(
            "productId" to product.productId,
            "quantity" to product.quantity
        )
    },
    "user_id" to userId,
    "delivery_type" to deliveryType,
    "delivery_address" to deliveryAddress
).filterValues { value -> value != null }
