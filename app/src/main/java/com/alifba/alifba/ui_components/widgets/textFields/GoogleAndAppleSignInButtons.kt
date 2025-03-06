package com.alifba.alifba.ui_components.widgets.textFields

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R

@Composable
fun GoogleAndAppleSignInButtons(

) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Google Sign-In Button
        val alifbaFont = FontFamily(
            Font(R.font.more_sugar_regular, FontWeight.SemiBold)
        )
        val context = LocalContext.current
        Button(
            onClick = { Toast.makeText(
                context,
                "Login feature coming soon, please use normal email signup/signin.",
                Toast.LENGTH_SHORT
            ).show() },

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google), // Add your Google icon here
                contentDescription = "Google Sign In",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Sign in with Google",
                color = Color.Black,
                fontFamily = alifbaFont)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Apple Sign-In Button
        Button(
            onClick = {Toast.makeText(
                context,
                "Login feature coming soon, please use normal email signup/signin.",
                Toast.LENGTH_SHORT
            ).show() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.apple), // Add your Apple icon here
                contentDescription = "Apple Sign In",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Sign in with Apple",
                color = Color.White,
                fontFamily = alifbaFont)
        }
    }
}