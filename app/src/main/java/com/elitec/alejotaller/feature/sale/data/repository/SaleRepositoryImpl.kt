package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import com.elitec.alejotaller.infraestructure.core.domain.Repository

class SaleRepositoryImpl: SaleRepository {
    override suspend fun getAll(userId: String): List<Sale> {
        TODO("Not yet implemented")
    }

    override suspend fun getById(itemId: String): Sale {
        TODO("Not yet implemented")
    }

    override suspend fun save(
        item: Sale,
        onSave: () -> Unit,
    ) {
        TODO("Not yet implemented")
    }
}