package com.elitec.alejotaller.infraestructure.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.elitec.alejotaller.infraestructure.presentation.navigation.MainRoutesKey

@Composable
fun LoginScreen(
    onNavigateTo: (MainRoutesKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Button(
            onClick = {
                onNavigateTo(MainRoutesKey.Home("Usuario de Prueba"))
            }
        ) {
            Text(
                text = "Navigate to Home"
            )
        }
        Button(
            onClick = {
                onNavigateTo(MainRoutesKey.Register)
            }
        ) {
            Text(
                text = "Navigate to Register"
            )
        }
    }
}