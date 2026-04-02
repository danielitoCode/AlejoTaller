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
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationStatusFilter
import com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel.OperatorReservationSearchViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
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
    val filteredRecentSales = recentSales.filter { sale ->
        searchState.statusFilter.state?.let { targetState -> sale.verified == targetState } ?: true
    }

    OperatorScreen(
        title = "Reservas y ventas",
        subtitle = "Busca en Appwrite por pedido, cliente, nombre o fecha, revisa sus items y luego confirma."
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
                        "Si una venta no guarda customer_name en backend, la busqueda por nombre no devolvera coincidencias.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReservationStatusFilter.entries.forEach { filter ->
                            val selected = filter == searchState.statusFilter
                            AssistChip(
                                onClick = { searchViewModel.updateStatusFilter(filter) },
                                label = {
                                    Text(
                                        when (filter) {
                                            ReservationStatusFilter.ALL -> "Todos"
                                            ReservationStatusFilter.PENDING -> "Pendientes"
                                            ReservationStatusFilter.CONFIRMED -> "Confirmadas"
                                            ReservationStatusFilter.REJECTED -> "Rechazadas"
                                        }
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (selected) {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            )
                        }
                    }

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
                ReservationSection(
                    title = "Resultados remotos",
                    sales = searchState.results,
                    onSelect = { sale ->
                        salesViewModel.selectSale(sale, onOpenSale)
                    }
                )
            }

            ReservationSection(
                title = "Recientes en cache",
                sales = filteredRecentSales,
                emptyText = "Todavia no hay reservas en cache local.",
                onSelect = { sale ->
                    salesViewModel.selectSale(sale, onOpenSale)
                }
            )

            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Text("Volver", modifier = Modifier.padding(start = 10.dp))
            }
        }
    }
}

@Composable
private fun ReservationSection(
    title: String,
    sales: List<Sale>,
    emptyText: String = "No hay resultados.",
    onSelect: (Sale) -> Unit
) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (sales.isEmpty()) {
                Text(emptyText)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sales, key = { it.id }) { sale ->
                        ReservationSaleCard(
                            sale = sale,
                            onClick = { onSelect(sale) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationSaleCard(
    sale: Sale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
            Text(
                "Items: ${sale.products.size} - Unidades: ${sale.products.sumOf { it.quantity }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            sale.products.forEach { item ->
                Text(
                    "• ${item.productName ?: item.productId} x${item.quantity}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "Toca para revisar y confirmar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
