package com.elitec.alejotallerscan.feature.reservation.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ReceiptLong
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
import com.elitec.alejotallerscan.feature.sale.presentation.viewmodel.OperatorSalesViewModel
import com.elitec.alejotallerscan.infraestructure.core.presentation.components.OperatorScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun OperatorReservationsScreen(
    onBack: () -> Unit,
    onOpenSale: () -> Unit
) {
    val salesViewModel: OperatorSalesViewModel = koinViewModel()
    val recentSales by salesViewModel.recentSales.collectAsState()

    OperatorScreen(
        title = "Reservas recientes",
        subtitle = "Muestra las ventas cargadas por escaneo o consulta durante esta sesion."
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (recentSales.isEmpty()) {
                    Text("Todavia no hay reservas en cache local.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(recentSales) { sale ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        salesViewModel.selectSale(sale, onOpenSale)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(sale.id, style = MaterialTheme.typography.titleMedium)
                                    Text("Estado: ${sale.verified}")
                                    Text("Importe: \$${"%.2f".format(sale.amount)}")
                                }
                            }
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
}
