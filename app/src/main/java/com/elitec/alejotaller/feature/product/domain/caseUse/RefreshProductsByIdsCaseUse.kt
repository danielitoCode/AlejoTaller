package com.elitec.alejotaller.feature.product.domain.caseUse

import android.util.Log
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.domain.repository.ProductRepository

/**
 * Refresh selectivo offline-first tras stock:changed (Pusher).
 * Solo los productIds de la señal se leen de Appwrite y actualizan cache local.
 */
class RefreshProductsByIdsCaseUse(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(productIds: List<String>): Result<List<Product>> = runCatching {
        val ids = productIds.filter { it.isNotBlank() }.distinct()
        val updated = mutableListOf<Product>()
        for (id in ids) {
            val product = repository.refreshFromRemote(id)
            if (product != null) updated += product
        }
        Log.i(TAG, "event=refresh_by_ids count=${updated.size}/${ids.size}")
        updated
    }

    companion object {
        private const val TAG = "RefreshProductsByIds"
    }
}
