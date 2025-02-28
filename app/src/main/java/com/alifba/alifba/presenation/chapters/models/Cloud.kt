package com.alifba.alifba.presenation.chapters.models

import androidx.compose.ui.geometry.Offset

data class Cloud(
    val letter: String,
    var position: Offset,
    val scale: Float,
    val speed: Float,
    val isTargetLetter: Boolean,
    var isActive: Boolean = true,
    var burstAnimation: Float = 0f
)