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

    /** Contador de llamadas a decrementReserved (para aserciones de rollback). */
    val decrementReservedCalls = mutableListOf<Pair<String, Int>>()

    /** Permite simular un rechazo de la mutación atómica para pruebas de compensación. */
    val incrementFailureIds = mutableSetOf<String>()

    override fun observeAll(): Flow<List<Product>> = flow

    override suspend fun getById(itemId: String): Product? = byId[itemId]

    override suspend fun refreshFromRemote(productId: String): Product? = byId[productId]

    override suspend fun sync(): Result<Unit> = syncResult

    /**
     * Realtime Appwrite: aplica documento post-mutación a memoria (sin Room en tests).
     * Stub mínimo para cumplir el contrato de ProductRepository.
     */
    override suspend fun applyLocalSnapshot(raw: Map<String, Any>): Product? {
        val id = (raw["$id"] as? String)?.trim()
            ?: (raw["id"] as? String)?.trim()
            ?: return null
        val current = byId[id] ?: return null
        // En tests no hidratamos campos; devolvemos el producto ya conocido.
        return current
    }

    override suspend fun incrementReserved(productId: String, quantity: Int): Product? {
        incrementReservedCalls += productId to quantity
        if (productId in incrementFailureIds) return null

        val current = byId[productId] ?: return null
        val updated = current.copy(reserved = (current.reserved + quantity).coerceAtLeast(0))
        byId[productId] = updated
        flow.value = byId.values.toList()
        return updated
    }

    override suspend fun decrementReserved(productId: String, quantity: Int): Product? {
        decrementReservedCalls += productId to quantity
        val current = byId[productId] ?: return null
        val updated = current.copy(reserved = (current.reserved - quantity).coerceAtLeast(0))
        byId[productId] = updated
        flow.value = byId.values.toList()
        return updated
    }
}
