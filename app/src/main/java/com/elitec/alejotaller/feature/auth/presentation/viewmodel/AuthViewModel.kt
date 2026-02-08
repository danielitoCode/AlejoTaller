package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthWithGoogleCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authUserCaseUse: AuthUserCaseUse,
    private val authWhitGoogleUseCase: AuthWithGoogleCaseUse,
    private val logoutUserCaseUse: CloseSessionCaseUse
): ViewModel() {
    fun autUser(email: String, pass: String, onUserLogIn: () -> Unit,onFail: () -> Unit) {
        viewModelScope.launch {
            authUserCaseUse(email, pass)
                .onSuccess { onUserLogIn() }
                .onFailure { onFail() }
        }
    }

    fun authWithGoogle(onUserLogIn: () -> Unit,onFail: () -> Unit) {
        viewModelScope.launch {
            authWhitGoogleUseCase()
                .onSuccess { onUserLogIn() }
                .onFailure { onFail() }
        }
    }

    fun logoutUser(onLogout: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            logoutUserCaseUse()
                .onSuccess { onLogout() }
                .onFailure { onFail() }
        }
    }
}