package com.elitec.alejotallerscan.feature.inventory.data.repository

import android.util.Log
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementRecord
import com.elitec.alejotallerscan.feature.inventory.domain.entity.StockMovementWrite
import com.elitec.alejotallerscan.feature.inventory.domain.repository.OperatorStockMovementRepository
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases

class AppwriteOperatorStockMovementRepository(
    private val databases: Databases
) : OperatorStockMovementRepository {

    private val collectionId: String
        get() = BuildConfig.STOCK_MOVEMENTS_TABLE_ID.ifBlank { "stock_movements" }

    override suspend fun listBySaleId(saleId: String): List<StockMovementRecord> {
        val sid = saleId.trim()
        if (sid.isEmpty()) return emptyList()
        val response = databases.listDocuments(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = collectionId,
            queries = listOf(
                Query.equal("sale_id", sid),
                Query.limit(100)
            )
        )
        return response.documents.map { doc ->
            val data = doc.data
            StockMovementRecord(
                id = doc.id,
                productId = (data["product_id"] as? String).orEmpty(),
                type = (data["type"] as? String).orEmpty(),
                quantity = (data["quantity"] as? Number)?.toInt() ?: 0,
                balanceAfter = (data["balance_after"] as? Number)?.toInt() ?: 0,
                saleId = data["sale_id"] as? String
            )
        }
    }

    override suspend fun create(movement: StockMovementWrite): StockMovementRecord {
        val id = ID.unique()
        val data = mutableMapOf<String, Any>(
            "product_id" to movement.productId,
            "type" to movement.type,
            "quantity" to movement.quantity,
            "balance_after" to movement.balanceAfter,
            "reason" to movement.reason,
            "user_id" to movement.userId
        )
        movement.saleId?.takeIf { it.isNotBlank() }?.let { data["sale_id"] = it }

        val doc = databases.createDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = collectionId,
            documentId = id,
            data = data
        )
        Log.i(
            TAG,
            "event=operator_movement_created id=${doc.id} type=${movement.type} " +
                "productId=${movement.productId} saleId=${movement.saleId} qty=${movement.quantity}"
        )
        return StockMovementRecord(
            id = doc.id,
            productId = movement.productId,
            type = movement.type,
            quantity = movement.quantity,
            balanceAfter = movement.balanceAfter,
            saleId = movement.saleId
        )
    }

    companion object {
        private const val TAG = "OperatorMovementRepo"
    }
}
