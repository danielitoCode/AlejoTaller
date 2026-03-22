package com.elitec.alejotaller.feature.sale.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.feature.sale.domain.entity.PaymentChannel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme

private enum class PaymentMethod(val label: String) {
    SolucionesCuba("Tarjeta virtual (UltraPay / SolucionesCuba)"),
    Transfermovil("Metodo de pago por transferencia directa"),
}

@Composable
fun BuyConfirmScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onBackClick: () -> Unit,
    onSubmitPurchase: (PaymentChannel?) -> Unit,
    onRegisterInUltrapay: () -> Unit,
    isSubmitting: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf<PaymentChannel?>(null) }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }

    val ultraPayBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF49535D).copy(0.3f),
            Color(0xFF49535D).copy(0.1f),
            Color(0xFF49535D).copy(0.4f),
            Color(0xFF49535D).copy(0.2f),
            Color(0xFF49535D).copy(0.6f),
        )
    )
    val transfermovilBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF49535D).copy(0.2f),
            Color(0xFF32FF72).copy(0.4f),
            Color.White.copy(0.1f),
            Color(0xFF606DFF).copy(0.2f),
            Color(0xFF32FF72).copy(0.3f),
            Color.White.copy(0.6f),
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = ""
            )
            Text(
                text = "Confirmar compra",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Text(
            text = "Puedes reservar sin pago online o elegir un canal de pago experimental.",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.product.id }) { item ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 5.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                text = item.product.name
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cantidad: "
                                )
                                Text(
                                    style = MaterialTheme.typography.titleSmall,
                                    text = "${item.quantity}"
                                )
                            }
                        }
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "A pagar:"
                            )
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                text = "${"%.2f".format(item.product.price * item.quantity)} $"
                            )
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Total final:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                fontWeight = FontWeight.Bold,
                text = "${"%.2f".format(totalAmount)} $",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.onSurface,
                        imageVector = Icons.Default.Payment,
                        contentDescription = ""
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = "Métodos de pago",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Surface(
                    tonalElevation = 5.dp,
                    shadowElevation = 5.dp,
                    shape = RoundedCornerShape(20.dp),
                    onClick = { selectedMethod = PaymentChannel.ULTRAPAY },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(ultraPayBrush)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.soluciones_cuba),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(15.dp))
                            )
                            Column(
                            ) {
                                Text(
                                    text = "UltraPay",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text(
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                                    text = "Targeta virtual SolucionesCuba",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            RadioButton(
                                selected = selectedMethod == PaymentChannel.ULTRAPAY,
                                onClick = { selectedMethod = PaymentChannel.ULTRAPAY }
                            )
                        }
                        Text(
                            text = "Si aún no tienes cuenta, regístrate debajo",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(
                                start = 15.dp,
                                top = 5.dp,
                                bottom = 5.dp
                            )
                        )
                        Button(
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF49535D),
                                disabledContainerColor = Color(0xFF49535D).copy(0.7f)
                            ),
                            onClick = {},
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    bottom = 10.dp
                                )
                        ) {
                            Text(
                                color = Color.White,
                                text = "Registrarse en UltraPay"
                            )
                        }
                    }
                }
                Surface(
                    tonalElevation = 5.dp,
                    shadowElevation = 5.dp,
                    shape = RoundedCornerShape(20.dp),
                    onClick = { selectedMethod = PaymentChannel.TRANSFERMOVIL },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .background(transfermovilBrush)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.transfermovil),
                                contentDescription = "",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(15.dp))
                            )
                            Column(
                            ) {
                                Text(
                                    text = "Transfermóvil",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text(
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.8f),
                                    text = "Pasarela de pagos cubana",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            RadioButton(
                                selected = selectedMethod == PaymentChannel.TRANSFERMOVIL,
                                onClick = { selectedMethod = PaymentChannel.TRANSFERMOVIL }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showConfirmSubmitDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting && items.isNotEmpty()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator()
            } else {
                Text(text = if (selectedMethod == null) "Reservar sin pago online" else "Solicitar pedido y pagar")
            }
        }

       /* OutlinedButton(
            onClick = onRegisterInUltrapay,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Registrarme en UltraPay")
        }*/

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text(text = "Volver")
        }

        if (showConfirmSubmitDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmSubmitDialog = false },
                title = { Text("Confirmar pedido") },
                text = {
                    Text(
                        if (selectedMethod == null) {
                            "Se registrará tu reservación como pendiente de confirmación. ¿Deseas continuar?"
                        } else {
                            "Se registrará el pedido y se abrirá la pasarela de pago seleccionada. ¿Deseas continuar?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConfirmSubmitDialog = false
                            onSubmitPurchase(selectedMethod)
                        },
                        enabled = !isSubmitting
                    ) {
                        Text("Sí, continuar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showConfirmSubmitDialog = false },
                        enabled = !isSubmitting
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_APPLIANCE,
    showSystemUi = true
)
@Composable
private fun BuyConfirmScreenPreview() {
    val salesUIState = listOf(
        UiSaleItem(
            Product(
                "product1",
                "Product test 1",
                "Product test 1 description",
                332.2,
                "photo url 1",
                "categoryId",
                4.3
            ), 5
        ),
        UiSaleItem(
            Product(
                "product2",
                "Product test 2",
                "Product test 2 description",
                332.2,
                "photo url 2",
                "categoryId",
                4.7
            ), 3
        ),
    )
    val totalAmount = remember { salesUIState.sumOf { it.product.price * it.quantity } }

    AlejoTallerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            BuyConfirmScreen(
                items = salesUIState,
                totalAmount = totalAmount,
                onBackClick = {},
                onSubmitPurchase = {},
                onRegisterInUltrapay = {},
                isSubmitting = false
            )
        }

    }

}