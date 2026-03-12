package com.elitec.alejotaller.data.fakesRepositories

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository(
    products: List<Product> = emptyList(),
    private val syncResult: Result<Unit> = Result.success(Unit)
) : ProductRepository {

    private val byId = products.associateBy { it.id }
    private val flow = MutableStateFlow(products)

    override fun observeAll(): Flow<List<Product>> = flow

    override suspend fun getById(itemId: String): Product? = byId[itemId]

    override suspend fun sync(): Result<Unit> = syncResult
}