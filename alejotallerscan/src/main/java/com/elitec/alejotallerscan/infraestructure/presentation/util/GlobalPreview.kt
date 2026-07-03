package com.elitec.alejotallerscan.infraestructure.presentation.util

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elitec.alejotallerscan.infraestructure.core.presentation.theme.AlejoTallerTheme

@Composable
fun GlobalPreview(content: @Composable () -> Unit) {
    AlejoTallerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}