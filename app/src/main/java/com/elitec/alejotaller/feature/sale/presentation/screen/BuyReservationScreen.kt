package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun BuyReservationScreen(
    sales: List<Sale>,
    productNamesById: Map<String, String>,
    modifier: Modifier = Modifier
) {
    var selectedSale by remember(sales) { mutableStateOf(sales.firstOrNull()) }

    if (sales.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No hay compras registradas todavía",
                style = MaterialTheme.typography.titleMedium
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        selectedSale?.let { sale ->
            val qrContent = buildString {
                appendLine("transactionId=${sale.id}")
                appendLine("status=${sale.verified.name}")
                appendLine("amount=${"%.2f".format(sale.amount)}")
                appendLine("date=${sale.date}")
                appendLine("items=")
                sale.products.forEach { item ->
                    val name = item.productName ?: productNamesById[item.productId] ?: item.productId
                    appendLine("- $name x${item.quantity}")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Detalle de compra",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Image(
                        painter = rememberQrCodePainter(qrContent),
                        contentDescription = "Código QR de la compra seleccionada",
                        modifier = Modifier.size(180.dp)
                    )
                    Text(
                        text = "ID transacción: ${sale.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Estado: ${sale.verified.toLabel()}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                val isSelected = selectedSale?.id == sale.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSale = sale },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Compra #${sale.id.take(8)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Fecha: ${sale.date}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Estado: ${sale.verified.toLabel()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Total: ${"%.2f".format(sale.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        sale.products.forEach { item ->
                            val name = item.productName ?: productNamesById[item.productId] ?: item.productId
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = name, style = MaterialTheme.typography.bodySmall)
                                Text(text = "x${item.quantity}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun BuyState.toLabel(): String = when (this) {
    BuyState.UNVERIFIED -> "Reservada"
    BuyState.VERIFIED -> "Lista"
    BuyState.DELETED -> "Cancelada"
}