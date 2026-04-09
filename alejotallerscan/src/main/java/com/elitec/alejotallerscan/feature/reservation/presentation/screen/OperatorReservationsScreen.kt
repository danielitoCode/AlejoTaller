package com.elitec.alejotallerscan.feature.reservation.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.PersonSearch
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RemoveCircle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationSearchField
import com.elitec.alejotallerscan.feature.reservation.domain.entity.ReservationStatusFilter
import com.elitec.alejotallerscan.feature.reservation.presentation.viewmodel.OperatorReservationSearchViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorPanelCard
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorSectionLabel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
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
        subtitle = "Busca por pedido o cliente, revisa las lineas del pedido y entra al reproceso sin perder contexto.",
        heroIcon = Icons.Rounded.ReceiptLong
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OperatorPanelCard {
                OperatorSectionLabel("Busqueda manual")
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

                searchState.error?.let {
                    FeedbackBanner(
                        text = it,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                searchState.notice?.let {
                    FeedbackBanner(
                        text = it,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Button(
                    onClick = searchViewModel::search,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !searchState.isLoading
                ) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                    Text("Buscar", modifier = Modifier.padding(start = 10.dp))
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
    OperatorPanelCard {
        OperatorSectionLabel(title)
        if (sales.isEmpty()) {
            Text(emptyText)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                sales.forEach { sale ->
                    ReservationSaleCard(sale = sale, onClick = { onSelect(sale) })
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
    val accent = reservationAccentFor(sale)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = accent.card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(color = accent.container, shape = CardDefaults.shape) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = accent.icon,
                        contentDescription = null,
                        tint = accent.content
                    )
                    Text(
                        text = accent.label,
                        color = accent.content,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Text(sale.id, style = MaterialTheme.typography.titleMedium)
            Text("Nombre: ${sale.customerName ?: "No disponible"}")
            Text("Cliente: ${sale.userId}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Fecha: ${sale.date}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Importe: \$${"%.2f".format(sale.amount)}", style = MaterialTheme.typography.titleSmall)
            Text(
                "Items: ${sale.products.size} - Unidades: ${sale.products.sumOf { it.quantity }}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            sale.products.forEach { item ->
                Text(
                    "- ${item.productName ?: item.productId} x${item.quantity}",
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

@Composable
private fun FeedbackBanner(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(color = containerColor, shape = CardDefaults.shape) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = contentColor
        )
    }
}

private data class ReservationAccent(
    val label: String,
    val card: Color,
    val container: Color,
    val content: Color,
    val icon: ImageVector
)

@Composable
private fun reservationAccentFor(sale: Sale): ReservationAccent = when (sale.verified) {
    BuyState.UNVERIFIED -> ReservationAccent(
        label = "Pendiente local",
        card = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer,
        icon = Icons.Rounded.PendingActions
    )

    BuyState.VERIFIED -> ReservationAccent(
        label = "Verificada",
        card = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        container = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = Icons.Rounded.CheckCircle
    )

    BuyState.DELETED -> ReservationAccent(
        label = "Rechazada",
        card = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
        icon = Icons.Rounded.RemoveCircle
    )
}
