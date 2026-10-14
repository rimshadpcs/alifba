package com.alifba.alifba.presenation.chapters.layout

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.sp
import com.alifba.alifba.ui_components.widgets.LessonTheme
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
//import androidx.compose.ui.graphics.Color.Companion.White // White is available by default
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.alifba.alifba.ui_components.theme.AlifbaTheme
import com.alifba.alifba.ui_components.widgets.DotLottieView
import com.alifba.alifba.ui_components.theme.darkerNavy
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightPurple
import com.alifba.alifba.ui_components.theme.mediumNavyBlue
import com.alifba.alifba.ui_components.theme.mediumpurple
import com.alifba.alifba.ui_components.theme.navyBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
enum class ChapterNodeShapeType { CIRCLE, BOOK }

@Composable
fun ChapterPathItems(
    lesson: Chapter,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val imageSize = if (isTablet) 100.dp else 70.dp
    val shapeType = if (lesson.chapterType.equals("lesson", ignoreCase = true)) {
        ChapterNodeShapeType.CIRCLE
    } else {
        ChapterNodeShapeType.BOOK
    }
    val density = LocalDensity.current
    val horizontalOffset = with(density) {
        chapterHorizontalOffsetPx(index, isTablet, density).toDp()
    }

    // Press animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "Scale"
    )
    // The current (unlocked, not completed) lesson — the one the Moon stands on — is drawn 1.2x
    // larger so the eye lands on it. Paint-only (graphicsLayer scale around the node's own
    // centre): layout size, the zigzag and the dotted connector (which reads the layout centre)
    // are all unchanged.
    val currentScale = if (shapeType == ChapterNodeShapeType.CIRCLE && !lesson.isLocked && !lesson.isCompleted) 1.2f else 1f
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp, // This creates the "press down" effect
        label = "OffsetY"
    )
    val coroutineScope = rememberCoroutineScope()
    Log.d("LessonPathItems", "Chapter ${lesson.id} - isCompleted: ${lesson.isCompleted}")

    // 2) Main container for each path item
    // OLD: .padding(vertical = 12.dp) — trimmed to tighten the gap between nodes per request.
    Box(
        modifier = Modifier
            .fillMaxWidth().padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(

            modifier = Modifier
                .size(imageSize + 24.dp) // Overall clickable area
                .offset(x = horizontalOffset)
                .then(modifier)
                .scale(scale * currentScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // No ripple effect
                    onClick = {
                        coroutineScope.launch {
                            delay(100)
                            onClick()
                        }
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // This Box is for the main image and its "press down" animation
            if (shapeType == ChapterNodeShapeType.CIRCLE) {
                Box(
                    modifier = Modifier
                        .size(imageSize + 16.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PressableCircleNode(
                        isLocked = lesson.isLocked,
                        isCompleted = lesson.isCompleted,
                        offsetY = offsetY,
                        modifier = Modifier.size(imageSize)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(imageSize + 16.dp)
                        // Sinks 3dp while pressed, like iOS
                        .offset(y = (5.dp - offsetY) * 0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    BookNode(
                        isLocked = lesson.isLocked,
                        isCompleted = lesson.isCompleted,
                        modifier = Modifier.requiredSize(imageSize * 1.14f)
                    )
                }
            }

            // OLD: small white corner badge (top-right, overlapping the node edge) — replaced
            // per request with a mark drawn directly ON the node itself, same treatment as the
            // padlock (large, centered, drawn straight into the node's own Canvas). See the
            // `isCompleted`/`isLocked` branches inside PressableCircleNode (circle/lesson) and
            // the book branch above (Canvas + drawCheckmarkMark) for where these now live.
        }

        // "Start" beside the current chapter, on the open side: right of a left node, left of a
        // right node, and for a centre node on the side the trail leaves from below
        if (!lesson.isLocked && !lesson.isCompleted) {
            val side = if (index % 4 <= 1) 1f else -1f
            val pillWidth = (74 * LessonTheme.scale()).dp
            StartPill(
                width = pillWidth,
                onClick = onClick,
                modifier = Modifier.offset(x = horizontalOffset + (imageSize + pillWidth / 2) * side)
            )
        }
    }
}

@Composable
fun ChapterNodeShape(
    shapeType: ChapterNodeShapeType,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Muted, desaturated tones for the locked state; vivid tones when unlocked.
        val lockedMain = Color(0xFF6E655C)
        val lockedShadow = Color(0xFF564F48)

        when (shapeType) {
            ChapterNodeShapeType.CIRCLE -> {
                val mainColor = if (isLocked) lockedMain else Color(0xFF6178FA) // mediumNavyBlue
                val shadowColor = if (isLocked) lockedShadow else Color(0xFF3D52C7)
                val radius = min(w, h) / 2f
                val center = Offset(w / 2f, h / 2f)

                // "3D bead" look: shadow disc peeking out below, main disc on top, soft highlight.
                drawCircle(color = shadowColor, radius = radius * 0.94f, center = center + Offset(0f, radius * 0.08f))
                drawCircle(color = mainColor, radius = radius * 0.94f, center = center - Offset(0f, radius * 0.02f))
                drawCircle(
                    color = Color.White.copy(alpha = 0.16f),
                    radius = radius * 0.38f,
                    center = center - Offset(radius * 0.28f, radius * 0.32f)
                )
            }

            ChapterNodeShapeType.BOOK -> {
                val mainColor = if (isLocked) lockedMain else Color(0xFFEA4041) // mediumRed
                val shadowColor = if (isLocked) lockedShadow else Color(0xFFB93031)
                val bodyWidth = w * 0.78f
                val bodyHeight = h * 0.62f
                val left = (w - bodyWidth) / 2f
                val top = (h - bodyHeight) / 2f
                val corner = CornerRadius(w * 0.06f)

                // Shadow "pages" peeking out below the cover, then the cover itself.
                drawRoundRect(
                    color = shadowColor,
                    topLeft = Offset(left, top + bodyHeight * 0.1f),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = corner
                )
                drawRoundRect(
                    color = mainColor,
                    topLeft = Offset(left, top),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = corner
                )
                // Spine line near the left edge.
                drawLine(
                    color = Color.Black.copy(alpha = 0.18f),
                    start = Offset(left + bodyWidth * 0.16f, top + h * 0.02f),
                    end = Offset(left + bodyWidth * 0.16f, top + bodyHeight - h * 0.02f),
                    strokeWidth = w * 0.012f
                )
                // Page-edge highlight strip near the right/spine-opposite side.
                if (!isLocked) {
                    drawRoundRect(
                        color = Color(0xFFFFF3DE),
                        topLeft = Offset(left + bodyWidth * 0.86f, top + bodyHeight * 0.1f),
                        size = Size(bodyWidth * 0.08f, bodyHeight * 0.8f),
                        cornerRadius = CornerRadius(w * 0.015f)
                    )
                }
            }
        }

        if (isLocked) {
            drawPadlock(lockedMain)
        }
    }
}


private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPadlock(lockedColor: Color) {
    val w = size.width
    val h = size.height
    val lockWidth = w * 0.26f
    val shackleHeight = lockWidth * 0.62f
    val bodyHeight = lockWidth * 0.62f
    val left = w / 2f - lockWidth / 2f
    val bodyTop = h / 2f - bodyHeight * 0.25f

    drawArc(
        color = Color.White.copy(alpha = 0.92f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(left + lockWidth * 0.12f, bodyTop - shackleHeight),
        size = Size(lockWidth * 0.76f, shackleHeight * 1.7f),
        style = Stroke(width = w * 0.032f, cap = StrokeCap.Round)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.95f),
        topLeft = Offset(left, bodyTop),
        size = Size(lockWidth, bodyHeight),
        cornerRadius = CornerRadius(w * 0.025f)
    )
    drawCircle(
        color = lockedColor,
        radius = w * 0.018f,
        center = Offset(w / 2f, bodyTop + bodyHeight * 0.42f)
    )
}

/** Centered checkmark drawn directly on the node itself (completed state). */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCheckmarkMark(color: Color = Color.White) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.38f, h * 0.52f)
        lineTo(w * 0.47f, h * 0.61f)
        lineTo(w * 0.63f, h * 0.43f)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = w * 0.06f,
            cap = StrokeCap.Round,
            join = androidx.compose.ui.graphics.StrokeJoin.Round
        )
    )
}

// Same colours on iOS (ChapterPathItem.swift)
private val currentMain = Color(0xff789fdf)
private val currentShadow = Color(0xFF4673B0)
private val completedMain = Color(0xFFFDEA60)
private val completedShadow = Color(0xFFF0BB47)
private val lockedMain = Color(0xFFD9CFC3)
private val lockedShadow = Color(0xFFBFB2A3)
private val bookCover = Color(0xFFF2545B)
private val bookEdge = Color(0xFFC93A41)
private val bookPagesShadow = Color(0xFFE6D9D9)
private val bookmarkBlue = Color(0xFF5B7BF5)


/** Rounded check mark drawn in this square (same points as iOS's NodeCheckShape) */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNodeCheck(strokeWidth: Float, color: Color = Color.White) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w * 0.30f, h * 0.52f)
        lineTo(w * 0.44f, h * 0.66f)
        lineTo(w * 0.70f, h * 0.38f)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    )
}

/** Outline padlock drawn in this square: rounded body, shackle arching over (same as iOS's NodeLockShape) */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNodeLock(strokeWidth: Float, color: Color = Color.White) {
    val w = size.width
    val h = size.height
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    drawRoundRect(
        color = color,
        topLeft = Offset(w * 0.28f, h * 0.45f),
        size = Size(w * 0.44f, h * 0.33f),
        cornerRadius = CornerRadius(w * 0.08f),
        style = stroke
    )
    val radius = w * 0.14f
    val cx = w * 0.5f
    val cy = h * 0.34f
    val shackle = Path().apply {
        moveTo(cx - radius, h * 0.45f)
        lineTo(cx - radius, cy)
        arcTo(
            rect = androidx.compose.ui.geometry.Rect(cx - radius, cy - radius, cx + radius, cy + radius),
            startAngleDegrees = 180f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )
        lineTo(cx + radius, h * 0.45f)
    }
    drawPath(path = shackle, color = color, style = stroke)
}

/**
 * Book-shaped node for story (and any other non-lesson) chapters, drawn so it matches iOS exactly:
 * a slightly tilted cover on a darker 3D edge, a white pages strip, a blue bookmark and a round
 * badge (yellow with a check when completed, a soft white disc with a lock when locked; the current
 * story shows the plain cover, with the Start pill beside it).
 */
@Composable
fun BookNode(isLocked: Boolean, isCompleted: Boolean, modifier: Modifier = Modifier) {
    val cover = if (isLocked) lockedMain else bookCover
    val edge = if (isLocked) lockedShadow else bookEdge
    Canvas(modifier = modifier.rotate(-5f)) {
        val w = size.width
        val top = w * 0.02f
        val faceHeight = w * 0.8f
        val radius = CornerRadius(w * 0.14f)
        val pagesRadius = CornerRadius(w * 0.14f * 0.85f)

        // Pages: grey line under a white strip, peeking out below the edge
        drawRoundRect(bookPagesShadow, Offset(w * 0.06f, top + w * 0.10f), Size(w * 0.88f, faceHeight), pagesRadius)
        drawRoundRect(Color.White, Offset(w * 0.06f, top + w * 0.08f), Size(w * 0.88f, faceHeight), pagesRadius)
        // 3D edge, then the cover
        drawRoundRect(edge, Offset(0f, top + w * 0.05f), Size(w, faceHeight), radius)
        drawRoundRect(cover, Offset(0f, top), Size(w, faceHeight), radius)

        // Bookmark ribbon with a V notch
        val bx = w * 0.25f
        val bw = w * 0.13f
        val by = w * 0.66f
        val bh = w * 0.32f
        drawPath(
            Path().apply {
                moveTo(bx, by)
                lineTo(bx + bw, by)
                lineTo(bx + bw, by + bh)
                lineTo(bx + bw / 2f, by + bh - bw * 0.4f)
                lineTo(bx, by + bh)
                close()
            },
            bookmarkBlue
        )

        if (isLocked || isCompleted) {
            val badge = w * 0.58f
            val center = Offset(w / 2f, top + faceHeight / 2f)
            drawCircle(
                color = if (isLocked) Color.White.copy(alpha = 0.3f) else completedMain,
                radius = badge / 2f,
                center = center
            )
            if (isLocked) {
                val box = badge * 0.72f
                translate(center.x - box / 2f, center.y - box / 2f) {
                    scaleDraw(box) { drawNodeLock(strokeWidth = badge * 0.08f) }
                }
            } else {
                translate(center.x - badge / 2f, center.y - badge / 2f) {
                    scaleDraw(badge) { drawNodeCheck(strokeWidth = badge * 0.09f) }
                }
            }
        }
    }
}

/** Runs [block] with the draw size set to a [side] x [side] square at the current origin */
private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.scaleDraw(
    side: Float,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    val previous = drawContext.size
    drawContext.size = Size(side, side)
    block()
    drawContext.size = previous
}

/** White "Start" pill beside the current chapter */
@Composable
fun StartPill(width: Dp, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val height = (36 * k).dp
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .size(width, height + 3.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                SoundEffectManager.playClickSound()
                onClick()
            }
    ) {
        Box(
            modifier = Modifier
                .size(width, height)
                .offset(y = 3.dp)
                .background(LessonTheme.lineEdge, shape)
        )
        Box(
            modifier = Modifier
                .size(width, height)
                .background(Color.White, shape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(
                text = "Start",
                style = LessonTheme.answer().copy(fontSize = (18 * k).sp, color = LessonTheme.primaryEdge)
            )
        }
    }
}

@Composable
fun PressableCircleNode(
    isLocked: Boolean,
    offsetY: Dp,
    // Two fixed states only now — no type-based or position-cycled color logic.
    isCompleted: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (mainColor, shadowColor) = when {
        isLocked -> lockedMain to lockedShadow
        isCompleted -> completedMain to completedShadow
        else -> currentMain to currentShadow // current (unlocked, not completed)
    }


    val isCurrent = !isLocked && !isCompleted

    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(shadowColor)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY)
                .clip(CircleShape)
                .background(mainColor),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLocked -> {
                    Canvas(modifier = Modifier.fillMaxSize(0.6f)) {
                        drawNodeLock(strokeWidth = size.width / 0.6f * 0.055f)
                    }
                }
                isCompleted -> {
                    Canvas(modifier = Modifier.fillMaxSize(0.7f)) {
                        drawNodeCheck(strokeWidth = size.width / 0.7f * 0.065f, color = shadowColor)
                    }
                }
                // isCurrent: pedestal renders empty — the mascot (below) stands on top of it,
                // unclipped, instead of a mark drawn inside the disc.
            }
        }

        if (isCurrent) {
            // Looping waving-mascot animation, standing on the pedestal — see DotLottieView
            // (ui_components/widgets/) for how assets/moonwave/ is wired up and why.
            DotLottieView(
                name = "moonwave",
                viewScale = 1.3f,
                // matchParentSize() locks the LAYOUT size to the disc's own bounds (so it
                // doesn't push sibling nodes apart) — viewScale above then enlarges the PAINT
                // only, free to overflow past the disc's edge since nothing here clips, which
                // is what makes the mascot look like it's standing on top of the pedestal
                // rather than being a same-size sticker clipped inside it.
                modifier = Modifier
                    .matchParentSize()
                    .padding(bottom = offsetY)
                    .offset(y = (-6).dp)
            )
        }
    }
}

/** Programmatically-drawn status mark for the corner badge — a checkmark when completed, a
 * play triangle otherwise (current/unlocked-not-completed). Replaces the old R.drawable.tick /
 * R.drawable.start bitmap icons entirely. */
@Composable
fun NodeStatusMark(isCompleted: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        if (isCompleted) {
            val checkColor = Color(0xFF58C065) // lightCandyGreen
            val path = Path().apply {
                moveTo(w * 0.18f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.76f)
                lineTo(w * 0.84f, h * 0.26f)
            }
            drawPath(
                path = path,
                color = checkColor,
                style = Stroke(width = w * 0.16f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
            )
        } else {
            val playColor = Color(0xFF6178FA) // mediumNavyBlue
            val path = Path().apply {
                moveTo(w * 0.28f, h * 0.18f)
                lineTo(w * 0.28f, h * 0.82f)
                lineTo(w * 0.82f, h * 0.5f)
                close()
            }
            drawPath(path = path, color = playColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChapterPathItemsPreview() {
    AlifbaTheme {
        Column {
            ChapterPathItems(
                lesson = Chapter(1, "Lesson 1", "lesson", isCompleted = false, isLocked = false, isUnlocked = true),
                index = 0,
                onClick = {}
            )
            ChapterPathItems(
                lesson = Chapter(2, "Lesson 2", "lesson", isCompleted = true, isLocked = false, isUnlocked = true),
                index = 1,
                onClick = {}
            )
            ChapterPathItems(
                lesson = Chapter(3, "Story 1", "story", isCompleted = true, isLocked = false, isUnlocked = true),
                index = 2,
                onClick = {}
            )
            ChapterPathItems(
                lesson = Chapter(4, "Lesson 3", "lesson", isCompleted = false, isLocked = true, isUnlocked = false),
                index = 3,
                onClick = {}
            )
        }
    }
}
