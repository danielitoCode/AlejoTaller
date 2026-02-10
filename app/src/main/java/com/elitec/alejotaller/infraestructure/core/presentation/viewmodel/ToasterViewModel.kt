package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ToasterViewModel: ViewModel() {
    private val _toastActions = MutableSharedFlow<ToastAction>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val toastActions = _toastActions.asSharedFlow()

    fun showMessage(message: String, type: ToastType, id: String? = null, isInfinite: Boolean = false)  {
        val event = ToastEvent(
            message = message,
            type = type,
            id = id ?: Clock.System.now().toString(),
            isInfinite = isInfinite
        )
        viewModelScope.launch {
            _toastActions.emit(ToastAction.Show(event))
        }
    }

    fun dismissMessage(id: String) {
        viewModelScope.launch {
            _toastActions.emit(ToastAction.Dismiss(id))
        }
    }
}