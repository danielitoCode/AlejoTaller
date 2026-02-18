package com.elitec.alejotaller.feature.sale.data.realtime.processor

import com.elitec.alejotaller.feature.sale.data.model.SaleEventResponse
import com.elitec.alejotaller.infraestructure.core.data.realtime.ChainedRealtimeEventProcessor
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealtimeEventEnvelope
import com.pusher.client.channel.PusherEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SaleEventProcessor(
    private val onSuccess: (saleId: String) -> Unit,
    private val onError: (saleId: String, cause: String) -> Unit,
) : ChainedRealtimeEventProcessor() {
    private val json: Json = Json { ignoreUnknownKeys = true }

    override fun handle(event: RealtimeEventEnvelope): Boolean {
        if (!event.isSaleEvent()) return false
        val response = event.payload?.let(::decode) ?: return true

        when (response) {
            is SaleEventResponse.SaleSuccessResponse -> onSuccess(response.saleId)
            is SaleEventResponse.SaleErrorResponse -> onError(response.saleId, response.cause)
        }

        return true
    }

    private fun decode(payload: String): SaleEventResponse {
        val root = json.parseToJsonElement(payload).jsonObject
        val saleId = root["saleId"]?.jsonPrimitive?.content
            ?: root["sale_id"]?.jsonPrimitive?.content
            ?: ""
        val cause = root["cause"]?.jsonPrimitive?.content
            ?: "Unknown sale error"

        return when (root["type"]?.jsonPrimitive?.content?.lowercase()) {
            "sale.error", "error" -> SaleEventResponse.SaleErrorResponse(saleId = saleId, cause = cause)
            else -> SaleEventResponse.SaleSuccessResponse(saleId = saleId)
        }
    }

    private fun RealtimeEventEnvelope.isSaleEvent(): Boolean =
        name.startsWith("sale.") || payload?.contains("\"sale") == true
}