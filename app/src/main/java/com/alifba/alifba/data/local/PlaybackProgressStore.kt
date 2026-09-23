package com.alifba.alifba.data.local

import android.content.Context
import android.content.SharedPreferences

data class PlaybackProgress(
    val position: Long = 0L,
    val duration: Long = 0L,
    val completed: Boolean = false,
    val updatedAt: Long = 0L
)

class PlaybackProgressStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("playback_progress", Context.MODE_PRIVATE)

    fun getProgress(documentId: String): PlaybackProgress {
        if (documentId.isEmpty()) return PlaybackProgress()
        val position = prefs.getLong("${documentId}_pos", 0L)
        val duration = prefs.getLong("${documentId}_dur", 0L)
        val completed = prefs.getBoolean("${documentId}_done", false)
        val updatedAt = prefs.getLong("${documentId}_ts", 0L)
        return PlaybackProgress(position, duration, completed, updatedAt)
    }

    fun setProgress(documentId: String, position: Long, duration: Long, completed: Boolean) {
        if (documentId.isEmpty()) return
        prefs.edit()
            .putLong("${documentId}_pos", position)
            .putLong("${documentId}_dur", duration)
            .putBoolean("${documentId}_done", completed)
            .putLong("${documentId}_ts", System.currentTimeMillis())
            .apply()
    }

    fun setLastPlayed(documentId: String) {
        if (documentId.isEmpty()) return
        prefs.edit().putString("last_played_id", documentId).apply()
    }

    fun getLastPlayed(): String? = prefs.getString("last_played_id", null)
}

