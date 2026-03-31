package com.elitec.alejotallerscan.feature.sale.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
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
    private val updateSaleVerificationFromRealtimeCaseUse: UpdateSaleVerificationFromRealtimeCaseUse
) : ViewModel() {

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
            updateSaleVerificationFromRealtimeCaseUse(selectedSale.id, isSuccess)
                .onSuccess {
                    val nextState = if (isSuccess) BuyState.VERIFIED else BuyState.DELETED
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        selectedSale = selectedSale.copy(verified = nextState),
                        notice = if (isSuccess) "Pago confirmado y cliente notificado." else "Reserva marcada como rechazada."
                    )
                    onDone()
                }
                .onFailure { error ->
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

    companion object {
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
}
