package com.elitec.alejotallerscan.feature.home.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elitec.alejotallerscan.feature.auth.presentation.viewmodel.OperatorAuthViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorHomeScreen(
    onOpenScan: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenConfirmation: () -> Unit,
    onLogout: () -> Unit
) {
    val authViewModel: OperatorAuthViewModel = koinViewModel()
    val authState by authViewModel.uiState.collectAsState()

    OperatorScreen(
        title = "Panel operador",
        subtitle = "Confirmacion de efectivo, pagos directos y reservas del MVP."
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = authState.currentUser?.name ?: "Operador",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Rol activo: ${authState.currentUser?.userProfile?.role ?: "sin rol"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(onClick = onOpenScan, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null)
                    Text("Escanear pedido", modifier = Modifier.padding(start = 10.dp))
                }
                Button(onClick = onOpenConfirmation, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.PointOfSale, contentDescription = null)
                    Text("Confirmar venta cargada", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(onClick = onOpenReservations, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                    Text("Reservas recientes", modifier = Modifier.padding(start = 10.dp))
                }
                OutlinedButton(
                    onClick = { authViewModel.logout(onLogout) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Text("Cerrar sesion", modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}
