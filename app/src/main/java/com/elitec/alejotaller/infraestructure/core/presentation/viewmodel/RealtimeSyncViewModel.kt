package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.notifications.data.models.PromotionEvent
import com.elitec.alejotaller.feature.notifications.domain.caseuse.SavePromotionCaseUse
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.sale.domain.caseUse.InterpretSaleRealtimeEventCaseUse
import com.elitec.alejotaller.feature.sale.domain.realtime.RealtimeMessageKind
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeCommand
import com.elitec.alejotaller.feature.sale.domain.realtime.SaleRealtimeEvent
import com.elitec.alejotaller.infraestructure.core.data.realtime.RealTimeManagerImpl
import com.elitec.alejotaller.infraestructure.core.presentation.services.OrderNotificationService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class RealtimeSyncViewModel(
    private val realTimeManager: RealTimeManagerImpl,
    private val savePromotionCaseUse: SavePromotionCaseUse,
    private val orderNotificationService: OrderNotificationService,
    private val interpretSaleRealtimeEventCaseUse: InterpretSaleRealtimeEventCaseUse
) : ViewModel() {

    private var isSubscribed = false
    private val _uiMessages = MutableSharedFlow<RealtimeUiMessage>(extraBufferCapacity = 16)
    val uiMessages = _uiMessages.asSharedFlow()

    fun startRealtimeSync() {
        if (isSubscribed) return

        realTimeManager.subscribeToABuyConfirmationChannel(
            onConnect = { isSubscribed = true },
            onDisconnect = { isSubscribed = false },
            onConfirmationEvent = { saleId ->
                dispatchSaleEvent(SaleRealtimeEvent(saleId = saleId, isSuccess = true))
            },
            onConfirmationError = { saleId, cause ->
                dispatchSaleEvent(SaleRealtimeEvent(saleId = saleId, isSuccess = false, cause = cause))
            },
            onPromotionEvent = { event ->
                persistPromotion(event)
            }
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

    private fun persistPromotion(event: PromotionEvent) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            savePromotionCaseUse(
                Promotion(
                    id = event.id.ifBlank { "promo-$now" },
                    title = event.title,
                    message = event.message,
                    imageUrl = event.imageUrl,
                    validFromEpochMillis = event.validFromEpochMillis ?: now,
                    validUntilEpochMillis = event.validUntilEpochMillis ?: (now + DEFAULT_PROMO_TTL)
                )
            )
        }
    }

    companion object {
        private const val DEFAULT_PROMO_TTL = 1000L * 60L * 60L * 24L * 7L
    }
}

data class RealtimeUiMessage(
    val message: String,
    val isError: Boolean
)