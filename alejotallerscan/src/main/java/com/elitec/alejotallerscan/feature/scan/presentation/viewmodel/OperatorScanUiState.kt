package com.elitec.alejotallerscan.feature.scan.presentation.viewmodel

import com.elitec.alejotallerscan.feature.scan.domain.entity.ParsedSaleQrPayload

data class OperatorScanUiState(
    val isLoading: Boolean = false,
    val lastPayload: String = "",
    val parsedPayload: ParsedSaleQrPayload? = null,
    val error: String? = null,
    val notice: String? = null
)
