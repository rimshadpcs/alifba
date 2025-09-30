package com.alifba.alifba.presenation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

data class ActivityFeedback(
    val selected_activity: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

class ActivitiesViewModel : ViewModel() {

    private val _hasVoted = MutableStateFlow(false)
    val hasVoted: StateFlow<Boolean> = _hasVoted

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun checkForVote() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _hasVoted.value = true // If no user, act as if they voted to not show the form
            return
        }

        viewModelScope.launch {
            try {
                val doc = firestore.collection("activity_feedback").document(userId).get().await()
                _hasVoted.value = doc.exists()
            } catch (e: Exception) {
                // Handle error, for now, assume they voted to be safe
                _hasVoted.value = true
            }
        }
    }

    fun submitFeedback(activity: String, onComplete: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        val feedback = ActivityFeedback(
            selected_activity = activity
        )

        viewModelScope.launch {
            try {
                firestore.collection("activity_feedback").document(userId).set(feedback).await()
                _hasVoted.value = true
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
