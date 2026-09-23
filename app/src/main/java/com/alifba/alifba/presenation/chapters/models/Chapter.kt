package com.alifba.alifba.presenation.chapters.models

data class Chapter(
    val id: Int,
    val title: String,
    val chapterType: String,
    val isCompleted: Boolean,
    val isLocked: Boolean,
    var isUnlocked: Boolean
)