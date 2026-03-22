package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.product.data.mapper.toProductDto
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.data.mapper.toSaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleNetRepository
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Databases

class SaleNetRepositoryImpl(
    private val netDB: Databases
): SaleNetRepository {
    override suspend fun getAll(userId: String): List<SaleDto> {
        val response = netDB.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            queries = listOf(Query.equal("user_id", userId))
        )
        return response.documents.map { document -> document.toSaleDto() }
    }

    override suspend fun getById(itemId: String): SaleDto {
        val response = netDB.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            documentId = itemId
        )
        return response.toSaleDto()
    }

    override suspend fun save(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        netDB.createDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            documentId = resolvedId,
            data = item.toAppwriteData()
        )
    }

    override suspend fun upsert(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        val payload = item.copy(id = resolvedId).toAppwriteData()
        runCatching {
            netDB.createDocument(
                databaseId = BuildConfig.APPWRITE_DATABASE_ID,
                collectionId = BuildConfig.SALE_TABLE_ID,
                documentId = resolvedId,
                data = payload
            )
        }.recoverCatching { throwable ->
            val appwriteError = throwable as? AppwriteException
            if (appwriteError?.code == 409) {
                netDB.updateDocument(
                    databaseId = BuildConfig.APPWRITE_DATABASE_ID,
                    collectionId = BuildConfig.SALE_TABLE_ID,
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
    "delivery_type" to deliveryType
).filterValues { value -> value != null }