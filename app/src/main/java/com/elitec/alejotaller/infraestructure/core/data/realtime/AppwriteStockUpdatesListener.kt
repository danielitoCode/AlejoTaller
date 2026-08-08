package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.alejotaller.BuildConfig
import com.elitec.alejotaller.feature.product.domain.realtime.StockChangedPayload
import com.elitec.alejotaller.feature.product.domain.realtime.StockUpdatesListener
import io.appwrite.Client
import io.appwrite.models.RealtimeResponse
import io.appwrite.models.RealtimeSubscription
import java.util.concurrent.atomic.AtomicReference

/**
 * Stock realtime vía Appwrite (paridad web).
 * Canal: databases.{DB}.collections.{product}.documents
 * No requiere secret de Pusher ni publish desde operador.
 */
class AppwriteStockUpdatesListener(
    private val client: Client
) : StockUpdatesListener {

    private val activeSub = AtomicReference<RealtimeSubscription?>(null)

    override fun start(onEvent: (StockChangedPayload) -> Unit): () -> Unit {
        // Idempotente: si ya hay sub, reutiliza (handler se reemplaza vía nueva sub)
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

        val subscription = try {
            client.subscribe(listOf(channel)) { response ->
                handleResponse(response, onEvent)
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
        response: RealtimeResponse,
        onEvent: (StockChangedPayload) -> Unit
    ) {
        val events = response.events.orEmpty()
        val payloadAny = response.payload
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

        val existence = readInt(map, "existence", "status")
        val reserved = readInt(map, "reserved", "reservado")
        val available = (existence - reserved).coerceAtLeast(0)

        Log.i(
            TAG,
            "event=stock_rt_notification productId=$productId " +
                "existence=$existence reserved=$reserved available=$available events=$events"
        )

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
        private const val TAG = "AppwriteStockRT"

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

        private fun readInt(map: Map<String, Any>, vararg keys: String): Int {
            for (key in keys) {
                val raw = map[key] ?: continue
                val n = when (raw) {
                    is Number -> raw.toInt()
                    is String -> raw.toIntOrNull()
                    else -> null
                }
                if (n != null) return n.coerceAtLeast(0)
            }
            return 0
        }
    }
}
