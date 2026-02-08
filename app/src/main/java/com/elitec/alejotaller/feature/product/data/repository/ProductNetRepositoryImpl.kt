package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.category.data.mapper.toCategoryDto
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.mapper.toProductDto
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import io.appwrite.services.Databases

class ProductNetRepositoryImpl(
    private val netDB: Databases
) {
    suspend fun getAll(): List<ProductDto> {
        val response = netDB.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID
        )
        return  response.documents.map { document -> document.toProductDto() }
    }

    suspend fun getById(itemId: String): ProductDto {
        val response = netDB.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = itemId
        )
        return  response.toProductDto()
    }
}