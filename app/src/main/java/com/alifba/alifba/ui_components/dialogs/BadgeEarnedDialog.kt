package com.alifba.alifba.ui_components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.data.models.Badge
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.white

@Composable
fun BadgeEarnedSnackBar(badge: Badge, onDismiss: () -> Unit) {
    var isVisible by remember { mutableStateOf(true) }

    val alifbaFont = FontFamily(Font(R.font.more_sugar_regular, FontWeight.SemiBold))
    // Automatically dismiss after 2 seconds
    LaunchedEffect(badge) {
        kotlinx.coroutines.delay(2000)
        isVisible = false
        onDismiss()
    }

    if (isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(lightNavyBlue, shape = MaterialTheme.shapes.medium)
                .padding(16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Badge Image

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        text = "You earned a new Badge \uD83C\uDF89 ",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = white,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = alifbaFont
                    )
                    // Title
                    Text(
                        text = " \uD83C\uDF96\uFE0F${badge.title}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = white,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = alifbaFont
                    )

                    // Description
                    Text(
                        text = "${badge.description} \uD83E\uDD73",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = white
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = alifbaFont
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewBadgeEarnedSnackBar() {
    val sampleBadge = Badge(
        id = "light_starter",
        title = "Light Starter",
        description = "Awarded for completing the first lesson.",
        imageUrl = "https://via.placeholder.com/150" // Placeholder image URL
    )

    BadgeEarnedSnackBar(badge = sampleBadge, onDismiss = {})
}
