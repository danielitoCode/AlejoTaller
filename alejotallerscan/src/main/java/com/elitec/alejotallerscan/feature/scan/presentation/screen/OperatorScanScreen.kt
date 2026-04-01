package com.elitec.alejotallerscan.feature.scan.presentation.screen

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.scan.presentation.viewmodel.OperatorScanViewModel
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OperatorScanScreen(
    onBack: () -> Unit,
    onProceedToConfirm: () -> Unit
) {
    val context = LocalContext.current
    val scanViewModel: OperatorScanViewModel = koinViewModel()
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val uiState by scanViewModel.uiState.collectAsState()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var manualCode by remember { mutableStateOf(uiState.lastPayload) }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val content = result.contents ?: return@rememberLauncherForActivityResult
        scanViewModel.loadSaleByPayload(content) { sale ->
            salesViewModel.selectSale(sale, onProceedToConfirm)
        }
    }

    OperatorScreen(
        title = "Escanear reserva",
        subtitle = "Usa el QR del cliente o pega el identificador manualmente para cargar la venta."
    ) {
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
                when (val permission = cameraPermissionState.status) {
                    PermissionStatus.Granted -> {
                        Button(
                            onClick = {
                                val options = ScanOptions()
                                    .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                    .setPrompt("Escanea el QR del pedido")
                                    .setBeepEnabled(false)
                                    .setOrientationLocked(false)
                                scanLauncher.launch(options)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                            Text("Abrir camara", modifier = Modifier.padding(start = 10.dp))
                        }
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
                                    "Permiso de camara requerido para el escaneo QR.",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    if (permission.shouldShowRationale) {
                                        "Necesitamos acceso a la camara para leer el QR del pedido. Si prefieres, puedes seguir usando el ingreso manual."
                                    } else {
                                        "El permiso esta bloqueado o aun no se ha concedido. Puedes concederlo ahora o abrir ajustes del sistema."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Button(
                                    onClick = { cameraPermissionState.launchPermissionRequest() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                                    Text("Conceder permiso", modifier = Modifier.padding(start = 10.dp))
                                }
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(
                                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts("package", context.packageName, null)
                                        )
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Abrir ajustes")
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Codigo o contenido QR") },
                    supportingText = { Text("Acepta un id directo o una URL con ?id=...") },
                    leadingIcon = { Icon(Icons.Rounded.Keyboard, contentDescription = null) }
                )

                if (uiState.error != null) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                }
                if (uiState.notice != null) {
                    Text(uiState.notice ?: "", color = MaterialTheme.colorScheme.primary)
                }

                Button(
                    onClick = {
                        scanViewModel.loadSaleByPayload(manualCode) { sale ->
                            salesViewModel.selectSale(sale, onProceedToConfirm)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = manualCode.isNotBlank() && !uiState.isLoading
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                    Text("Cargar venta", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Text("Volver", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
