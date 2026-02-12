package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem

@Composable
fun BuyConfirmScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onBackClick: () -> Unit,
    onSubmitPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Confirmar compra",
            style = MaterialTheme.typography.headlineSmall
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.product.id }) { item ->
                Text(text = "• ${item.product.name} x${item.quantity} = $ ${"%.2f".format(item.product.price * item.quantity)}")
            }
        }

        Text(
            text = "Total final: $ ${"%.2f".format(totalAmount)}",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = onSubmitPurchase,
            modifier = Modifier.fillMaxWidth(),
            enabled = items.isNotEmpty()
        ) {
            Text(text = "Confirmar pedido")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver")
        }
    }
}