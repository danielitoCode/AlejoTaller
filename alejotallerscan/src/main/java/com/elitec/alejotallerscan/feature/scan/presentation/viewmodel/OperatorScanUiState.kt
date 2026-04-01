package com.elitec.alejotallerscan.feature.scan.presentation.viewmodel

data class OperatorScanUiState(
    val isLoading: Boolean = false,
    val lastPayload: String = "",
    val error: String? = null,
    val notice: String? = null
)
