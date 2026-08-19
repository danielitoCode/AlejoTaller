package com.elitec.alejotallerscan.feature.inventory.domain.repository

import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementRecord
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementWrite

interface OperatorStockMovementRepository {
    /** Movimientos existentes de una venta (idempotencia salida_venta). */
    suspend fun listBySaleId(saleId: String): List<StockMovementRecord>

    suspend fun create(movement: StockMovementWrite): StockMovementRecord
}
