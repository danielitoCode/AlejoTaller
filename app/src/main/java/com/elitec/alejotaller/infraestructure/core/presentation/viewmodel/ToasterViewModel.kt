package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dokar.sonner.ToastType
import com.dokar.sonner.ToasterState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ToasterViewModel: ViewModel() {
    var toasterState = MutableStateFlow<ToasterState?>(null)
    var toasterStatePublic: ToasterState? = null

    fun asignToastState(toastState: ToasterState) {
        toasterStatePublic = toastState
        toasterState.value = toastState
    }

    fun showMessage(message: String, type: ToastType, id: String? = null)  {
        toasterState.value?.show(
            message = message,
            type = type,
            id = id ?: Clock.System.now().toString()
        )
    }

    fun dismissMessage(id: String) {
        toasterState.value?.dismiss(id)
    }
}