package com.elitec.alejotaller.feature.sale.data.repository

import com.elitec.alejotaller.feature.product.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.sale.data.dao.SaleDao
import com.elitec.alejotaller.feature.sale.data.mapper.toDomain
import com.elitec.alejotaller.feature.sale.data.mapper.toDto
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SaleOfflineFirstRepository(
    private val net: SaleNetRepositoryImpl,
    private val bd: SaleDao
) : SaleRepository {
    override fun observeAll(): Flow<List<Sale>> =
        bd.getAllFlow().map { saleDtoList ->
            saleDtoList.map { saleDto -> saleDto.toDomain() }
        }

    override suspend fun getById(itemId: String): Sale = bd.getById(itemId).toDomain()

    override suspend fun save(item: Sale) {
        val saleDto = item.toDto()
        bd.insert(saleDto)
        runCatching { net.save(saleDto) }
    }

    override suspend fun sync(userId: String): Result<Unit> = runCatching {
        val remote = net.getAll(userId)
        bd.replaceAll(remote)
    }
}