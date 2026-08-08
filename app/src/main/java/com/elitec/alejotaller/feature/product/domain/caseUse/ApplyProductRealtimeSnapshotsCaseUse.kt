package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

/**
 * Aplica documentos product post-mutación (Appwrite Realtime) a Room sin re-fetch.
 */
class ApplyProductRealtimeSnapshotsCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        snapshots: Map<String, Map<String, Any>>
    ): Result<List<Product>> = runCatching {
        val updated = mutableListOf<Product>()
        for ((id, raw) in snapshots) {
            val product = repository.applyLocalSnapshot(raw)
            if (product != null) {
                updated += product
                Log.i(
                    TAG,
                    "event=product_rt_snapshot_applied id=$id " +
                        "ex=${product.existence} rs=${product.reserved}"
                )
            } else {
                Log.w(TAG, "event=product_rt_snapshot_skip id=$id")
            }
        }
        updated
    }

    companion object {
        private const val TAG = "ApplyProductRtSnap"
    }
}
