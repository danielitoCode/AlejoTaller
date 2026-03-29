package com.elitec.alejotaller.feature.sale.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryAddress
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.PaymentChannel
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme

@Composable
fun BuyConfirmScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onBackClick: () -> Unit,
    onSubmitPurchase: (PaymentChannel?, DeliveryType, DeliveryAddress?) -> Unit,
    onRegisterInUltrapay: () -> Unit,
    isSubmitting: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedMethod by remember { mutableStateOf<PaymentChannel?>(null) }
    var selectedDeliveryType by remember { mutableStateOf<DeliveryType?>(null) }
    var province by remember { mutableStateOf("") }
    var municipality by remember { mutableStateOf("") }
    var mainStreet by remember { mutableStateOf("") }
    var betweenStreets by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var houseNumber by remember { mutableStateOf("") }
    var referenceName by remember { mutableStateOf("") }
    var showConfirmSubmitDialog by remember { mutableStateOf(false) }

    val needsAddress = selectedDeliveryType == DeliveryType.DELIVERY
    val isAddressValid = !needsAddress || listOf(
        province,
        municipality,
        mainStreet,
        phone,
        houseNumber
    ).all { it.isNotBlank() }

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
                tint = MaterialTheme.colorScheme.onBackground,
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null
            )
            Spacer(Modifier.width(5.dp))
            Text(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                text = "Confirmar compra",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Text(
            text = "Primero eliges como pagar y luego como recibiras el pedido. Asi la venta queda completa desde el inicio.",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = "Cantidad: ${item.quantity}"
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "A pagar:")
                            Text(
                                style = MaterialTheme.typography.titleMedium,
                                text = "${"%.2f".format(item.product.price * item.quantity)} $"
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(Modifier.weight(1f))
                    Text(text = "Total final:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        fontWeight = FontWeight.Bold,
                        text = "${"%.2f".format(totalAmount)} $",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(0.6f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(
                            title = "Metodos de pago",
                            icon = Icons.Default.Payment
                        )

                        PaymentOption(
                            title = "UltraPay",
                            subtitle = "Tarjeta virtual SolucionesCuba",
                            selected = selectedMethod == PaymentChannel.ULTRAPAY,
                            imageRes = R.drawable.soluciones_cuba,
                            brush = ultraPayBrush,
                            onClick = { selectedMethod = PaymentChannel.ULTRAPAY }
                        )

                        PaymentOption(
                            title = "Transfermovil",
                            subtitle = "Pasarela de pagos cubana",
                            selected = selectedMethod == PaymentChannel.TRANSFERMOVIL,
                            imageRes = R.drawable.transfermovil,
                            brush = transfermovilBrush,
                            onClick = { selectedMethod = PaymentChannel.TRANSFERMOVIL }
                        )

                        SectionHeader(
                            title = "Entrega del pedido",
                            icon = if (selectedDeliveryType == DeliveryType.DELIVERY) Icons.Default.DeliveryDining else Icons.Default.Store
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DeliveryMethodCard(
                                title = "Recoger en tienda",
                                subtitle = "Pasas por el taller cuando tu compra este lista.",
                                selected = selectedDeliveryType == DeliveryType.PICKUP,
                                onClick = { selectedDeliveryType = DeliveryType.PICKUP },
                                modifier = Modifier.weight(1f)
                            )
                            DeliveryMethodCard(
                                title = "Entrega a domicilio",
                                subtitle = "Se registra la direccion junto con la venta.",
                                selected = selectedDeliveryType == DeliveryType.DELIVERY,
                                onClick = { selectedDeliveryType = DeliveryType.DELIVERY },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (needsAddress) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(value = province, onValueChange = { province = it }, label = { Text("Provincia") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = municipality, onValueChange = { municipality = it }, label = { Text("Municipio") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = mainStreet, onValueChange = { mainStreet = it }, label = { Text("Calle principal") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = houseNumber, onValueChange = { houseNumber = it }, label = { Text("Numero de la casa") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = betweenStreets, onValueChange = { betweenStreets = it }, label = { Text("Entre calles (opcional)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = referenceName, onValueChange = { referenceName = it }, label = { Text("Preguntar por (opcional)") }, modifier = Modifier.fillMaxWidth())
                                Text(
                                    text = "Obligatorios: provincia, municipio, calle principal, telefono y numero de la casa.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = { showConfirmSubmitDialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting && items.isNotEmpty() && selectedDeliveryType != null && isAddressValid
        ) {
            if (isSubmitting) {
                CircularProgressIndicator()
            } else {
                Text(text = if (selectedMethod == null) "Reservar sin pago online" else "Solicitar pedido y pagar")
            }
        }

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
                            "Se registrara tu reservacion con la entrega seleccionada. Deseas continuar?"
                        } else {
                            "Se registrara el pedido con la entrega seleccionada y luego se abrira la pasarela de pago. Deseas continuar?"
                        }
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val deliveryType = selectedDeliveryType ?: return@Button
                            val deliveryAddress = if (deliveryType == DeliveryType.DELIVERY) {
                                DeliveryAddress(
                                    province = province.trim(),
                                    municipality = municipality.trim(),
                                    mainStreet = mainStreet.trim(),
                                    betweenStreets = betweenStreets.trim().ifBlank { null },
                                    phone = phone.trim(),
                                    houseNumber = houseNumber.trim(),
                                    referenceName = referenceName.trim().ifBlank { null }
                                )
                            } else {
                                null
                            }
                            showConfirmSubmitDialog = false
                            onSubmitPurchase(selectedMethod, deliveryType, deliveryAddress)
                        },
                        enabled = !isSubmitting
                    ) {
                        Text("Si, continuar")
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

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.padding(start = 10.dp, top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            tint = MaterialTheme.colorScheme.onSurface,
            imageVector = icon,
            contentDescription = null
        )
        Text(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun PaymentOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    imageRes: Int,
    brush: Brush,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 5.dp,
        shadowElevation = 5.dp,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.background(brush)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Image(
                    painter = painterResource(imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(15.dp))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        text = subtitle,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                RadioButton(selected = selected, onClick = onClick)
            }
            if (title == "UltraPay") {
                Text(
                    text = "Si aun no tienes cuenta, registrate debajo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 15.dp, top = 5.dp, bottom = 5.dp)
                )
                Button(
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF49535D),
                        disabledContainerColor = Color(0xFF49535D).copy(alpha = 0.7f)
                    ),
                    onClick = {},
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 15.dp, end = 15.dp, bottom = 10.dp)
                ) {
                    Text(color = Color.White, text = "Registrarse en UltraPay")
                }
            }
        }
    }
}

@Composable
private fun DeliveryMethodCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = if (selected) 6.dp else 2.dp,
        shadowElevation = if (selected) 6.dp else 2.dp,
        shape = RoundedCornerShape(18.dp),
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RadioButton(selected = selected, onClick = onClick)
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
            Product("product1", "Product test 1", "Product test 1 description", 332.2, "photo url 1", "categoryId", 4.3),
            5
        ),
        UiSaleItem(
            Product("product2", "Product test 2", "Product test 2 description", 332.2, "photo url 2", "categoryId", 4.7),
            3
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
                onSubmitPurchase = { _, _, _ -> },
                onRegisterInUltrapay = {},
                isSubmitting = false
            )
        }
    }
}
