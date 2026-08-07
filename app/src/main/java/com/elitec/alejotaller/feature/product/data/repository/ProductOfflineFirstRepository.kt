package com.elitec.alejotaller.feature.product.data.repository

import com.elitec.alejotaller.feature.product.data.dao.ProductDao
import com.elitec.alejotaller.feature.product.data.mapper.toDomain
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductNetRepository
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductOfflineFirstRepository(
    private val net: ProductNetRepository,
    private val bd: ProductDao
): ProductRepository {
    override fun observeAll(): Flow<List<Product>> =
        bd.getAllFlow().map { productDtoList ->
            productDtoList.map { productDto ->
                productDto.toDomain()
            }
        }

    override suspend fun getById(itemId: String): Product? = bd.getById(itemId)?.toDomain()

    override suspend fun sync(): Result<Unit> = runCatching {
        val remote = net.getAll()
        bd.replaceAll(remote)
    }

    override suspend fun refreshFromRemote(productId: String): Product? {
        return runCatching {
            val remote = net.getById(productId)
            bd.insert(remote)
            remote.toDomain()
        }.getOrNull()
    }

    override suspend fun incrementReserved(productId: String, quantity: Int): Product? {
        if (quantity <= 0) return getById(productId)
        // Concurrencia: leer siempre de red antes de escribir
        val current = runCatching { net.getById(productId) }.getOrNull()
            ?: bd.getById(productId)
            ?: return null
        val nextReserved = (current.reserved + quantity).coerceAtLeast(0)
        val updated = net.updateReserved(productId, nextReserved)
        bd.insert(updated)
        return updated.toDomain()
    }

    override suspend fun decrementReserved(productId: String, quantity: Int): Product? {
        if (quantity <= 0) return getById(productId)
        val current = runCatching { net.getById(productId) }.getOrNull()
            ?: bd.getById(productId)
            ?: return null
        val nextReserved = (current.reserved - quantity).coerceAtLeast(0)
        val updated = net.updateReserved(productId, nextReserved)
        bd.insert(updated)
        return updated.toDomain()
    }
}
