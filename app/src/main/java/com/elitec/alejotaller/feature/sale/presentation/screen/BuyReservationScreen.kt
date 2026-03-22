package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.datetime.LocalDate

@Composable
fun BuyReservationScreen(
    sales: List<Sale>,
    onGoToShop: () -> Unit = {},
    onSaleSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // ─────────────────────────────────────────────────────────────────────
    // VACÍO 1: Estado completamente vacío — sin ninguna compra
    // ─────────────────────────────────────────────────────────────────────
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Shopping Bag",
                tint = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "Mis reservas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(5.dp))

        // ── Lista de reservas ────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                SaleListItem(
                    sale = sale,
                    onClick = {

                    }
                )
            }
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Empty State — primera visita, ninguna compra hecha todavía
// ─────────────────────────────────────────────────────────────────────────────
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
// ─────────────────────────────────────────────────────────────────────────────
// Badge de estado visual
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SaleStatusBadge(state: BuyState) {
    val (icon, label, color) = when (state) {
        BuyState.UNVERIFIED -> Triple(
            Icons.Outlined.HourglassEmpty,
            "Reservada — esperando confirmación del taller",
            MaterialTheme.colorScheme.tertiary
        )
        BuyState.VERIFIED -> Triple(
            Icons.Outlined.CheckCircle,
            "¡Lista! Tu pedido está listo",
            MaterialTheme.colorScheme.primary
        )
        BuyState.DELETED -> Triple(
            Icons.Outlined.Cancel,
            "Cancelada",
            MaterialTheme.colorScheme.error
        )
    }
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(600),
        label = "statusColor"
    )
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = animatedColor.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = animatedColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = animatedColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Selector de entrega — aparece SOLO cuando el pedido está VERIFIED
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeliveryOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "cardColor"
    )
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (selected) CardDefaults.outlinedCardBorder() else null,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
// ─────────────────────────────────────────────────────────────────────────────
// Item de la lista de reservas
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SaleListItem(
    sale: Sale,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
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
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
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
                    Text(
                        text = "Monto total:",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "${"%.2f".format(sale.amount)} CUP",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            // Badge de estado compacto
            val (icon, label, color) = when (sale.verified) {
                BuyState.UNVERIFIED -> Triple(
                    Icons.Outlined.HourglassEmpty,
                    "Pendiente",
                    Color(0xFFFF973C)
                )
                BuyState.VERIFIED -> Triple(
                    Icons.Outlined.CheckCircle,
                    "Listo",
                    MaterialTheme.colorScheme.primary
                )
                BuyState.DELETED -> Triple(
                    Icons.Outlined.Cancel,
                    "Cancelada",
                    MaterialTheme.colorScheme.error
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector =  icon,
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

@Preview(
    showBackground = true
)
@Composable
private fun BuyReservationScreenPreview() {
    val products = remember {
        listOf(
            Product(
                "product1",
                "Product test 1",
                "Product test 1 description",
                332.2,
                "photo url 1",
                "categoryId",
                4.3
            ),
            Product(
                "product2",
                "Product test 2",
                "Product test 2 description",
                332.2,
                "photo url 2",
                "categoryId",
                4.7
            ),
            Product(
                "product3",
                "Product test 3",
                "Product test 3 description",
                312.5,
                "photo url 3",
                "categoryId",
                4.0
            )
        )
    }

    val salesUIState = remember {
        listOf(
            UiSaleItem( products[0], 5),
            UiSaleItem( products[1], 3),
        )
    }
    val salesUIState2 = remember {
        listOf(
            UiSaleItem( products[1], 5),
            UiSaleItem( products[2], 3),
        )
    }
    val salesUIState3 = remember {
        listOf(
            UiSaleItem( products[2], 5),
            UiSaleItem( products[0], 3),
        )
    }

    val salesItemsList = remember {
        salesUIState.map {
            SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name)
        }
    }
    val salesItemsList2 = remember {
        salesUIState2.map {
            SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name)
        }
    }
    val salesItemsList3 = remember {
        salesUIState3.map {
            SaleItem(it.product.id, quantity = it.quantity, productName = it.product.name)
        }
    }

    val totalAmount = remember {
        salesUIState.sumOf {
            it.product.price * it.quantity
        }
    }
    val totalAmount2 = remember {
        salesUIState.sumOf {
            it.product.price * it.quantity
        }
    }
    val totalAmount3 = remember {
        salesUIState.sumOf {
            it.product.price * it.quantity
        }
    }

    val saleList = remember {
        listOf(
            Sale(
                id = "sale1",
                date = LocalDate(2023,1,12),
                amount = totalAmount,
                verified = BuyState.UNVERIFIED,
                products = salesItemsList,
                userId = "userId test",
                deliveryType = DeliveryType.DELIVERY
            ),
            Sale(
                id = "sale2",
                date = LocalDate(2023,1,13),
                amount = totalAmount2,
                verified = BuyState.VERIFIED,
                products = salesItemsList2,
                userId = "userId test",
                deliveryType = DeliveryType.DELIVERY
            ),
            Sale(
                id = "sale3",
                date = LocalDate(2023,1,14),
                amount = totalAmount3,
                verified = BuyState.DELETED,
                products = salesItemsList3,
                userId = "userId test",
                deliveryType = DeliveryType.PICKUP
            )
        )
    }

    val translateMap = mutableMapOf<String, String>()

    products.forEach { product ->
        translateMap[product.id] = product.name
    }

    AlejoTallerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            BuyReservationScreen(
                sales = saleList,
                onGoToShop = {},
                onSaleSelected = {_->}
            )
        }
    }
}