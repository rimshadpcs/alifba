package com.alifba.alifba.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

// One review-prompt value moment. Carries the profile whose progress triggered it, since
// milestone state (has this profile's first lesson/streak/badge already fired a prompt
// attempt?) is tracked per profile — a second child profile on the same device reaching their
// own first lesson is a distinct, legitimate value moment, not a repeat of the first child's.
data class ReviewPromptMilestone(val type: Type, val profileId: String) {
    enum class Type { FIRST_LESSON, FIRST_STREAK, FIRST_BADGE }
}

// Fires Google's native In-App Review API (not a custom UI) at real value moments — first
// lesson completed, first 7-day streak, first badge earned — never on app open or during
// onboarding, and never behind a custom "are you enjoying the app?" pre-screen (gating the
// native prompt on a sentiment question violates Google's and Apple's review-manipulation
// guidelines; ChaptersViewModel's milestone detection doesn't do that, and neither does this).
//
// The API itself decides internally whether anything actually displays and enforces its own
// quota — we never see or duplicate that logic. What we DO control is how often we even
// ATTEMPT to ask: each of the three milestones fires our own check at most once ever per
// profile, and actual attempts (regardless of which milestone triggered them) are additionally
// spaced at least a week apart, so a user isn't hit with three review prompts back to back just
// because they happened to complete their first lesson, hit a 7-day streak, and earn a badge
// in the same sitting.
object ReviewPromptManager {
    private const val PREFS_NAME = "ReviewPromptPreferences"
    private const val KEY_LAST_ATTEMPT_AT = "review_prompt_last_attempt_at"
    private const val TAG = "ReviewPromptManager"
    private val cooldownMillis = 7L * 24 * 60 * 60 * 1000

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun firedKey(type: ReviewPromptMilestone.Type, profileId: String) =
        "review_prompt_${type.name}_fired_$profileId"

    private fun hasFired(context: Context, type: ReviewPromptMilestone.Type, profileId: String): Boolean =
        prefs(context).getBoolean(firedKey(type, profileId), false)

    private fun markFired(context: Context, type: ReviewPromptMilestone.Type, profileId: String) {
        prefs(context).edit().putBoolean(firedKey(type, profileId), true).apply()
    }

    private fun isCooldownElapsed(context: Context): Boolean {
        val last = prefs(context).getLong(KEY_LAST_ATTEMPT_AT, 0L)
        if (last == 0L) return true
        return System.currentTimeMillis() - last >= cooldownMillis
    }

    // Called once per (milestone type, profile) ever, regardless of whether the cooldown below
    // ends up allowing an actual API call this time — these are one-shot value moments, not
    // retried later, so a milestone that arrives during another one's cooldown window is simply
    // skipped rather than queued.
    fun notify(context: Context, milestone: ReviewPromptMilestone) {
        if (hasFired(context, milestone.type, milestone.profileId)) return
        markFired(context, milestone.type, milestone.profileId)
        requestReview(context)
    }

    private fun requestReview(context: Context) {
        if (!isCooldownElapsed(context)) return
        val activity = context as? Activity
        if (activity == null) {
            Log.w(TAG, "requestReview called without an Activity context; skipping")
            return
        }
        prefs(context).edit().putLong(KEY_LAST_ATTEMPT_AT, System.currentTimeMillis()).apply()
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    manager.launchReviewFlow(activity, task.result)
                } else {
                    Log.d(TAG, "requestReviewFlow failed: ${task.exception?.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting review: ${e.message}")
        }
    }
}
