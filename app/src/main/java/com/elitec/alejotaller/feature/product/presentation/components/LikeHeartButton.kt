package com.elitec.alejotaller.feature.product.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val InstagramPink = Color(0xFFFF2D55)

/**
 * Corazón de like local con pop + partículas (estilo Instagram)
 * solo al activar el like, no al quitarlo.
 */
@Composable
fun LikeHeartButton(
    isLiked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 22.dp,
    showBackground: Boolean = true
) {
    var burstToken by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(burstToken) {
        if (burstToken == 0) return@LaunchedEffect
        scale.snapTo(1f)
        scale.animateTo(0.75f, tween(80))
        scale.animateTo(
            1.35f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
        scale.animateTo(1f, tween(120))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (burstToken > 0) {
            BurstParticles(token = burstToken)
        }
        IconButton(
            onClick = {
                val wasLiked = isLiked
                onToggle()
                if (!wasLiked) {
                    burstToken += 1
                }
            },
            modifier = Modifier
                .size(size)
                .then(
                    if (showBackground) {
                        Modifier.background(
                            color = if (isLiked) {
                                InstagramPink.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                            },
                            shape = CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isLiked) "Quitar de me gusta" else "Me gusta",
                tint = if (isLiked) InstagramPink else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    }
            )
        }
    }
}

@Composable
private fun BurstParticles(token: Int) {
    val alphas = remember(token) { List(6) { Animatable(0f) } }
    val distances = remember(token) { List(6) { Animatable(0f) } }

    LaunchedEffect(token) {
        alphas.forEachIndexed { index, anim ->
            launch {
                anim.snapTo(1f)
                anim.animateTo(0f, tween(durationMillis = 650, delayMillis = index * 20))
            }
        }
        distances.forEachIndexed { index, anim ->
            launch {
                anim.snapTo(0f)
                anim.animateTo(22f, tween(durationMillis = 650, delayMillis = index * 20))
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        alphas.forEachIndexed { index, alphaAnim ->
            val angleDeg = index * 60f
            val rad = Math.toRadians(angleDeg.toDouble())
            val dist = distances[index].value
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        translationX = (kotlin.math.cos(rad) * dist).toFloat()
                        translationY = (kotlin.math.sin(rad) * dist).toFloat()
                        alpha = alphaAnim.value
                        scaleX = 0.6f + alphaAnim.value * 0.4f
                        scaleY = 0.6f + alphaAnim.value * 0.4f
                    }
                    .size(6.dp)
                    .background(
                        color = if (index % 2 == 0) InstagramPink else Color(0xFFFF7A9A),
                        shape = CircleShape
                    )
            )
        }
    }
}
