package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem

@Composable
fun BuyScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Tu carrito",
            style = MaterialTheme.typography.headlineSmall
        )

        if (items.isEmpty()) {
            Text(
                text = "No hay productos en el carrito.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.product.id }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = item.product.name, style = MaterialTheme.typography.titleMedium)
                            Text(text = "$ ${item.product.price}")
                            Text(text = "Cantidad: ${item.quantity}")

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(onClick = { onDecreaseQuantity(item.product.id) }) {
                                    Text(text = "-1")
                                }
                                OutlinedButton(onClick = { onIncreaseQuantity(item.product.id) }) {
                                    Text(text = "+1")
                                }
                                OutlinedButton(onClick = { onRemoveItem(item.product.id) }) {
                                    Text(text = "Quitar")
                                }
                            }
                        }
                    }
                }
            }

            Text(
                text = "Total: $ ${"%.2f".format(totalAmount)}",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = onConfirmClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Continuar")
            }
        }
    }
}