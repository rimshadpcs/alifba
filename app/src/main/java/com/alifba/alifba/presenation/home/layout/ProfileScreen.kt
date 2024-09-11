package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.darkSkyBlue
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.lightSkyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.buttons.MCQChoiceButton

@Composable
fun ProfileScreen() {
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_bg),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,

        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(0.dp, 16.dp)
                    .background(darkSkyBlue),
                text = "Salam, Ri",
                fontFamily = alifbaFont,
                fontSize = 38.sp,
                color = white
            )
            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Switch child Profile",
                shadowColor = Color.Gray,
                mainColor = white,
                textColor = navyBlue
            )
            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Change Avatar",
                shadowColor = Color.Gray,
                mainColor = white,
                textColor = navyBlue
            )
            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Change Background",
                shadowColor = Color.Gray,
                mainColor = white,
                textColor = navyBlue
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Image(
            painter = painterResource(id = R.drawable.avatar_profile),
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 60.dp)
                .size(350.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF, widthDp = 320, heightDp = 640)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}