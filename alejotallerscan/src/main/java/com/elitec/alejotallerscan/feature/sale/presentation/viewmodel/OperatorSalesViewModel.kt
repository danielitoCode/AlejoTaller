package com.elitec.alejotallerscan.feature.sale.presentation.viewmodel

import android.util.Log
import com.elitec.alejotallerscan.feature.confirmation.domain.caseuse.NotifyOperatorSaleDecisionCaseUse
import com.elitec.alejotallerscan.feature.history.domain.caseuse.RegisterOperatorSaleRecordCaseUse
import com.elitec.alejotallerscan.feature.history.domain.entity.OperatorSaleRecordAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.mapper.toDto
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.caseUse.ObserveAllSalesCaseUse
import com.elitec.shared.sale.feature.sale.domain.caseUse.UpdateSaleVerificationFromRealtimeCaseUse
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
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
    private val registerOperatorSaleRecordCaseUse: RegisterOperatorSaleRecordCaseUse
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, notice = null)
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

    fun confirmSelectedSale(onDone: () -> Unit = {}) {
        changeSelectedSale(true, onDone)
    }

    fun rejectSelectedSale(onDone: () -> Unit = {}) {
        changeSelectedSale(false, onDone)
    }

    private fun changeSelectedSale(isSuccess: Boolean, onDone: () -> Unit) {
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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, notice = null)
            Log.i(
                TAG,
                "event=operator_sale_update_start saleId=${selectedSale.id} " +
                    "userId=${selectedSale.userId} decision=${if (isSuccess) "confirmed" else "rejected"}"
            )
            updateSaleVerificationFromRealtimeCaseUse(selectedSale.id, isSuccess)
                .onSuccess {
                    val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
                    val updatedSale = selectedSale.copy(verified = nextState)
                    val action = if (isSuccess) {
                        OperatorSaleRecordAction.CONFIRMED
                    } else {
                        OperatorSaleRecordAction.REJECTED
                    }

                    Log.i(
                        TAG,
                        "event=operator_sale_update_success saleId=${updatedSale.id} " +
                            "nextState=$nextState"
                    )

                    val remoteVerification = runCatching {
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
                            selectedSale = selectedSale,
                            error = error.message ?: "Appwrite no confirmo el cambio de estado."
                        )
                        return@onSuccess
                    }

                    Log.i(
                        TAG,
                        "event=operator_remote_verification_success saleId=${confirmedRemoteSale.id} " +
                            "verified=${confirmedRemoteSale.verified}"
                    )

                    val notificationResult = runCatching {
                        notifyOperatorSaleDecisionCaseUse(confirmedRemoteSale, isSuccess)
                    }
                    notificationResult.onFailure { error ->
                        Log.e(
                            TAG,
                            "event=operator_pusher_failure saleId=${confirmedRemoteSale.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedSale = confirmedRemoteSale,
                            error = error.message ?: "Pusher no pudo notificar al cliente."
                        )
                        return@onSuccess
                    }
                    Log.i(TAG, "event=operator_pusher_success saleId=${confirmedRemoteSale.id}")

                    val recordResult = runCatching {
                        registerOperatorSaleRecordCaseUse(confirmedRemoteSale, action)
                    }
                    recordResult.onFailure { error ->
                        Log.e(
                            TAG,
                            "event=operator_local_record_failure saleId=${confirmedRemoteSale.id} cause=${error.message}",
                            error
                        )
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            selectedSale = confirmedRemoteSale,
                            error = error.message ?: "No se pudo registrar la venta en el dispositivo."
                        )
                        return@onSuccess
                    }
                    Log.i(TAG, "event=operator_local_record_saved saleId=${confirmedRemoteSale.id} action=$action")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedSale = confirmedRemoteSale,
                        notice = if (isSuccess) {
                            "Venta confirmada, notificada y registrada correctamente."
                        } else {
                            "Venta rechazada, notificada y registrada correctamente."
                        }
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
                        error = error.message ?: "No se pudo actualizar la venta."
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, notice = null)
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
