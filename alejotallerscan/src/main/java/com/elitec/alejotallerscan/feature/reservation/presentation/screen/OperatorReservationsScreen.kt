package com.elitec.alejotallerscan.feature.reservation.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationSearchField
import com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel.OperatorReservationSearchViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorReservationsScreen(
    onBack: () -> Unit,
    onOpenSale: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val searchViewModel: OperatorReservationSearchViewModel = koinViewModel()
    val recentSales by salesViewModel.recentSales.collectAsState()
    val searchState by searchViewModel.uiState.collectAsState()

    OperatorScreen(
        title = "Reservas y ventas",
        subtitle = "Busca por ID visual del pedido, ID del cliente, nombre o fecha. El nombre funcionara cuando backend persista customer_name."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                    OutlinedTextField(
                        value = searchState.query,
                        onValueChange = searchViewModel::updateQuery,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                when (searchState.field) {
                                    ReservationSearchField.SALE_ID -> "ID visual del pedido"
                                    ReservationSearchField.USER_ID -> "ID del cliente"
                                    ReservationSearchField.CUSTOMER_NAME -> "Nombre del cliente"
                                    ReservationSearchField.DATE -> "Fecha (AAAA-MM-DD)"
                                }
                            )
                        },
                        leadingIcon = { Icon(Icons.Rounded.PersonSearch, contentDescription = null) }
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReservationSearchField.entries.forEach { field ->
                            val selected = field == searchState.field
                            AssistChip(
                                onClick = { searchViewModel.updateField(field) },
                                label = {
                                    Text(
                                        when (field) {
                                            ReservationSearchField.SALE_ID -> "Pedido"
                                            ReservationSearchField.USER_ID -> "Cliente"
                                            ReservationSearchField.CUSTOMER_NAME -> "Nombre"
                                            ReservationSearchField.DATE -> "Fecha"
                                        }
                                    )
                                },
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

                    Text(
                        "Si una venta todavia no guarda customer_name en backend, la busqueda por nombre no devolvera coincidencias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    searchState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    searchState.notice?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

                    Button(
                        onClick = searchViewModel::search,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !searchState.isLoading
                    ) {
                        Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                        Text("Buscar", modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }

            if (searchState.results.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Resultados remotos", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(searchState.results, key = { it.id }) { sale ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            salesViewModel.selectSale(sale, onOpenSale)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(sale.id, style = MaterialTheme.typography.titleMedium)
                                        Text("Nombre: ${sale.customerName ?: "No disponible"}")
                                        Text("Cliente: ${sale.userId}")
                                        Text("Fecha: ${sale.date}")
                                        Text("Estado: ${sale.verified}")
                                        Text("Importe: \$${"%.2f".format(sale.amount)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Recientes en caché", style = MaterialTheme.typography.titleMedium)
                    if (recentSales.isEmpty()) {
                        Text("Todavia no hay reservas en cache local.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentSales, key = { it.id }) { sale ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            salesViewModel.selectSale(sale, onOpenSale)
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(sale.id, style = MaterialTheme.typography.titleMedium)
                                        Text("Nombre: ${sale.customerName ?: "No disponible"}")
                                        Text("Cliente: ${sale.userId}")
                                        Text("Fecha: ${sale.date}")
                                        Text("Estado: ${sale.verified}")
                                        Text("Importe: \$${"%.2f".format(sale.amount)}")
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                        Text("Volver", modifier = Modifier.padding(start = 10.dp))
                    }
                }
            }
        }
    }
}
