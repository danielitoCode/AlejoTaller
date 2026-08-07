package com.elitec.alejotaller.feature.product.domain.realtime

/**
 * Abstracción de suscripción a stock:changed (Pusher / Alset Pulse).
 * La implementación concreta puede usar Pusher Java o el bridge HTTP de pulse.
 */
fun interface StockUpdatesListener {
    fun start(onEvent: (StockChangedPayload) -> Unit): () -> Unit
}
