package com.alifba.alifba.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

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
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("FCM", "FCM token updated successfully in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FCM", "Error updating FCM token: ${e.localizedMessage}")
            }
    }
}
