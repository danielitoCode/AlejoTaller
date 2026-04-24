package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.shared.sale.feature.sale.domain.entity.BuyState
import com.elitec.shared.sale.feature.sale.domain.entity.DeliveryType
import com.elitec.shared.sale.feature.sale.domain.entity.Sale
import com.elitec.shared.sale.feature.sale.domain.entity.SaleItem
import kotlinx.datetime.LocalDate

@Composable
fun BuyReservationScreen(
    sales: List<Sale>,
    onGoToShop: () -> Unit = {},
    onSaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (sales.isEmpty()) {
        EmptyReservationsState(onGoToShop = onGoToShop, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Shopping Bag",
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(5.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                SaleListItem(
                    sale = sale,
                    onClick = { onSaleSelected(sale.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyReservationsState(
    onGoToShop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Aún no tienes compras",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Aquí aparecerán tus pedidos una vez que hayas realizado alguna compra.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onGoToShop,
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ver productos")
        }
    }
}

@Composable
private fun SaleListItem(
    sale: Sale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Calendar day",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(text = sale.date.toString(), style = MaterialTheme.typography.bodySmall)
                }
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Pedido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(0.8f),
                        text = "#${sale.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Monto total:", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "${"%.2f".format(sale.amount)} CUP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val (icon, label, color) = when (sale.verified) {
                BuyState.UNVERIFIED -> Triple(Icons.Outlined.HourglassEmpty, "Pendiente", Color(0xFFFF973C))
                BuyState.VERIFIED -> Triple(Icons.Outlined.CheckCircle, "Listo", MaterialTheme.colorScheme.primary)
                BuyState.DELETED -> Triple(Icons.Outlined.Cancel, "Cancelada", MaterialTheme.colorScheme.error)
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Calendar day",
                        tint = color,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun BuyState.toLabel(): String = when (this) {
    BuyState.UNVERIFIED -> "Reservada"
    BuyState.VERIFIED -> "Lista"
    BuyState.DELETED -> "Cancelada"
}

@Preview(showBackground = true)
@Composable
private fun BuyReservationScreenPreview() {
    val products = remember {
        listOf(
            Product("product1", "Product test 1", "Product test 1 description", 332.2, "photo url 1", "categoryId", 4.3),
            Product("product2", "Product test 2", "Product test 2 description", 332.2, "photo url 2", "categoryId", 4.7),
            Product("product3", "Product test 3", "Product test 3 description", 312.5, "photo url 3", "categoryId", 4.0)
        )
    }

    val salesUIState = remember {
        listOf(UiSaleItem(products[0], 5), UiSaleItem(products[1], 3))
    }
    val salesUIState2 = remember {
        listOf(UiSaleItem(products[1], 5), UiSaleItem(products[2], 3))
    }
    val salesUIState3 = remember {
        listOf(UiSaleItem(products[2], 5), UiSaleItem(products[0], 3))
    }

    val salesItemsList = remember {
        salesUIState.map { SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name) }
    }
    val salesItemsList2 = remember {
        salesUIState2.map { SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name) }
    }
    val salesItemsList3 = remember {
        salesUIState3.map { SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name) }
    }

    val totalAmount = remember { salesUIState.sumOf { it.product.price * it.quantity } }
    val totalAmount2 = remember { salesUIState2.sumOf { it.product.price * it.quantity } }
    val totalAmount3 = remember { salesUIState3.sumOf { it.product.price * it.quantity } }

    val saleList = remember {
        listOf(
            Sale(
                id = "sale1",
                date = LocalDate(2023, 1, 12),
                amount = totalAmount,
                verified = BuyState.UNVERIFIED,
                products = salesItemsList,
                userId = "userId test",
                deliveryType = DeliveryType.DELIVERY
            ),
            Sale(
                id = "sale2",
                date = LocalDate(2023, 1, 13),
                amount = totalAmount2,
                verified = BuyState.VERIFIED,
                products = salesItemsList2,
                userId = "userId test",
                deliveryType = DeliveryType.DELIVERY
            ),
            Sale(
                id = "sale3",
                date = LocalDate(2023, 1, 14),
                amount = totalAmount3,
                verified = BuyState.DELETED,
                products = salesItemsList3,
                userId = "userId test",
                deliveryType = DeliveryType.PICKUP
            )
        )
    }

    AlejoTallerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            BuyReservationScreen(
                sales = saleList,
                onGoToShop = {},
                onSaleSelected = { _ -> }
            )
        }
    }
}
