package com.elitec.alejotaller.infraestructure.extents.presentation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun NavBackStack<NavKey>.navigateBackTo(destination: NavKey) {
    if (this.isEmpty()) return
    removeLastOrNull()

    if(destination !in this) return

    while (isNotEmpty() && last() != destination) {
        removeLastOrNull()
    }
}