package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

/**
 * Refresh selectivo offline-first tras stock:changed (Pusher).
 * Solo los productIds de la señal se leen de Appwrite y actualizan cache local.
 * Fuente de verdad: remoto. available = existence - reserved (política warehouse).
 */
class RefreshProductsByIdsCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productIds: List<String>): Result<List<Product>> = runCatching {
        val ids = productIds.filter { it.isNotBlank() }.distinct()
        Log.i(TAG, "event=refresh_by_ids_start count=${ids.size} ids=${ids.joinToString(",")}")

        val updated = mutableListOf<Product>()
        for (id in ids) {
            val product = repository.refreshFromRemote(id)
            if (product != null) {
                val available = (product.existence - product.reserved).coerceAtLeast(0)
                Log.i(
                    TAG,
                    "event=refresh_by_ids_ok id=$id existence=${product.existence} " +
                        "reserved=${product.reserved} available=$available"
                )
                updated += product
            } else {
                Log.w(TAG, "event=refresh_by_ids_null id=$id")
            }
        }

        Log.i(TAG, "event=refresh_by_ids_done refreshed=${updated.size}/${ids.size}")
        updated
    }

    companion object {
        private const val TAG = "RefreshProductsByIds"
    }
}
