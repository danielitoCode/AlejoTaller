package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.auth.domain.caseuse.CustomRegisterCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.RegisterWithGoogleUseCase
import kotlinx.coroutines.launch

class RegistrationViewModel(
    private val registerUserCaseUse: RegisterWithGoogleUseCase,
    private val customRegisterCaseUse: CustomRegisterCaseUse
): ViewModel() {
    fun registerWithGoogle(onUserRegister: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            registerUserCaseUse()
                .onSuccess { userId -> onUserRegister(userId) }
                .onFailure { error -> onFail(error.message ?: "") }
        }
    }
    fun customRegister(email: String, password: String, name: String, onUserRegister: (String) -> Unit, onFail: (String) -> Unit) {
        viewModelScope.launch {
            customRegisterCaseUse(email, password, name)
                .onSuccess { userId -> onUserRegister(userId) }
                .onFailure { error -> onFail(error.message ?: "") }
        }
    }
}