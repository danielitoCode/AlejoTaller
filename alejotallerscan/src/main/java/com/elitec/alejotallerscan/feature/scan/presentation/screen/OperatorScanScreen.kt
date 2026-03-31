package com.elitec.alejotallerscan.feature.scan.presentation.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorScanScreen(
    onBack: () -> Unit,
    onProceedToConfirm: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val uiState by salesViewModel.uiState.collectAsState()
    var manualCode by remember { mutableStateOf(uiState.lastScannedPayload) }
    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val content = result.contents ?: return@rememberLauncherForActivityResult
        salesViewModel.loadSaleByCode(content, onProceedToConfirm)
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
                    onClick = { salesViewModel.loadSaleByCode(manualCode, onProceedToConfirm) },
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
