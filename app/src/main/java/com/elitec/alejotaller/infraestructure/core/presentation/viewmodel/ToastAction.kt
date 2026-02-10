package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

sealed interface ToastAction {
    data class Show(val event: ToastEvent) : ToastAction
    data class Dismiss(val id: String) : ToastAction
}
