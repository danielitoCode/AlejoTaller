package com.elitec.alejotallerscan.feature.sale.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.confirmation.domain.caseuse.NotifyOperatorSaleDecisionCaseUse
import com.elitec.alejotallerscan.feature.history.domain.caseuse.RegisterOperatorSaleRecordCaseUse
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import com.elitec.alejotallerscan.feature.product.domain.caseuse.ApplyOperatorStockDecisionCaseUse
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.mapper.toDto
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OperatorSalesViewModel(
    observeAllSalesCaseUse: ObserveAllSalesCaseUse,
    private val saleNetRepository: SaleNetRepository,
    private val saleDao: SaleDao,
    private val updateSaleVerificationFromRealtimeCaseUse: UpdateSaleVerificationFromRealtimeCaseUse,
    private val notifyOperatorSaleDecisionCaseUse: NotifyOperatorSaleDecisionCaseUse,
    private val registerOperatorSaleRecordCaseUse: RegisterOperatorSaleRecordCaseUse,
    private val applyOperatorStockDecisionCaseUse: ApplyOperatorStockDecisionCaseUse
) : ViewModel() {
    companion object {
        const val TAG = "OperatorSalesVM"
        fun extractSaleId(rawPayload: String): String {
            val trimmed = rawPayload.trim()
            if (trimmed.isBlank()) return ""

            val queryId = Regex("""(?:^|[?&])(id|saleId|reservationId)=([^&]+)""", RegexOption.IGNORE_CASE)
                .find(trimmed)
                ?.groupValues
                ?.getOrNull(2)
                ?.trim()
            if (!queryId.isNullOrBlank()) return queryId

            return trimmed.substringAfterLast('/').substringAfterLast('=').trim()
        }
    }

    val recentSales = observeAllSalesCaseUse()
        .map { sales -> sales.sortedByDescending { it.date }.take(20) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(OperatorSalesUiState())
    val uiState: StateFlow<OperatorSalesUiState> = _uiState.asStateFlow()

    fun loadSaleByCode(rawPayload: String, onLoaded: () -> Unit = {}) {
        val saleId = extractSaleId(rawPayload)
        if (saleId.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "No se pudo extraer un codigo de venta valido.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Cargando datos de la reserva...",
                error = null,
                notice = null
            )
            runCatching {
                val remoteSale = saleNetRepository.getById(saleId)
                saleDao.insert(remoteSale)
                remoteSale.toDomain()
            }.onSuccess { sale ->
                _uiState.value = OperatorSalesUiState(
                    selectedSale = sale,
                    lastScannedPayload = rawPayload,
                    notice = "Reserva cargada correctamente."
                )
                onLoaded()
            }.onFailure { error ->
                _uiState.value = OperatorSalesUiState(
                    error = error.message ?: "No se encontro la reserva solicitada.",
                    lastScannedPayload = rawPayload
                )
            }
        }
    }

    fun selectSale(sale: Sale, onLoaded: () -> Unit = {}) {
        _uiState.value = _uiState.value.copy(selectedSale = sale, error = null, notice = null)
        onLoaded()
    }

    /**
     * Confirma la venta con el [saleType] elegido por el operador (SALE_POLICY).
     * GIFT fuerza amount = 0; stock baja igual (WAREHOUSE_POLICY).
     */
    fun confirmSelectedSale(
        saleType: SaleType = SaleType.NORMAL,
        onDone: () -> Unit = {}
    ) {
        changeSelectedSale(isSuccess = true, saleType = saleType, onDone = onDone)
    }

    fun rejectSelectedSale(onDone: () -> Unit = {}) {
        changeSelectedSale(isSuccess = false, saleType = null, onDone = onDone)
    }

    private fun changeSelectedSale(
        isSuccess: Boolean,
        saleType: SaleType?,
        onDone: () -> Unit
    ) {
        val selectedSale = _uiState.value.selectedSale ?: run {
            _uiState.value = _uiState.value.copy(error = "No hay una venta seleccionada.")
            return
        }

        if (selectedSale.verified != BuyState.UNVERIFIED) {
            _uiState.value = _uiState.value.copy(
                notice = "La venta ya fue procesada anteriormente."
            )
            onDone()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                loadingMessage = "Actualizando la venta en Appwrite...",
                error = null,
                notice = null
            )
            val effectiveType = if (isSuccess) saleType ?: SaleType.NORMAL else null
            Log.i(
                TAG,
                "event=operator_sale_update_start saleId=${selectedSale.id} " +
                    "userId=${selectedSale.userId} decision=${if (isSuccess) "confirmed" else "rejected"} " +
                    "saleType=${effectiveType?.name ?: "n/a"}"
            )
            updateSaleVerificationFromRealtimeCaseUse(
                saleId = selectedSale.id,
                isSuccess = isSuccess,
                saleType = effectiveType
            )
                .onSuccess {
                    val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
                    val resolvedType = if (isSuccess) effectiveType else selectedSale.saleType
                    val resolvedAmount = when {
                        !isSuccess -> selectedSale.amount
                        resolvedType == SaleType.GIFT -> 0.0
                        else -> selectedSale.amount
                    }
                    val updatedSale = selectedSale.copy(
                        verified = nextState,
                        saleType = resolvedType,
                        amount = resolvedAmount
                    )
                    val action = if (isSuccess) {
                        OperatorSaleRecordAction.CONFIRMED
                    } else {
                        OperatorSaleRecordAction.REJECTED
                    }

                    Log.i(
                        TAG,
                        "event=operator_sale_update_success saleId=${updatedSale.id} " +
                            "nextState=$nextState saleType=${resolvedType?.name}"
                    )

                    val remoteVerification = runCatching {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            loadingMessage = "Verificando el cambio remoto de la venta..."
                        )
                        verifyRemoteSaleState(updatedSale.id, nextState)
                    }
                    val confirmedRemoteSale = remoteVerification.getOrElse { error ->
                        rollbackLocalSale(selectedSale)
                        Log.e(
                            TAG,
                            "event=operator_remote_verification_failure saleId=${updatedSale.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loadingMessage = null,
                            selectedSale = selectedSale,
                            error = error.message ?: "Appwrite no confirmo el cambio de estado."
                        )
                        return@onSuccess
                    }

                    // Preferimos el saleType local resuelto si el remoto aún no lo refleja
                    val saleForSideEffects = confirmedRemoteSale.copy(
                        saleType = confirmedRemoteSale.saleType ?: resolvedType,
                        amount = if (resolvedType == SaleType.GIFT) 0.0 else confirmedRemoteSale.amount
                    )

                    Log.i(
                        TAG,
                        "event=operator_remote_verification_success saleId=${saleForSideEffects.id} " +
                            "verified=${saleForSideEffects.verified} saleType=${saleForSideEffects.saleType}"
                    )

                    var stockWarning: String? = null
                    _uiState.value = _uiState.value.copy(
                        isLoading = true,
                        loadingMessage = if (isSuccess) {
                            "Actualizando inventario (salida de stock)..."
                        } else {
                            "Liberando reserva de inventario..."
                        }
                    )
                    applyOperatorStockDecisionCaseUse(saleForSideEffects, isSuccess)
                        .onFailure { error ->
                            Log.e(
                                TAG,
                                "event=operator_stock_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                                error
                            )
                            stockWarning =
                                "La venta quedó actualizada, pero falló el ajuste de stock. " +
                                    "Revisar inventario manualmente. (${error.message ?: "sin detalle"})"
                        }
                        .onSuccess {
                            Log.i(
                                TAG,
                                "event=operator_stock_success saleId=${saleForSideEffects.id} confirmed=$isSuccess"
                            )
                        }

                    var realtimeWarning: String? = null
                    val notificationResult = runCatching {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            loadingMessage = "Publicando notificación en tiempo real..."
                        )
                        notifyOperatorSaleDecisionCaseUse(saleForSideEffects, isSuccess)
                    }
                    notificationResult.onFailure { error ->
                        Log.e(
                            TAG,
                            "event=operator_pusher_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                            error
                        )
                        realtimeWarning =
                            "La venta quedó actualizada en el servidor, pero no se pudo notificar en tiempo real. " +
                                "El cliente puede ver el cambio al sincronizar. (${error.message ?: "sin detalle"})"
                    }
                    if (notificationResult.isSuccess) {
                        Log.i(TAG, "event=operator_pusher_success saleId=${saleForSideEffects.id}")
                    }

                    val recordResult = runCatching {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            loadingMessage = "Guardando el registro local de la operacion..."
                        )
                        registerOperatorSaleRecordCaseUse(saleForSideEffects, action)
                    }
                    recordResult.onFailure { error ->
                        Log.e(
                            TAG,
                            "event=operator_local_record_failure saleId=${saleForSideEffects.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loadingMessage = null,
                            selectedSale = saleForSideEffects,
                            notice = if (isSuccess) {
                                "Venta confirmada (${resolvedType?.name ?: "NORMAL"}) en servidor."
                            } else {
                                "Venta rechazada en servidor."
                            },
                            error = listOfNotNull(
                                stockWarning,
                                realtimeWarning,
                                error.message ?: "No se pudo registrar la venta en el dispositivo."
                            ).joinToString(" ")
                        )
                        onDone()
                        return@onSuccess
                    }
                    Log.i(TAG, "event=operator_local_record_saved saleId=${saleForSideEffects.id} action=$action")

                    val baseNotice = if (isSuccess) {
                        "Venta confirmada como ${resolvedType?.name ?: "NORMAL"}, stock actualizado."
                    } else {
                        "Venta rechazada, hold liberado y registrada correctamente."
                    }

                    val extraWarnings = listOfNotNull(stockWarning, realtimeWarning).joinToString(" ").ifBlank { null }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        selectedSale = saleForSideEffects,
                        notice = if (extraWarnings == null) baseNotice else "$baseNotice $extraWarnings",
                        error = null
                    )
                    onDone()
                }
                .onFailure { error ->
                    Log.e(
                        TAG,
                        "event=operator_sale_update_failure saleId=${selectedSale.id} cause=${error.message}",
                        error
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        error = error.message ?: "No se pudo actualizar la venta."
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, notice = null)
    }

    fun resetState() {
        _uiState.value = OperatorSalesUiState()
    }

    private suspend fun verifyRemoteSaleState(saleId: String, expectedState: BuyState): Sale {
        val remoteSale = saleNetRepository.getById(saleId).toDomain()
        if (remoteSale.verified != expectedState) {
            error(
                "Appwrite no confirmo el estado esperado para la venta $saleId. " +
                    "Esperado=$expectedState actual=${remoteSale.verified}"
            )
        }
        return remoteSale
    }

    private suspend fun rollbackLocalSale(originalSale: Sale) {
        runCatching {
            saleDao.insert(originalSale.toDto())
            Log.i(TAG, "event=operator_local_rollback_success saleId=${originalSale.id}")
        }.onFailure { error ->
            Log.e(TAG, "event=operator_local_rollback_failure saleId=${originalSale.id} cause=${error.message}", error)
        }
    }
}
