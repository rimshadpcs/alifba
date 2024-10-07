package com.alifba.alifba.ui_components.widgets.textFields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.OutlinedTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

@Composable
fun CustomInputField(
    value: String,  // Current value of the text field
    onValueChange: (String) -> Unit,  // Function to update the value
    labelText: String  // Label for the input field
) {
    // Load custom font for the text field
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                labelText,  // Use the labelText parameter for hint
                fontFamily = alifbaFont,
                textAlign = TextAlign.Center,
            )
        },
        textStyle = TextStyle(
            fontFamily = alifbaFont,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),  // Rounded corners
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            //containerColor = Color.Transparent // Matching background
        )
    )
}