package com.elitec.alejotaller.feature.sale.domain.repository

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun observeAll(): Flow<List<Sale>>
    suspend fun getById(itemId: String): Sale
    suspend fun save(item: Sale)
    suspend fun sync(userId: String): Result<Unit>
}