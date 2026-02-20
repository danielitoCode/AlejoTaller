package com.elitec.alejotaller.feature.auth.presentation.uiStates

data class ProfileUiState(
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val errorMessage: String? = null
)