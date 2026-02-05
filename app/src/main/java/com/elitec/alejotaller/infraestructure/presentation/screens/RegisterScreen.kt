package com.elitec.alejotaller.infraestructure.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterReady: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            onClick = {
                onRegisterReady("Usuario registrado")
            }
        ) {
            Text(
                text = "Registrarse"
            )
        }
        Button(
            onClick = {
                onNavigateBack()
            }
        ) {
            Text(
                text = "Navegar atras"
            )
        }
    }
}