package com.alifba.alifba.ui_components.widgets.textFields

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white

@Composable
fun CustomInputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = labelText,
                fontFamily = alifbaFont,
                color = navyBlue.copy(alpha = 0.8f)
            )
            TextFieldDefaults.colors(
                focusedTextColor = navyBlue,
                cursorColor = navyBlue
            )

        },
        leadingIcon = leadingIcon,
        textStyle = TextStyle(
            fontFamily = alifbaFont,
            fontSize = 16.sp,
            color = navyBlue
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = lightNavyBlue.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = lightNavyBlue,
            unfocusedBorderColor = lightNavyBlue.copy(alpha = 0.6f),
            focusedContainerColor = white,
            unfocusedContainerColor = white.copy(alpha = 0.9f),
            focusedLabelColor = navyBlue,
            cursorColor = navyBlue
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onImeAction() },
            onNext = { onImeAction() }
        ),
        singleLine = true
    )
}