package com.alifba.alifba.ui_components.widgets.textFields

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

@Composable
fun GoogleAndAppleSignInButtons(
    onGoogleClick: () -> Unit,
    onAppleClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600
    val buttonHeight = if (isTablet) 72.dp else 60.dp
    val horizontalPadding = if (isTablet) 10.dp else 8.dp
    val cornerRadius = if (isTablet) 36.dp else 32.dp
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val alifbaFont = FontFamily(
            Font(R.font.vag_round, FontWeight.SemiBold)
        )

        // Google Sign-In Button
        Button(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                // heightIn(min:), not a fixed height — this button's content is two lines
                // (title + the "Recommended for multi-device" subtitle), which doesn't fit
                // within a fixed buttonHeight once Material3's default vertical content padding
                // is subtracted, clipping the subtitle's second line. A minimum height keeps the
                // button the same size as before whenever content fits (e.g. Apple's single-line
                // button below), but lets it grow taller here instead of clipping.
                .heightIn(min = buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google Sign In",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Continue with Google",
                    color = Color.White,
                    fontFamily = alifbaFont,
                    fontSize = if (isTablet) 20.sp else 18.sp
                )
                Text(
                    text = "(Recommended for multi-device)",
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = alifbaFont,
                    fontSize = if (isTablet) 12.sp else 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apple Sign-In Button
        Button(
            onClick = onAppleClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                // Same heightIn(min:) as the Google button above, for consistency — this
                // button's content is single-line today so it renders identically to before,
                // but stays safe if a subtitle is ever added here too.
                .heightIn(min = buttonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(cornerRadius)
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple), // Add your Apple icon here
                contentDescription = "Apple Sign In",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue with Apple",
                color = Color.White,
                fontFamily = alifbaFont
            )
        }
    }
}
