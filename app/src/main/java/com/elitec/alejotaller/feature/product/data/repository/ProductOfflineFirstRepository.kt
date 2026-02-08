package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.feature.category.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.feature.product.data.dto.ProductDto
import com.elitec.alejotaller.feature.product.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductOfflineFirstRepository(
    private val net: ProductNetRepositoryImpl,
    private val bd: ProductDao
): ProductRepository {
    override suspend fun observeAll(): Flow<List<Product>> =
        bd.getAllFlow().map { productDtoList ->
            productDtoList.map { productDto ->
                productDto.toDomain()
            }
        }

    override suspend fun getById(itemId: String): Product = bd.getById(itemId).toDomain()

    override suspend fun sync(): Result<Unit> = runCatching {
        val remote = net.getAll()
        bd.replaceAll(remote)
    }
}