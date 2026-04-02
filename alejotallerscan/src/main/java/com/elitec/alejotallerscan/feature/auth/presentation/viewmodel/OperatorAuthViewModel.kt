package com.elitec.alejotallerscan.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.shared.auth.feature.auth.domain.caseuse.AuthOperatorUserCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.shared.auth.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.shared.auth.feature.auth.domain.entity.hasOperatorAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OperatorAuthViewModel(
    private val authOperatorUserCaseUse: AuthOperatorUserCaseUse,
    private val getCurrentUserInfoCaseUse: GetCurrentUserInfoCaseUse,
    private val closeSessionCaseUse: CloseSessionCaseUse
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorAuthUiState())
    val uiState: StateFlow<OperatorAuthUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            getCurrentUserInfoCaseUse()
                .onSuccess { user ->
                    if (user.userProfile.role.hasOperatorAccess()) {
                        _uiState.value = _uiState.value.copy(currentUser = user, error = null)
                    } else {
                        closeSessionCaseUse()
                        _uiState.value = OperatorAuthUiState(
                            error = "La cuenta autenticada no tiene permisos de operador."
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(currentUser = null)
                }
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            authOperatorUserCaseUse(email, pass)
                .onSuccess { user ->
                    _uiState.value = OperatorAuthUiState(currentUser = user)
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.value = OperatorAuthUiState(
                        error = error.message ?: "No se pudo iniciar sesion"
                    )
                }
        }
    }

    fun logout(onLogout: () -> Unit) {
        viewModelScope.launch {
            closeSessionCaseUse()
            _uiState.value = OperatorAuthUiState()
            onLogout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
