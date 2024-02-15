package com.alifba.alifba.presentation.lessonScreens.lessonSegment.DragAndDropLesson

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.ui.components.buttons.DottedBorderButton

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R

@Composable
fun FillInTheBlanksExercise() {
    var selectedWord by remember { mutableStateOf("") }

    val sentencePart1 = "When we want to start doing something important, like reading or beginning a new task, it's recommended to say"
    val sentencePart2 = "to seek Allah's blessing."
    val options = listOf("Alhamdulillah", "Bismillah", "Insha'Allah", "Astaghfirullah")
    val blankPart = if (selectedWord.isEmpty()) "___________" else selectedWord

    // Assuming you have an image in your drawable
    val imageResId = R.drawable.lailatalkingkitchen // Replace with actual image resource

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = "Descriptive text",
            modifier = Modifier
                .fillMaxWidth() // Adjust to fill width or specific size
                .height(200.dp) // Adjust based on your UI needs
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Combining text and blank/dashed line
        Text(
            text = "$sentencePart1 $blankPart. $sentencePart2",
            fontWeight = FontWeight.Bold,
            color = Color.Black,

            modifier = Modifier.padding(
                20.dp
            )

        )

        Spacer(modifier = Modifier.height(20.dp))

        // Options for filling in the blank
        options.forEach { word ->
            DottedBorderButton(
                text = word.uppercase(),
                onClick = { selectedWord = word },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }

    }
}

// Adjust DottedBorderButton as per previous instructions,
// ensuring text color within is set to Color.Black or as desired.

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FillInTheBlanksExercise()
}


