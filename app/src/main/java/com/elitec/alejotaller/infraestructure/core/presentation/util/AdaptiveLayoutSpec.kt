package com.elitec.alejotaller.infraestructure.core.presentation.util

import androidx.compose.runtime.Immutable

@Immutable
data class AdaptiveLayoutSpec(
    val posture: DevicePosture,
    val showListAndDetail: Boolean,
    val showTopBarInDetail: Boolean,
    val maxContentWidthDp: Int,
)