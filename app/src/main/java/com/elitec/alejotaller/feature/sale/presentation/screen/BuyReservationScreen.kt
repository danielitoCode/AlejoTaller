package com.elitec.alejotaller.feature.sale.presentation.screen

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.sale.domain.entity.BuyState
import com.elitec.alejotaller.feature.sale.domain.entity.DeliveryType
import com.elitec.alejotaller.feature.sale.domain.entity.Sale
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
fun BuyReservationScreen(
    sales: List<Sale>,
    productNamesById: Map<String, String>,
    // ── Nuevo callback para cuando el usuario confirma tipo de entrega ──
    onDeliveryTypeSelected: (saleId: String, type: DeliveryType) -> Unit = { _, _ -> },
    // ── Nuevo callback para navegar al carrito (desde el EmptyState) ──
    onGoToShop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSale by remember(sales) { mutableStateOf(sales.firstOrNull()) }
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Detalle de la venta seleccionada con QR ──────────────────────
        selectedSale?.let { sale ->
            SaleDetailCard(
                sale = sale,
                productNamesById = productNamesById,
                onDeliveryTypeSelected = onDeliveryTypeSelected
            )
        }
        // ── Lista de reservas ────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                SaleListItem(
                    sale = sale,
                    isSelected = selectedSale?.id == sale.id,
                    onClick = { selectedSale = sale }
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
// Card de detalle con QR + estado + selector de entrega cuando está VERIFIED
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SaleDetailCard(
    sale: Sale,
    productNamesById: Map<String, String>,
    onDeliveryTypeSelected: (saleId: String, type: DeliveryType) -> Unit
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Estado visual del pedido
            SaleStatusBadge(sale.verified)
            // QR
            Image(
                painter = rememberQrCodePainter(qrContent),
                contentDescription = "Código QR del pedido",
                modifier = Modifier.size(160.dp)
            )
            Text(
                text = "Pedido #${sale.id.take(8)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Total: ${"%.2f".format(sale.amount)} CUP",
                style = MaterialTheme.typography.bodyMedium
            )
            // ─────────────────────────────────────────────────────────────
            // VACÍO 2: Pedido VERIFIED — mostrar selector de entrega
            // ─────────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = sale.verified == BuyState.VERIFIED,
                enter = fadeIn(tween(400)) + expandVertically(tween(400))
            ) {
                DeliverySelectionSection(
                    currentDeliveryType = sale.deliveryType,
                    onTypeSelected = { type -> onDeliveryTypeSelected(sale.id, type) }
                )
            }
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
            // Opción: Recoger en tienda
            DeliveryOptionCard(
                icon = Icons.Default.Store,
                title = "Recoger",
                subtitle = "Paso por el taller",
                selected = currentDeliveryType == DeliveryType.PICKUP,
                onClick = { onTypeSelected(DeliveryType.PICKUP) },
                modifier = Modifier.weight(1f)
            )
            // Opción: Domicilio
            DeliveryOptionCard(
                icon = Icons.Default.DeliveryDining,
                title = "Domicilio",
                subtitle = "Me lo traen",
                selected = currentDeliveryType == DeliveryType.DELIVERY,
                onClick = { onTypeSelected(DeliveryType.DELIVERY) },
                modifier = Modifier.weight(1f)
            )
        }
        // Confirmación cuando ya eligió
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
// ─────────────────────────────────────────────────────────────────────────────
// Item de la lista de reservas
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SaleListItem(
    sale: Sale,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
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
                Text(
                    text = "Pedido #${sale.id.take(8)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(text = sale.date.toString(), style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${"%.2f".format(sale.amount)} CUP",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            // Badge de estado compacto
            val (stateLabel, stateColor) = when (sale.verified) {
                BuyState.UNVERIFIED -> "Reservada" to MaterialTheme.colorScheme.tertiary
                BuyState.VERIFIED   -> "Lista ✓"   to MaterialTheme.colorScheme.primary
                BuyState.DELETED    -> "Cancelada" to MaterialTheme.colorScheme.error
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = stateColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = stateLabel,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = stateColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun BuyState.toLabel(): String = when (this) {
    BuyState.UNVERIFIED -> "Reservada"
    BuyState.VERIFIED -> "Lista"
    BuyState.DELETED -> "Cancelada"
}