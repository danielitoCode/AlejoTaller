package com.elitec.alejotaller.feature.auth.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitec.alejotaller.feature.auth.domain.caseuse.CloseSessionCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.GetCurrentUserInfoCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhotoUrlCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserNameUseCase
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPassCaseUse
import com.elitec.alejotaller.feature.auth.domain.caseuse.UpdateUserPhoneCaseUse
import com.elitec.alejotaller.feature.auth.domain.entity.User
import kotlinx.coroutines.flow.MutableStateFlow
import android.net.Uri
import androidx.core.graphics.scale
import com.elitec.alejotaller.feature.auth.presentation.uiStates.ProfileUiState
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    private val getProfileUseCase: GetCurrentUserInfoCaseUse,
    private val updatePhotoUrlCaseUse: UpdateUserPhotoUrlCaseUse,
    private val closeCurrentSession: CloseSessionCaseUse,
    private val updateNameCaseUse: UpdateUserNameUseCase,
    private val updateUserPassCaseUse: UpdateUserPassCaseUse,
    private val updatePhoneCaseUse: UpdateUserPhoneCaseUse,
) : ViewModel() {
    private var _userProfile = MutableStateFlow<User?>(null)
    val userProfile get() = _userProfile.asStateFlow()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    fun clearMessages() {
        _uiState.update { it.copy(saveMessage = null, errorMessage = null) }
    }

    fun updateProfile(
        userId: String,
        newName: String,
        currentName: String,
        newPhone: String,
        currentPhone: String,
        photoUri: Uri?,
        currentPhotoUrl: String?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveMessage = null, errorMessage = null) }
            val hasNameChange = newName != currentName
            val hasPhoneChange = newPhone != currentPhone
            val hasPhotoChange = photoUri != null && photoUri.toString() != currentPhotoUrl
            if (!hasNameChange && !hasPhoneChange && !hasPhotoChange) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveMessage = "No hay cambios para guardar"
                    )
                }
                return@launch
            }
            runCatching {
                if (hasNameChange) updateNameCaseUse(newName).getOrThrow()
                if (hasPhoneChange) updatePhoneCaseUse(newPhone).getOrThrow()
                if (hasPhotoChange) {
                    val compressedFile = compressImage(context, photoUri!!)
                    updatePhotoUrlCaseUse(compressedFile, userId).getOrThrow()
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveMessage = "Perfil actualizado correctamente"
                    )
                }
                onSuccess()
            }.onFailure {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo actualizar el perfil"
                    )
                }
            }
        }
    }

    private fun compressImage(context: Context, uri: Uri): File {
        val originalBitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        } ?: throw IllegalStateException("No se pudo abrir el archivo desde el URI")

        val scaledBitmap = originalBitmap.scale(800, 800, true)
        val compressedFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
        FileOutputStream(compressedFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }

        originalBitmap.recycle()
        scaledBitmap.recycle()

        return compressedFile
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
    fun getAccountInfo(onGetInfo: (String) -> Unit, onFail: () -> Unit) {
        viewModelScope.launch {
            getProfileUseCase()
                .onSuccess { user ->
                    _userProfile.value = user
                    onGetInfo(user.id)
                }
                .onFailure {
                    closeCurrentSession()
                    onFail()
                }
        }
    }
}