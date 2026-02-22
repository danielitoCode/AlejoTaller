package com.elitec.alejotaller.infraestructure.core.presentation.util

import android.content.res.Configuration

fun Configuration.toDeviceMode(): AppWindowType =
    when {
        orientation == 1 && screenWidthDp <= 768 -> AppWindowType.MobilePortrait
        orientation == 2 && screenHeightDp <= 768 -> AppWindowType.MobileLandscape
        orientation == 1 && (screenWidthDp in 768..1024) -> AppWindowType.TabletPortrait
        orientation == 2 && (screenHeightDp in 768..1024) -> AppWindowType.TabletLandscape
        screenWidthDp in 1024..1440 -> AppWindowType.Laptop
        screenWidthDp >= 1440 -> AppWindowType.Expanded
        else -> AppWindowType.MobilePortrait
    }