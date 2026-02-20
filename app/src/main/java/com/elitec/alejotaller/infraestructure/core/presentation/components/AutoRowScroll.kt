package com.elitec.alejotaller.infraestructure.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.elitec.alejotaller.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun AutoScrollRows(
    modifier: Modifier = Modifier,
    rowCount: Int = 3,
    itemCountPerRow: Int = 40,
    itemSize: Dp = 250.dp,
    itemCornerRadius: Dp = 30.dp,
    rowSpacing: Dp = 26.dp,
    itemSpacing: Dp = 18.dp,
    horizontalPadding: Dp = 18.dp,
) {
    val images: List<Int> = remember {
        listOf(
            R.drawable.echoflow_transparent,
            R.drawable.li3_2a,
            R.drawable.bms5v,
            R.drawable.t2n3904
        )
    }

    val rows = remember(rowCount, itemCountPerRow, images) {
        List(rowCount) { rowIndex ->
            val scrollToLeft = rowIndex % 2 == 0
            val items = List(itemCountPerRow) { itemIndex ->
                val imageRes = images[(rowIndex * 7 + itemIndex) % max(images.size, 1)]
                RowItem(id = "R$rowIndex-$itemIndex", imageRes = imageRes)
            }
            RowSpec(
                id = rowIndex,
                scrollToLeft = scrollToLeft,
                speedPxPerSec = 95f + rowIndex * 18f,
                items = items
            )
        }
    }

    val background = Color(0xFFF1EEE7)

    var selected by remember { mutableStateOf<SelectedItem?>(null) }
    val scope = rememberCoroutineScope()

    val rowVisibility = remember(rowCount) { List(rowCount) { Animatable(0f) } }
    val scatterProgress = remember { Animatable(0f) }
    val heroProgress = remember { Animatable(0f) }

    var rootSize by remember { mutableStateOf(IntSize.Zero) }
    var rootBoundsInRoot by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(rowCount) {
        rowVisibility.forEachIndexed { index, anim ->
            launch {
                delay(110L * index)
                anim.snapTo(0f)
                anim.animateTo(
                    1f,
                    animationSpec = tween(
                        durationMillis = 760,
                        easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
                    )
                )
            }
        }
    }

    val scatterSpecOut = tween<Float>(
        durationMillis = 980,
        easing = CubicBezierEasing(0.22f, 0f, 0.18f, 1f)
    )
    val scatterSpecIn = tween<Float>(
        durationMillis = 980,
        easing = CubicBezierEasing(0.22f, 0f, 0.18f, 1f)
    )

    val heroSpecOpen = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = 0.001f
    )
    val heroSpecClose = spring<Float>(
        dampingRatio = 0.55f,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = 0.001f
    )

    fun showDetail(item: RowItem, instanceKey: String, itemBoundsInRoot: Rect) {
        scope.launch {
            val rb = rootBoundsInRoot ?: return@launch
            val localStartBounds = itemBoundsInRoot.offsetBy(-rb.left, -rb.top)

            selected =
                SelectedItem(item = item, startBounds = localStartBounds, instanceKey = instanceKey)

            scatterProgress.snapTo(0f)
            heroProgress.snapTo(0f)

            withFrameNanos { }

            scatterProgress.animateTo(1f, animationSpec = scatterSpecOut)
            heroProgress.animateTo(1f, animationSpec = heroSpecOpen)
        }
    }

    fun hideDetail() {
        scope.launch {
            heroProgress.animateTo(0f, animationSpec = heroSpecClose)
            scatterProgress.animateTo(0f, animationSpec = scatterSpecIn)
            heroProgress.snapTo(0f)
            selected = null
        }
    }

    val freezeScroll = selected != null ||
            scatterProgress.value > 0.01f ||
            heroProgress.value > 0.01f

    Box(
        modifier = modifier
            .statusBarsPadding()
            .onSizeChanged { rootSize = it }
            .onGloballyPositioned { rootBoundsInRoot = it.boundsInRoot() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(rowSpacing)
        ) {
            rows.forEachIndexed { index, row ->
                SeamlessRow(
                    rowId = row.id,
                    items = row.items,
                    scrollToLeft = row.scrollToLeft,
                    speedPxPerSec = row.speedPxPerSec,
                    height = itemSize,
                    horizontalPadding = horizontalPadding,
                    itemSpacing = itemSpacing,
                    itemSize = itemSize,
                    itemCornerRadius = itemCornerRadius,
                    visibility = rowVisibility[index].value,
                    scrollEnabled = !freezeScroll && rowVisibility[index].value > 0.02f,
                    hiddenInstanceKey = selected?.instanceKey,
                    rootSize = rootSize,
                    scatter = scatterProgress.value,
                    selectedInstanceKey = selected?.instanceKey,
                    onItemTapped = { tapped, instanceKey, boundsInRoot ->
                        if (selected != null) return@SeamlessRow
                        showDetail(tapped, instanceKey, boundsInRoot)
                    }
                )
            }
        }

        val sel = selected
        if (sel != null && rootSize.width > 0 && rootSize.height > 0) {
            HeroOverlayFullscreenBounds(
                selected = sel,
                progress = heroProgress.value,
                rootSize = rootSize,
                initialCornerRadius = itemCornerRadius,
                background = background,
                onBackdropClick = { hideDetail() }
            )
        }
    }
}

private data class RowSpec(
    val id: Int,
    val scrollToLeft: Boolean,
    val speedPxPerSec: Float,
    val items: List<RowItem>,
)

private data class RowItem(
    val id: String,
    val imageRes: Int
)

private data class SelectedItem(
    val item: RowItem,
    val startBounds: Rect,
    val instanceKey: String
)

/**
 * A horizontally scrolling row that renders an effectively infinite list by repeating [items].
 * The row auto-scrolls while enabled and uses [visibility] to animate its entrance.
 */
@Composable
private fun SeamlessRow(
    rowId: Int,
    items: List<RowItem>,
    scrollToLeft: Boolean,
    speedPxPerSec: Float,
    height: Dp,
    horizontalPadding: Dp,
    itemSpacing: Dp,
    itemSize: Dp,
    itemCornerRadius: Dp,
    visibility: Float,
    scrollEnabled: Boolean,
    hiddenInstanceKey: String?,
    rootSize: IntSize,
    scatter: Float,
    selectedInstanceKey: String?,
    onItemTapped: (RowItem, String, Rect) -> Unit
) {
    val listState = rememberLazyListState()
    var widthPx by remember(rowId) { mutableStateOf(0f) }

    val startTx = remember(scrollToLeft, widthPx) { if (scrollToLeft) +widthPx else -widthPx }
    val v = visibility.coerceIn(0f, 1f)

    val tx = lerp(startTx, 0f, easeOutQuint(v))
    val alpha = easeOutCubic(v)
    val scale = lerp(0.985f, 1f, easeOutQuint(v))

    val latestVisibility by rememberUpdatedState(visibility)
    val latestScrollEnabled by rememberUpdatedState(scrollEnabled)

    val baseCount = items.size.coerceAtLeast(1)
    val mid = Int.MAX_VALUE / 2
    val startIndex = remember(rowId, baseCount) { mid - (mid % baseCount) }
    var didCenter by remember(rowId) { mutableStateOf(false) }

    LaunchedEffect(rowId) {
        snapshotFlow { widthPx }
            .distinctUntilChanged()
            .filter { it > 0f }
            .first()

        if (!didCenter && items.isNotEmpty()) {
            didCenter = true
            listState.scrollToItem(startIndex)
        }
    }

    LaunchedEffect(rowId) {
        val scrollSign = if (scrollToLeft) +1f else -1f
        var lastNanos = 0L
        var shouldStop = false

        while (isActive && !shouldStop) {
            val now = withFrameNanos { it }
            if (lastNanos == 0L) {
                lastNanos = now
                continue
            }


            val dtSec = (now - lastNanos) / 1_000_000_000f
            lastNanos = now

            val motion = if (latestScrollEnabled) latestVisibility.coerceIn(0f, 1f) else 0f
            val deltaPx = scrollSign * speedPxPerSec * motion * dtSec

            if (deltaPx != 0f) {
                runCatching { listState.scrollBy(deltaPx) }
                    .onFailure { error ->
                        if (error is IllegalStateException &&
                            error.message?.contains("LayoutNode should be attached to an owner") == true
                        ) {
                            shouldStop = true
                        } else {
                            throw error
                        }
                    }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .onSizeChanged { widthPx = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .graphicsLayer {
                    translationX = tx
                    this.alpha = alpha
                    scaleX = scale
                    scaleY = scale
                }
                .fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                count = Int.MAX_VALUE,
                key = { absoluteIndex -> "row$rowId-$absoluteIndex" }
            ) { absoluteIndex ->
                if (items.isEmpty()) return@items

                val item = items[absoluteIndex % items.size]
                val instanceKey = "row$rowId-$absoluteIndex"
                val hidden = hiddenInstanceKey != null && instanceKey == hiddenInstanceKey

                MediaBoxScatter(
                    item = item,
                    instanceKey = instanceKey,
                    size = itemSize,
                    cornerRadius = itemCornerRadius,
                    hidden = hidden,
                    rootSize = rootSize,
                    scatter = scatter,
                    selectedInstanceKey = selectedInstanceKey,
                    onTapped = onItemTapped
                )
            }
        }
    }
}

@Composable
private fun MediaBoxScatter(
    item: RowItem,
    instanceKey: String,
    size: Dp,
    cornerRadius: Dp,
    hidden: Boolean,
    rootSize: IntSize,
    scatter: Float,
    selectedInstanceKey: String?,
    onTapped: (RowItem, String, Rect) -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    var coords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    var lastBounds by remember { mutableStateOf<Rect?>(null) }
    var lastCenter by remember { mutableStateOf(Offset.Zero) }

    val p = scatter.coerceIn(0f, 1f)
    val moveT = easeInOutCubic(p)

    val centerX = lastCenter.x
    val centerY = lastCenter.y

    val w = rootSize.width.toFloat().coerceAtLeast(1f)
    val h = rootSize.height.toFloat().coerceAtLeast(1f)

    val leftDist = centerX
    val rightDist = w - centerX
    val topDist = centerY
    val bottomDist = h - centerY
    val minDist = minOf(leftDist, rightDist, topDist, bottomDist)

    val itemW = (lastBounds?.width ?: 0f).coerceAtLeast(1f)
    val extra = (itemW * 1.65f) + 220f

    val (dx, dy) = when (minDist) {
        leftDist -> (-(centerX + extra)) to 0f
        rightDist -> (rightDist + extra) to 0f
        topDist -> 0f to (-(centerY + extra))
        else -> 0f to (bottomDist + extra)
    }

    val isSelected = selectedInstanceKey != null && instanceKey == selectedInstanceKey

    val alphaT = (easeInOutCubic(p) * 0.97f).coerceIn(0f, 1f)
    val flyAlpha = if (isSelected) 1f else lerp(1f, 0f, alphaT)
    val flyScale = if (isSelected) 1f else lerp(1f, 0.96f, moveT)

    val bgAlpha by animateFloatAsState(
        targetValue = if (pressed) 0f else 1f,
        animationSpec = tween(
            durationMillis = 220,
            easing = CubicBezierEasing(0.22f, 0f, 0.18f, 1f)
        ),
        label = "cardBgAlpha"
    )

    Box(
        modifier = Modifier
            .size(size)
            .onGloballyPositioned {
                coords = it
                val bounds = it.boundsInRoot()
                lastBounds = bounds
                lastCenter = bounds.center
            }
            .graphicsLayer {
                alpha = if (hidden) 0f else flyAlpha
                if (!isSelected && lastBounds != null) {
                    translationX = dx * moveT
                    translationY = dy * moveT
                    scaleX = flyScale
                    scaleY = flyScale
                }
            }
            .clip(shape)
            .background(Color.White.copy(alpha = bgAlpha))
            .clickable(
                enabled = !hidden,
                indication = null,
                interactionSource = interactionSource
            ) {
                val bounds = coords?.boundsInRoot() ?: lastBounds
                if (bounds != null) onTapped(item, instanceKey, bounds)
            }
    ) {
        item.imageRes?.let { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun HeroOverlayFullscreenBounds(
    selected: SelectedItem,
    progress: Float,
    rootSize: IntSize,
    initialCornerRadius: Dp,
    background: Color,
    onBackdropClick: () -> Unit
) {
    val density = LocalDensity.current

    val t = progress.coerceIn(-0.10f, 1.02f)
    val t01 = progress.coerceIn(0f, 1f)

    val rootW = rootSize.width.toFloat()
    val rootH = rootSize.height.toFloat()
    val side = minOf(rootW, rootH)
    val left = (rootW - side) / 2f
    val top = (rootH - side) / 2f

    val start = selected.startBounds
    val target = Rect(left, top, left + side, top + side)
    val current = lerpRect(start, target, t)

    val cornerPx = with(density) { initialCornerRadius.toPx() }
    val heroBgAlpha = lerp(1f, 0f, easeOutCubic(t01)).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background.copy(alpha = lerp(0f, 1f, easeOutQuint(t01))))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onBackdropClick() }
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(current.left.roundToInt(), current.top.roundToInt()) }
            .size(
                width = with(density) { current.width.toDp() },
                height = with(density) { current.height.toDp() }
            )
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(with(density) { cornerPx.toDp() })
            }
            .background(Color.White.copy(alpha = heroBgAlpha))
    ) {
        selected.item.imageRes?.let { res ->
            Image(
                painter = painterResource(res),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

private fun Rect.offsetBy(dx: Float, dy: Float): Rect =
    Rect(left + dx, top + dy, right + dx, bottom + dy)

private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

private fun lerpRect(a: Rect, b: Rect, t: Float): Rect =
    Rect(
        left = lerp(a.left, b.left, t),
        top = lerp(a.top, b.top, t),
        right = lerp(a.right, b.right, t),
        bottom = lerp(a.bottom, b.bottom, t),
    )

private fun easeOutCubic(t: Float): Float {
    val x = 1f - t
    return 1f - x * x * x
}

private fun easeOutQuint(t: Float): Float {
    val x = 1f - t
    return 1f - x * x * x * x * x
}

private fun easeInOutCubic(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).let { it * it * it } / 2f
}