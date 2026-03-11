package com.elitec.alejotaller.feature.sale.domain.repository

import com.elitec.alejotaller.feature.sale.data.dto.SaleDto

interface SaleNetRepository {
    suspend fun getAll(userId: String): List<SaleDto>
    suspend fun getById(itemId: String): SaleDto
    suspend fun save(item: SaleDto)
    suspend fun upsert(item: SaleDto)
}