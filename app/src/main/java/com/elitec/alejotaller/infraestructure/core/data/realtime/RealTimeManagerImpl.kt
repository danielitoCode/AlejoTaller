package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.data.realtime.processor.PromotionEventProcessor
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.data.realtime.processor.SaleEventProcessor
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent

class RealTimeManagerImpl(
    private val pusherManager: PusherManager
) : RealtimeSyncGateway {

    override fun subscribe(
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit
    ) {
        pusherManager.init(onConnect, onDisconnect)

        val saleProcessor = SaleEventProcessor(
            onSuccess = { saleId ->
                onSaleEvent(SaleRealtimeEvent(saleId = saleId, isSuccess = true))
            },
            onError = { saleId, cause ->
                onSaleEvent(SaleRealtimeEvent(saleId = saleId, isSuccess = false, cause = cause))
            }
        )
        val promotionProcessor = PromotionEventProcessor(onPromotionReceived = { event ->
            onPromotion(event.toDomainPromotion())
        })
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

private const val DEFAULT_PROMO_TTL = 1000L * 60L * 60L * 24L * 7L

private fun PromotionEvent.toDomainPromotion(): Promotion {
    val now = System.currentTimeMillis()

    return Promotion(
        id = id.ifBlank { "promo-$now" },
        title = title,
        message = message,
        imageUrl = imageUrl,
        validFromEpochMillis = validFromEpochMillis ?: now,
        validUntilEpochMillis = validUntilEpochMillis ?: (now + DEFAULT_PROMO_TTL)
    )
}