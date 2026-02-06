package com.elitec.alejotaller.infraestructure.core.presentation.screens

import android.window.SplashScreen
import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
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
        Icon(
            painter = painterResource(R.drawable.alejoicon_clean),
            contentDescription = "App icon",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(180.dp)
        )
    }
}