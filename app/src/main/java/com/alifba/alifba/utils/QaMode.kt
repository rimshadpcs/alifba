package com.alifba.alifba.utils

import com.alifba.alifba.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The internal "Alifba QA" build (build type `qa`, never uploaded to Play) for checking new
 * lessons and stories by hand: no sign-in, everything unlocked, and one child profile that lives
 * only on the device. Nothing under Firestore `users/` is read or written in this mode; content
 * collections (chapters, stories, badges) are read as usual. Always false in debug and release.
 */
object QaMode {
    val enabled: Boolean = BuildConfig.QA_MODE

    const val USER_ID = "qa-local-user"
    const val PROFILE_ID = "qa-local-profile"
    const val CHILD_NAME = "QA Tester"

    // Chapters finished in this run, kept in memory only (a fresh launch starts clean), so Home
    // can show them as done without anything being written to Firestore
    private val _completedChapterIds = MutableStateFlow<Set<String>>(emptySet())
    val completedChapterIds: StateFlow<Set<String>> = _completedChapterIds.asStateFlow()

    fun markChapterCompleted(chapterId: String) {
        _completedChapterIds.value = _completedChapterIds.value + chapterId
    }
}
