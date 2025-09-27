package com.alifba.alifba.ui_components.dialogs

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.ui_components.theme.black
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager

@Composable
fun BadgeEarnedSnackBar(badges: List<Badge>, onDismiss: () -> Unit) {
    val alifbaFontBold = FontFamily(Font(R.font.vag_round_boldd, FontWeight.Bold))

    val context = LocalContext.current

    var currentBadgeIndex by remember { mutableIntStateOf(0) }
    val currentBadge = badges[currentBadgeIndex]
    val isLastBadge = currentBadgeIndex == badges.size - 1

    Dialog(onDismissRequest = { }) { // Prevent dismissing by clicking outside
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(white)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Well done text
                Text(
                    text = "Well done!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = alifbaFontBold,
                    color = navyBlue,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Badge image
                Image(
                    painter = rememberAsyncImagePainter(currentBadge.imageUrl),
                    contentDescription = currentBadge.title,
                    modifier = Modifier
                        .size(150.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Badge description
                Text(
                    text = "You won a badge",
                    fontSize = 16.sp,
                    fontFamily = alifbaFontBold,
                    color = black,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons row
                if (badges.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Skip All button
                        CommonButton(
                            buttonText = "Skip All",
                            mainColor = lightNavyBlue,
                            shadowColor = navyBlue,
                            textColor = white,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                SoundEffectManager.playClickSound()
                                onDismiss()
                            }
                        )

                        // Next/OK button
                        CommonButton(
                            buttonText = if (isLastBadge) "OK" else "Next",
                            mainColor = lightNavyBlue,
                            shadowColor = navyBlue,
                            textColor = white,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                SoundEffectManager.playClickSound()
                                if (isLastBadge) {
                                    playCheerSound(context)
                                    onDismiss()
                                } else {
                                    currentBadgeIndex++
                                }
                            }
                        )
                    }
                } else {
                    // Single badge - show only OK button
                    CommonButton(
                        buttonText = "OK",
                        mainColor = lightNavyBlue,
                        shadowColor = navyBlue,
                        textColor = white,
                        onClick = {
                            SoundEffectManager.playClickSound()
                            playCheerSound(context)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

private fun playCheerSound(context: Context) {
    try {
        val mediaPlayer = MediaPlayer.create(context, R.raw.yay)
        mediaPlayer?.apply {
            setOnCompletionListener { release() }
            start()
        }
    } catch (e: Exception) {
        // Handle error silently
    }
}
@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewBadgeEarnedDialog() {
    val sampleBadge = Badge(
        id = "first_lesson_badge",
        title = "First Steps",
        description = "You completed your first lesson! Keep going to learn more Arabic letters and sounds.",
        imageUrl = "https://via.placeholder.com/100/4CAF50/FFFFFF?text=🏆" // Placeholder with trophy emoji
    )

    BadgeEarnedSnackBar(badges = listOf(sampleBadge), onDismiss = {})
}
