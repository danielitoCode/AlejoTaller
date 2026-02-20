package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthUserCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.AuthWithGoogleCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.alejotaller.feature.auth.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authUserCaseUse: AuthUserCaseUse,
    private val authWhitGoogleUseCase: AuthWithGoogleCaseUse,
    private val logoutUserCaseUse: CloseSessionCaseUse
): ViewModel() {
    fun autUser(email: String, pass: String, onUserLogIn: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            authUserCaseUse(email, pass)
                .onSuccess {
                    userId -> onUserLogIn(userId)
                }
                .onFailure { error ->
                    onFail(error.message ?: "")
                }
        }
    }

    fun authWithGoogle(context: Context, onUserLogIn: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            authWhitGoogleUseCase(context)
                .onSuccess { userId -> onUserLogIn(userId) }
                .onFailure { error -> onFail(error.message ?: "") }
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