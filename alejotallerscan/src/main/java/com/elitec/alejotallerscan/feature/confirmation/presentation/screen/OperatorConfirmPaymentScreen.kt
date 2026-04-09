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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorPanelCard
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorSectionLabel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorTone
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorConfirmPaymentScreen(
    onBack: () -> Unit,
    onOpenScan: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val scanViewModel: OperatorScanViewModel = koinViewModel()
    val uiState by salesViewModel.uiState.collectAsState()
    val sale = uiState.selectedSale
    var paymentMethod by rememberSaveable { mutableStateOf(OperatorPaymentMethod.CASH) }
    var operatorNote by rememberSaveable { mutableStateOf("") }

    OperatorScreen(
        title = "Confirmar reservacion",
        subtitle = "Revisa la reserva cargada, valida los items y confirma solo cuando el estado remoto haya quedado consistente.",
        heroIcon = Icons.Rounded.CheckCircle
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OperatorPanelCard {
                if (sale == null) {
                    OperatorSectionLabel("Sin pedido cargado", tone = OperatorTone.Warning)
                    Text("Aun no hay una venta cargada.")
                    Button(onClick = onOpenScan, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                        Text("Abrir registro de venta", modifier = Modifier.padding(start = 10.dp))
                    }
                } else {
                    OperatorSectionLabel("Resumen operativo")
                    Text("Venta: ${sale.id}", style = MaterialTheme.typography.titleLarge)
                    Text("Nombre: ${sale.customerName ?: "No disponible"}", style = MaterialTheme.typography.bodyLarge)

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StateBadge("Estado ${sale.verified}", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                        StateBadge("Importe \$${"%.2f".format(sale.amount)}", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    }

                    InfoBlock(
                        lines = listOf(
                            "Fecha: ${sale.date}",
                            "Cliente: ${sale.userId}",
                            "Entrega: ${sale.deliveryType ?: "Sin definir"}",
                            "Lineas del pedido: ${sale.products.size} - Unidades: ${sale.products.sumOf { it.quantity }}"
                        )
                    )

                    sale.deliveryAddress?.let { address ->
                        InfoBlock(
                            title = "Direccion operativa",
                            lines = buildList {
                                add("${address.mainStreet} #${address.houseNumber}, ${address.municipality}, ${address.province}")
                                add("Telefono: ${address.phone}")
                                if (!address.referenceName.isNullOrBlank()) {
                                    add("Preguntar por: ${address.referenceName}")
                                }
                            }
                        )
                    }
                }
            }

            if (sale != null) {
                OperatorPanelCard {
                    OperatorSectionLabel("Control operativo")
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
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Rounded.CheckCircle, contentDescription = null) }
                                } else null,
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OperatorPanelCard {
                    OperatorSectionLabel("Items del pedido")
                    sale.products.forEach { item ->
                        InfoBlock(
                            lines = listOf(
                                item.productName ?: item.productId,
                                "Cantidad: ${item.quantity}"
                            )
                        )
                    }
                }
            }

            OperatorPanelCard {
                uiState.notice?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                uiState.error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        salesViewModel.confirmSelectedSale {
                            salesViewModel.resetState()
                            scanViewModel.resetState()
                            onOpenScan()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("Confirmar venta", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(
                    onClick = {
                        salesViewModel.rejectSelectedSale {
                            salesViewModel.resetState()
                            scanViewModel.resetState()
                            onOpenScan()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    Icon(Icons.Rounded.Cancel, contentDescription = null)
                    Text("Marcar como rechazada", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Text("Volver", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun StateBadge(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        color = containerColor,
        shape = CardDefaults.shape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun InfoBlock(
    title: String? = null,
    lines: List<String>
) {
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            title?.let {
                Text(it, style = MaterialTheme.typography.titleSmall)
            }
            lines.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
