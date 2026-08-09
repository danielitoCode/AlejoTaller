package com.elitec.shared.core.feature.product.domain.realtime

/**
 * Señal de cambio de stock.
 * - Pusher legacy: solo productIds + reason.
 * - Appwrite Realtime: productIds + snapshotByProductId (documento post-mutación).
 */
data class StockChangedPayload(
    val productIds: List<String>,
    val reason: String, // hold | release | consume | stock-or-doc-update
    val saleId: String? = null,
    val timestamp: String,
    /** Documento Appwrite por productId cuando el canal es Appwrite Realtime. */
    val snapshotByProductId: Map<String, Map<String, Any>>? = null
)
