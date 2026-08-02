package com.elitec.alejotaller.data.fakesRepositories.product

import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository(
    products: List<Product> = emptyList(),
    private val syncResult: Result<Unit> = Result.success(Unit)
) : ProductRepository {

    private val byId = products.associateBy { it.id }.toMutableMap()
    private val flow = MutableStateFlow(products)

    /** Contador de llamadas a incrementReserved (para aserciones de soft-hold). */
    val incrementReservedCalls = mutableListOf<Pair<String, Int>>()

    override fun observeAll(): Flow<List<Product>> = flow

    override suspend fun getById(itemId: String): Product? = byId[itemId]

    override suspend fun sync(): Result<Unit> = syncResult

    override suspend fun incrementReserved(productId: String, quantity: Int): Product? {
        incrementReservedCalls += productId to quantity
        val current = byId[productId] ?: return null
        val updated = current.copy(reserved = (current.reserved + quantity).coerceAtLeast(0))
        byId[productId] = updated
        flow.value = byId.values.toList()
        return updated
    }
}
