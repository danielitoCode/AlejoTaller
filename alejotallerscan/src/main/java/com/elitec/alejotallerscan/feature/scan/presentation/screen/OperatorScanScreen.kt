package com.elitec.alejotallerscan.feature.scan.presentation.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.feature.scan.domain.entity.ParsedSaleQrItem
import com.elitec.alejotallerscan.feature.scan.presentation.entity.QrConsistencyCheck
import com.elitec.alejotallerscan.feature.scan.presentation.entity.QrConsistencyStatus
import com.elitec.alejotallerscan.feature.scan.presentation.component.OperatorQrScannerSection
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorPanelCard
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorSectionLabel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OperatorScanScreen(
    onBack: () -> Unit,
    onProceedToConfirm: () -> Unit,
    onOpenManualSearch: () -> Unit
) {
    val context = LocalContext.current
    val scanViewModel: OperatorScanViewModel = koinViewModel()
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val uiState by scanViewModel.uiState.collectAsState()
    val salesUiState by salesViewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val loadedSaleRequester = remember { BringIntoViewRequester() }
    var manualCode by remember(uiState.lastPayload) { mutableStateOf(uiState.lastPayload) }

    fun loadSale(payload: String) {
        scanViewModel.loadSaleByPayload(payload) { sale ->
            salesViewModel.selectSale(sale, onProceedToConfirm)
        }
    }

    LaunchedEffect(salesUiState.selectedSale?.id) {
        if (salesUiState.selectedSale != null) {
            loadedSaleRequester.bringIntoView()
        }
    }

    OperatorScreen(
        title = "Registrar venta o reservacion",
        subtitle = "Escanea dentro del marco, valida los datos del QR y revisa la coincidencia antes de confirmar.",
        heroIcon = Icons.Rounded.QrCode2
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OperatorPanelCard {
                OperatorSectionLabel("Escaneo guiado")

                when (val permission = cameraPermissionState.status) {
                    PermissionStatus.Granted -> {
                        OperatorQrScannerSection(
                            isEnabled = !uiState.isLoading,
                            onDetected = ::loadSale
                        )
                    }

                    is PermissionStatus.Denied -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Activa la camara para usar el escaneo guiado.",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    if (permission.shouldShowRationale) {
                                        "La camara se usa solo dentro de este panel de lectura para capturar el QR de la reserva."
                                    } else {
                                        "El permiso esta bloqueado o aun no fue concedido. Puedes habilitarlo ahora o abrir los ajustes del sistema."
                                    },
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(
                                    onClick = { cameraPermissionState.launchPermissionRequest() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.QrCode2, contentDescription = null)
                                    Text("Conceder permiso", modifier = Modifier.padding(start = 10.dp))
                                }
                                OutlinedButton(
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", context.packageName, null)
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Abrir ajustes")
                                }
                            }
                        }
                    }
                }
            }

            OperatorPanelCard {
                OperatorSectionLabel("Carga manual")
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ID o contenido QR") },
                    supportingText = { Text("Acepta un ID directo, una URL o un JSON que contenga id, saleId o reservationId.") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                )

                uiState.error?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                uiState.notice?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CardDefaults.shape
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Button(
                    onClick = { loadSale(manualCode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = manualCode.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("Cargar desde Appwrite", modifier = Modifier.padding(start = 10.dp))
                }

                OutlinedButton(
                    onClick = onOpenManualSearch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.ManageSearch, contentDescription = null)
                    Text("Busqueda avanzada por filtros", modifier = Modifier.padding(start = 10.dp))
                }
            }

            salesUiState.selectedSale?.let { sale ->
                OperatorPanelCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(loadedSaleRequester),
                ) {
                    OperatorSectionLabel("Venta cargada")

                    uiState.parsedPayload?.let { parsed ->
                        SummaryBlock(
                            title = "Datos detectados en QR",
                            lines = listOf(
                                "ID QR: ${parsed.saleId}",
                                "Usuario QR: ${parsed.userId ?: "No disponible"}",
                                "Importe QR: ${parsed.amount?.let { "$$it" } ?: "No disponible"}"
                            ) + parsed.items.map { qrItemLabel(it, sale) }
                        )
                    }

                    val consistencyChecks = remember(uiState.parsedPayload, sale) {
                        buildConsistencyChecks(uiState.parsedPayload, sale)
                    }
                    if (consistencyChecks.isNotEmpty()) {
                        Text("Chequeo de consistencia", style = MaterialTheme.typography.titleSmall)
                        consistencyChecks.forEach { check ->
                            ConsistencyRow(check = check)
                        }
                    }

                    SummaryBlock(
                        title = "Datos validados en Appwrite",
                        lines = listOf(
                            "ID pedido: ${sale.id}",
                            "ID usuario: ${sale.userId}",
                            "Nombre cliente: ${sale.customerName ?: "No disponible"}",
                            "Importe: \$${"%.2f".format(sale.amount)}",
                            "Estado: ${sale.verified}"
                        ) + sale.products.map { "- ${it.productName ?: it.productId} x${it.quantity}" }
                    )

                    Button(
                        onClick = onProceedToConfirm,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Inventory2, contentDescription = null)
                        Text("Revisar y confirmar", modifier = Modifier.padding(start = 10.dp))
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

private fun qrItemLabel(
    item: ParsedSaleQrItem,
    sale: com.elitec.shared.sale.feature.sale.domain.entity.Sale
): String {
    val matched = sale.products.firstOrNull { it.productId == item.productId }
    val name = matched?.productName ?: item.productId
    val pricePart = item.unitPrice?.let { " - \$${"%.2f".format(it)}" }.orEmpty()
    return "- $name x${item.quantity}$pricePart"
}

@Composable
private fun SummaryBlock(
    title: String,
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
            Text(title, style = MaterialTheme.typography.titleSmall)
            lines.forEach { line ->
                Text(line, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ConsistencyRow(
    check: QrConsistencyCheck
) {
    val (containerColor, contentColor) = when (check.status) {
        QrConsistencyStatus.MATCH -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        QrConsistencyStatus.MISMATCH -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        QrConsistencyStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = CardDefaults.shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = when (check.status) {
                    QrConsistencyStatus.MATCH -> Icons.Rounded.CheckCircle
                    QrConsistencyStatus.MISMATCH -> Icons.Rounded.Info
                    QrConsistencyStatus.UNKNOWN -> Icons.Rounded.Info
                },
                contentDescription = null,
                tint = contentColor
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(check.label, color = contentColor, style = MaterialTheme.typography.labelLarge)
                Text("QR: ${check.qrValue}", color = contentColor, style = MaterialTheme.typography.bodySmall)
                Text("Appwrite: ${check.appwriteValue}", color = contentColor, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun buildConsistencyChecks(
    parsedPayload: com.elitec.alejotallerscan.feature.scan.domain.entity.ParsedSaleQrPayload?,
    sale: Sale
): List<QrConsistencyCheck> {
    if (parsedPayload == null) return emptyList()

    val checks = mutableListOf<QrConsistencyCheck>()
    checks += QrConsistencyCheck(
        label = "ID de venta",
        qrValue = parsedPayload.saleId,
        appwriteValue = sale.id,
        status = if (parsedPayload.saleId == sale.id) QrConsistencyStatus.MATCH else QrConsistencyStatus.MISMATCH
    )

    checks += QrConsistencyCheck(
        label = "Usuario",
        qrValue = parsedPayload.userId ?: "No disponible",
        appwriteValue = sale.userId,
        status = when {
            parsedPayload.userId.isNullOrBlank() -> QrConsistencyStatus.UNKNOWN
            parsedPayload.userId == sale.userId -> QrConsistencyStatus.MATCH
            else -> QrConsistencyStatus.MISMATCH
        }
    )

    checks += QrConsistencyCheck(
        label = "Importe",
        qrValue = parsedPayload.amount?.let { "%.2f".format(it) } ?: "No disponible",
        appwriteValue = "%.2f".format(sale.amount),
        status = when (val amount = parsedPayload.amount) {
            null -> QrConsistencyStatus.UNKNOWN
            else -> if (kotlin.math.abs(amount - sale.amount) < 0.01) QrConsistencyStatus.MATCH else QrConsistencyStatus.MISMATCH
        }
    )

    val qrItems = parsedPayload.items.associateBy { it.productId }
    val appwriteItems = sale.products.associateBy { it.productId }
    val itemStatus = when {
        qrItems.isEmpty() -> QrConsistencyStatus.UNKNOWN
        qrItems.all { (productId, qrItem) ->
            val appwriteItem = appwriteItems[productId]
            appwriteItem != null && appwriteItem.quantity == qrItem.quantity
        } -> QrConsistencyStatus.MATCH
        else -> QrConsistencyStatus.MISMATCH
    }
    checks += QrConsistencyCheck(
        label = "Items",
        qrValue = parsedPayload.items.joinToString { "${it.productId} x${it.quantity}" }.ifBlank { "No disponibles" },
        appwriteValue = sale.products.joinToString { "${it.productId} x${it.quantity}" },
        status = itemStatus
    )

    return checks
}
