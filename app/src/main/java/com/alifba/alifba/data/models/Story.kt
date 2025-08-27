package com.alifba.alifba.data.models

data class Story(
    val id: Int = 0, // Integer ID from Firestore
    val documentId: String = "", // Document ID for reference
    val name: String = "",
    val background: String = "", // Changed from picture to background
    val thumbnail: String = "",
    val audio: String = "",
    val backgroundImage: String = "",
    val isLocked: Boolean = false,
    val isBedtime: Boolean = false,
    val duration: Long = 0L, // Duration in milliseconds
    val category: String = "",
    val status: String = "" // "free" or "premium"
)