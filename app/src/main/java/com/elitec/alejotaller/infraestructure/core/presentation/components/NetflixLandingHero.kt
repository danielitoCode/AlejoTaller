package com.elitec.alejotaller.infraestructure.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun NetflixLandingHeroStable(
    modifier: Modifier = Modifier,
    rowCount: Int = 3,
    itemSize: Dp = 220.dp,
    spacing: Dp = 16.dp,
    rowSpacing: Dp = 28.dp,
    horizontalPadding: Dp = 24.dp,
    baseSpeed: Float = 40f,
    cornerRadius: Dp = 22.dp
) {
    val images = remember {
        listOf(
            R.drawable.echoflow_transparent,
            R.drawable.li3_2a,
            R.drawable.bms5v,
            R.drawable.t2n3904
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        repeat(rowCount) { rowIndex ->
            InfiniteStableRow(
                items = images,
                itemSize = itemSize,
                spacing = spacing,
                horizontalPadding = horizontalPadding,
                speed = baseSpeed + rowIndex * 12f,
                reverse = rowIndex % 2 == 0,
                cornerRadius = cornerRadius
            )
        }
    }
}

@Composable
private fun InfiniteStableRow(
    items: List<Int>,
    itemSize: Dp,
    spacing: Dp,
    horizontalPadding: Dp,
    speed: Float,
    reverse: Boolean,
    cornerRadius: Dp
) {
    val density = LocalDensity.current
    val itemWidthPx = with(density) { itemSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    val contentWidth = (itemWidthPx + spacingPx) * items.size

    var offset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(reverse, speed) {
        var lastTime = 0L
        val direction = if (reverse) -1f else 1f

        while (isActive) {
            val frameTime = withFrameNanos { it }

            if (lastTime == 0L) {
                lastTime = frameTime
                continue
            }

            val deltaTime = (frameTime - lastTime) / 1_000_000_000f
            lastTime = frameTime

            offset += direction * speed * deltaTime

            // SOLO restamos o sumamos contentWidth
            if (offset <= -contentWidth) offset += contentWidth
            if (offset >= contentWidth) offset -= contentWidth
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemSize + 12.dp)
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(offset.toInt(), 0) }
                .padding(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {

            // SOLO duplicamos 2 veces
            repeat(2) {
                items.forEach { image ->
                    StableCard(
                        image = image,
                        size = itemSize,
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}

@Composable
private fun StableCard(
    image: Int,
    size: Dp,
    cornerRadius: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}