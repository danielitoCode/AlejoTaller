package com.elitec.alejotaller.feature.product.presentation.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.feature.product.presentation.components.LikeHeartButton
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode

@Composable
private fun StockStatusBadge(
    available: Int,
    modifier: Modifier = Modifier
) {
    val (container, content) = when {
        available <= 0 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        available <= 5 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        color = container,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier
    ) {
        Text(
            text = stockLabel(available),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content
        )
    }
}

@Composable
fun ProductDetailScreen(
    modifier: Modifier = Modifier,
    product: Product,
    showTopBar: Boolean = true,
    isLiked: Boolean = false,
    onBackClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onAddToCartClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val deviceMode = LocalConfiguration.current.toDeviceMode()
    val available = product.availableStock()

    val handleShare = {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://alejotaller.onrender.com/product/${product.id}")
            putExtra(Intent.EXTRA_SUBJECT, product.name)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir ${product.name}"))
    }

    when (deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                bottomBar = {
                    PriceAndAddToCartSection(
                        productPrice = product.price,
                        available = available,
                        onAddToCartClick = onAddToCartClick
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                ) {
                    if (showTopBar) {
                        HeaderSection(
                            onBackClick = onBackClick,
                            isLiked = isLiked,
                            onFavoriteClick = onFavoriteClick,
                            onShareClick = handleShare
                        )
                    }
                    ProductImageSection(
                        photoUrl = product.photoUrl,
                        modifier = Modifier
                            .height(300.dp)
                            .padding(horizontal = 16.dp)
                    )
                    ProductInfoSection(
                        productName = product.name,
                        productDescription = product.description,
                        available = available
                    )
                }
            }
        }

        AppWindowType.MobileLandscape,
        AppWindowType.TabletLandscape -> {
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    ProductImageSection(
                        photoUrl = product.photoUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (showTopBar) {
                        HeaderSection(
                            onBackClick = onBackClick,
                            isLiked = isLiked,
                            onFavoriteClick = onFavoriteClick,
                            onShareClick = handleShare,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
                Scaffold(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(15.dp)),
                    bottomBar = {
                        PriceAndAddToCartSection(
                            productPrice = product.price,
                            available = available,
                            onAddToCartClick = onAddToCartClick
                        )
                    }
                ) { paddingValues ->
                    ProductInfoSection(
                        productName = product.name,
                        productDescription = product.description,
                        available = available,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }

        AppWindowType.Laptop,
        AppWindowType.DesktopVertical,
        AppWindowType.Expanded -> {
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = modifier.fillMaxSize()
            ) {
                if (showTopBar) {
                    HeaderSection(
                        onBackClick = onBackClick,
                        isLiked = isLiked,
                        onFavoriteClick = onFavoriteClick,
                        onShareClick = handleShare
                    )
                }
                Row(modifier = Modifier.weight(1f).fillMaxSize()) {
                    ProductImageSection(
                        photoUrl = product.photoUrl,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(15.dp)
                    )
                    Scaffold(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .padding(vertical = 15.dp)
                            .clip(RoundedCornerShape(15.dp)),
                        bottomBar = {
                            PriceAndAddToCartSection(
                                productPrice = product.price,
                                available = available,
                                onAddToCartClick = onAddToCartClick
                            )
                        }
                    ) { paddingValues ->
                        ProductInfoSection(
                            productName = product.name,
                            productDescription = product.description,
                            available = available,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    onBackClick: () -> Unit,
    isLiked: Boolean,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            LikeHeartButton(
                isLiked = isLiked,
                onToggle = onFavoriteClick,
                size = 40.dp,
                iconSize = 20.dp,
                showBackground = true
            )
            IconButton(
                onClick = onShareClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Compartir",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ProductImageSection(
    photoUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(15.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            placeholder = painterResource(R.drawable.image),
            error = painterResource(R.drawable.errorimage),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ProductInfoSection(
    productName: String,
    productDescription: String = "",
    available: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {
        Text(
            text = productName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        StockStatusBadge(available = available)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RatingBadge(
                value = stringResource(R.string.rating_value),
                iconColor = MaterialTheme.colorScheme.tertiary
            )
            RatingBadge(
                value = stringResource(R.string.recommendation_percentage),
                iconColor = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.reviews_count),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = productDescription,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun RatingBadge(
    value: String,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(iconColor, CircleShape)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PriceAndAddToCartSection(
    productPrice: Double,
    available: Int,
    onAddToCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inStock = available > 0
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$ $productPrice",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = stockLabel(available),
                    color = when {
                        !inStock -> MaterialTheme.colorScheme.error
                        available <= 5 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Button(
                onClick = onAddToCartClick,
                enabled = inStock,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (inStock) stringResource(R.string.add_to_cart) else "Agotado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailScreenPreview() {
    AlejoTallerTheme {
        ProductDetailScreen(
            modifier = Modifier.fillMaxSize(),
            showTopBar = true,
            isLiked = false,
            onBackClick = {},
            onFavoriteClick = {},
            onAddToCartClick = {},
            product = Product(
                id = "preview",
                name = "Product test",
                description = "Descripcion de prueba",
                price = 23.4,
                photoUrl = "",
                categoryId = "cat",
                rating = 4.3,
                photoLocalResource = null,
                existence = 8,
                reserved = 2
            )
        )
    }
}
