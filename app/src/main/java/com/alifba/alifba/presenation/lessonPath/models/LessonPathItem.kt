package com.alifba.alifba.presenation.lessonPath.models

data class LessonPathItem(
    val id: Int,
    val title: String,
    var iconResId:Int,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    var isUnlocked: Boolean
)