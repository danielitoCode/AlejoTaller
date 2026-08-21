package com.elitec.alejotallerscan.feature.product.data.repository

import android.util.Log
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockRepository
import com.elitec.alejotallerscan.feature.product.domain.repository.OperatorStockSnapshot
import io.appwrite.services.Databases

class AppwriteOperatorStockRepository(
    private val databases: Databases
) : OperatorStockRepository {

    override suspend fun applyDeltas(
        productId: String,
        existenceDelta: Int,
        reservedDelta: Int
    ): OperatorStockSnapshot {
        val doc = databases.getDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = productId
        )
        val currentExistence = ((doc.data["existence"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        val currentReserved = ((doc.data["reserved"] as? Number)?.toInt() ?: 0).coerceAtLeast(0)
        val lastUnitCost = (doc.data["last_unit_cost"] as? Number)?.toDouble()
            ?: (doc.data["lastUnitCost"] as? Number)?.toDouble()

        val nextExistence = (currentExistence + existenceDelta).coerceAtLeast(0)
        val nextReserved = (currentReserved + reservedDelta).coerceAtLeast(0)

        if (existenceDelta < 0 && currentExistence + existenceDelta < 0) {
            Log.w(
                TAG,
                "event=operator_stock_clamp productId=$productId " +
                    "existence=$currentExistence delta=$existenceDelta -> $nextExistence"
            )
        }

        databases.updateDocument(
            databaseId = BuildConfig.APPWRITE_DATABASE_ID,
            collectionId = BuildConfig.PRODUCT_TABLE_ID,
            documentId = productId,
            data = mapOf(
                "existence" to nextExistence,
                "reserved" to nextReserved
            )
        )

        Log.i(
            TAG,
            "event=operator_stock_updated productId=$productId " +
                "existence=$currentExistence->$nextExistence " +
                "reserved=$currentReserved->$nextReserved lastUnitCost=$lastUnitCost"
        )

        return OperatorStockSnapshot(
            existenceAfter = nextExistence,
            reservedAfter = nextReserved,
            lastUnitCost = lastUnitCost
        )
    }

    companion object {
        private const val TAG = "OperatorStockRepo"
    }
}
