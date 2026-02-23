package com.elitec.alejotaller.infraestructure.core.data.realtime

import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.data.realtime.processor.PromotionEventProcessor
import com.elitec.alejotaller.feature.sale.data.realtime.processor.SaleEventProcessor
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlin.test.assertEquals

class RealtimeProcessorsTest {
    @Test
    fun `sale success event should be routed with saleId and userId`() {
        var confirmedUserId = ""
        var confirmedSaleId = ""

        val processor = SaleEventProcessor(
            onSuccess = { saleId, userId ->
                confirmedSaleId = saleId
                confirmedUserId = userId
            },
            onError = { _, _, _ -> }
        )

        val processed = processor.process(
            RealtimeEventEnvelope(
                channel = "sales",
                name = "sale.success",
                payload = "{\"type\":\"sale.success\",\"saleId\":\"abc-123\",\"userId\":\"user-1\"}"
            )
        )

        assertTrue(processed)
        assertEquals("abc-123", confirmedSaleId)
        assertEquals("user-1", confirmedUserId)
    }

    @Test
    fun `sale error event should be routed with saleId userId and cause`() {
        var saleId = ""
        var cause = ""
        var userId = ""

        val processor = SaleEventProcessor(
            onSuccess = { _, _ -> },
            onError = { incomingSaleId, incomingUserId, incomingCause ->
                saleId = incomingSaleId
                userId = incomingUserId
                cause = incomingCause
            }
        )

        val processed = processor.process(
            RealtimeEventEnvelope(
                channel = "sales",
                name = "sale.error",
                payload = "{\"type\":\"sale.error\",\"saleId\":\"sale-1\",\"userId\":\"user-7\",\"cause\":\"payment timeout\"}"
            )
        )

        assertTrue(processed)
        assertEquals("sale-1", saleId)
        assertEquals("user-7", userId)
        assertEquals("payment timeout", cause)
    }

    @Test
    fun `promotion should be resolved by next processor in chain`() {
        var promotion = PromotionEvent(id = "", title = "", message = "")

        val saleProcessor = SaleEventProcessor(onSuccess = { _, _ -> }, onError = { _, _, _ -> })
        saleProcessor.setNext(PromotionEventProcessor(onPromotionReceived = { promotion = it }))

        val processed = saleProcessor.process(
            RealtimeEventEnvelope(
                channel = "promo",
                name = "promotion.new",
                payload = "{\"id\":\"promo-1\",\"title\":\"Oferta\",\"message\":\"50% OFF\"}"
            )
        )

        assertTrue(processed)
        assertEquals("promo-1", promotion.id)
        assertEquals("Oferta", promotion.title)
    }

    @Test
    fun `unknown event should remain unprocessed`() {
        val saleProcessor = SaleEventProcessor(onSuccess = { _, _ -> }, onError = { _, _, _ -> })
        saleProcessor.setNext(PromotionEventProcessor(onPromotionReceived = {}))

        val processed = saleProcessor.process(
            RealtimeEventEnvelope(
                channel = "support",
                name = "support.message",
                payload = "{\"foo\":\"bar\"}"
            )
        )

        assertFalse(processed)
    }
}