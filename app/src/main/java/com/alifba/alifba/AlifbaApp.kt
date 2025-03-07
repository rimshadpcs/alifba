package com.alifba.alifba

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class AlifbaApp : Application() {


    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val oneSignalAppId = "1e41357a-fa51-49ce-9470-af0fefbb69b9"
    override fun onCreate() {
        super.onCreate()
        SoundEffectManager.initialize(this)
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
        firebaseAnalytics = Firebase.analytics

        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // OneSignal Initialization
        OneSignal.initWithContext(this, oneSignalAppId)

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


}
