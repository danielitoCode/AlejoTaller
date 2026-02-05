package com.elitec.alejotaller.infraestructure.extention.presentation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateBack() {
    if (this.isEmpty()) return
    removeLastOrNull()
}