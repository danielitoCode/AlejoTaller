package com.elitec.alejotaller.feature.auth.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R

@Composable
fun LoginTitle(
    glowAlpha: Float,
    colors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        SurfaceIcon(alpha = glowAlpha)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Accede con tu cuenta para continuar.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SurfaceIcon(
    alpha: Float
) {
    Surface(
        color = Color.Transparent,// MaterialTheme.colorScheme.primaryContainer.copy(alpha),
        shape = MaterialShapes.Cookie9Sided.toShape(),
        modifier = Modifier.size(180.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            LoadingIndicator(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha),
                modifier = Modifier.fillMaxSize()
            )
            Icon(
                painter = painterResource(R.drawable.alejoicon_clean),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxSize().padding(30.dp)
            )
        }
    }
}