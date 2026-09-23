package com.alifba.alifba

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.google.firebase.FirebaseApp
import com.onesignal.OneSignal
import com.alifba.alifba.BuildConfig
import com.onesignal.debug.LogLevel as OneSignalLogLevel
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import com.revenuecat.purchases.LogLevel as RevenueCatLogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@HiltAndroidApp
class AlifbaApp : Application() {

    private val oneSignalAppId = "1e41357a-fa51-49ce-9470-af0fefbb69b9"
    override fun onCreate() {
        super.onCreate()
        SoundEffectManager.initialize(this)
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
        val postHogConfig = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST
        )
        if (BuildConfig.DEBUG) {
            postHogConfig.debug = true
            postHogConfig.flushAt = 1
            postHogConfig.flushIntervalSeconds = 1
        }
        PostHogAndroid.setup(this, postHogConfig)
        ensureInstallDate()
        OneSignal.Debug.logLevel = OneSignalLogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, oneSignalAppId)

        // RevenueCat initialization
        setupRevenueCat()
    }

    private fun setupRevenueCat() {
        // Optional: verbose logs while developing
        Purchases.logLevel = RevenueCatLogLevel.DEBUG

        val configuration = PurchasesConfiguration
            .Builder(this, REVENUECAT_API_KEY)
            .build()

        Purchases.configure(configuration)
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lesson Reminder Channel"
            val descriptionText = "Channel for daily lesson reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("lesson_reminder_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    override fun onTerminate() {
        super.onTerminate()
        SoundEffectManager.release()
    }

    companion object {
        // In production, move this to secure config / remote config.
        const val REVENUECAT_API_KEY = "goog_CPYrtNlrcpWAkPFbwraSvvmoENi"
        const val POSTHOG_API_KEY = "phc_ds0oJ2CPsfBkLztyH6Xc3R3Viy5EwTQ763APLkBqL70"
        const val POSTHOG_HOST = "https://us.posthog.com"
    }

    private fun ensureInstallDate() {
        val prefs = getSharedPreferences("analytics_prefs", Context.MODE_PRIVATE)
        if (prefs.contains("install_date")) {
            return
        }
        prefs.edit().putString("install_date", nowIsoUtc()).apply()
    }

    private fun nowIsoUtc(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }

}
