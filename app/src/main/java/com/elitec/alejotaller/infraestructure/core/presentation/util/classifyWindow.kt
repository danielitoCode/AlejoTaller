package com.elitec.alejotaller.infraestructure.core.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass

/*@Composable
fun classifyWindow(): AppWindowType {
    val windowSize = calculateWindowSizeClass()

    return remember(windowSize.widthSizeClass, windowSize.heightSizeClass) {
        when (windowSize.widthSizeClass) {
            // MOBILE
            WindowWidthSizeClass.Compact if windowSize.heightSizeClass >= WindowHeightSizeClass.Medium ->
                AppWindowType.MobilePortrait

            WindowWidthSizeClass.Medium if windowSize.heightSizeClass == WindowHeightSizeClass.Compact ->
                AppWindowType.MobileLandscape

            // TABLET
            WindowWidthSizeClass.Medium if windowSize.heightSizeClass == WindowHeightSizeClass.Medium ->
                AppWindowType.TabletPortrait

            // DESKTOP / EXPANDED
            WindowWidthSizeClass.Expanded if windowSize.heightSizeClass == WindowHeightSizeClass.Medium ->
                AppWindowType.Laptop

            else -> AppWindowType.Expanded
        }
    }
}*/