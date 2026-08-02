package com.elitec.alejotallerscan.feature.product.domain.repository

/**
 * Ajuste de inventario desde el operador (WAREHOUSE_POLICY soft-hold).
 * existenceDelta / reservedDelta pueden ser negativos.
 */
interface OperatorStockRepository {
    /**
     * Lee existence/reserved, aplica deltas y persiste en Appwrite.
     * Garantiza existence >= 0 y reserved >= 0.
     * @return pair (existenceAfter, reservedAfter)
     */
    suspend fun applyDeltas(
        productId: String,
        existenceDelta: Int,
        reservedDelta: Int
    ): Pair<Int, Int>
}
