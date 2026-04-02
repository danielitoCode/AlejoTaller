package com.elitec.alejotallerscan.feature.scan.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.product.domain.caseuse.EnrichSaleProductsCaseUse
import com.elitec.alejotallerscan.feature.scan.domain.caseuse.ParseSaleScanPayloadCaseUse
import com.elitec.shared.data.feature.sale.data.dao.SaleDao
import com.elitec.shared.data.feature.sale.data.mapper.toDomain
import com.elitec.shared.data.feature.sale.data.repository.SaleNetRepository
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OperatorScanViewModel(
    private val parseSaleScanPayloadCaseUse: ParseSaleScanPayloadCaseUse,
    private val saleNetRepository: SaleNetRepository,
    private val saleDao: SaleDao,
    private val enrichSaleProductsCaseUse: EnrichSaleProductsCaseUse
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorScanUiState())
    val uiState: StateFlow<OperatorScanUiState> = _uiState.asStateFlow()

    fun loadSaleByPayload(rawPayload: String, onLoaded: (Sale) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, notice = null)
            parseSaleScanPayloadCaseUse(rawPayload)
                .mapCatching { parsedPayload ->
                    val remoteSale = saleNetRepository.getById(parsedPayload.saleId)
                    saleDao.insert(remoteSale)
                    parsedPayload to enrichSaleProductsCaseUse(remoteSale.toDomain())
                }
                .onSuccess { (parsedPayload, sale) ->
                    _uiState.value = OperatorScanUiState(
                        lastPayload = rawPayload,
                        parsedPayload = parsedPayload,
                        notice = "Venta cargada correctamente."
                    )
                    onLoaded(sale)
                }
                .onFailure { error ->
                    _uiState.value = OperatorScanUiState(
                        lastPayload = rawPayload,
                        parsedPayload = null,
                        error = error.message ?: "No se pudo cargar la venta escaneada."
                    )
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, notice = null)
    }
}
