package com.elitec.alejotaller.feature.product.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.product.data.test.productTestList
import com.elitec.alejotaller.feature.product.domain.entity.Product
import com.elitec.alejotaller.infraestructure.core.presentation.theme.AlejoTallerTheme
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState

@Composable
fun ProductScreen(
    navigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val products = remember {
        productTestList
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        SearchBar()
        Spacer(modifier = Modifier.height(16.dp))
        BannerSection()
        Spacer(modifier = Modifier.height(16.dp))
        CategoriesSection()
        Spacer(modifier = Modifier.height(16.dp))
        ProductGrid(
            onProductClick =  navigateToDetails,
            products = products
        )
    }
}

@Composable
fun SearchBar(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(id = R.string.search_placeholder), color = Color.Gray) },
        trailingIcon = { IconPlaceholder(modifier = Modifier.size(24.dp)) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.LightGray,
            focusedBorderColor = Color.LightGray
        ),
        singleLine = true
    )
}

@Composable
fun BannerSection(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF388E3C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.clearance_sales),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconPlaceholder(modifier = Modifier.size(14.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.up_to_50),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    contentScale = ContentScale.Crop,
                    painter = painterResource(R.drawable.echoflow_transparent),
                    contentDescription = "Banner Image"
                )
            }
        }
    }
}

@Composable
fun CategoriesSection(modifier: Modifier = Modifier) {
    val categories = listOf(
        stringResource(id = R.string.all),
        stringResource(id = R.string.smartphones),
        stringResource(id = R.string.headphones),
        stringResource(id = R.string.laptops)
    )
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.categories),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = stringResource(id = R.string.see_all),
                color = Color(0xFF4CAF50),
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == stringResource(id = R.string.all)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF4CAF50) else Color.Transparent)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF4CAF50) else Color.Black,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ProductGrid(
    onProductClick: (String) -> Unit,
    products: List<Product>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(products) { product ->
            ProductItem(
                onClick = { onProductClick(product.id) },
                product = product
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ProductItem(
    onClick: () -> Unit,
    product: Product,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()
        .clickable {
            onClick()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                contentScale = ContentScale.Crop,
                painter = painterResource(product.photoLocalResource?:0),
                contentDescription = "Product Image",
            )
            Box(
                modifier = Modifier.fillMaxWidth()
                    .hazeEffect(style = HazeMaterials.ultraThin())
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(
                        top = 5.dp,
                        bottom = 5.dp,
                        end = 10.dp,
                        start = 10.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HeartBroken,
                        contentDescription = "Favorite",
                        tint = Color.White
                    )
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(
                    top = 5.dp,
                    bottom = 5.dp,
                    end = 10.dp,
                    start = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.HeartBroken,
                    contentDescription = "Favorite",
                    tint = Color.Black
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

    }
}

@Composable
fun IconPlaceholder(
    modifier: Modifier = Modifier,
    color: Color = Color.Black
) {
    Box(
        modifier = modifier
            .background(color = color.copy(alpha = 0.1f))
            .border(1.dp, color, RoundedCornerShape(2.dp))
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductScreenPreview() {
    AlejoTallerTheme {
        ProductScreen({})
    }
}
