package com.elitec.alejotallerscan.feature.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OperatorHomeScreen(
    onOpenScan: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenConfirmation: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Panel operador", style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Funciones MVP", style = MaterialTheme.typography.titleLarge)
                Button(onClick = onOpenScan, modifier = Modifier.fillMaxWidth()) {
                    Text("Escanear pedido")
                }
                Button(onClick = onOpenConfirmation, modifier = Modifier.fillMaxWidth()) {
                    Text("Confirmar pago")
                }
                Button(onClick = onOpenReservations, modifier = Modifier.fillMaxWidth()) {
                    Text("Buscar reservas")
                }
            }
        }
    }
}
