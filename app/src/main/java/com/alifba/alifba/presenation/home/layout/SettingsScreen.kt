package com.alifba.alifba.presenation.home.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.darkPink
import com.alifba.alifba.ui_components.theme.lightPink
import com.alifba.alifba.ui_components.theme.white
import com.alifba.alifba.ui_components.widgets.buttons.CommonButton
import com.alifba.alifba.ui_components.widgets.texts.SettingsButton


@Preview
@Composable
fun SettingsScreen(){
    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    Box(
        modifier = Modifier
    ) {
        Column(
            Modifier
                .background(white)
                .fillMaxSize()
        )
        {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            ) {

                Text(
                    text = "Settings",
                    style = LocalTextStyle.current.merge(
                        TextStyle(
                            lineHeight = 1.5.em,
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.None
                            )
                        )
                    ),
                    color = Color.Gray,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = alifbaFont
                )
            }

            SettingsButton(
                text = "Account", onClick = {/*TODO*/}
            )
            SettingsButton(
                text = "Notifications", onClick = {/*TODO*/}
            )
            SettingsButton(
                text = "Privacy Settings", onClick = {/*TODO*/}
            )
            SettingsButton(
                text = "Report Bugs", onClick = {/*TODO*/}
            )
            SettingsButton(
                text = "Send Feedback", onClick = {/*TODO*/}
            )



        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            CommonButton(
                onClick = { /*TODO*/ },
                buttonText = "Logout",
                shadowColor = darkPink,
                mainColor = lightPink,
                textColor = white
            )
        }
    }
}