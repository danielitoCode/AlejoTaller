package com.elitec.alejotallerscan.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotallerscan.BuildConfig
import com.elitec.shared.core.feature.product.domain.realtime.StockChangedPayload
import com.elitec.shared.core.feature.product.domain.realtime.StockUpdatesListener
import io.appwrite.Client
import io.appwrite.models.RealtimeResponseEvent
import io.appwrite.models.RealtimeSubscription
import io.appwrite.services.Realtime
import java.util.concurrent.atomic.AtomicReference

/**
 * Stock realtime vía Appwrite en la app de operador.
 * Permite reaccionar a cambios externos en inventario (otros operadores o sistema).
 */
class AppwriteStockUpdatesListener(
    private val client: Client
) : StockUpdatesListener {

    private val activeSub = AtomicReference<RealtimeSubscription?>(null)

    override fun start(onEvent: (StockChangedPayload) -> Unit): () -> Unit {
        activeSub.get()?.close()
        activeSub.set(null)

        val databaseId = BuildConfig.APPWRITE_DATABASE_ID.trim()
        val collectionId = BuildConfig.PRODUCT_TABLE_ID.trim()
        if (databaseId.isEmpty() || collectionId.isEmpty()) {
            Log.w(TAG, "event=stock_rt_skip reason=missing_db_or_collection")
            return {}
        }

        val channel = "databases.$databaseId.collections.$collectionId.documents"
        Log.i(TAG, "event=stock_rt_subscribe channel=$channel")

        val realtime = Realtime(client)
        val subscription = try {
            realtime.subscribe(channel) { event ->
                handleResponse(event, onEvent)
            }
        } catch (error: Throwable) {
            Log.e(TAG, "event=stock_rt_subscribe_failed cause=${error.message}", error)
            return {}
        }

        activeSub.set(subscription)

        return {
            Log.i(TAG, "event=stock_rt_unsubscribe channel=$channel")
            runCatching { subscription.close() }
            activeSub.compareAndSet(subscription, null)
        }
    }

    private fun handleResponse(
        event: RealtimeResponseEvent<Any>,
        onEvent: (StockChangedPayload) -> Unit
    ) {
        val events = event.events
        val payloadAny = event.payload
        val map = payloadToMap(payloadAny) ?: run {
            Log.w(TAG, "event=stock_rt_ignored reason=no_payload events=$events")
            return
        }

        val productId = (map["\$id"] as? String)?.trim()
            ?: (map["id"] as? String)?.trim()
            ?: ""
        if (productId.isEmpty()) {
            Log.w(TAG, "event=stock_rt_ignored reason=missing_id")
            return
        }

        val reason = when {
            events.any { it.contains(".create", ignoreCase = true) } -> "create"
            events.any { it.contains(".delete", ignoreCase = true) } -> "delete"
            else -> "stock-or-doc-update"
        }

        val payload = StockChangedPayload(
            productIds = listOf(productId),
            reason = reason,
            saleId = null,
            timestamp = System.currentTimeMillis().toString(),
            snapshotByProductId = mapOf(productId to map)
        )
        onEvent(payload)
    }

    companion object {
        private const val TAG = "OperatorAppwriteStockRT"

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
