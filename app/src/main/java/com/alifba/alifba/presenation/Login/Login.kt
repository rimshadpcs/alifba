package com.alifba.alifba.presenation.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton

@Preview
@Composable
fun LoginScreen(){
    Box(modifier = Modifier.fillMaxSize()) {
        Image(painter = painterResource(id = R.drawable.login_background) ,
            contentDescription ="background",
            contentScale = ContentScale.FillHeight,
            modifier = Modifier.fillMaxSize())
        
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(50.dp))
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.cloud),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Foreground Logo
            Image(
                painter = painterResource(id = R.drawable.alifba_logo_transparent),
                contentDescription = "logo",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Sign in",
                shadowColor = navyBlue,
                mainColor = lightNavyBlue,
                textColor = white
            )
            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Sign up",
                shadowColor = navyBlue,
                mainColor = lightNavyBlue,
                textColor = white
            )

        }
    }

}