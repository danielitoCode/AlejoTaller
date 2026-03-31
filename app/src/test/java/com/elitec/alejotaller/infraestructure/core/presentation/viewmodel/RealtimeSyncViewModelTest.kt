package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import com.elitec.alejotaller.core.MainDispatcherRule
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.
import com.elitec.shared.sale.feature.sale.domain.realtime.RealtimeSyncGateway
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RealtimeSyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts realtime only when current user has pending sales`() {
        val gateway = RecordingRealtimeGateway()
        val viewModel = buildViewModel(gateway)

        viewModel.updateSubscriptionScope(userId = "user-1", pendingSaleIds = emptySet())
        viewModel.startRealtimeSync()
        assertTrue(gateway.subscribedUserIds.isEmpty())

        viewModel.updateSubscriptionScope(userId = "user-1", pendingSaleIds = setOf("sale-1"))
        viewModel.startRealtimeSync()

        assertEquals(listOf("user-1"), gateway.subscribedUserIds)
    }

    @Test
    fun `stops previous subscription when active user changes`() {
        val gateway = RecordingRealtimeGateway()
        val viewModel = buildViewModel(gateway)

        viewModel.updateSubscriptionScope(userId = "user-1", pendingSaleIds = setOf("sale-1"))
        viewModel.startRealtimeSync()
        viewModel.updateSubscriptionScope(userId = "user-2", pendingSaleIds = setOf("sale-2"))
        viewModel.startRealtimeSync()

        assertEquals(listOf("user-1", "user-2"), gateway.subscribedUserIds)
        assertTrue(gateway.unsubscribeCount >= 1)
    }

    @Test
    fun `stop clears active realtime subscription`() {
        val gateway = RecordingRealtimeGateway()
        val viewModel = buildViewModel(gateway)

        viewModel.updateSubscriptionScope(userId = "user-1", pendingSaleIds = setOf("sale-1"))
        viewModel.startRealtimeSync()
        viewModel.stopRealtimeSync()

        assertEquals(1, gateway.unsubscribeCount)
    }

    private fun buildViewModel(gateway: RecordingRealtimeGateway): RealtimeSyncViewModel =
        RealtimeSyncViewModel(
            subscribeRealtimeSyncCaseUse = SubscribeRealtimeSyncCaseUse(gateway),
            savePromotionCaseUse = mockk<SavePromotionCaseUse>(relaxed = true),
            orderNotificationService = mockk<OrderNotificationService>(relaxed = true),
            interpretSaleRealtimeEventCaseUse = mockk<InterpretSaleRealtimeEventCaseUse>(relaxed = true),
            updateSaleVerificationFromRealtimeCaseUse = mockk<UpdateSaleVerificationFromRealtimeCaseUse>(relaxed = true)
        )
}

private class RecordingRealtimeGateway : RealtimeSyncGateway {
    val subscribedUserIds = mutableListOf<String>()
    var unsubscribeCount: Int = 0

    override fun subscribe(
        userId: String,
        onConnect: () -> Unit,
        onDisconnect: () -> Unit,
        onSaleEvent: (SaleRealtimeEvent) -> Unit,
        onPromotion: (Promotion) -> Unit
    ) {
        subscribedUserIds += userId
        onConnect()
    }

    override fun unsubscribeAll() {
        unsubscribeCount += 1
    }
}
