package com.elitec.alejotallerscan.feature.sale.presentation.viewmodel

import com.elitec.shared.sale.feature.sale.domain.entity.Sale

data class OperatorSalesUiState(
    val isLoading: Boolean = false,
    val selectedSale: Sale? = null,
    val lastScannedPayload: String = "",
    val error: String? = null,
    val notice: String? = null
)
