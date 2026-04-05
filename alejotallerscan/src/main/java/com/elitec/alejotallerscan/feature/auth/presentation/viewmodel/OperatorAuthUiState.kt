package com.elitec.alejotallerscan.feature.auth.presentation.viewmodel

import com.elitec.shared.auth.feature.auth.domain.entity.User

data class OperatorAuthUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)
