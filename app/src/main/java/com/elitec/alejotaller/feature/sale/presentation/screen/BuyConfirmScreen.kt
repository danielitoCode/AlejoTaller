package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem

private enum class PaymentMethod(val label: String) {
    Transfermovil("Transfermóvil")
}

@Composable
fun BuyConfirmScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onBackClick: () -> Unit,
    onFail: (String) -> Unit,
    onSubmitPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethod.Transfermovil) }

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

        Text(
            text = "Revisa tu pedido y confirma el método de pago.",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.product.id }) { item ->
                Text(text = "• ${item.product.name} x${item.quantity} = $ ${"%.2f".format(item.product.price * item.quantity)}")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Método de pago",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedButton(
                    onClick = { selectedMethod = PaymentMethod.Transfermovil },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedMethod == PaymentMethod.Transfermovil,
                        onClick = { selectedMethod = PaymentMethod.Transfermovil }
                    )
                    Text(
                        text = selectedMethod.label,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Text(
                    text = "Al confirmar, se registrará el pedido para pago por Transfermóvil.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Total final: $ ${"%.2f".format(totalAmount)}",
            style = MaterialTheme.typography.titleLarge
        )

        Button(
            onClick = onSubmitPurchase,
            modifier = Modifier.fillMaxWidth(),
            enabled = items.isNotEmpty() && selectedMethod == PaymentMethod.Transfermovil
        ) {
            Text(text = "Confirmar pago")
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Volver")
        }
    }
}