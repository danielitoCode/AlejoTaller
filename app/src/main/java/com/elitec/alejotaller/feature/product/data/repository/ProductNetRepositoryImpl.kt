package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.mapper.toProductDto
import com.elitec.alejotaller.feature.product.domain.repository.ProductNetRepository
import io.appwrite.services.Databases

class ProductNetRepositoryImpl(
    private val netDB: Databases
): ProductNetRepository{

    override suspend fun getAll(): List<ProductDto> {
        val response = netDB.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID
        )
        val decode = response.documents.map { document -> document.toProductDto() }
        return decode
    }

    override suspend fun getById(itemId: String): ProductDto {
        val response = netDB.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = itemId
        )
        return response.toProductDto()
    }

    override suspend fun incrementReserved(
        productId: String,
        quantity: Int,
        maxReserved: Int
    ): ProductDto {
        require(quantity > 0) { "quantity debe ser > 0" }
        require(maxReserved >= 0) { "maxReserved debe ser >= 0" }

        val response = netDB.incrementDocumentAttribute(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = productId,
            attribute = "reserved",
            value = quantity,
            max = maxReserved
        )
        return response.toProductDto()
    }

    override suspend fun decrementReserved(productId: String, quantity: Int): ProductDto {
        require(quantity > 0) { "quantity debe ser > 0" }

        val response = netDB.decrementDocumentAttribute(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = productId,
            attribute = "reserved",
            value = quantity,
            min = 0
        )
        return response.toProductDto()
    }
}
