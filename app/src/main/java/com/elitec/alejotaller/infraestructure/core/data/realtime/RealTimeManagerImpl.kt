package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.notifications.data.realtime.PromotionEventProcessor
import com.elitec.alejotaller.feature.notifications.domain.entity.PromotionEvent
import com.elitec.alejotaller.feature.sale.data.realtime.processor.SaleEventProcessor

class RealTimeManagerImpl(
    private val pusherManager: PusherManager
) {
    fun subscribeToABuyConfirmationChannel(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onConfirmationEvent: (String) -> Unit,
        onConfirmationError: (String, String) -> Unit,
        onPromotionEvent: (PromotionEvent) -> Unit = {}
    ) {
        pusherManager.init(onConnect, onDisconnect)

        val saleProcessor = SaleEventProcessor(
            onSuccess = onConfirmationEvent,
            onError = onConfirmationError
        )
        val promotionProcessor = PromotionEventProcessor(onPromotionReceived = onPromotionEvent)
        saleProcessor.setNext(promotionProcessor)

        pusherManager.subscribe(
            channel = BuildConfig.PUSHER_SALE_CHANNEL,
            eventNames = SALE_EVENTS,
            onReceive = { saleProcessor.process(it) }
        )

        pusherManager.subscribe(
            channel = BuildConfig.PUSHER_PROMO_CHANNEL,
            eventNames = PROMOTION_EVENTS,
            onReceive = { event ->
                if (!saleProcessor.process(event)) {
                    Log.w(TAG, "Evento realtime no manejado: ${event.name} en canal ${event.channel}")
                }
            }
        )
    }

    companion object {
        private const val TAG = "RealTimeManager"
        private val SALE_EVENTS = listOf("sale.success", "sale.error")
        private val PROMOTION_EVENTS = listOf("promotion.new", "promotion.update")
    }
}