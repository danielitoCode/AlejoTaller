package com.elitec.alejotaller.feature.category.data.repository

import com.elitec.alejotaller.feature.category.domain.entity.Category
import com.elitec.alejotaller.feature.category.domain.repository.CategoriesRepository
import io.appwrite.ID
import io.appwrite.services.Databases
import io.appwrite.services.TablesDB

class CategoriesNetRepositoryImpl(
    private val netDB: Databases
) {
    private val CATEGORIA_TABLE_ID = "category"

    suspend fun getAll(): List<Category> {
        val response = netDB.listDocuments(
            databaseId = CATEGORIA_TABLE_ID,
            collectionId = CATEGORIA_TABLE_ID
        )
    }
}