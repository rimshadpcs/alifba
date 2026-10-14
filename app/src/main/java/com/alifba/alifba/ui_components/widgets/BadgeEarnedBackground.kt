package com.alifba.alifba.ui_components.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.hypot
import kotlin.math.max

private val BadgeCream = Color(0xFFFFF6DE)
private val BadgeRay = Color(0xFFFFE7A3)

private class ConfettiStrip(val x: Float, val y: Float, val angle: Float, val color: Color)
private class ConfettiDot(val x: Float, val y: Float, val color: Color)

private class BadgeBackgroundLayout(
    val designWidth: Float,
    val designHeight: Float,
    val rayX: Float,
    val rayY: Float,
    val rayReach: Float,
    val stripWidth: Float,
    val stripHeight: Float,
    val stripRadius: Float,
    val dotRadius: Float,
    val strips: List<ConfettiStrip>,
    val dots: List<ConfettiDot>
)

private val Blue = Color(0xFF5B7BF5)
private val Red = Color(0xFFF2545B)
private val Green = Color(0xFF21B35B)
private val Yellow = Color(0xFFFFC93C)

private val PhoneLayout = BadgeBackgroundLayout(
    designWidth = 390f, designHeight = 844f,
    rayX = 195f, rayY = 330f, rayReach = 1400f,
    stripWidth = 10f, stripHeight = 16f, stripRadius = 3f, dotRadius = 6f,
    strips = listOf(
        ConfettiStrip(60f, 120f, 20f, Blue),
        ConfettiStrip(318f, 150f, -25f, Red),
        ConfettiStrip(300f, 470f, 35f, Green),
        ConfettiStrip(74f, 450f, -15f, Yellow)
    ),
    dots = listOf(
        ConfettiDot(110f, 200f, Red),
        ConfettiDot(290f, 240f, Blue),
        ConfettiDot(96f, 380f, Green)
    )
)

private val TabletLayout = BadgeBackgroundLayout(
    designWidth = 834f, designHeight = 1194f,
    rayX = 417f, rayY = 470f, rayReach = 2100f,
    stripWidth = 15f, stripHeight = 24f, stripRadius = 4.5f, dotRadius = 9f,
    strips = listOf(
        ConfettiStrip(128.3f, 152.8f, 20f, Blue),
        ConfettiStrip(680f, 191f, -25f, Red),
        ConfettiStrip(641.5f, 598.4f, 35f, Green),
        ConfettiStrip(158.2f, 573f, -15f, Yellow)
    ),
    dots = listOf(
        ConfettiDot(235.2f, 254.6f, Red),
        ConfettiDot(620.2f, 305.6f, Blue),
        ConfettiDot(205.3f, 483.8f, Green)
    )
)

/**
 * Warm cream sunburst with a little confetti, drawn natively so it stays crisp at any size.
 * Mirrors the supplied badge_earned_bg_phone / badge_earned_bg_tablet artwork: thin rays around a
 * point above the middle of the screen, confetti keeps its position as a fraction of the screen
 * and its size in dp.
 */
@Composable
fun BadgeEarnedBackground(
    modifier: Modifier = Modifier,
    isTablet: Boolean = LocalConfiguration.current.screenWidthDp > 600
) {
    val layout = if (isTablet) TabletLayout else PhoneLayout

    Canvas(modifier = modifier.fillMaxSize()) {
        val d = density
        val w = size.width
        val h = size.height
        fun x(designX: Float) = designX / layout.designWidth * w
        fun y(designY: Float) = designY / layout.designHeight * h

        drawRect(BadgeCream)

        // Rays: a wedge ~2.5 degrees wide every 30 degrees, long enough to leave the screen
        val reach = max(layout.rayReach * d, hypot(w, h) * 1.2f)
        val half = reach * 30f / 1400f
        val wedge = Path().apply {
            moveTo(0f, 0f)
            lineTo(-half, -reach)
            lineTo(half, -reach)
            close()
        }
        translate(left = x(layout.rayX), top = y(layout.rayY)) {
            for (step in 0 until 12) {
                rotate(degrees = step * 30f, pivot = Offset.Zero) {
                    drawPath(wedge, color = BadgeRay, alpha = 0.7f)
                }
            }
        }

        // Confetti: rounded strips on a slant
        val stripW = layout.stripWidth * d
        val stripH = layout.stripHeight * d
        layout.strips.forEach { strip ->
            val center = Offset(x(strip.x), y(strip.y))
            rotate(degrees = strip.angle, pivot = center) {
                drawRoundRect(
                    color = strip.color,
                    topLeft = Offset(center.x - stripW / 2, center.y - stripH / 2),
                    size = Size(stripW, stripH),
                    cornerRadius = CornerRadius(layout.stripRadius * d)
                )
            }
        }

        // Confetti: round dots
        layout.dots.forEach { dot ->
            drawCircle(
                color = dot.color,
                radius = layout.dotRadius * d,
                center = Offset(x(dot.x), y(dot.y))
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun BadgeEarnedBackgroundPhonePreview() {
    BadgeEarnedBackground(isTablet = false)
}

@Preview(widthDp = 834, heightDp = 1194)
@Composable
private fun BadgeEarnedBackgroundTabletPreview() {
    BadgeEarnedBackground(isTablet = true)
}
