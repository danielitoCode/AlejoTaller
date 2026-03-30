package com.elitec.alejotaller.infraestructure.core.presentation.navigation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun InternalShellShimmer(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "internal-shell-shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "internal-shell-progress"
    )

    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val bright = MaterialTheme.colorScheme.surfaceBright
    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.26f)
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            base.copy(alpha = 0.92f),
            bright.copy(alpha = 0.98f),
            base.copy(alpha = 0.92f)
        ),
        start = Offset(progress * 900f, 0f),
        end = Offset(progress * 900f + 320f, 260f)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBlock(
            brush = shimmerBrush,
            shape = RoundedCornerShape(18.dp),
            borderColor = outline,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        )

        ShimmerBlock(
            brush = shimmerBrush,
            shape = RoundedCornerShape(24.dp),
            borderColor = outline,
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(4) {
                ShimmerBlock(
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(14.dp),
                    borderColor = outline,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items((0 until 6).toList()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShimmerBlock(
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(24.dp),
                        borderColor = outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(182.dp)
                    )
                    ShimmerBlock(
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(18.dp)
                    )
                    ShimmerBlock(
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .width(88.dp)
                            .height(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(68.dp))
    }
}

@Composable
private fun ShimmerBlock(
    brush: Brush,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color = Color.Transparent
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = Color.Transparent,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        )
    }
}
