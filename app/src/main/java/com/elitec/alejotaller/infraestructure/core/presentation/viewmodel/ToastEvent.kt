package com.elitec.alejotaller.infraestructure.core.presentation.viewmodel

import com.dokar.sonner.ToastType

data class ToastEvent(
    val message: String,
    val type: ToastType,
    val id: String
)