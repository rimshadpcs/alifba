package com.alifba.alifba.presenation.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration

import kotlinx.coroutines.launch

import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    data class OnboardingSlide(val image: Int)

    val onboardingSlides = listOf(
        OnboardingSlide(R.drawable.onboardingone),
        OnboardingSlide(R.drawable.onboardingtwo),
        OnboardingSlide(R.drawable.onboardingthree),
        OnboardingSlide(R.drawable.onboardingfour),
        OnboardingSlide(R.drawable.onboardingfive),
        OnboardingSlide(R.drawable.onboardingsix),
        OnboardingSlide(R.drawable.onboardingseven),
        OnboardingSlide(R.drawable.onboardingeight),
        OnboardingSlide(R.drawable.onboardingnine),
        OnboardingSlide(R.drawable.onboardingten)
    )

    var currentSlide by remember { mutableStateOf(0) }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Ensure contrast
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            userScrollEnabled = false
        ) {
            items(onboardingSlides, key = { it.image }) { slide ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(LocalConfiguration.current.screenHeightDp.dp)
                ) {
                    Image(
                        painter = painterResource(id = slide.image),
                        contentDescription = "Onboarding Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }

        if (currentSlide < onboardingSlides.size - 1) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                onboardingSlides.forEachIndexed { index, _ ->
                    val isActive = index == currentSlide
                    val size by animateDpAsState(if (isActive) 16.dp else 8.dp)
                    val color by animateColorAsState(if (isActive) Color.White else Color.Gray)

                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                CommonButton(
                    modifier = Modifier.padding(start = 48.dp, end = 48.dp),
                    onClick = {
                        onComplete()
                    },
                    buttonText = "Start",
                    shadowColor = navyBlue,
                    mainColor = white,
                    textColor = navyBlue
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up button
            Card(
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        coroutineScope.launch {
                            if (currentSlide > 0) {
                                currentSlide--
                                lazyListState.animateScrollToItem(currentSlide)
                            }
                        }
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.uparrowbutton),
                        contentDescription = "Up",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }

            // Down button
            Card(
                modifier = Modifier
                    .size(56.dp)
                    .clickable {
                        coroutineScope.launch {
                            if (currentSlide < onboardingSlides.size - 1) {
                                currentSlide++
                                lazyListState.animateScrollToItem(currentSlide)
                            }
                        }
                    },
                shape = CircleShape,
                colors = CardDefaults.cardColors(Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.downarrowbutton),
                        contentDescription = "Down",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(Color.Black)
                    )
                }
            }
        }
    }
}
