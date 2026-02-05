package com.elitec.alejotaller.infraestructure.presentation.screens

import android.window.SplashScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay

@Suppress("LambdaParameterInEffect")
@Composable
fun SplashScreen(
    onInitChargeReady: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(null) {
        delay(2000)
        onInitChargeReady()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = "Splash Screen")
    }
}