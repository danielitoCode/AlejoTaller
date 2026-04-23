package com.elitec.alejotaller.feature.sale.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.model.UiSaleItem
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme

@Composable
fun BuyScreen(
    items: List<UiSaleItem>,
    totalAmount: Double,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val isWideLayout = maxWidth >= 840.dp
        val contentPadding = if (isWideLayout) 24.dp else 16.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Shopping Bag",
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "Tu carrito",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            if (items.isEmpty()) {
                Text(
                    color = MaterialTheme.colorScheme.onBackground,
                    text = "No hay productos en el carrito.",
                    style = MaterialTheme.typography.bodyLarge
                )
            } else if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1.45f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items, key = { it.product.id }) { item ->
                            CartItemCard(
                                item = item,
                                onIncreaseQuantity = onIncreaseQuantity,
                                onDecreaseQuantity = onDecreaseQuantity,
                                onRemoveItem = onRemoveItem
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(0.85f),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Resumen del pedido",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${items.size} productos en el carrito",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Total: $ ${"%.2f".format(totalAmount)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onConfirmClick,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Continuar")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.product.id }) { item ->
                        CartItemCard(
                            item = item,
                            onIncreaseQuantity = onIncreaseQuantity,
                            onDecreaseQuantity = onDecreaseQuantity,
                            onRemoveItem = onRemoveItem
                        )
                    }
                }

                Text(
                    text = "Total: $ ${"%.2f".format(totalAmount)}",
                    style = MaterialTheme.typography.titleLarge
                )

                Button(
                    onClick = onConfirmClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Continuar")
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: UiSaleItem,
    onIncreaseQuantity: (String) -> Unit,
    onDecreaseQuantity: (String) -> Unit,
    onRemoveItem: (String) -> Unit
) {
    Card(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.product.name,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "Precio por unidad:")
                Text(
                    fontWeight = FontWeight.SemiBold,
                    text = "$ ${item.product.price}",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "Cantidad:")
                Text(
                    fontWeight = FontWeight.SemiBold,
                    text = "${item.quantity}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        ),
                        onClick = { onDecreaseQuantity(item.product.id) }
                    ) {
                        Text(
                            color = MaterialTheme.colorScheme.onError,
                            text = "-1"
                        )
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF57965C).copy(alpha = 0.7f)
                        ),
                        onClick = { onIncreaseQuantity(item.product.id) }
                    ) {
                        Text(
                            color = Color(0xFF25401A),
                            text = "+1"
                        )
                    }
                }
                OutlinedButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    ),
                    onClick = { onRemoveItem(item.product.id) }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete icon"
                        )
                        Text(
                            style = MaterialTheme.typography.titleMedium,
                            text = "Quitar"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BuyScreenPreview() {
    val salesUIState = listOf(
        UiSaleItem(
            Product(
                "product1",
                "Product test 1",
                "Product test 1 description",
                332.2,
                "photo url 1",
                "categoryId",
                4.3
            ),
            5
        ),
        UiSaleItem(
            Product(
                "product2",
                "Product test 2",
                "Product test 2 description",
                332.2,
                "photo url 2",
                "categoryId",
                4.7
            ),
            3
        ),
    )
    val totalAmount = remember { salesUIState.sumOf { it.product.price * it.quantity } }
    AlejoTallerTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize()
        ) {
            BuyScreen(
                items = salesUIState,
                totalAmount = totalAmount,
                onRemoveItem = {},
                onConfirmClick = {},
                onDecreaseQuantity = {},
                onIncreaseQuantity = {}
            )
        }
    }
}
