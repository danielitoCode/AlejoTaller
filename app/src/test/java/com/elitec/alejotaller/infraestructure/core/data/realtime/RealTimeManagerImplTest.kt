package com.elitec.alejotaller.infraestructure.core.data.realtime

import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import com.pusher.client.Pusher
import com.pusher.client.channel.Channel
import com.pusher.client.channel.SubscriptionEventListener
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RealTimeManagerImplTest {

    @Test
    fun `subscribes sale confirmation channel using current user id`() {
        val pusher = mockk<Pusher>(relaxed = true)
        val saleChannel = mockk<Channel>(relaxed = true)
        val promoChannel = mockk<Channel>(relaxed = true)
        val manager = RealTimeManagerImpl(PusherManager(pusher))

        every { pusher.connect(any(), any()) } just runs
        every { pusher.subscribe("sale-verification-user-42") } returns saleChannel
        every { pusher.subscribe("promo") } returns promoChannel

        manager.subscribe(
            userId = "user-42",
            onConnect = {},
            onDisconnect = {},
            onSaleEvent = {},
            onPromotion = {}
        )

        verify(exactly = 1) { pusher.subscribe("sale-verification-user-42") }
        verify(exactly = 1) { pusher.subscribe("promo") }
    }

    @Test
    fun `routes confirmed and rejected events from colon event names`() {
        val pusher = mockk<Pusher>(relaxed = true)
        val saleChannel = mockk<Channel>(relaxed = true)
        val promoChannel = mockk<Channel>(relaxed = true)
        val bindHandlers = mutableMapOf<String, SubscriptionEventListener>()
        val manager = RealTimeManagerImpl(PusherManager(pusher))
        val receivedEvents = mutableListOf<SaleRealtimeEvent>()

        every { pusher.connect(any(), any()) } just runs
        every { pusher.subscribe("sale-verification-user-7") } returns saleChannel
        every { pusher.subscribe("promo") } returns promoChannel
        every { saleChannel.bind(any(), any()) } answers {
            val eventName = invocation.args[0] as String
            val handler = invocation.args[1] as SubscriptionEventListener
            bindHandlers[eventName] = handler
        }

        manager.subscribe(
            userId = "user-7",
            onConnect = {},
            onDisconnect = {},
            onSaleEvent = { receivedEvents += it },
            onPromotion = {}
        )

        val confirmedEvent = mockk<com.pusher.client.channel.PusherEvent>()
        every { confirmedEvent.data } returns """{"saleId":"sale-1","userId":"user-7"}"""
        bindHandlers.getValue("sale:confirmed").onEvent(confirmedEvent)

        val rejectedEvent = mockk<com.pusher.client.channel.PusherEvent>()
        every { rejectedEvent.data } returns """{"saleId":"sale-2","userId":"user-7","cause":"declined"}"""
        bindHandlers.getValue("sale:rejected").onEvent(rejectedEvent)

        assertTrue(bindHandlers.containsKey("sale:confirmed"))
        assertTrue(bindHandlers.containsKey("sale:rejected"))
        assertEquals(SaleRealtimeEvent(saleId = "sale-1", userId = "user-7", isSuccess = true), receivedEvents[0])
        assertEquals(SaleRealtimeEvent(saleId = "sale-2", userId = "user-7", isSuccess = false, cause = "declined"), receivedEvents[1])
    }
}
