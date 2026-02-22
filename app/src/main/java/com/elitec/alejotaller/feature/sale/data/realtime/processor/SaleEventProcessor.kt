package com.elitec.alejotaller.feature.sale.data.realtime.processor

import android.util.Log
import com.elitec.alejotaller.feature.sale.data.model.SaleEventResponse
import com.elitec.alejotaller.infraestructure.core.data.realtime.ChainedRealtimeEventProcessor
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealtimeEventEnvelope
import com.pusher.client.channel.PusherEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SaleEventProcessor(
    private val onSuccess: (saleId: String, userId: String) -> Unit,
    private val onError: (saleId: String, userId: String, cause: String) -> Unit,
) : ChainedRealtimeEventProcessor() {
    private val json: Json = Json { ignoreUnknownKeys = true }

    override fun handle(event: RealtimeEventEnvelope): Boolean {
        if (!event.isSaleEvent()) return false
        Log.i(TAG, "Sale event received: name=${event.name}, channel=${event.channel}, payload=${event.payload}")

        val response = event.payload?.let(::decode) ?: return true

        when (response) {
            is SaleEventResponse.SaleSuccessResponse -> {
                Log.i(TAG, "Sale success interpreted for saleId=${response.saleId}")
                onSuccess(response.saleId, response.userId)
            }
            is SaleEventResponse.SaleErrorResponse -> {
                Log.w(TAG, "Sale error interpreted for saleId=${response.saleId}, cause=${response.cause}")
                onError(response.saleId, response.userId, response.cause)
            }
        }

        return true
    }

    private fun decode(payload: String): SaleEventResponse {
        val root = json.parseToJsonElement(payload).jsonObject
        val userId = root["userId"]?.jsonPrimitive?.content
            ?: root["user_id"]?.jsonPrimitive?.content
            ?: ""
        val saleId = root["saleId"]?.jsonPrimitive?.content
            ?: root["sale_id"]?.jsonPrimitive?.content
            ?: ""
        val cause = root["cause"]?.jsonPrimitive?.content
            ?: "Unknown sale error"

        return when (root["type"]?.jsonPrimitive?.content?.lowercase()) {
            "sale.error", "error" -> SaleEventResponse.SaleErrorResponse(saleId = saleId, userId = userId, cause = cause)
            else -> SaleEventResponse.SaleSuccessResponse(saleId = saleId, userId = userId)
        }
    }

    private fun RealtimeEventEnvelope.isSaleEvent(): Boolean =
        name.startsWith("sale.") || payload?.contains("\"sale") == true

    companion object {
        private const val TAG = "SaleEventProcessor"
    }
}