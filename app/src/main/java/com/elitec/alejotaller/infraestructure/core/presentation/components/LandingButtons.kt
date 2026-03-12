package com.elitec.alejotaller.infraestructure.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// TestTag
const val NAVIGATE_TO_LOGIN_BUTTON = "NAVIGATE_TO_LOGIN_BUTTON"
const val NAVIGATE_TO_REGISTER_BUTTON = "NAVIGATE_TO_REGISTER_BUTTON"
@Composable
fun LandingButtons(
    modifier: Modifier = Modifier,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(NAVIGATE_TO_LOGIN_BUTTON),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Iniciar sesión")
        }
        ElevatedButton(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag(NAVIGATE_TO_REGISTER_BUTTON),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(text = "Crear cuenta")
        }
    }
}