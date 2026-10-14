package com.alifba.alifba.presenation.profile

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.presenation.home.layout.profile.AvatarCarousels
import com.alifba.alifba.presenation.login.AllAvatars
import com.alifba.alifba.ui_components.widgets.AnswerCard
import com.alifba.alifba.ui_components.widgets.AnswerState
import com.alifba.alifba.ui_components.widgets.LessonCircleButton
import com.alifba.alifba.ui_components.widgets.LessonTheme

// Pieces of the profile set-up screens (first child after onboarding, and Add a child): the
// buddy picker on the ufo background with the white form sheet overlapping its bottom.

/** How far the white sheet rides up over the background photo */
private val SheetOverlap = 28.dp

/**
 * The buddy picker and, under it, a white sheet with [title] and [content]. Together they fill
 * the screen: the picker takes whatever height the sheet leaves, so a shorter form gets a bigger
 * buddy. The whole thing scrolls when the keyboard is up.
 */
@Composable
fun ProfileSetupLayout(
    title: String,
    selectedAvatarName: MutableState<String>,
    onBack: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val k = LessonTheme.scale()
    var avatarIndex by remember { mutableIntStateOf(0) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Measured before the keyboard padding, so the buddy doesn't jump when it opens
        val fillHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            HeroOverSheet(
                minHeight = fillHeight,
                heroMin = (300 * k).dp,
                overlap = SheetOverlap,
                hero = {
                    ProfileSetupHero(
                        selectedAvatarName = selectedAvatarName,
                        avatarIndex = avatarIndex,
                        onIndexChange = { avatarIndex = it },
                        onBack = onBack
                    )
                },
                sheet = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 560.dp)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(start = LessonTheme.margin, end = LessonTheme.margin, top = 26.dp, bottom = 12.dp)
                        ) {
                            Text(
                                text = title,
                                style = LessonTheme.question().copy(fontSize = (28 * k).sp, textAlign = TextAlign.Center),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 18.dp)
                            )
                            content()
                        }
                    }
                }
            )
        }
    }
}

/** The hero on top and the sheet overlapping its bottom by [overlap]; see ProfileSetupLayout */
@Composable
private fun HeroOverSheet(
    minHeight: Dp,
    heroMin: Dp,
    overlap: Dp,
    hero: @Composable () -> Unit,
    sheet: @Composable () -> Unit
) {
    Layout(content = {
        Box { hero() }
        Box { sheet() }
    }) { measurables, constraints ->
        val width = constraints.maxWidth
        val overlapPx = overlap.roundToPx()
        val sheetPlaceable = measurables[1].measure(Constraints(minWidth = width, maxWidth = width))
        val heroHeight = maxOf(heroMin.roundToPx(), minHeight.roundToPx() - sheetPlaceable.height + overlapPx)
        val heroPlaceable = measurables[0].measure(Constraints.fixed(width, heroHeight))
        layout(width, heroHeight - overlapPx + sheetPlaceable.height) {
            heroPlaceable.place(0, 0)
            sheetPlaceable.place(0, heroHeight - overlapPx)
        }
    }
}

/** The ufo background with the buddy carousel, its name and the dots; the back arrow top left */
@Composable
private fun ProfileSetupHero(
    selectedAvatarName: MutableState<String>,
    avatarIndex: Int,
    onIndexChange: (Int) -> Unit,
    onBack: (() -> Unit)?
) {
    val k = LessonTheme.scale()
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val safeTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        // Below the buddy: its name pill, the dots and the part the sheet covers
        val below = (70 * k).dp + SheetOverlap + 18.dp
        val avatarSize = (maxHeight - safeTop - (24 * k).dp - below).coerceIn((110 * k).dp, (210 * k).dp)

        Image(
            painter = painterResource(id = R.drawable.ufo_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = SheetOverlap + 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarCarousels(
                selectedAvatarName = selectedAvatarName,
                currentAvatarName = selectedAvatarName.value,
                avatarSize = avatarSize,
                onIndexChange = onIndexChange
            )
            AvatarNamePill(
                name = selectedAvatarName.value,
                modifier = Modifier.offset(y = (-6).dp)
            )
            SlidingPageDots(
                count = AllAvatars.size,
                current = avatarIndex,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (onBack != null) {
            LessonCircleButton(
                iconRes = R.drawable.backarrow,
                contentDescription = "Back",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 8.dp),
                onClick = onBack
            )
        }
    }
}

/** The buddy's name in a white pill with a raised edge, sitting on the background photo */
@Composable
fun AvatarNamePill(name: String, modifier: Modifier = Modifier) {
    val k = LessonTheme.scale()
    val shape = RoundedCornerShape(50)
    Box(modifier = modifier.padding(bottom = 4.dp)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .background(LessonTheme.lineEdge, shape)
        )
        Text(
            text = name,
            style = LessonTheme.question().copy(fontSize = (22 * k).sp),
            maxLines = 1,
            modifier = Modifier
                .background(Color.White, shape)
                .padding(horizontal = (22 * k).dp, vertical = (7 * k).dp)
        )
    }
}

/**
 * Five dots for a longer list: the wide one moves through them, and the window slides along
 * when the current item goes past either end (wrapping from the last item back to the first
 * jumps the window back to the start)
 */
@Composable
fun SlidingPageDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.White,
    inactiveColor: Color = Color.White.copy(alpha = 0.6f)
) {
    val visible = minOf(5, count)
    val windowStart = remember { mutableIntStateOf(0) }
    var start = windowStart.intValue
    if (current < start) {
        start = current
    } else if (current >= start + visible) {
        start = current - visible + 1
    }
    start = start.coerceIn(0, maxOf(0, count - visible))
    SideEffect { windowStart.intValue = start }

    Row(
        modifier = modifier.semantics { contentDescription = "${current + 1} of $count" },
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(visible) { slot ->
            val active = start + slot == current
            val width by animateDpAsState(if (active) 22.dp else 8.dp, label = "dotWidth")
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(if (active) activeColor else inactiveColor, CircleShape)
            )
        }
    }
}

private val ProfileAges = (4..13).toList()

/** Ages 4 to 13 in two rows of five; the last one reads "13+" */
@Composable
fun ProfileAgeGrid(selectedAge: Int?, onSelect: (Int) -> Unit) {
    val k = LessonTheme.scale()
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        ProfileAges.chunked(5).forEach { rowAges ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowAges.forEach { value ->
                    Box(modifier = Modifier.weight(1f)) {
                        AnswerCard(
                            state = if (selectedAge == value) AnswerState.Selected else AnswerState.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((52 * k).dp),
                            onClick = { onSelect(value) }
                        ) { labelColor ->
                            Text(
                                text = if (value == ProfileAges.last()) "$value+" else "$value",
                                style = LessonTheme.answer().copy(fontSize = (22 * k).sp, color = labelColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** "How old is Zia?", or "your child" until a name is typed */
@Composable
fun ProfileAgeLabel(childName: String) {
    val k = LessonTheme.scale()
    val name = childName.trim().ifEmpty { "your child" }
    Text(
        text = "How old is $name?",
        style = LessonTheme.answer().copy(fontSize = (17 * k).sp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
    )
}
