package com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotallerscan.feature.reservation.domain.caseuse.SearchReservationsCaseUse
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationSearchField
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationStatusFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OperatorReservationSearchViewModel(
    private val searchReservationsCaseUse: SearchReservationsCaseUse
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorReservationSearchUiState())
    val uiState: StateFlow<OperatorReservationSearchUiState> = _uiState.asStateFlow()

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
    }

    fun updateField(field: ReservationSearchField) {
        _uiState.value = _uiState.value.copy(field = field, error = null, notice = null)
    }

    fun updateStatusFilter(filter: ReservationStatusFilter) {
        _uiState.value = _uiState.value.copy(statusFilter = filter, error = null, notice = null)
    }

    fun search() {
        val current = _uiState.value
        if (current.query.isBlank()) {
            _uiState.value = current.copy(error = "Debes escribir un criterio de busqueda.")
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, error = null, notice = null)
            searchReservationsCaseUse(current.field, current.query)
                .map { sales ->
                    current.statusFilter.state?.let { targetState ->
                        sales.filter { sale -> sale.verified == targetState }
                    } ?: sales
                }
                .onSuccess { sales ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = sales,
                        notice = if (sales.isEmpty()) "No se encontraron coincidencias." else "Se encontraron ${sales.size} coincidencias."
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "No se pudo completar la busqueda."
                    )
                }
        }
    }
}
