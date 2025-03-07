package com.alifba.alifba.ui_components.widgets.textFields

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

@Composable
fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    labelText,
                    fontFamily = alifbaFont,
                    textAlign = TextAlign.Center
                )
            },
            textStyle = TextStyle(
                fontFamily = alifbaFont,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisibility) {
                    R.drawable.eye_open
                } else {
                    R.drawable.eye_closed
                }

                IconButton(onClick = {
                    passwordVisibility = !passwordVisibility
                },
                    modifier = Modifier.size(24.dp)) {
                    Icon(painter = painterResource(id = image), contentDescription = null)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray,
                //containerColor = Color.Transparent // Matching background
            )
        )
    }
}


@Preview
@Composable
fun PreviewCustomPasswordInputField() {
    PasswordInputField(
        value = "password",
        onValueChange = {},
        labelText = "Password"
    )
}
