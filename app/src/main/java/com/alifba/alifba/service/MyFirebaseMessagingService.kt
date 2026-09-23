package com.alifba.alifba.service

import android.util.Log
import com.alifba.alifba.features.authentication.DataStoreManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var dataStoreManager: DataStoreManager
    @Inject lateinit var firestore: FirebaseFirestore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Called if the FCM token is updated.
     * This happens if the token is compromised,
     * or user reinstalls the app, etc.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New Token: $token")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            saveFcmToken(userId, token)
        }
    }

    /**
     * Called when a message is received while the app is in foreground.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received from: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d("FCM", "Notification Title: ${it.title}")
            Log.d("FCM", "Notification Body: ${it.body}")
            // If you want to show a custom notification, handle here
        }
    }

    /**
     * Helper to update the Firestore document with new FCM token
     */
    private fun saveFcmToken(userId: String, token: String) {
        serviceScope.launch {
            try {
                val deviceId = dataStoreManager.getOrCreateDeviceId()
                firestore.collection("users")
                    .document(userId)
                    .collection("devices")
                    .document(deviceId)
                    .set(
                        mapOf(
                            "deviceId" to deviceId,
                            "fcmToken" to token,
                            "platform" to "Android",
                            "lastSeen" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    )
                    .await()
                Log.d("FCM", "FCM token updated successfully in device doc")
            } catch (e: Exception) {
                Log.e("FCM", "Error updating FCM token: ${e.localizedMessage}")
            }
        }
    }
}
