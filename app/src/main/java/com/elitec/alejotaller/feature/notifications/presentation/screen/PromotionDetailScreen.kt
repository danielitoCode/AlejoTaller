package com.elitec.alejotaller.feature.notifications.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.notifications.presentation.screen.components.PromotionPriceSection
import com.elitec.alejotaller.infraestructure.core.presentation.util.AppWindowType
import com.elitec.alejotaller.infraestructure.core.presentation.util.GlobalPreview
import com.elitec.alejotaller.infraestructure.core.presentation.util.toDeviceMode
import com.elitec.shared.core.feature.notifications.domain.entity.Promotion

@Composable
fun PromotionDetailScreen(
    promotion: Promotion,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
    val deviceMode = LocalConfiguration.current.toDeviceMode()

    when(deviceMode) {
        AppWindowType.MobilePortrait,
        AppWindowType.TabletPortrait -> {
            Scaffold(
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { onBackClick() }
                            ) {
                                Surface(
                                    shadowElevation = 5.dp,
                                    tonalElevation = 5.dp,
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Icon(
                                        modifier = Modifier.padding(10.dp),
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Oferta especial",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        IconButton(
                            onClick = {}
                        ) {
                            Surface(
                                shadowElevation = 5.dp,
                                tonalElevation = 5.dp,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    modifier = Modifier.padding(10.dp),
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Shared icon",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            contentScale = ContentScale.Crop,
                            painter = painterResource(R.drawable.echoflow_transparent),
                            contentDescription = promotion.title
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.background.copy(0.8f),
                            tonalElevation = 5.dp,
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(15.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "GiftCard icon",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = promotion.title,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = promotion.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        PromotionPriceSection(
                            oldPrice = promotion.oldPrice,
                            currentPrice = promotion.currentPrice
                        )

                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Vigente por tiempo limitado",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
        AppWindowType.MobileLandscape ,
        AppWindowType.TabletLandscape,
        AppWindowType.Laptop,
        AppWindowType.DesktopVertical,
        AppWindowType.Expanded -> {
            Scaffold(
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Column(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier,
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = { onBackClick() }
                            ) {
                                Surface(
                                    shadowElevation = 5.dp,
                                    tonalElevation = 5.dp,
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = "Oferta especial",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        IconButton(
                            onClick = {}
                        ) {
                            Surface(
                                shadowElevation = 5.dp,
                                tonalElevation = 5.dp,
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Icon(
                                    modifier = Modifier.padding(10.dp),
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Shared icon",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    contentScale = ContentScale.Crop,
                                    painter = painterResource(R.drawable.echoflow_transparent),
                                    contentDescription = promotion.title
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.background.copy(0.8f),
                                    tonalElevation = 5.dp,
                                    shadowElevation = 5.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(15.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = "GiftCard icon",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(2f)
                                .padding(paddingValues)
                                .fillMaxSize()
                        ) {
                            Text(
                                text = promotion.title,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = promotion.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            PromotionPriceSection(
                                oldPrice = promotion.oldPrice,
                                currentPrice = promotion.currentPrice
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Vigente por tiempo limitado",
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 8.dp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:parent=pixel_5,orientation=landscape")
@Composable
fun PromotionDetailScreenPreview() {
    val promotion = Promotion(
        id = "id test",
        title = "Promotion test tittle",
        message = "Hello this is a promotion test message, if you like press like button please",
        imageUrl = "",
        oldPrice = 22.3,
        currentPrice = 34.2,
        validFromEpochMillis = 123123123123,
        validUntilEpochMillis = 123123123123
    )
    GlobalPreview {
        PromotionDetailScreen(
            promotion = promotion,
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            onBackClick = {}
        )
    }
}