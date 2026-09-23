package com.alifba.alifba.presenation.chapters.layout

import android.annotation.SuppressLint
import android.graphics.BitmapShader
import android.graphics.Shader
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.ChaptersViewModel
import com.alifba.alifba.presenation.chapters.models.Chapter
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

// OLD: this entire composable used to be built on LazyColumn + a separately-synced background
// Canvas that had to chase the list's own independent scroll state every frame (offset math,
// windowing, coverage-gap fixes, etc). Rearchitected below: the background and the nodes are now
// both children of ONE Modifier.verticalScroll(...) container, so they move together as literally
// the same content — there's no separate scroll position to keep in sync at all.
@Composable
fun LazyChapterColumn(
    lessons: List<Chapter>,
    modifier: Modifier = Modifier,
    onChapterClick: (Int, Chapter) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    val constrainedModifier = if (isTablet) {
        Modifier.widthIn(max = 600.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    // OLD: val itemSpacingDp = if (isTablet) 48.dp else 32.dp
    val itemSpacingDp = if (isTablet) 12.dp else 8.dp

    // Auto-scroll to the current (first incomplete-and-unlocked) lesson on load. Items are laid
    // out top-to-bottom in REVERSE index order (see displayOrder below), so index 0 sits at the
    // bottom — convert the target's real index into its top-down pixel position in that order.
    LaunchedEffect(lessons) {
        if (lessons.isNotEmpty()) {
            delay(300)

            val targetIndex = lessons.indexOfLast { chapter ->
                !chapter.isCompleted && !chapter.isLocked
            }

            if (targetIndex != -1) {
                Log.d("ChapterScroll", "Starting smooth scroll to target index: $targetIndex")
                try {
                    val itemStepPx = with(density) { chapterItemStepDp(isTablet).toPx() }
                    val topDownPosition = lessons.size - 1 - targetIndex
                    val targetScrollPx = (topDownPosition * itemStepPx).roundToInt()
                        .coerceIn(0, scrollState.maxValue)

                    scrollState.animateScrollTo(
                        value = targetScrollPx,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = 20f,
                            visibilityThreshold = 0.5f
                        )
                    )
                } catch (e: Exception) {
                    Log.e("ChapterScroll", "Error during scroll animation", e)
                }
                Log.d("ChapterScroll", "Smooth scroll animation completed")
            }
        }
    }

    // Real center position of each node, relative to the scrollable content Box below — captured
    // via onGloballyPositioned as each item lays out. Since this is a plain (non-lazy) Column,
    // every item is always composed, so every index's true position is always available once
    // layout settles; nothing here is approximated or windowed.
    val itemCenters = remember { mutableStateMapOf<Int, Offset>() }
    var scrollContainerCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { scrollContainerCoordinates = it }
        ) {
            // Background sized via matchParentSize to exactly match the real, measured height
            // of the Column below (not an approximation) — since both are children of the same
            // Box, this is pixel-exact rather than estimated from itemStepPx.
            ChapterPathBackgroundStatic(
                itemCount = lessons.size,
                isTablet = isTablet,
                modifier = Modifier.matchParentSize()
            )

            // Dotted connecting path, drawn from the real captured node centers above — sits
            // above the grass, below the actual node composables (Column below paints over it).
            ChapterPathDots(
                itemCenters = itemCenters,
                modifier = Modifier.matchParentSize()
            )

            // .align(TopCenter): this inner Box has no contentAlignment set (defaults to
            // TopStart), so on tablet — where constrainedModifier caps the Column's width at
            // 600.dp instead of filling the wider screen — the Column was hugging the left edge
            // instead of sitting centered like it does on phone (where fillMaxWidth makes
            // alignment moot). The outer Box's contentAlignment = TopCenter doesn't help here
            // since this Column is a child of the INNER Box, not the outer one.
            Column(
                modifier = constrainedModifier.align(Alignment.TopCenter),
                verticalArrangement = Arrangement.spacedBy(itemSpacingDp)
            ) {
                // Display order is reversed so index 0 ends up at the bottom, matching the old
                // reverseLayout = true visual (index 0 = first/bottom-most chapter).
                val displayOrder = lessons.asReversed()
                displayOrder.forEachIndexed { displayIdx, lesson ->
                    val index = lessons.size - 1 - displayIdx

                    val nodeModifier = Modifier.onGloballyPositioned { coords ->
                        val container = scrollContainerCoordinates ?: return@onGloballyPositioned
                        val centerInOwnSpace = Offset(coords.size.width / 2f, coords.size.height / 2f)
                        itemCenters[index] = container.localPositionOf(coords, centerInOwnSpace)
                    }

                    // nodeModifier is passed straight into ChapterPathItems, which applies it to
                    // the REAL offset-shifted node Box — NOT to a wrapper here, since a wrapper
                    // around ChapterPathItems (which is itself fillMaxWidth) would only ever
                    // capture the wrapper's own un-offset, always-centered position.
                    // OLD: current (unlocked, not completed) node used to be wrapped in
                    // PulsingStartIndicator for a bouncing/pulsing scale animation — removed per
                    // request, current node now renders the same as any other unlocked node.
                    // OLD: also used to compute a per-chapter "completedLessonRank" to cycle the
                    // completed circle node's color — dropped along with that cycling logic;
                    // node color is now just two fixed states (current/completed), no longer
                    // dependent on position or count.
                    ChapterPathItems(
                        lesson = lesson,
                        index = index,
                        onClick = { onChapterClick(index, lesson) },
                        modifier = nodeModifier
                    )
                }

                // index 0 (the first chapter) is the LAST child here, so it sits at the very
                // bottom of the scrollable content. BottomNavigationBar is a plain overlay Box
                // (HomeScreenWithNavigation.kt), not a Scaffold bottomBar — it reserves no layout
                // space of its own — and this Column has no bottom padding, so without this
                // spacer scrollState's max scroll position lands exactly when the content's
                // bottom reaches the true screen edge, which is behind the bar. That leaves the
                // first chapter's node permanently clipped by the bar with no way to scroll it
                // clear.
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
@Composable
fun PulsingStartIndicator(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Scale animation
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Box(
        modifier = modifier
            .scale(scale.value).padding(4.dp)
    ) {
        content()
    }
}

/**
 * Per-item scroll step, in dp, matching the approximation already used by the
 * scroll-to-target LaunchedEffect above (item height + vertical spacing).
 * Kept as a separate helper (rather than editing the existing inline literals)
 * so the background layer and the scroll animation can't silently drift apart.
 */
private fun chapterItemStepDp(isTablet: Boolean) = if (isTablet) 396.dp else 280.dp

/**
 * Single source of truth for the horizontal offset applied to each lesson node. Used by
 * [ChapterPathItems] to actually place the node, and by [ChapterPathDots] to know where to draw
 * the connecting path so the two can't silently drift apart. Returns the offset in px, positive
 * = right of center.
 *
 * OLD: strict left/right alternation every item (index % 2). Replaced with a 4-step repeating
 * pattern — center, left, center, right — matching the reference: index 0 (bottom-most) sits
 * dead-center, 1 swings left, 2 back to center, 3 swings right, then repeats from 4.
 */
fun chapterHorizontalOffsetPx(index: Int, isTablet: Boolean, density: Density): Float {
    val baseOffsetPx = with(density) { (if (isTablet) 120.dp else 80.dp).toPx() }
    return when (index % 4) {
        1 -> -baseOffsetPx
        3 -> baseOffsetPx
        else -> 0f // 0 and 2 -> center
    }
}

/**
 * Continuous, scroll-synced background for [LazyChapterColumn]: a tiled grass texture
 * (alternating every 10 items) with a lamp-post landmark at every 10-item boundary.
 *
 * Because LazyColumn virtualizes its items, we can't draw this "per visible item" without
 * it jumping as items compose/decompose. Instead this draws ONE Canvas sized to the full
 * (virtual) content height, and translates it by the LazyColumn's real scroll offset in
 * pixels, so it tracks 1:1 with no parallax.
 *
 * Coordinate derivation (list uses reverseLayout = true, bottom-anchored):
 *  - Let C = "content coordinate", distance in px from the BOTTOM of the full content
 *    (i.e. from item index 0's bottom edge, since index 0 is laid out first / at the bottom).
 *    Item k occupies C ∈ [k * itemStepPx, (k+1) * itemStepPx).
 *  - Let s = scrolledFromStartPx = listState.firstVisibleItemIndex * itemStepPx +
 *    listState.firstVisibleItemScrollOffset. For a reverseLayout list this is exactly the
 *    number of px scrolled away from the initial bottom-anchored position, regardless of
 *    which direction is visually "up" or "down".
 *  - Viewport (box) maps content coordinate C to screen Y via: screenY = boxHeightPx - C + s.
 *    (Verified: increasing C moves an item toward the top of the screen; increasing s pushes
 *    already-visible low-C items down and off the bottom, which is what scrolling toward
 *    higher-index items should do.)
 *  - The background canvas is a plain top-to-bottom Box of height totalHeightPx, with its
 *    own internal y (0 at canvas top). Canvas-top corresponds to C = totalHeightPx, so its
 *    screen position is boxHeightPx - totalHeightPx + s — that's the single offset we apply
 *    to the whole canvas every frame.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChapterPathBackground(
    itemCount: Int,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    if (itemCount == 0) return

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    val density = LocalDensity.current

    val itemStepPx = with(density) { chapterItemStepDp(isTablet).toPx() }
    val totalHeightPx = itemStepPx * itemCount
    val totalHeightDp = with(density) { totalHeightPx.toDp() }

    // Dead code (see note below) — grass_tile_v1/v2 no longer exist as assets, so these just
    // point at the current single grass_tile to keep this unused function compiling.
    val grassV1 = ImageBitmap.imageResource(id = R.drawable.grass_tile)
    val grassV2 = ImageBitmap.imageResource(id = R.drawable.grass_tile)
    val lampPost = ImageBitmap.imageResource(id = R.drawable.lamp_post)

    val grassPaintV1 = remember(grassV1) {
        Paint().apply {
            asFrameworkPaint().shader =
                BitmapShader(grassV1.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }
    val grassPaintV2 = remember(grassV2) {
        Paint().apply {
            asFrameworkPaint().shader =
                BitmapShader(grassV2.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }

    // OLD (oversized-Canvas approach — sized the Canvas itself to the full scrollable content
    // height, e.g. 15400px for 20 items, and moved the whole thing with Modifier.offset{}.
    // Confirmed via debug probes that this rendered nothing in practice — likely exceeds a
    // safe single-layer/texture size on some devices. Restructured below: keep the Canvas
    // pinned to (a small multiple of) the viewport, and recompute each block's/lamp-post's
    // on-screen position from the current scroll offset every frame, culling anything outside
    // the Canvas bounds instead of pre-laying-out the whole scrollable world at once.)
    // OLD: BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    // OLD:     val boxWidthPx = with(density) { maxWidth.toPx() }
    // OLD:     val viewportHeightPx = with(density) { maxHeight.toPx() }
    // OLD:     val marginPx = with(density) { 24.dp.toPx() }
    // OLD:     val nativeLampWidthPx = lampPost.width.toFloat()
    // OLD:     val nativeLampHeightPx = lampPost.height.toFloat()
    // OLD:     val lampAspect = nativeLampWidthPx / nativeLampHeightPx
    // OLD:     val maxLampWidthPx = boxWidthPx - marginPx * 2f
    // OLD:     val lampWidthPx = min(nativeLampWidthPx, maxLampWidthPx)
    // OLD:     val lampHeightPx = lampWidthPx / lampAspect
    // OLD:     val scrolledFromStartPx by remember(listState) {
    // OLD:         derivedStateOf {
    // OLD:             listState.firstVisibleItemIndex * itemStepPx + listState.firstVisibleItemScrollOffset
    // OLD:         }
    // OLD:     }
    // OLD:     Box(
    // OLD:         modifier = Modifier.fillMaxWidth().requiredHeight(totalHeightDp)
    // OLD:             .offset { IntOffset(0, (viewportHeightPx - totalHeightPx + scrolledFromStartPx).roundToInt()) }
    // OLD:     ) {
    // OLD:         Canvas(modifier = Modifier.fillMaxSize()) { /* drew the whole 15400px world once */ }
    // OLD:     }
    // OLD: }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val boxWidthPx = with(density) { maxWidth.toPx() }
        // The real viewport height (constraints come from the fillMaxSize parent Box in
        // LazyChapterColumn) — NOT the full scrollable content height.
        val viewportHeightPx = with(density) { maxHeight.toPx() }
        // Small buffer above/below the viewport so a lamp post straddling the edge doesn't
        // get hard-clipped mid-image; purely a safety margin, not required for correctness
        // since everything is recomputed from real scroll position every frame anyway.
        val canvasHeightPx = viewportHeightPx * 1.5f
        val canvasHeightDp = with(density) { canvasHeightPx.toDp() }
        // Canvas is vertically centered on the viewport (buffer split evenly above/below).
        val canvasTopOffsetPx = -(canvasHeightPx - viewportHeightPx) / 2f

        // Lamp post: native aspect ratio, capped to screen width with margin, never upscaled.
        val marginPx = with(density) { 24.dp.toPx() }
        val nativeLampWidthPx = lampPost.width.toFloat()
        val nativeLampHeightPx = lampPost.height.toFloat()
        val lampAspect = nativeLampWidthPx / nativeLampHeightPx
        val maxLampWidthPx = boxWidthPx - marginPx * 2f
        val lampWidthPx = min(nativeLampWidthPx, maxLampWidthPx)
        val lampHeightPx = lampWidthPx / lampAspect

        val scrolledFromStartPx by remember(listState) {
            derivedStateOf {
                listState.firstVisibleItemIndex * itemStepPx + listState.firstVisibleItemScrollOffset
            }
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(canvasHeightDp)
                .offset { IntOffset(0, canvasTopOffsetPx.roundToInt()) }
        ) {
            // Content-coordinate (distance from the bottom of the FULL virtual content, i.e.
            // from item index 0's bottom edge) that currently sits at this canvas's own y=0
            // (its top edge). Every block/lamp-post position below is expressed relative to
            // this, recomputed fresh every frame from the live scroll offset — nothing about
            // the world is pre-laid-out or cached, so there's nothing to desync or pop.
            // Derivation: screenY(C) = viewportHeightPx - C + s (screen Y=0 at viewport top).
            // Canvas top sits at screenY = canvasTopOffsetPx = -(canvasHeightPx-viewportHeightPx)/2.
            // Solving screenY(cAtCanvasTop) = canvasTopOffsetPx for C gives the line below.
            val cAtCanvasTop = viewportHeightPx + scrolledFromStartPx +
                (canvasHeightPx - viewportHeightPx) / 2f

            val blockCount = (itemCount + 9) / 10
            for (block in 0 until blockCount) {
                val startIndex = block * 10
                val endIndexExclusive = min(startIndex + 10, itemCount)

                // Block's content-coordinate range (distance from bottom of full content).
                val cLower = startIndex * itemStepPx
                val cUpper = endIndexExclusive * itemStepPx

                // Map to this (fixed-size, always-on-screen) canvas's local top-down coordinates.
                // Local y = cAtCanvasTop - C (higher content-coordinate = further up = smaller y).
                var yTop = cAtCanvasTop - cUpper
                var yBottom = cAtCanvasTop - cLower

                // COVERAGE FIX: the grass "world" only exists for C in [0, totalHeightPx] (the
                // real chapters). Near either end of the list, the canvas's visible window can
                // extend past that range, leaving canvas pixels with nothing drawn on them (the
                // white gap). Since block 0 is the last one drawn for C->0 and the final block is
                // the last one drawn for C->totalHeightPx, stretching just those two to the
                // canvas's own edges acts as a floor/ceiling fill — everything in between is
                // already contiguous, so this only ever affects the two boundary blocks.
                if (block == 0) yBottom = maxOf(yBottom, size.height)
                if (block == blockCount - 1) yTop = minOf(yTop, 0f)

                // Skip blocks that don't intersect this canvas at all — cheap and avoids
                // wasted native-canvas calls; correctness doesn't depend on this check.
                if (yBottom < 0f || yTop > size.height) continue

                // OLD (magenta/cyan debug fill, used to visually confirm scroll-sync + coverage
                // fix — confirmed working, reverted back to the real grass shader fill below):
                // drawRect(
                //     color = if (block % 2 == 0) Color.Magenta else Color.Cyan,
                //     topLeft = Offset(0f, yTop),
                //     size = Size(size.width, yBottom - yTop)
                // )
                val paint = if (block % 2 == 0) grassPaintV1 else grassPaintV2
                drawContext.canvas.nativeCanvas.drawRect(
                    0f, yTop, size.width, yBottom,
                    paint.asFrameworkPaint()
                )
            }

            // OLD: dashed-path drawing used to live here, built on the same itemStepPx-windowed
            // approximation as the grass fill. Moved out to its own composable (ChapterPathConnector,
            // below) that reads real layout positions from listState.layoutInfo.visibleItemsInfo
            // instead — the grass/lamp math above is a different problem (uniform decoration
            // tolerant of approximation) from connecting a line to each node's TRUE position,
            // and conflating the two was why the path connected every other item instead of
            // consecutive ones. See ChapterPathConnector for the replacement.

            // Lamp posts at every 10-item boundary (after item index 9, 19, 29, ...).
            var boundaryEnd = 10
            while (boundaryEnd <= itemCount) {
                val cBoundary = boundaryEnd * itemStepPx
                val yBoundary = cAtCanvasTop - cBoundary

                if (yBoundary >= -lampHeightPx && yBoundary <= size.height + lampHeightPx) {
                    val lampLeft = (size.width - lampWidthPx) / 2f
                    val lampTop = yBoundary - lampHeightPx / 2f

                    drawImage(
                        image = lampPost,
                        dstOffset = IntOffset(lampLeft.roundToInt(), lampTop.roundToInt()),
                        dstSize = IntSize(lampWidthPx.roundToInt(), lampHeightPx.roundToInt())
                    )
                }

                boundaryEnd += 10
            }
        }
    }
}

/**
 * OLD/unused: [ChapterPathBackground] above was built for the LazyColumn + independently-tracked
 * scroll-offset architecture. Now that [LazyChapterColumn] uses a single Modifier.verticalScroll
 * container for both the background and the nodes, none of that scroll-offset math is needed —
 * this draws the grass/lamp-posts ONCE, statically, sized exactly to match the real Column height
 * via Modifier.matchParentSize() at the call site. It scrolls "for free" because it's a sibling
 * inside the same scrollable Box as the Column, not because anything here tracks scroll position.
 */
@Composable
fun ChapterPathBackgroundStatic(
    itemCount: Int,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    if (itemCount == 0) return

    val density = LocalDensity.current
    val itemStepPx = with(density) { chapterItemStepDp(isTablet).toPx() }

    // Single seamless-tiling texture — replaces the old alternating grass_tile_v1/v2 pair (which
    // had visible flower/clover motifs that created an obvious seam wherever the same tile
    // repeated) and the per-block draw loop that alternated between them. This tile has no
    // strong directional pattern, so one BitmapShader-repeated rect across the whole canvas
    // covers it with no seam to hide in the first place.
    val grassTile = ImageBitmap.imageResource(id = R.drawable.grass_tile)
    val lampPost = ImageBitmap.imageResource(id = R.drawable.lamp_post)

    val grassPaint = remember(grassTile) {
        Paint().apply {
            asFrameworkPaint().shader =
                BitmapShader(grassTile.asAndroidBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
        }
    }

    Canvas(modifier = modifier) {
        // Canvas height here is the REAL, exact Column height (matchParentSize), not an
        // itemStepPx estimate.
        val canvasHeightPx = size.height

        drawContext.canvas.nativeCanvas.drawRect(
            0f, 0f, size.width, canvasHeightPx,
            grassPaint.asFrameworkPaint()
        )

        // Subtle depth gradient over the tiled grass texture — the grass tiles are opaque baked
        // PNGs (flowers/clovers painted directly into the pixels, no separate layer to dim), so
        // this is a translucent color wash on top rather than a true background replacement.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFC7E9A0).copy(alpha = 0.35f), Color(0xFF91D17A).copy(alpha = 0.35f)),
                startY = 0f,
                endY = canvasHeightPx
            ),
            size = Size(size.width, canvasHeightPx)
        )

        val marginPx = with(density) { 24.dp.toPx() }
        val nativeLampWidthPx = lampPost.width.toFloat()
        val nativeLampHeightPx = lampPost.height.toFloat()
        val lampAspect = nativeLampWidthPx / nativeLampHeightPx
        val maxLampWidthPx = size.width - marginPx * 2f
        val lampWidthPx = min(nativeLampWidthPx, maxLampWidthPx)
        val lampHeightPx = lampWidthPx / lampAspect

        var boundaryEnd = 10
        while (boundaryEnd <= itemCount) {
            val cBoundary = boundaryEnd * itemStepPx
            val yBoundary = canvasHeightPx - cBoundary

            val lampLeft = (size.width - lampWidthPx) / 2f
            val lampTop = yBoundary - lampHeightPx / 2f

            drawImage(
                image = lampPost,
                dstOffset = IntOffset(lampLeft.roundToInt(), lampTop.roundToInt()),
                dstSize = IntSize(lampWidthPx.roundToInt(), lampHeightPx.roundToInt())
            )

            boundaryEnd += 10
        }
    }
}

/**
 * Beaded/dotted path connecting consecutive lesson nodes — styled after the reference (small
 * evenly-spaced circular dots along a curve, not a dashed stroke), using each node's REAL center
 * position as captured by [LazyChapterColumn] via onGloballyPositioned. Since this Column is not
 * lazy, every item is always composed, so [itemCenters] eventually contains every index — no
 * windowing or scroll-offset math needed; this Canvas is just another sibling in the same
 * scrollable content, so it moves for free with everything else.
 *
 * Styling only for now (color/curve shape) — not yet wired to lock/completed state, so every
 * segment currently uses the same muted tone.
 */
@Composable
fun ChapterPathDots(
    itemCenters: Map<Int, Offset>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val dotRadiusPx = with(this) { 5.dp.toPx() }
        // OLD: val dotOutlineRadiusPx = with(this) { 6.5.dp.toPx() }
        // OLD: val dotSpacingPx = with(this) { 14.dp.toPx() }
        // Widened so each segment (now much shorter, per the tightened item spacing) shows
        // roughly 4 dots instead of a dozen.
        val dotOutlineRadiusPx = with(this) { 6.5.dp.toPx() }
        val dotSpacingPx = with(this) { 22.dp.toPx() }
        // OLD: val dotColor = Color(0xFF3F6B3A) / val dotOutlineColor = Color(0xFFE8F3E0)...
        // Both fill and outline now full white per request — the outline circle (slightly
        // larger, drawn first) just gives the dot a clean, slightly larger silhouette; since
        // it's the same color as the fill there's no visible ring, just a solid white dot.
        val dotColor = Color.White
        val dotOutlineColor = Color.White

        val sortedIndices = itemCenters.keys.sorted()
        for (i in sortedIndices) {
            val next = i + 1
            val current = itemCenters[i] ?: continue
            val nextCenter = itemCenters[next] ?: continue

            // OLD (gentle curve): val controlPoint = Offset((current.x + nextCenter.x) / 2f, (current.y + nextCenter.y) / 2f)
            // Increased curvature: control point sits at the NEXT node's x but the CURRENT
            // node's y (rather than the midpoint), so the path leaves each node roughly level
            // and hooks more sharply into the next one — a noticeably more pronounced curve.
            val controlPoint = Offset(nextCenter.x, current.y)
            val path = Path().apply {
                moveTo(current.x, current.y)
                quadraticTo(controlPoint.x, controlPoint.y, nextCenter.x, nextCenter.y)
            }

            val measure = PathMeasure().apply { setPath(path, false) }
            val length = measure.length
            if (length <= 0f) continue

            var distance = 0f
            while (distance <= length) {
                val position = measure.getPosition(distance)
                drawCircle(color = dotOutlineColor, radius = dotOutlineRadiusPx, center = position)
                drawCircle(color = dotColor, radius = dotRadiusPx, center = position)
                distance += dotSpacingPx
            }
        }
    }
}

/**
 * OLD/unused: superseded by [ChapterPathDots] above, which reads real node centers captured via
 * onGloballyPositioned instead of LazyListState.layoutInfo — this composable pre-dates the
 * LazyColumn -> Column/verticalScroll rearchitecture and no longer has a LazyListState to read.
 */
@Composable
fun ChapterPathConnector(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    Canvas(modifier = modifier.fillMaxSize()) {
        val dashPathEffect = PathEffect.dashPathEffect(
            floatArrayOf(with(this) { 10.dp.toPx() }, with(this) { 8.dp.toPx() })
        )
        val pathStrokeWidthPx = with(this) { 5.dp.toPx() }

        val visibleItems = listState.layoutInfo.visibleItemsInfo
        for (i in 0 until visibleItems.size - 1) {
            val current = visibleItems[i]
            val next = visibleItems[i + 1]

            // Only connect genuinely consecutive chapter indices — guards against drawing a
            // segment across a gap if a non-item element were ever mixed into this LazyColumn.
            if (next.index != current.index + 1) continue

            val yCurrent = current.offset + current.size / 2f
            val yNext = next.offset + next.size / 2f
            val xCurrent = size.width / 2f + chapterHorizontalOffsetPx(current.index, isTablet, this)
            val xNext = size.width / 2f + chapterHorizontalOffsetPx(next.index, isTablet, this)

            // Quadratic bezier so the path visibly curves between the zigzagging node centers
            // instead of a rigid straight segment. Control point pulled toward (xNext, yCurrent)
            // — the curve leaves the current node roughly level, then bends down/up into the
            // next one.
            val path = Path().apply {
                moveTo(xCurrent, yCurrent)
                quadraticTo(xNext, yCurrent, xNext, yNext)
            }

            drawPath(
                path = path,
                color = Color(0xFF8D6E4A).copy(alpha = 0.85f),
                style = Stroke(
                    width = pathStrokeWidthPx,
                    cap = StrokeCap.Round,
                    pathEffect = dashPathEffect
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LazyChapterColumnPreview() {
    val sampleChapters = listOf(
        Chapter(1, "Lesson 1", "lesson", true, false, true),
        Chapter(2, "Lesson 2", "lesson", false, false, true),
        Chapter(3, "Story 1", "story", false, true, false),
        Chapter(4, "Alphabet 1", "alphabet", false, true, false),
        Chapter(5, "Lesson 3", "lesson", false, true, false),
        Chapter(6, "Lesson 4", "lesson", false, true, false),
        Chapter(7, "Lesson 5", "lesson", false, true, false),
        Chapter(8, "Lesson 6", "lesson", false, true, false),
        Chapter(9, "Lesson 7", "lesson", false, true, false),
        Chapter(10, "Lesson 10", "lesson", false, true, false),
        Chapter(11, "Lesson 11", "lesson", false, true, false),
        Chapter(12, "Lesson 12", "lesson", false, true, false)
    )

    AlifbaTheme {
        LazyChapterColumn(
            lessons = sampleChapters,
            onChapterClick = { _, _ -> }
        )
    }
}
