package com.elitec.alejotaller.infraestructure.core.presentation.uiModels

import androidx.compose.ui.graphics.vector.ImageVector
import com.elitec.alejotaller.infraestructure.core.presentation.navigation.InternalRoutesKey

data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: InternalRoutesKey
)