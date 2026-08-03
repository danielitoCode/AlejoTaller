package com.elitec.alejotallerscan.feature.confirmation.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCodeScanner
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.confirmation.presentation.entity.OperatorPaymentMethod
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesUiState
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorPanelCard
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorSectionLabel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorTone
import com.elitec.alejotallerscan.infraestructure.presentation.util.GlobalPreview
import com.elitec.alejotallerscan.infraestructure.presentation.util.toDeviceMode
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.Currency
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import com.elitec.shared.sale.feature.sale.domain.entity.SaleType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun OperatorConfirmPaymentScreen(
    onBack: () -> Unit,
    onOpenScan: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val scanViewModel: OperatorScanViewModel = koinViewModel()
    val uiState by salesViewModel.uiState.collectAsState()
    var paymentMethod by rememberSaveable { mutableStateOf(OperatorPaymentMethod.CASH) }
    var operatorNote by rememberSaveable { mutableStateOf("") }
    var saleType by rememberSaveable { mutableStateOf(SaleType.NORMAL) }
    var discountAmountText by rememberSaveable { mutableStateOf("") }

    val listAmount = uiState.selectedSale?.amount ?: 0.0

    // Al cambiar a DISCOUNT, precargar un valor sugerido (90% del lista) si el campo está vacío
    LaunchedEffect(saleType, listAmount) {
        if (saleType == SaleType.DISCOUNT && discountAmountText.isBlank() && listAmount > 0) {
            val suggested = (listAmount * 0.9).let { kotlin.math.round(it * 100) / 100 }
            discountAmountText = suggested.toString()
        }
    }

    OperatorConfirmPaymentScreenContent(
        uiState = uiState,
        paymentMethod = paymentMethod,
        operatorNote = operatorNote,
        saleType = saleType,
        discountAmountText = discountAmountText,
        onOperatorNoteChange = { operatorNote = it },
        onPaymentMethodChange = { paymentMethod = it },
        onSaleTypeChange = { saleType = it },
        onDiscountAmountTextChange = { discountAmountText = it },
        onBuyConfirmClick = {
            val discountAmount = if (saleType == SaleType.DISCOUNT) {
                discountAmountText.replace(',', '.').toDoubleOrNull()
            } else {
                null
            }
            salesViewModel.confirmSelectedSale(
                saleType = saleType,
                discountAmount = discountAmount
            ) {
                salesViewModel.resetState()
                scanViewModel.resetState()
                onOpenScan()
            }
        },
        onRejectBuyButtonClick = {
            salesViewModel.rejectSelectedSale {
                salesViewModel.resetState()
                scanViewModel.resetState()
                onOpenScan()
            }
        },
        onBack = onBack,
        onOpenScan = onOpenScan,
        modifier = Modifier.fillMaxSize(),
    )
}

@Composable
private fun StateBadge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        shape = CardDefaults.shape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun InfoBlock(
    title: String? = null,
    lines: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            title?.let {
                Text(it, style = MaterialTheme.typography.titleSmall)
            }
            lines.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun SaleType.labelEs(): String = when (this) {
    SaleType.NORMAL -> "Normal"
    SaleType.DISCOUNT -> "Descuento"
    SaleType.GIFT -> "Regalia"
}

private fun SaleType.hintEs(): String = when (this) {
    SaleType.NORMAL -> "Precio de lista del pedido"
    SaleType.DISCOUNT -> "Ingresa el importe efectivo (menor al de lista)"
    SaleType.GIFT -> "Obsequio: importe final 0; el stock si baja"
}

/** Formato dinero sin comillas anidadas en templates (evita romper el parser). */
private fun money(amount: Double): String =
    String.format(Locale.US, "%.2f", amount)

/**
 * Valida importe efectivo según SALE_POLICY.
 * DISCOUNT: >= 0 y estrictamente menor al de lista (si lista > 0).
 */
private fun isDiscountAmountValid(listAmount: Double, text: String): Boolean {
    val value = text.replace(',', '.').toDoubleOrNull() ?: return false
    if (value < 0.0) return false
    if (listAmount > 0.0 && value >= listAmount) return false
    return true
}

private fun resolveDisplayAmount(
    listAmount: Double,
    saleType: SaleType,
    discountAmountText: String
): Double = when (saleType) {
    SaleType.GIFT -> 0.0
    SaleType.DISCOUNT -> discountAmountText.replace(',', '.').toDoubleOrNull() ?: listAmount
    SaleType.NORMAL -> listAmount
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OperatorConfirmPaymentScreenContent(
    uiState: OperatorSalesUiState,
    paymentMethod: OperatorPaymentMethod,
    operatorNote: String,
    saleType: SaleType,
    discountAmountText: String,
    onOperatorNoteChange: (String) -> Unit,
    onPaymentMethodChange: (OperatorPaymentMethod) -> Unit,
    onSaleTypeChange: (SaleType) -> Unit,
    onDiscountAmountTextChange: (String) -> Unit,
    onBuyConfirmClick: () -> Unit,
    onRejectBuyButtonClick: () -> Unit,
    onBack: () -> Unit,
    onOpenScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocalConfiguration.current.toDeviceMode()
    val sale = uiState.selectedSale
    val listAmount = sale?.amount ?: 0.0
    val displayAmount = resolveDisplayAmount(listAmount, saleType, discountAmountText)
    val discountValid = saleType != SaleType.DISCOUNT || isDiscountAmountValid(listAmount, discountAmountText)
    val canConfirm = sale != null && !uiState.isLoading && discountValid
    val listAmountLabel = money(listAmount)
    val displayAmountLabel = money(displayAmount)

    OperatorScreen(
        title = "Confirmar reservacion",
        subtitle = "Revisa la reserva, elige el tipo de venta e importe efectivo segun SALE_POLICY.",
        heroIcon = Icons.Rounded.CheckCircle,
        modifier = modifier.fillMaxSize()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OperatorPanelCard {
                if (sale == null) {
                    OperatorSectionLabel("Sin pedido cargado", tone = OperatorTone.Warning)
                    Text("Aun no hay una venta cargada.")
                    Button(onClick = onOpenScan, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                        Text("Abrir registro de venta", modifier = Modifier.padding(start = 10.dp))
                    }
                } else {
                    OperatorSectionLabel("Resumen operativo")
                    Text("Venta: ${sale.id}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Nombre: ${sale.customerName ?: "No disponible"}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StateBadge(
                            text = "Estado ${sale.verified}",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        StateBadge(
                            text = "Lista $$listAmountLabel",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StateBadge(
                            text = "Efectivo $$displayAmountLabel",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        StateBadge(
                            text = "Tipo ${saleType.labelEs()}",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    InfoBlock(
                        lines = listOf(
                            "Fecha: ${sale.date}",
                            "Cliente: ${sale.userId}",
                            "Entrega: ${sale.deliveryType ?: "Sin definir"}",
                            "Lineas del pedido: ${sale.products.size} - Unidades: ${sale.products.sumOf { it.quantity }}"
                        )
                    )

                    sale.deliveryAddress?.let { address ->
                        InfoBlock(
                            title = "Direccion operativa",
                            lines = buildList {
                                add(
                                    address.mainStreet + " #" + address.houseNumber +
                                        ", " + address.municipality + ", " + address.province
                                )
                                add("Telefono: " + address.phone)
                                if (!address.referenceName.isNullOrBlank()) {
                                    add("Preguntar por: " + address.referenceName)
                                }
                            }
                        )
                    }
                }
            }

            if (sale != null) {
                OperatorPanelCard {
                    OperatorSectionLabel("Tipo de venta")
                    Text(
                        "Define el tipo comercial al confirmar. El stock baja igual en los tres casos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (type in SaleType.entries) {
                            val selected = saleType == type
                            AssistChip(
                                onClick = { onSaleTypeChange(type) },
                                label = { Text(type.labelEs()) },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null
                                        )
                                    }
                                } else {
                                    null
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
                        saleType.hintEs(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (saleType == SaleType.DISCOUNT) {
                        OutlinedTextField(
                            value = discountAmountText,
                            onValueChange = onDiscountAmountTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Importe efectivo (descuento)") },
                            supportingText = {
                                val support = if (discountValid) {
                                    "Debe ser >= 0 y menor a $$listAmountLabel (precio de lista)"
                                } else {
                                    "Importe invalido: usa un valor >= 0 y menor al de lista"
                                }
                                Text(support)
                            },
                            isError = !discountValid,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    if (saleType == SaleType.GIFT) {
                        Text(
                            "Importe final al confirmar: $0.00",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                OperatorPanelCard {
                    OperatorSectionLabel("Control operativo")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (method in OperatorPaymentMethod.entries) {
                            val selected = paymentMethod == method
                            AssistChip(
                                onClick = { onPaymentMethodChange(method) },
                                label = {
                                    Text(
                                        if (method == OperatorPaymentMethod.CASH) {
                                            "Efectivo"
                                        } else {
                                            "Pago directo"
                                        }
                                    )
                                },
                                leadingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = null
                                        )
                                    }
                                } else {
                                    null
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
                    OutlinedTextField(
                        value = operatorNote,
                        onValueChange = onOperatorNoteChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observacion del operador") },
                        supportingText = {
                            Text("Apoyo visual local (no se sincroniza).")
                        }
                    )
                    Text(
                        "Metodo: " +
                            if (paymentMethod == OperatorPaymentMethod.CASH) {
                                "Efectivo"
                            } else {
                                "Pago directo"
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OperatorPanelCard {
                    OperatorSectionLabel("Items del pedido")
                    sale.products.forEach { item ->
                        InfoBlock(
                            lines = listOf(
                                item.productName ?: item.productId,
                                "Cantidad: ${item.quantity}"
                            )
                        )
                    }
                }
            }

            OperatorPanelCard {
                uiState.notice?.let { notice ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = notice,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                uiState.error?.let { errorMsg ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = errorMsg,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Button(
                    onClick = onBuyConfirmClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = canConfirm
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text(
                        "Confirmar (${saleType.labelEs()} · $$displayAmountLabel)",
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
                OutlinedButton(
                    onClick = onRejectBuyButtonClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = sale != null && !uiState.isLoading
                ) {
                    Icon(Icons.Rounded.Cancel, contentDescription = null)
                    Text("Marcar como rechazada", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Text("Volver", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun OperatorConfirmPaymentScreenContentPreview() {
    var paymentMethod by rememberSaveable { mutableStateOf(OperatorPaymentMethod.CASH) }
    var operatorNote by rememberSaveable { mutableStateOf("") }
    var saleType by rememberSaveable { mutableStateOf(SaleType.DISCOUNT) }
    var discountAmountText by rememberSaveable { mutableStateOf("20.00") }
    var uiState by remember {
        mutableStateOf(
            OperatorSalesUiState().copy(
                isLoading = false,
                selectedSale = Sale(
                    id = "id",
                    date = LocalDate(year = 2026, month = 12, day = 23),
                    amount = 23.4,
                    currency = Currency.USD,
                    verified = BuyState.UNVERIFIED,
                    products = listOf(
                        SaleItem(
                            productId = "product test id 1",
                            quantity = 2,
                            productName = "Product test 1"
                        ),
                        SaleItem(
                            productId = "product test id 2",
                            quantity = 1,
                            productName = "Product test 2"
                        )
                    ),
                    userId = "user test id",
                    customerName = "User customer test",
                    deliveryType = DeliveryType.DELIVERY,
                    deliveryAddress = null
                )
            )
        )
    }

    val scope = rememberCoroutineScope()

    GlobalPreview {
        OperatorConfirmPaymentScreenContent(
            uiState = uiState,
            paymentMethod = paymentMethod,
            operatorNote = operatorNote,
            saleType = saleType,
            discountAmountText = discountAmountText,
            onOperatorNoteChange = { operatorNote = it },
            onPaymentMethodChange = { paymentMethod = it },
            onSaleTypeChange = { saleType = it },
            onDiscountAmountTextChange = { discountAmountText = it },
            onBuyConfirmClick = {
                scope.launch {
                    delay(500)
                    val current = uiState.selectedSale ?: return@launch
                    uiState = uiState.copy(
                        selectedSale = current.copy(
                            verified = BuyState.VERIFIED,
                            saleType = saleType,
                            amount = resolveDisplayAmount(
                                current.amount,
                                saleType,
                                discountAmountText
                            )
                        )
                    )
                }
            },
            onRejectBuyButtonClick = {
                scope.launch {
                    delay(500)
                    val current = uiState.selectedSale ?: return@launch
                    uiState = uiState.copy(
                        selectedSale = current.copy(verified = BuyState.DELETED)
                    )
                }
            },
            onBack = {},
            onOpenScan = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}
