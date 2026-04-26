package com.elitec.shared.sale.feature.sale.domain.caseuse

import com.elitec.shared.sale.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.shared.sale.feature.sale.fakes.FakeRealtimeSyncGateway
import org.junit.Test
import org.junit.Assert.assertEquals

class SubscribeRealtimeSyncCaseUseTest {

    @Test
    fun `delegates subscription to realtime gateway with user id`() {
        val gateway = FakeRealtimeSyncGateway()
        val caseUse = SubscribeRealtimeSyncCaseUse(gateway)

        caseUse(
            userId = "user-1",
            onConnect = {},
            onDisconnect = {},
            onSaleEvent = {}
        )

        assertEquals("user-1", gateway.subscribedUserId)
    }

    @Test
    fun `unsubscribeAll delegator to a gateway`() {
        val gateway = FakeRealtimeSyncGateway()
        val caseUse = SubscribeRealtimeSyncCaseUse(gateway)

        caseUse.unsubscribeAll()

        assertEquals(1, gateway.unsubscribeCalls)
    }
}
