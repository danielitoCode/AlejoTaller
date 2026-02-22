package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.notifications.data.mappers.toDomainPromotion
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

        val saleChannel = BuildConfig.PUSHER_SALE_CHANNEL.orFallback(DEFAULT_SALE_CHANNEL, "PUSHER_SALE_CHANNEL")
        val promoChannel = BuildConfig.PUSHER_PROMO_CHANNEL.orFallback(DEFAULT_PROMO_CHANNEL, "PUSHER_PROMO_CHANNEL")

        Log.i(TAG, "Realtime subscription config -> saleChannel='$saleChannel', promoChannel='$promoChannel', saleEvents=$SALE_EVENTS, promoEvents=$PROMOTION_EVENTS")

        pusherManager.subscribe(
            channel = saleChannel,
            eventNames = SALE_EVENTS,
            onReceive = { saleProcessor.process(it) }
        )

        pusherManager.subscribe(
            channel = promoChannel,
            eventNames = PROMOTION_EVENTS,
            onReceive = { event ->
                if (!saleProcessor.process(event)) {
                    Log.w(TAG, "Evento realtime no manejado: ${event.name} en canal ${event.channel}")
                }
            }
        )
    }

    override fun unsubscribeAll() {
        pusherManager.unsubscribeAll()
    }

    companion object {
        private const val TAG = "RealTimeManager"
        private const val DEFAULT_SALE_CHANNEL = "sales"
        private const val DEFAULT_PROMO_CHANNEL = "promo"
        private val SALE_EVENTS = listOf("sale.success", "sale.error")
        private val PROMOTION_EVENTS = listOf("promotion.new", "promotion.update")
    }
}

private fun String?.orFallback(default: String, keyName: String): String {
    val normalized = this?.trim().orEmpty()
    if (normalized.isNotBlank()) return normalized

    Log.w("RealTimeManager", "$keyName is blank. Falling back to channel '$default' for realtime subscription.")
    return default
}

private const val DEFAULT_PROMO_TTL = 1000L * 60L * 60L * 24L * 7L
