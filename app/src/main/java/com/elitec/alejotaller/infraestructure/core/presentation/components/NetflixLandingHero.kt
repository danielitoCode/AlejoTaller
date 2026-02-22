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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun NetflixLandingHero(
    modifier: Modifier = Modifier,
    rowCount: Int = 3,
    itemSize: Dp = 240.dp,
    itemSpacing: Dp = 18.dp,
    rowSpacing: Dp = 28.dp,
    horizontalPadding: Dp = 24.dp,
    autoScrollSpeed: Float = 40f,
    cornerRadius: Dp = 26.dp
) {
    val images = remember {
        listOf(
            R.drawable.echoflow_transparent,
            R.drawable.li3_2a,
            R.drawable.bms5v,
            R.drawable.t2n3904
        )
    }

    var selected by remember { mutableStateOf<Int?>(null) }
    val heroProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val freeze = selected != null

    Box(modifier.fillMaxSize()) {

        Column(
            verticalArrangement = Arrangement.spacedBy(rowSpacing),
            modifier = Modifier.padding(top = 20.dp)
        ) {
            repeat(rowCount) { rowIndex ->
                NetflixAutoScrollRow(
                    items = images,
                    scrollLeft = rowIndex % 2 == 0,
                    itemSize = itemSize,
                    spacing = itemSpacing,
                    horizontalPadding = horizontalPadding,
                    speed = autoScrollSpeed + rowIndex * 10,
                    cornerRadius = cornerRadius,
                    freeze = freeze,
                    onItemClick = { index ->
                        if (selected == null) {
                            selected = index
                            scope.launch {
                                heroProgress.snapTo(0f)
                                heroProgress.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = 0.8f,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }

        selected?.let { index ->
            HeroOverlay(
                imageRes = images[index],
                progress = heroProgress.value,
                cornerRadius = cornerRadius
            ) {
                scope.launch {
                    heroProgress.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = 0.7f,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    selected = null
                }
            }
        }
    }
}

@Composable
private fun NetflixAutoScrollRow(
    items: List<Int>,
    scrollLeft: Boolean,
    itemSize: Dp,
    spacing: Dp,
    horizontalPadding: Dp,
    speed: Float,
    cornerRadius: Dp,
    freeze: Boolean,
    onItemClick: (Int) -> Unit
) {
    val density = LocalDensity.current
    val itemWidthPx = with(density) { itemSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }

    val totalItemWidth = itemWidthPx + spacingPx
    val contentWidth = totalItemWidth * items.size

    val offset = remember { Animatable(0f) }

    LaunchedEffect(freeze, scrollLeft, speed) {

        if (freeze) return@LaunchedEffect

        val direction = if (scrollLeft) -1f else 1f
        var lastFrameTime = 0L

        while (isActive) {

            val frameTime = withFrameNanos { it }

            if (lastFrameTime == 0L) {
                lastFrameTime = frameTime
                continue
            }

            val deltaTimeSec = (frameTime - lastFrameTime) / 1_000_000_000f
            lastFrameTime = frameTime

            val delta = direction * speed * deltaTimeSec
            val new = offset.value + delta

            val wrapped = when {
                new < -contentWidth -> 0f
                new > 0f -> -contentWidth
                else -> new
            }

            offset.snapTo(wrapped)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemSize + 12.dp)
            .clipToBounds()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier
                .graphicsLayer {
                    translationX = offset.value
                }
                .padding(horizontal = horizontalPadding)
        ) {
            repeat(3) { repeatIndex ->
                items.forEachIndexed { index, image ->
                    NetflixCard(
                        image = image,
                        size = itemSize,
                        cornerRadius = cornerRadius,
                        onClick = { onItemClick(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NetflixCard(
    image: Int,
    size: Dp,
    cornerRadius: Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HeroOverlay(
    imageRes: Int,
    progress: Float,
    cornerRadius: Dp,
    onDismiss: () -> Unit
) {
    val t = progress.coerceIn(0f, 1f)
    val scale = lerp(0.85f, 1f, t)
    val alpha = lerp(0f, 1f, t)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = alpha))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .graphicsLayer {
                    this.alpha = t
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}