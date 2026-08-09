package com.elitec.shared.core.feature.product.domain.realtime

/**
 * Abstracción de suscripción a stock:changed.
 */
fun interface StockUpdatesListener {
    fun start(onEvent: (StockChangedPayload) -> Unit): () -> Unit
}
