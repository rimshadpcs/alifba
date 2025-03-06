package com.alifba.alifba.ui_components.widgets.buttons

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alifba.alifba.R
import com.alifba.alifba.ui_components.theme.lightNavyBlue
import com.alifba.alifba.ui_components.theme.navyBlue
import com.alifba.alifba.ui_components.theme.white
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundEffectManager {
    private var soundPool: SoundPool? = null
    private var soundId: Int = 0
    private var isInitialized = false
    private var volume: Float = 1.0f
    private lateinit var prefs: SharedPreferences

    // Using proper property declaration
    private var _isSoundEnabled by mutableStateOf(true)
    val isSoundEnabled: Boolean
        get() = _isSoundEnabled

    fun initialize(context: Context) {
        if (isInitialized) return

        // Initialize SharedPreferences
        prefs = context.getSharedPreferences("sound_settings", Context.MODE_PRIVATE)
        _isSoundEnabled = prefs.getBoolean("sound_enabled", true)

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(attributes)
            .build()

        soundId = soundPool?.load(context, R.raw.buttonclick, 1) ?: 0
        isInitialized = true
    }

    fun toggleSound(enabled: Boolean) {
        _isSoundEnabled = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }


    fun playClickSound() {
        if (_isSoundEnabled) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isInitialized = false
    }
}

@Composable
fun CommonButton(
    onClick: () -> Unit,
    buttonText: String,
    shadowColor: Color,
    mainColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Initialize sound manager when the button is first created
    LaunchedEffect(Unit) {
        SoundEffectManager.initialize(context)
    }

    val alifbaFont = FontFamily(
        Font(R.font.more_sugar_regular, FontWeight.SemiBold)
    )
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val coroutineScope = rememberCoroutineScope()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 5.dp,
        animationSpec = spring(),
        label = ""
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(shadowColor)
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = offsetY)
                .clip(RoundedCornerShape(32.dp))
                .background(mainColor)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            SoundEffectManager.playClickSound()
                            delay(100)
                            onClick()
                        }
                    },
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = buttonText,
                fontFamily = alifbaFont,
                color = textColor
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewCommonButton() {
    CommonButton(
        onClick = {},
        buttonText = "Next",
        mainColor = lightNavyBlue,
        shadowColor = navyBlue,
        textColor = white
    )
}
