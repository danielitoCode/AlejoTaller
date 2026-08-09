package com.elitec.alejotaller.infraestructure.core.data.realtime

import android.util.Log
import com.elitec.shared.core.feature.product.domain.realtime.StockChangedPayload
import com.elitec.shared.core.feature.product.domain.realtime.StockUpdatesListener
import org.json.JSONArray
import org.json.JSONObject

/**
 * Suscripción Pusher al canal stock-updates / evento stock:changed.
 * Alineado con web (WAREHOUSE_POLICY §4).
 */
class PusherStockUpdatesListener(
    private val pusherManager: PusherManager
) : StockUpdatesListener {

    override fun start(onEvent: (StockChangedPayload) -> Unit): () -> Unit {
        val channel = STOCK_CHANNEL

        Log.i(TAG, "event=stock_subscribe channel=$channel event=$STOCK_EVENT")

        pusherManager.init(
            onConnect = { Log.i(TAG, "event=pusher_connected for stock-updates") },
            onDisconnect = { Log.w(TAG, "event=pusher_disconnected stock-updates") }
        )

        pusherManager.subscribe(
            channel = channel,
            eventNames = listOf(STOCK_EVENT)
        ) { envelope ->
            if (envelope.name != STOCK_EVENT) return@subscribe
            val payload = parsePayload(envelope.payload)
            if (payload == null) {
                Log.w(TAG, "event=stock_changed_ignored reason=invalid_payload raw=${envelope.payload}")
                return@subscribe
            }
            Log.i(
                TAG,
                "event=stock_changed_received reason=${payload.reason} " +
                    "saleId=${payload.saleId} ids=${payload.productIds.joinToString(",")}"
            )
            onEvent(payload)
        }

        return {
            Log.i(TAG, "event=stock_unsubscribe channel=$channel")
            pusherManager.unsubscribe(channel)
        }
    }

    private fun parsePayload(raw: String?): StockChangedPayload? {
        if (raw.isNullOrBlank()) return null
        return runCatching {
            val root = JSONObject(raw)
            // Algunos bridges envuelven en { data: {...} }
            val data = if (root.has("productIds")) root else root.optJSONObject("data") ?: root

            val idsJson = data.optJSONArray("productIds") ?: JSONArray()
            val ids = buildList {
                for (i in 0 until idsJson.length()) {
                    val id = idsJson.optString(i).trim()
                    if (id.isNotEmpty()) add(id)
                }
            }
            if (ids.isEmpty()) return null

            val reason = data.optString("reason", "hold").ifBlank { "hold" }
            val saleId = data.optString("saleId").takeIf { it.isNotBlank() }
            val timestamp = data.optString("timestamp").ifBlank {
                System.currentTimeMillis().toString()
            }

            StockChangedPayload(
                productIds = ids,
                reason = reason,
                saleId = saleId,
                timestamp = timestamp
            )
        }.getOrElse {
            Log.w(TAG, "event=stock_parse_failed cause=${it.message} raw=$raw", it)
            null
        }
    }

    companion object {
        private const val TAG = "StockUpdatesListener"
        const val STOCK_CHANNEL = "stock-updates"
        const val STOCK_EVENT = "stock:changed"
    }
}
