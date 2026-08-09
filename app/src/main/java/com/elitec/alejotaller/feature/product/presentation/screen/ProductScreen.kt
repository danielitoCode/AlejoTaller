package com.elitec.alejotaller.feature.product.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion
import kotlinx.coroutines.delay

@Composable
fun ProductScreen(
    onProductClick: (String) -> Unit,
    products: List<Product>,
    categories: List<com.elitec.alejotaller.feature.category.domain.entity.Category> = emptyList(),
    promotions: List<Promotion> = emptyList(),
    onPromotionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    selectedCategoryId: String? = null,
    onCategorySelected: (com.elitec.alejotaller.feature.category.domain.entity.Category?) -> Unit = {}
) {
    ProductScreenContent(
        onProductClick = onProductClick,
        products = products,
        categories = categories,
        promotions = promotions,
        onPromotionClick = onPromotionClick,
        modifier = modifier,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = onCategorySelected
    )
}

@Composable
fun ProductGrid(
    onProductClick: (String) -> Unit,
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val minCellSize = when {
            maxWidth < 600.dp -> 160.dp
            maxWidth < 900.dp -> 190.dp
            else -> 220.dp
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = minCellSize),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(products, key = { it.id }) { product ->
                ProductItem(
                    onClick = { onProductClick(product.id) },
                    product = product
                )
            }
        }
    }
}

@Composable
private fun CompactLandscapeProductItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 3.dp,
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.image),
                    error = painterResource(R.drawable.errorimage),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stockLabel(product.availableStock()),
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        product.availableStock() <= 0 -> MaterialTheme.colorScheme.error
                        product.availableStock() <= 5 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
        }
    }
}

@Composable
fun IconPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.1f))
            .border(1.dp, color, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun ProductScreenContent(
    onProductClick: (String) -> Unit,
    products: List<Product>,
    categories: List<com.elitec.alejotaller.feature.category.domain.entity.Category>,
    promotions: List<Promotion>,
    onPromotionClick: (String) -> Unit,
    modifier: Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategoryId: String?,
    onCategorySelected: (com.elitec.alejotaller.feature.category.domain.entity.Category?) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors()
        )

        if (categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val allSelected = selectedCategoryId == null
                    Surface(
                        onClick = { onCategorySelected(null) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (allSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Todas",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (allSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(categories, key = { it.id }) { category ->
                    val isSelected = selectedCategoryId == category.id
                    Surface(
                        onClick = { onCategorySelected(category) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = category.name,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        BannerSection(
            visible = promotions.isNotEmpty(),
            promotions = promotions,
            onPromotionClick = onPromotionClick
        )

        ProductGrid(
            onProductClick = onProductClick,
            products = products,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun BannerSection(
    visible: Boolean = true,
    promotions: List<Promotion> = emptyList(),
    onPromotionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var activeIndex by remember(promotions) { mutableStateOf(0) }
    val activePromotion = promotions.getOrNull(activeIndex)

    LaunchedEffect(promotions, visible) {
        if (!visible || promotions.size <= 1) return@LaunchedEffect
        while (true) {
            delay(4_000)
            activeIndex = (activeIndex + 1) % promotions.size
        }
    }

    AnimatedVisibility(visible = visible && promotions.isNotEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable(enabled = activePromotion != null) {
                    activePromotion?.let { promotion: Promotion -> onPromotionClick(promotion.id) }
                }
                .padding(20.dp)
        ) {
            Crossfade(targetState = activePromotion?.id ?: "default", label = "banner") {
                Text(
                    text = activePromotion?.title ?: stringResource(R.string.clearance_sales, defaultValue = "Ofertas"),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductScreenPreview() {
    AlejoTallerTheme {
        ProductScreen(
            onProductClick = {},
            products = emptyList()
        )
    }
}

private fun stringResource(id: Int, defaultValue: String): String = defaultValue
