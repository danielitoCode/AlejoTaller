package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.product.data.mapper.toProductDto
import com.elitec.alejotaller.feature.sale.data.dto.SaleDto
import com.elitec.alejotaller.feature.sale.data.mapper.toSaleDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases

class SaleNetRepositoryImpl(
    private val netDB: Databases
) {
    suspend fun getAll(userId: String): List<SaleDto> {
        val response = netDB.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            queries = listOf(Query.equal("user_id", userId))
        )
        return response.documents.map { document -> document.toSaleDto() }
    }

    suspend fun getById(itemId: String): SaleDto {
        val response = netDB.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            documentId = itemId
        )
        return response.toSaleDto()
    }

    suspend fun save(item: SaleDto) {
        val resolvedId = item.id.ifBlank { ID.unique() }
        netDB.createDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.SALE_TABLE_ID,
            documentId = resolvedId,
            data = item.copy(id = resolvedId)
        )
    }
}