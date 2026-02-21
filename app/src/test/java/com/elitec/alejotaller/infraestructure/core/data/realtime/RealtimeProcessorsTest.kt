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
    fun `sale success event should be routed to sale callback`() {
        var confirmedSaleId = ""
        val processor = SaleEventProcessor(
            onSuccess = { confirmedSaleId = it },
            onError = { _, _ -> }
        )

        val processed = processor.process(
            RealtimeEventEnvelope(
                channel = "sales",
                name = "sale.success",
                payload = "{\"type\":\"sale.success\",\"saleId\":\"abc-123\"}"
            )
        )

        assertTrue(processed)
        assertEquals("abc-123", confirmedSaleId)
    }

    @Test
    fun `sale error event should be routed to error callback`() {
        var saleId = ""
        var cause = ""
        val processor = SaleEventProcessor(
            onSuccess = { },
            onError = { incomingSaleId, incomingCause ->
                saleId = incomingSaleId
                cause = incomingCause
            }
        )

        val processed = processor.process(
            RealtimeEventEnvelope(
                channel = "sales",
                name = "sale.error",
                payload = "{\"type\":\"sale.error\",\"saleId\":\"sale-1\",\"cause\":\"payment timeout\"}"
            )
        )

        assertTrue(processed)
        assertEquals("sale-1", saleId)
        assertEquals("payment timeout", cause)
    }

    @Test
    fun `promotion should be resolved by next processor in chain`() {
        var promotion = PromotionEvent(id = "", title = "", message = "")

        val saleProcessor = SaleEventProcessor(onSuccess = {}, onError = { _, _ -> })
        saleProcessor.setNext(
            PromotionEventProcessor(onPromotionReceived = { promotion = it })
        )

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
        val saleProcessor = SaleEventProcessor(onSuccess = {}, onError = { _, _ -> })
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