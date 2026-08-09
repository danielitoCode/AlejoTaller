package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.notifications.data.mappers.toDomainPromotion
import com.elitec.alejotaller.feature.notifications.data.realtime.processor.PromotionEventProcessor
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.shared.sale.feature.sale.domain.realtime.SaleRealtimeEvent
import io.appwrite.Client
import io.appwrite.models.RealtimeSubscription
import io.appwrite.services.Realtime

/**
 * Realtime híbrido:
 * - **Ventas**: Appwrite Realtime sobre colección sale (sin secret Pusher).
 * - **Promociones**: Pusher (sin migrar aún).
 */
class RealTimeManagerImpl(
    private val client: Client,
    private val pusherManager: PusherManager
) : RealtimeSyncGateway {

    private var saleSubscription: RealtimeSubscription? = null
    private var activeUserId: String = ""

    override fun subscribe(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit
    ) {
        activeUserId = userId

        // --- Sale: Appwrite ---
        val databaseId = BuildConfig.APPWRITE_DATABASE_ID.trim()
        val saleCollectionId = BuildConfig.SALE_TABLE_ID.trim()
        if (databaseId.isNotEmpty() && saleCollectionId.isNotEmpty()) {
            val channel = "databases.$databaseId.collections.$saleCollectionId.documents"
            Log.i(TAG, "event=sale_rt_subscribe channel=$channel userId=$userId")

            val realtime = Realtime(client)
            runCatching {
                saleSubscription?.close()
                saleSubscription = realtime.subscribe(channel) { response ->
                    handleSaleRealtime(response.events.toList(), response.payload, userId, onSaleEvent)
                }
                onConnect()
            }.onFailure { error ->
                Log.e(TAG, "event=sale_rt_subscribe_failed cause=${error.message}", error)
                onDisconnect()
            }
        } else {
            Log.w(TAG, "event=sale_rt_skip reason=missing_db_or_sale_collection")
        }

        // --- Promo: Pusher (sin cambios) ---
        pusherManager.init(onConnect, onDisconnect)

        val promotionProcessor = PromotionEventProcessor(onPromotionReceived = { event ->
            onPromotion(event.toDomainPromotion())
        })

        val promoChannel = BuildConfig.PUSHER_PROMO_CHANNEL.orFallback(DEFAULT_PROMO_CHANNEL, "PUSHER_PROMO_CHANNEL")
        Log.i(TAG, "event=promo_rt_subscribe channel=$promoChannel (Pusher)")

        pusherManager.subscribe(
            channel = promoChannel,
            eventNames = PROMOTION_EVENTS,
            onReceive = { event ->
                if (!promotionProcessor.process(event)) {
                    Log.w(TAG, "Evento promo no manejado: ${event.name} en canal ${event.channel}")
                }
            }
        )
    }

    override fun unsubscribeAll() {
        runCatching { saleSubscription?.close() }
        saleSubscription = null
        activeUserId = ""
        pusherManager.unsubscribeAll()
        Log.i(TAG, "event=realtime_unsubscribe_all")
    }

    private fun handleSaleRealtime(
        events: List<String>,
        payloadAny: Any?,
        expectedUserId: String,
        onSaleEvent: (SaleRealtimeEvent) -> Unit
    ) {
        val map = payloadToMap(payloadAny) ?: run {
            Log.w(TAG, "event=sale_rt_ignored reason=no_payload events=$events")
            return
        }

        val saleId = (map["\$id"] as? String)?.trim()
            ?: (map["id"] as? String)?.trim()
            ?: ""
        val ownerId = (map["user_id"] as? String)?.trim().orEmpty()
        val buyState = (map["buy_state"] as? String)?.trim().orEmpty()

        if (saleId.isEmpty()) {
            Log.w(TAG, "event=sale_rt_ignored reason=missing_id")
            return
        }

        if (ownerId.isNotEmpty() && expectedUserId.isNotEmpty() && ownerId != expectedUserId) {
            Log.i(TAG, "event=sale_rt_ignored reason=other_user saleId=$saleId owner=$ownerId")
            return
        }

        val isSuccess = when (buyState.uppercase()) {
            "VERIFIED" -> true
            "DELETED" -> false
            else -> {
                Log.i(TAG, "event=sale_rt_snapshot_pending saleId=$saleId buy_state=$buyState")
                return
            }
        }

        Log.i(
            TAG,
            "event=sale_rt_notification saleId=$saleId buy_state=$buyState " +
                "decision=${if (isSuccess) "confirmed" else "rejected"} userId=$ownerId"
        )

        onSaleEvent(
            SaleRealtimeEvent(
                saleId = saleId,
                userId = ownerId.ifBlank { expectedUserId },
                isSuccess = isSuccess,
                cause = if (isSuccess) null else "Pedido rechazado"
            )
        )
    }

    companion object {
        private const val TAG = "RealTimeManager"
        private const val DEFAULT_PROMO_CHANNEL = "promo"
        private val PROMOTION_EVENTS = listOf("promotion.new", "promotion.update")

        @Suppress("UNCHECKED_CAST")
        private fun payloadToMap(payload: Any?): Map<String, Any>? {
            return when (payload) {
                is Map<*, *> -> payload.entries
                    .mapNotNull { (k, v) ->
                        val key = k as? String ?: return@mapNotNull null
                        key to (v as Any)
                    }
                    .toMap()
                else -> null
            }
        }
    }
}

private fun String?.orFallback(default: String, keyName: String): String {
    val normalized = this?.trim().orEmpty()
    if (normalized.isNotBlank()) return normalized

    Log.w("RealTimeManager", "$keyName is blank. Falling back to channel '$default'.")
    return default
}
