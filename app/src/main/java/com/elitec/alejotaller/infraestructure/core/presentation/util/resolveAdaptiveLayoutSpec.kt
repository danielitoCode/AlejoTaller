package com.elitec.alejotaller.infraestructure.core.presentation.util

import android.content.res.Configuration
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

fun resolveAdaptiveLayoutSpec(
    configuration: Configuration,
    windowSizeClass: WindowSizeClass,
): AdaptiveLayoutSpec {
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val posture = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> if (isLandscape) DevicePosture.CompactLandscape else DevicePosture.CompactPortrait
        WindowWidthSizeClass.MEDIUM -> if (isLandscape) DevicePosture.MediumLandscape else DevicePosture.MediumPortrait
        else -> DevicePosture.Expanded
    }

    return when (posture) {
        DevicePosture.CompactPortrait -> AdaptiveLayoutSpec(
            posture = posture,
            showListAndDetail = false,
            showTopBarInDetail = true,
            maxContentWidthDp = 600,
        )

        DevicePosture.CompactLandscape,
        DevicePosture.MediumPortrait,
        DevicePosture.MediumLandscape,
        DevicePosture.Expanded -> AdaptiveLayoutSpec(
            posture = posture,
            showListAndDetail = true,
            showTopBarInDetail = false,
            maxContentWidthDp = 920,
        )
    }
}