package com.alifba.alifba.ui_components.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alifba.alifba.R

// VAG Round font family with actual bold font file
val VagRoundFontFamily = FontFamily(
    Font(R.font.vag_round, FontWeight.Normal),
    Font(R.font.vag_round_boldd, FontWeight.Bold),
    Font(R.font.vag_round_boldd, FontWeight.ExtraBold),
    Font(R.font.vag_round_boldd, FontWeight.Black)
)

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = VagRoundFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)