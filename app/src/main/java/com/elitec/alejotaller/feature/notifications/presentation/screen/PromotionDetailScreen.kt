package com.elitec.alejotaller.feature.notifications.presentation.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import com.elitec.alejotaller.feature.notifications.domain.entity.Promotion
import com.elitec.alejotaller.feature.notifications.presentation.screen.components.PromotionPriceSection

@Composable
fun PromotionDetailScreen(
    promotion: Promotion,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {}
) {
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
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Text(
                    text = "Oferta especial",
                    style = MaterialTheme.typography.titleLarge
                )
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