package com.elitec.alejotaller.infraestructure.core.presentation.util

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberAdaptiveLayoutSpec(): AdaptiveLayoutSpec {
    val configuration = LocalConfiguration.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return remember(configuration, windowSizeClass) {
        resolveAdaptiveLayoutSpec(configuration, windowSizeClass)
    }
}