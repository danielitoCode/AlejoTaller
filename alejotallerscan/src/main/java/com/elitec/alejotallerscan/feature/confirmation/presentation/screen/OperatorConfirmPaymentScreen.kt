package com.elitec.alejotallerscan.feature.confirmation.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorConfirmPaymentScreen(
    onBack: () -> Unit,
    onOpenScan: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val uiState by salesViewModel.uiState.collectAsState()
    val sale = uiState.selectedSale

    OperatorScreen(
        title = "Confirmar pago",
        subtitle = "Actualiza la reserva del cliente y dispara la confirmacion hacia su app."
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (sale == null) {
                    Text("Aun no hay una venta cargada.")
                    Button(onClick = onOpenScan, modifier = Modifier.fillMaxWidth()) {
                        androidx.compose.material3.Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                        Text("Escanear reserva", modifier = Modifier.padding(start = 10.dp))
                    }
                } else {
                    Text("Venta: ${sale.id}", style = MaterialTheme.typography.titleLarge)
                    Text("Estado actual: ${sale.verified}", style = MaterialTheme.typography.bodyLarge)
                    Text("Importe: \$${"%.2f".format(sale.amount)}")
                    Text("Cliente: ${sale.userId}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Entrega: ${sale.deliveryType ?: "Sin definir"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Productos", style = MaterialTheme.typography.titleMedium)
                    sale.products.forEach { item ->
                        Text("- ${item.productId} x${item.quantity}")
                    }
                }

                uiState.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = { salesViewModel.confirmSelectedSale() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("Confirmar pago", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(
                    onClick = { salesViewModel.rejectSelectedSale() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.Cancel, contentDescription = null)
                    Text("Rechazar reserva", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Text("Volver", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
