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
import kotlinx.coroutines.flow.asStateFlow
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
): ViewModel() {
    private var _userProfile = MutableStateFlow<User?>(null)
    val userProfile get() = _userProfile.asStateFlow()

    fun updatePhotoUrl(
        userId: String,
        uri: Uri,
        context: Context,
        onPhotoUpload: (String) -> Unit,
        onFail: () -> Unit
    ) {
        viewModelScope.launch {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("No se pudo abrir el archivo desde el URI")

            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            val scaledBitmap = originalBitmap.scale(800, 800, true)

            // Archivo temporal comprimido
            val compressedFile = File.createTempFile("compressed_", ".jpg", context.cacheDir)
            FileOutputStream(compressedFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
            // Liberar memoria
            originalBitmap.recycle()
            scaledBitmap.recycle()

            updatePhotoUrlCaseUse(compressedFile, userId)
                .onSuccess { url -> onPhotoUpload(url) }
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