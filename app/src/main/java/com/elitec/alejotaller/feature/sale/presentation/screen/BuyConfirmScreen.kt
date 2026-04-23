package com.elitec.alejotaller.feature.sale.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryAddress
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.PaymentChannel

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
    var selectedMethodKey by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedDeliveryTypeKey by rememberSaveable { mutableStateOf<String?>(null) }
    var province by rememberSaveable { mutableStateOf("") }
    var municipality by rememberSaveable { mutableStateOf("") }
    var mainStreet by rememberSaveable { mutableStateOf("") }
    var betweenStreets by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var houseNumber by rememberSaveable { mutableStateOf("") }
    var referenceName by rememberSaveable { mutableStateOf("") }
    var showConfirmSubmitDialog by rememberSaveable { mutableStateOf(false) }

    val selectedMethod = selectedMethodKey?.let(PaymentChannel::valueOf)
    val selectedDeliveryType = selectedDeliveryTypeKey?.let(DeliveryType::valueOf)

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
            Color(0xFF49535D).copy(alpha = 0.3f),
            Color(0xFF49535D).copy(alpha = 0.1f),
            Color(0xFF49535D).copy(alpha = 0.4f),
            Color(0xFF49535D).copy(alpha = 0.2f),
            Color(0xFF49535D).copy(alpha = 0.6f),
        )
    )
    val transfermovilBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF49535D).copy(alpha = 0.2f),
            Color(0xFF32FF72).copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.1f),
            Color(0xFF606DFF).copy(alpha = 0.2f),
            Color(0xFF32FF72).copy(alpha = 0.3f),
            Color.White.copy(alpha = 0.6f),
        )
    )

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isWideLayout = maxWidth >= 920.dp
        val contentPadding = if (isWideLayout) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
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

            if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OrderSummaryPane(
                        items = items,
                        totalAmount = totalAmount,
                        modifier = Modifier.weight(0.9f)
                    )
                    CheckoutConfigurationPane(
                        selectedMethod = selectedMethod,
                        selectedDeliveryType = selectedDeliveryType,
                        needsAddress = needsAddress,
                        province = province,
                        municipality = municipality,
                        mainStreet = mainStreet,
                        betweenStreets = betweenStreets,
                        phone = phone,
                        houseNumber = houseNumber,
                        referenceName = referenceName,
                        ultraPayBrush = ultraPayBrush,
                        transfermovilBrush = transfermovilBrush,
                        onPaymentMethodSelected = { selectedMethodKey = it.name },
                        onDeliveryTypeSelected = { selectedDeliveryTypeKey = it.name },
                        onProvinceChange = { province = it },
                        onMunicipalityChange = { municipality = it },
                        onMainStreetChange = { mainStreet = it },
                        onBetweenStreetsChange = { betweenStreets = it },
                        onPhoneChange = { phone = it },
                        onHouseNumberChange = { houseNumber = it },
                        onReferenceNameChange = { referenceName = it },
                        onRegisterInUltrapay = onRegisterInUltrapay,
                        modifier = Modifier.weight(1.1f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.product.id }) { item ->
                        OrderItemCard(item = item)
                    }
                    item {
                        TotalRow(totalAmount = totalAmount)
                    }
                    item {
                        CheckoutConfigurationCard(
                            selectedMethod = selectedMethod,
                            selectedDeliveryType = selectedDeliveryType,
                            needsAddress = needsAddress,
                            province = province,
                            municipality = municipality,
                            mainStreet = mainStreet,
                            betweenStreets = betweenStreets,
                            phone = phone,
                            houseNumber = houseNumber,
                            referenceName = referenceName,
                            ultraPayBrush = ultraPayBrush,
                            transfermovilBrush = transfermovilBrush,
                            onPaymentMethodSelected = { selectedMethodKey = it.name },
                            onDeliveryTypeSelected = { selectedDeliveryTypeKey = it.name },
                            onProvinceChange = { province = it },
                            onMunicipalityChange = { municipality = it },
                            onMainStreetChange = { mainStreet = it },
                            onBetweenStreetsChange = { betweenStreets = it },
                            onPhoneChange = { phone = it },
                            onHouseNumberChange = { houseNumber = it },
                            onReferenceNameChange = { referenceName = it },
                            onRegisterInUltrapay = onRegisterInUltrapay
                        )
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
        }
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

@Composable
private fun OrderSummaryPane(
    items: List<UiSaleItem>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "Resumen del pedido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        items(items, key = { it.product.id }) { item ->
            OrderItemCard(item = item)
        }
        item {
            TotalRow(totalAmount = totalAmount)
        }
    }
}

@Composable
private fun OrderItemCard(item: UiSaleItem) {
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

@Composable
private fun TotalRow(totalAmount: Double) {
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

@Composable
private fun CheckoutConfigurationPane(
    selectedMethod: PaymentChannel?,
    selectedDeliveryType: DeliveryType?,
    needsAddress: Boolean,
    province: String,
    municipality: String,
    mainStreet: String,
    betweenStreets: String,
    phone: String,
    houseNumber: String,
    referenceName: String,
    ultraPayBrush: Brush,
    transfermovilBrush: Brush,
    onPaymentMethodSelected: (PaymentChannel) -> Unit,
    onDeliveryTypeSelected: (DeliveryType) -> Unit,
    onProvinceChange: (String) -> Unit,
    onMunicipalityChange: (String) -> Unit,
    onMainStreetChange: (String) -> Unit,
    onBetweenStreetsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onHouseNumberChange: (String) -> Unit,
    onReferenceNameChange: (String) -> Unit,
    onRegisterInUltrapay: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            CheckoutConfigurationCard(
                selectedMethod = selectedMethod,
                selectedDeliveryType = selectedDeliveryType,
                needsAddress = needsAddress,
                province = province,
                municipality = municipality,
                mainStreet = mainStreet,
                betweenStreets = betweenStreets,
                phone = phone,
                houseNumber = houseNumber,
                referenceName = referenceName,
                ultraPayBrush = ultraPayBrush,
                transfermovilBrush = transfermovilBrush,
                onPaymentMethodSelected = onPaymentMethodSelected,
                onDeliveryTypeSelected = onDeliveryTypeSelected,
                onProvinceChange = onProvinceChange,
                onMunicipalityChange = onMunicipalityChange,
                onMainStreetChange = onMainStreetChange,
                onBetweenStreetsChange = onBetweenStreetsChange,
                onPhoneChange = onPhoneChange,
                onHouseNumberChange = onHouseNumberChange,
                onReferenceNameChange = onReferenceNameChange,
                onRegisterInUltrapay = onRegisterInUltrapay
            )
        }
    }
}

@Composable
private fun CheckoutConfigurationCard(
    selectedMethod: PaymentChannel?,
    selectedDeliveryType: DeliveryType?,
    needsAddress: Boolean,
    province: String,
    municipality: String,
    mainStreet: String,
    betweenStreets: String,
    phone: String,
    houseNumber: String,
    referenceName: String,
    ultraPayBrush: Brush,
    transfermovilBrush: Brush,
    onPaymentMethodSelected: (PaymentChannel) -> Unit,
    onDeliveryTypeSelected: (DeliveryType) -> Unit,
    onProvinceChange: (String) -> Unit,
    onMunicipalityChange: (String) -> Unit,
    onMainStreetChange: (String) -> Unit,
    onBetweenStreetsChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onHouseNumberChange: (String) -> Unit,
    onReferenceNameChange: (String) -> Unit,
    onRegisterInUltrapay: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
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
                onClick = { onPaymentMethodSelected(PaymentChannel.ULTRAPAY) },
                onRegisterInUltrapay = onRegisterInUltrapay
            )

            PaymentOption(
                title = "Transfermovil",
                subtitle = "Pasarela de pagos cubana",
                selected = selectedMethod == PaymentChannel.TRANSFERMOVIL,
                imageRes = R.drawable.transfermovil,
                brush = transfermovilBrush,
                onClick = { onPaymentMethodSelected(PaymentChannel.TRANSFERMOVIL) },
                onRegisterInUltrapay = onRegisterInUltrapay
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
                    onClick = { onDeliveryTypeSelected(DeliveryType.PICKUP) },
                    modifier = Modifier.weight(1f)
                )
                DeliveryMethodCard(
                    title = "Entrega a domicilio",
                    subtitle = "Se registra la direccion junto con la venta.",
                    selected = selectedDeliveryType == DeliveryType.DELIVERY,
                    onClick = { onDeliveryTypeSelected(DeliveryType.DELIVERY) },
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
                    OutlinedTextField(value = province, onValueChange = onProvinceChange, label = { Text("Provincia") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = municipality, onValueChange = onMunicipalityChange, label = { Text("Municipio") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = mainStreet, onValueChange = onMainStreetChange, label = { Text("Calle principal") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = houseNumber, onValueChange = onHouseNumberChange, label = { Text("Numero de la casa") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = onPhoneChange, label = { Text("Telefono") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = betweenStreets, onValueChange = onBetweenStreetsChange, label = { Text("Entre calles (opcional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = referenceName, onValueChange = onReferenceNameChange, label = { Text("Preguntar por (opcional)") }, modifier = Modifier.fillMaxWidth())
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

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
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
    onClick: () -> Unit,
    onRegisterInUltrapay: () -> Unit
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
                    onClick = onRegisterInUltrapay,
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
