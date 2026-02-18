package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.alejotaller.feature.sale.domain.caseUse.SubscribeRealtimeSyncCaseUse
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealTimeManagerImpl
import com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RealtimeSyncViewModel(
    private val subscribeRealtimeSyncCaseUse: SubscribeRealtimeSyncCaseUse,
    private val savePromotionCaseUse: SavePromotionCaseUse,
    private val orderNotificationService: OrderNotificationService,
    private val interpretSaleRealtimeEventCaseUse: InterpretSaleRealtimeEventCaseUse
) : ViewModel() {

    private var isSubscribed = false
    private val _uiMessages = MutableSharedFlow<RealtimeUiMessage>(extraBufferCapacity = 16)
    val uiMessages = _uiMessages.asSharedFlow()

    fun startRealtimeSync() {
        if (isSubscribed) return

        subscribeRealtimeSyncCaseUse(
            onConnect = { isSubscribed = true },
            onDisconnect = { isSubscribed = false },
            onSaleEvent = ::dispatchSaleEvent,
            onPromotion = ::persistPromotion
        )
    }

    private fun dispatchSaleEvent(event: SaleRealtimeEvent) {
        interpretSaleRealtimeEventCaseUse(event).forEach { command ->
            when (command) {
                is SaleRealtimeCommand.InAppMessage -> viewModelScope.launch {
                    _uiMessages.emit(
                        RealtimeUiMessage(
                            message = command.message,
                            isError = command.kind == RealtimeMessageKind.Error
                        )
                    )
                }

                is SaleRealtimeCommand.PushNotification ->
                    orderNotificationService.showOrderStatus(
                        title = command.title,
                        message = command.body
                    )
            }
        }
    }

    private fun persistPromotion(promotion: Promotion) {
        viewModelScope.launch {
            savePromotionCaseUse(promotion)
        }
    }
}

data class RealtimeUiMessage(
    val message: String,
    val isError: Boolean
)