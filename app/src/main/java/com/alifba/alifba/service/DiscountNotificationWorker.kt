package com.alifba.alifba.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.content.Context
import com.alifba.alifba.R

// Fires one signup-discount reminder notification. Scheduled (and its delay chosen) by
// DiscountNotificationScheduler — this class only knows how to render and post whichever
// tier it's told about, and how to point its tap target at the matching paywall route.
class DiscountNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val tier = inputData.getString(KEY_TIER) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val slotId = inputData.getString(KEY_SLOT_ID) ?: tier
        val baseRoute = if (tier == TIER_35_OFF) ROUTE_ANNUAL_DISCOUNT_35 else ROUTE_DISCOUNT_PAYWALL
        // Tells MainActivity's NavHost to show the entry transition first — the same routes
        // reached via HomeScreen.kt's standard-paywall-skip navigate() call omit this, which
        // resolves to its false default there.
        val route = "$baseRoute?viaNotification=true"

        val launchIntent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)
            ?.apply {
                putExtra("navigate_to", route)
                putExtra("action", "open_discount_paywall")
                putExtra("tier", tier)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            slotId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.alifbatransround)
            .setContentTitle("Salam!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_PROMO)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(NOTIFICATION_ID_BASE + slotId.hashCode(), notification)
        } catch (e: SecurityException) {
            // Notification permission not granted — nothing to do, this isn't fatal to the work.
            Log.e(TAG, "Cannot post discount notification: ${e.message}")
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "DiscountNotifWorker"

        // Reuses the channel AlifbaApp.kt already creates for lesson/subscription reminders —
        // it's a general reminder channel, not specific to lessons.
        private const val CHANNEL_ID = "lesson_reminder_channel"
        private const val NOTIFICATION_ID_BASE = 3000

        const val KEY_TIER = "tier"
        const val KEY_MESSAGE = "message"
        const val KEY_SLOT_ID = "slot_id"

        const val TIER_20_OFF = "20off"
        const val TIER_35_OFF = "35off"

        const val ROUTE_DISCOUNT_PAYWALL = "discountPaywall"
        const val ROUTE_ANNUAL_DISCOUNT_35 = "annualDiscount35Paywall"
    }
}
