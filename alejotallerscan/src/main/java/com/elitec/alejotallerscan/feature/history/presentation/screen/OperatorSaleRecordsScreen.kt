package com.elitec.alejotallerscan.feature.history.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PendingActions
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.history.presentation.viewmodel.OperatorSaleRecordsViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperatorSaleRecordsScreen(
    onBack: () -> Unit,
    onOpenSale: () -> Unit
) {
    val viewModel: OperatorSaleRecordsViewModel = koinViewModel()
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val records by viewModel.records.collectAsState()
    val localSales by viewModel.localSales.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val pendingSales = localSales.filter { it.verified == BuyState.UNVERIFIED }
    val processedSales = localSales.filter { it.verified != BuyState.UNVERIFIED }

    OperatorScreen(
        title = "Registro interno",
        subtitle = "Revisa ventas pendientes, sincroniza cambios de otros operadores y abre una reserva para reprocesarla."
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sincronizacion", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Busca si alguna reserva pendiente ya fue procesada por otro operador y actualiza tu cache local.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = viewModel::refreshPendingSales,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null)
                        Text(
                            if (isSyncing) "Sincronizando pendientes..." else "Sincronizar pendientes",
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    }
                }
            }

            SaleSection(
                title = "Pendientes en este dispositivo",
                emptyText = "No hay reservas pendientes en cache.",
                sales = pendingSales,
                actionLabel = "Reprocesar reserva",
                onAction = { sale ->
                    salesViewModel.selectSale(sale) { onOpenSale() }
                }
            )

            SaleSection(
                title = "Ventas ya procesadas en cache",
                emptyText = "No hay ventas procesadas en cache.",
                sales = processedSales,
                actionLabel = null,
                onAction = {}
            )

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
                    Text("Historial de acciones del operador", style = MaterialTheme.typography.titleMedium)
                    if (records.isEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.History, contentDescription = null)
                            Text("Todavia no hay acciones registradas.")
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        records.forEach { record ->
                            StyledSaleCard(
                                title = record.saleId,
                                customerName = record.customerName,
                                userId = record.userId,
                                dateLabel = "Fecha venta",
                                dateValue = record.saleDate,
                                amount = record.amount,
                                items = record.itemsSummary,
                                accent = if (record.action.name == "CONFIRMED") VerifiedAccent else RejectedAccent,
                                extraLine = "Registrado: ${record.recordedAtEpochMillis.toReadableDate()}",
                                actionContent = null
                            )
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

@Composable
private fun SaleSection(
    title: String,
    emptyText: String,
    sales: List<Sale>,
    actionLabel: String?,
    onAction: (Sale) -> Unit
) {
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
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (sales.isEmpty()) {
                Text(emptyText)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    sales.forEach { sale ->
                        StyledSaleCard(
                            title = sale.id,
                            customerName = sale.customerName,
                            userId = sale.userId,
                            dateLabel = "Fecha",
                            dateValue = sale.date.toString(),
                            amount = sale.amount,
                            items = sale.products.map { item -> "${item.productName ?: item.productId} x${item.quantity}" },
                            accent = sale.toAccent(),
                            extraLine = "Estado local: ${sale.verified}",
                            actionContent = if (actionLabel != null) {
                                {
                                    Button(
                                        onClick = { onAction(sale) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(actionLabel)
                                    }
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleStateBadge(sale: Sale) {
    val accent = sale.toAccent()

    Surface(
        color = accent.container,
        shape = CardDefaults.shape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun StyledSaleCard(
    title: String,
    customerName: String?,
    userId: String,
    dateLabel: String,
    dateValue: String,
    amount: Double,
    items: List<String>,
    accent: SaleAccent,
    extraLine: String,
    actionContent: (@Composable () -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = accent.card
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .padding(bottom = 2.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(color = accent.container, shape = CardDefaults.shape) {
                    Text(
                        accent.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = accent.content,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
            Text(title, style = MaterialTheme.typography.titleLarge, color = accent.headline)
            Text(
                customerName ?: "Cliente sin nombre disponible",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text("Cliente: $userId", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("$dateLabel: $dateValue", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(extraLine, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "Importe: \$${"%.2f".format(amount)}",
                style = MaterialTheme.typography.titleMedium,
                color = accent.headline
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Items", style = MaterialTheme.typography.labelLarge)
                items.forEach { item ->
                    Text("- $item", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            actionContent?.invoke()
        }
    }
}

private fun Sale.toAccent(): SaleAccent = when (verified) {
    BuyState.UNVERIFIED -> PendingAccent
    BuyState.VERIFIED -> VerifiedAccent
    BuyState.DELETED -> RejectedAccent
}

private data class SaleAccent(
    val label: String,
    val card: Color,
    val container: Color,
    val content: Color,
    val headline: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val PendingAccent = SaleAccent(
    label = "Pendiente local",
    card = Color(0xFFFFF3E8),
    container = Color(0xFFFFB74D),
    content = Color(0xFF5D3200),
    headline = Color(0xFFB85C00),
    icon = Icons.Rounded.PendingActions
)

private val VerifiedAccent = SaleAccent(
    label = "Verificada remotamente",
    card = Color(0xFFEAF8EE),
    container = Color(0xFF4CAF50),
    content = Color(0xFFFFFFFF),
    headline = Color(0xFF1E7A34),
    icon = Icons.Rounded.CheckCircle
)

private val RejectedAccent = SaleAccent(
    label = "Rechazada remotamente",
    card = Color(0xFFFFECEC),
    container = Color(0xFFE53935),
    content = Color(0xFFFFFFFF),
    headline = Color(0xFFB3261E),
    icon = Icons.Rounded.RemoveCircle
)

private fun Long.toReadableDate(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(this))
