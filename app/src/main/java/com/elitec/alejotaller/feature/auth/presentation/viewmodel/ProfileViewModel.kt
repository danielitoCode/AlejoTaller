package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhotoUrlCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserNameUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPassCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhoneCaseUse
import com.elitec.alejotaller.feature.auth.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getProfileUseCase: GetCurrentUserInfoCaseUse,
    private val updatePhotoUrlCaseUse: UpdateUserPhotoUrlCaseUse,
    private val updateNameCaseUse: UpdateUserNameUseCase,
    private val updateUserPassCaseUse: UpdateUserPassCaseUse,
    private val updatePhoneCaseUse: UpdateUserPhoneCaseUse,
): ViewModel() {
    private var _userProfile = MutableStateFlow<User?>(null)
    val userProfile get() = _userProfile.asStateFlow()

    fun updatePhotoUrl(photoUrl: String, onPhotoUpload: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            updatePhotoUrlCaseUse(photoUrl)
                .onSuccess { onPhotoUpload() }
                .onFailure { onFail() }
        }
    }
    fun updateName(newName: String, onNameUpdate: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            updateNameCaseUse(newName)
                .onSuccess { onNameUpdate() }
                .onFailure { onFail() }
        }
    }
    fun updatePass(newPass: String, onPassUpdate: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            updateUserPassCaseUse(newPass)
                .onSuccess { onPassUpdate() }
                .onFailure { onFail() }
        }
    }
    fun updatePhone(newPhone: String, onPhoneUpdate: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            updatePhoneCaseUse(newPhone)
                .onSuccess { onPhoneUpdate() }
                .onFailure { onFail() }
        }
    }
    fun getAccountInfo(onGetInfo: () -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            getProfileUseCase()
                .onSuccess { user ->
                    _userProfile.value = user
                    onGetInfo()
                }
                .onFailure { onFail() }
        }
    }
}