package com.alifba.alifba.service

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.alifba.alifba.BuildConfig
import java.util.concurrent.TimeUnit

// TEMP-DEV: set back to false before shipping — when true (and only in a debug build,
// since it's also gated on BuildConfig.DEBUG below), schedules the signup discount
// notifications with short delays instead of 3/14/30 days, so the whole flow — including
// the deep link to the right paywall — can be exercised without waiting real days.
// Mirrors DEV_DISABLE_PAYWALL_AND_GATE's convention elsewhere in the app.
const val DEV_FAST_DISCOUNT_NOTIFICATIONS = false

// Schedules the three-notification signup discount sequence via WorkManager (not
// AlarmManager) specifically because WorkManager persists its queue to disk and
// automatically re-enqueues pending work after an app kill or device reboot — no manual
// boot receiver needed, unlike SubscriptionTrialReminderReceiver's AlarmManager-based
// scheduling elsewhere in this file's package.
object DiscountNotificationScheduler {

    private const val WORK_NAME_3_DAY = "discount_notification_3day"
    private const val WORK_NAME_14_DAY = "discount_notification_14day"
    private const val WORK_NAME_30_DAY = "discount_notification_30day"

    private val ALL_WORK_NAMES = listOf(WORK_NAME_3_DAY, WORK_NAME_14_DAY, WORK_NAME_30_DAY)

    private const val MESSAGE_20_OFF = "Only for you ~20% off on annual subscription"
    private const val MESSAGE_35_OFF = "Only for you ~35% off on annual subscription"

    fun scheduleSignupSequence(context: Context) {
        val useFastDebugDelays = BuildConfig.DEBUG && DEV_FAST_DISCOUNT_NOTIFICATIONS

        val (amount3, unit3) = if (useFastDebugDelays) 30L to TimeUnit.SECONDS else 3L to TimeUnit.DAYS
        val (amount14, unit14) = if (useFastDebugDelays) 90L to TimeUnit.SECONDS else 14L to TimeUnit.DAYS
        val (amount30, unit30) = if (useFastDebugDelays) 150L to TimeUnit.SECONDS else 30L to TimeUnit.DAYS

        schedule(context, WORK_NAME_3_DAY, amount3, unit3, DiscountNotificationWorker.TIER_20_OFF, MESSAGE_20_OFF)
        schedule(context, WORK_NAME_14_DAY, amount14, unit14, DiscountNotificationWorker.TIER_20_OFF, MESSAGE_20_OFF)
        schedule(context, WORK_NAME_30_DAY, amount30, unit30, DiscountNotificationWorker.TIER_35_OFF, MESSAGE_35_OFF)
    }

    // Called from SubscriptionViewModel.setPremium(true) — the single place "the user is now
    // premium" gets recorded regardless of which paywall or restore path got them there — so
    // a purchase before day 3/14/30 always pulls all three pending notifications, not just
    // whichever tier they happened to buy through.
    fun cancelAll(context: Context) {
        val workManager = WorkManager.getInstance(context)
        ALL_WORK_NAMES.forEach { workManager.cancelUniqueWork(it) }
    }

    private const val WORK_NAME_DEBUG_TEST = "discount_notification_debug_test"

    // DEBUG-only manual test trigger — fires a single notification for the given tier ~5s
    // from now, independent of the real signup-triggered sequence and its own
    // DEV_FAST_DISCOUNT_NOTIFICATIONS flag. Wired to debug-only buttons on the profile
    // selection screen so the whole flow (tilde rendering, tap routing) can be checked in
    // one tap without waiting on a signup or flipping that flag.
    fun debugFireTestNotification(context: Context, tier: String) {
        if (!BuildConfig.DEBUG) return
        val message = if (tier == DiscountNotificationWorker.TIER_35_OFF) MESSAGE_35_OFF else MESSAGE_20_OFF
        schedule(context, WORK_NAME_DEBUG_TEST, 5L, TimeUnit.SECONDS, tier, message)
    }

    private fun schedule(
        context: Context,
        workName: String,
        delayAmount: Long,
        delayUnit: TimeUnit,
        tier: String,
        message: String
    ) {
        val inputData = Data.Builder()
            .putString(DiscountNotificationWorker.KEY_TIER, tier)
            .putString(DiscountNotificationWorker.KEY_MESSAGE, message)
            // Distinct per scheduling slot (3-day/14-day/30-day/debug-test), not just per tier —
            // the 3-day and 14-day notifications share the same tier, and without this they'd
            // collide on the same notification ID/PendingIntent request code, so the second one
            // would silently replace the first in the tray instead of alerting again.
            .putString(DiscountNotificationWorker.KEY_SLOT_ID, workName)
            .build()

        val request = OneTimeWorkRequestBuilder<DiscountNotificationWorker>()
            .setInitialDelay(delayAmount, delayUnit)
            .setInputData(inputData)
            .build()

        // REPLACE guards against double-scheduling if scheduleSignupSequence is ever
        // accidentally called twice for the same user (e.g. a retried signup callback).
        WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }
}
