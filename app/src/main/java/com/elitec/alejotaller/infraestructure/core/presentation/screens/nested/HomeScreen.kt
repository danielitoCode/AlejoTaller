package com.elitec.alejotaller.infraestructure.core.presentation.screens.nested

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Suppress("ModifierTopMost")
@Composable
fun HomeScreen(
    onNavigateBack: () -> Unit,
    userId: String,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Text(
                text = "Usuario autenticado: $userId"
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(innerPadding)
        ) {
            Button(
                onClick = {
                    onNavigateBack()
                }
            ) {
                Text(
                    text = "Navegar atras"
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(30) { item ->
                    Surface(
                        onClick = {

                        }
                    ) {
                        Text(
                            modifier = Modifier.padding(5.dp),
                            text = "Item $item"
                        )
                    }
                }
            }
        }
    }
}