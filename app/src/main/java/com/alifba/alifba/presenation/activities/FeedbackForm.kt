package com.alifba.alifba.presenation.activities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white

@Composable
fun FeedbackForm(onSubmit: (String) -> Unit) {
    var selectedOption by remember { mutableStateOf("") }
    val options = listOf(
        "Islamic-based coloring pages",
        "Educational mini-games",
        "Arabic alphabet tracing/drawing"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Activities Coming Soon - We Need Your Feedback!",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "We are currently developing the new Activities section. To help us prioritize our work, please let us know which of the following features you would find most valuable. Which of these would you like us to build first?",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedOption = option }
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { selectedOption = option },
                    colors = RadioButtonDefaults.colors(selectedColor = navyBlue)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = option, fontSize = 20.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        CommonButton(
            onClick = { onSubmit(selectedOption) },
            buttonText = "Submit Feedback",
            mainColor = lightNavyBlue,
            shadowColor = navyBlue,
            textColor = white,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
