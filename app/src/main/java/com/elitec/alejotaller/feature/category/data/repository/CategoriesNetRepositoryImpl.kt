package com.elitec.alejotaller.feature.category.data.repository

import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.category.data.dto.CategoryDto
import com.elitec.alejotaller.feature.category.data.mapper.toCategoryDto
import io.appwrite.services.Databases

class CategoriesNetRepositoryImpl(
    private val netDB: Databases
) {
    suspend fun getAll(): List<CategoryDto> {
        val response = netDB.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.CATEGORY_TABLE_ID
        )
        return  response.documents.map { document -> document.toCategoryDto() }
    }
}