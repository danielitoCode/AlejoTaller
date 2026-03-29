package com.elitec.alejotaller.feature.sale.presentation.screen

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import com.elitec.alejotaller.feature.sale.domain.entity.SaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.datetime.LocalDate

// Constantes para testing
const val DELIVERY_SECTION_TAG = "delivery_section"
const val OPTION_PICKUP_TAG = "option_pickup"
const val OPTION_DELIVERY_TAG = "option_delivery"

@Composable
fun BuyReservationDetailsScreen(
    sale: Sale,
    findProductPrice: (String) -> Double,
    productNamesById: Map<String, String>,
    onDeliveryTypeSelected: (saleId: String, type: DeliveryType) -> Unit,
    modifier: Modifier = Modifier
) {
    val qrContent = buildString {
        appendLine("id=${sale.id}")
        appendLine("status=${sale.verified.name}")
        appendLine("amount=${"%.2f".format(sale.amount)}")
        appendLine("date=${sale.date}")
        sale.products.forEach { item ->
            val name = item.productName ?: productNamesById[item.productId] ?: item.productId
            appendLine("- $name x${item.quantity}")
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp),
        modifier = modifier
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.onBackground,
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = ""
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    text = "Estado de su pedido"
                )
            }
            Text(
                color = MaterialTheme.colorScheme.onBackground.copy(0.6f),
                text = "Id: ${sale.id.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Monto total:"
            )
            Spacer(Modifier.width(5.dp))
            Text(
                fontWeight = FontWeight.Bold,
                text = "${"%.2f".format(sale.amount)} CUP",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        // Estado visual del pedido
        SaleStatusBadge(sale.verified)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // QR
                    Image(
                        painter = rememberQrCodePainter(qrContent),
                        contentDescription = "Código QR del pedido",
                        modifier = Modifier.size(160.dp)
                    )
                    // Selector de entrega si está VERIFIED
                    AnimatedVisibility(
                        visible = sale.verified == BuyState.VERIFIED,
                        enter = fadeIn(tween(400)) + expandVertically(tween(400)),
                        modifier = Modifier.testTag(DELIVERY_SECTION_TAG)
                    ) {
                        DeliverySelectionSection(
                            currentDeliveryType = sale.deliveryType,
                            onTypeSelected = { type -> onDeliveryTypeSelected(sale.id, type) }
                        )
                    }
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Productos en su compra:",
                style = MaterialTheme.typography.headlineSmall
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(sale.products) { product ->
                    val productPrice = findProductPrice(product.productId)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                        ) {
                            Column {
                                Text(

                                    text = productNamesById[product.productId] ?: ""
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        text = "Cant.:"
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        fontWeight = FontWeight.Bold,
                                        text = "${product.quantity}"
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Prec.:"
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    fontWeight = FontWeight.Bold,
                                    text = "$productPrice CUP"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeliverySelectionSection(
    currentDeliveryType: DeliveryType?,
    onTypeSelected: (DeliveryType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HorizontalDivider()
        Text(
            text = "🎉 ¡Tu pedido está listo!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = "¿Cómo prefieres recibirlo?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DeliveryOptionCard(
                icon = Icons.Default.Store,
                title = "Recoger",
                subtitle = "Paso por el taller",
                selected = currentDeliveryType == DeliveryType.PICKUP,
                onClick = { onTypeSelected(DeliveryType.PICKUP) },
                modifier = Modifier.weight(1f).testTag(OPTION_PICKUP_TAG)
            )
            DeliveryOptionCard(
                icon = Icons.Default.DeliveryDining,
                title = "Domicilio",
                subtitle = "Me lo traen",
                selected = currentDeliveryType == DeliveryType.DELIVERY,
                onClick = { onTypeSelected(DeliveryType.DELIVERY) },
                modifier = Modifier.weight(1f).testTag(OPTION_DELIVERY_TAG)
            )
        }
        currentDeliveryType?.let { type ->
            val confirmText = when (type) {
                DeliveryType.PICKUP   ->
                    "✅ Perfecto, te esperamos en el taller. Trae el código QR para retirar tu pedido."
                DeliveryType.DELIVERY ->
                    "✅ ¡Entendido! El taller te contactará pronto para coordinar la entrega."
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = confirmText,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

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

@Composable
private fun BuyReservationDetailsPreviewContent(state: BuyState) {
    val products = remember {
        listOf(
            Product("p1", "Batería 12V", "", 12000.0, "", "", 4.5),
            Product("p2", "Aceite Motor", "", 4500.0, "", "", 4.8)
        )
    }
    val salesItems = products.map {
        SaleItem(it.id, quantity = 1, productName = it.name)
    }
    val translateMap = products.associate { it.id to it.name }

    AlejoTallerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            BuyReservationDetailsScreen(
                sale = Sale(
                    id = "ORD-777",
                    date = LocalDate(2023, 10, 25),
                    amount = 16500.0,
                    verified = state,
                    products = salesItems,
                    userId = "user123",
                    deliveryType = if (state == BuyState.VERIFIED) DeliveryType.PICKUP else null
                ),
                productNamesById = translateMap,
                findProductPrice = { id -> products.find { it.id == id }?.price ?: 0.0 },
                onDeliveryTypeSelected = { _, _ -> },
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Estado: Pendiente")
@Composable
private fun PreviewUnverified() {
    BuyReservationDetailsPreviewContent(BuyState.UNVERIFIED)
}

@Preview(showBackground = true, name = "Estado: Verificado (Listo)")
@Composable
private fun PreviewVerified() {
    BuyReservationDetailsPreviewContent(BuyState.VERIFIED)
}

@Preview(showBackground = true, name = "Estado: Cancelado")
@Composable
private fun PreviewDeleted() {
    BuyReservationDetailsPreviewContent(BuyState.DELETED)
}
