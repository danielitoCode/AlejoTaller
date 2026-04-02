package com.elitec.alejotallerscan.feature.confirmation.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.confirmation.presentation.entity.OperatorPaymentMethod
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
    var paymentMethod by rememberSaveable { mutableStateOf(OperatorPaymentMethod.CASH) }
    var operatorNote by rememberSaveable { mutableStateOf("") }

    OperatorScreen(
        title = "Confirmar reservacion",
        subtitle = "Revisa la venta cargada, valida sus items y decide si se confirma o se rechaza."
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
                        Text("Abrir registro de venta", modifier = Modifier.padding(start = 10.dp))
                    }
                } else {
                    Text("Venta: ${sale.id}", style = MaterialTheme.typography.titleLarge)
                    Text("Nombre: ${sale.customerName ?: "No disponible"}", style = MaterialTheme.typography.bodyLarge)
                    Text("Estado actual: ${sale.verified}", style = MaterialTheme.typography.bodyLarge)
                    Text("Importe: \$${"%.2f".format(sale.amount)}")
                    Text("Fecha: ${sale.date}")
                    Text("Cliente: ${sale.userId}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Entrega: ${sale.deliveryType ?: "Sin definir"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Lineas del pedido: ${sale.products.size} - Unidades: ${sale.products.sumOf { it.quantity }}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    sale.deliveryAddress?.let { address ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Direccion operativa", style = MaterialTheme.typography.titleSmall)
                                Text("${address.mainStreet} #${address.houseNumber}, ${address.municipality}, ${address.province}")
                                Text("Telefono: ${address.phone}")
                                if (!address.referenceName.isNullOrBlank()) {
                                    Text("Preguntar por: ${address.referenceName}")
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Control operativo", style = MaterialTheme.typography.titleMedium)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (method in OperatorPaymentMethod.entries) {
                                    val selected = paymentMethod == method
                                    AssistChip(
                                        onClick = { paymentMethod = method },
                                        label = {
                                            Text(
                                                if (method == OperatorPaymentMethod.CASH) "Efectivo" else "Pago directo"
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = if (selected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = operatorNote,
                                onValueChange = { operatorNote = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Observacion del operador") },
                                supportingText = {
                                    Text("Aun se conserva solo como apoyo visual local.")
                                }
                            )
                            Text(
                                "Metodo seleccionado: ${if (paymentMethod == OperatorPaymentMethod.CASH) "Efectivo" else "Pago directo entre cliente y vendedor"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Text("Items del pedido", style = MaterialTheme.typography.titleMedium)
                    sale.products.forEach { item ->
                        Text("- ${item.productName ?: item.productId} x${item.quantity}")
                    }
                }

                if (uiState.notice != null) {
                    Text(uiState.notice ?: "", color = MaterialTheme.colorScheme.primary)
                }
                if (uiState.error != null) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { salesViewModel.confirmSelectedSale() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("Confirmar venta", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(
                    onClick = { salesViewModel.rejectSelectedSale() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    androidx.compose.material3.Icon(Icons.Rounded.Cancel, contentDescription = null)
                    Text("Marcar como rechazada", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.material3.Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Text("Volver", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
