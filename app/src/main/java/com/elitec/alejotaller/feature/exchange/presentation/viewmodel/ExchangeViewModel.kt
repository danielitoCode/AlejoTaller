package com.elitec.alejotaller.feature.exchange.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetCachedTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.caseUse.GetTodayExchangeCaseUse
import com.elitec.alejotaller.feature.exchange.domain.entity.CupExchange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class ExchangeUiState(
    val selectedCurrency: String = "CUP", // "CUP" or "USD"
    val exchange: CupExchange? = null,
    val loading: Boolean = false,
    val error: String? = null
)

class ExchangeViewModel(
    private val getTodayExchangeCaseUse: GetTodayExchangeCaseUse,
    private val getCachedTodayExchangeCaseUse: GetCachedTodayExchangeCaseUse
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExchangeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshForSplash()
    }

    private fun todayCacheKey(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
    }

    private fun refreshForSplash() {
        viewModelScope.launch {
            val cached = getCachedTodayExchangeCaseUse()
            if (cached != null && cached.updatedAt.take(10) == todayCacheKey()) {
                _uiState.update { it.copy(exchange = cached) }
            } else {
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            getTodayExchangeCaseUse().onSuccess { exchange ->
                _uiState.update { it.copy(exchange = exchange, loading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(loading = false, error = error.message) }
            }
        }
    }

    fun toggleCurrency() {
        _uiState.update { 
            it.copy(selectedCurrency = if (it.selectedCurrency == "CUP") "USD" else "CUP")
        }
    }

    fun setCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }
}
