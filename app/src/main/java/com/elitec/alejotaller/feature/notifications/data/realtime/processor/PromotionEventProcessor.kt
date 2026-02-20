package com.elitec.alejotaller.feature.notifications.data.realtime.processor

import android.util.Log
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.infraestructure.core.data.realtime.ChainedRealtimeEventProcessor
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealtimeEventEnvelope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PromotionEventProcessor(
    private val onPromotionReceived: (PromotionEvent) -> Unit,
) : ChainedRealtimeEventProcessor() {

    private val tag = "Promotion Events"
    private val json: Json = Json { ignoreUnknownKeys = true }

    override fun handle(event: RealtimeEventEnvelope): Boolean {
        if (!event.isPromotionEvent()) return false
        Log.i(tag, "Promotion event received: name=${event.name}, channel=${event.channel}, payload=${event.payload}")
        val payload = event.payload ?: return true
        val decoded = decode(payload)
        Log.i(tag, "Promotion interpreted: id=${decoded.id}, title=${decoded.title}, currentPrice=${decoded.currentPrice}")
        onPromotionReceived(decode(payload))
        return true
    }

    private fun decode(payload: String): PromotionEvent {
        val root = json.parseToJsonElement(payload).jsonObject

        return PromotionEvent(
            id = root["id"]?.jsonPrimitive?.content ?: "",
            title = root["title"]?.jsonPrimitive?.content ?: "Promoción",
            message = root["message"]?.jsonPrimitive?.content ?: "",
            imageUrl = root["imageUrl"]?.jsonPrimitive?.content,
            oldPrice = root["oldPrice"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull(),
            currentPrice = root["currentPrice"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull(),
            validFromEpochMillis = root["validFromEpochMillis"]?.jsonPrimitive?.contentOrNull?.toLongOrNull(),
            validUntilEpochMillis = root["validUntilEpochMillis"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
        )
    }

    private fun RealtimeEventEnvelope.isPromotionEvent(): Boolean =
        name.startsWith("promotion.") || payload?.contains("\"promotion") == true
}