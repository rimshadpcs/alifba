package com.alifba.alifba.presenation.chapters

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alifba.alifba.R
import com.alifba.alifba.presenation.chapters.models.Chapter
import com.google.firebase.firestore.FirebaseFirestore

class ChaptersViewModel : ViewModel() {

    private val fireStore = FirebaseFirestore.getInstance()

    private val _lessons = MutableLiveData<List<Chapter>>()
    val lessons: LiveData<List<Chapter>> = _lessons

    fun loadChapters(levelId: String) {
        val levelPath = "lessons/level$levelId/chapters"

        fireStore.collection(levelPath)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && !snapshot.isEmpty) {
                    val chapters = snapshot.documents.map { doc ->
                        Chapter(
                            id = doc.getLong("id")?.toInt() ?: 0,
                            title = doc.getString("title") ?: "",
                            iconResId = R.drawable.start,
                            isCompleted = doc.getBoolean("isCompleted") ?: false,
                            isLocked = doc.getBoolean("isLocked") ?: false,
                            isUnlocked = doc.getBoolean("isUnlocked") ?: true
                        )
                    }
                    _lessons.value = chapters
                } else {
                    Log.e("ChaptersViewModel", "No chapters found for levelID: $levelId")
                }
            }
            .addOnFailureListener {
                Log.e("ChaptersViewModel", "Error loading chapters for levelID: $levelId", it)
            }
    }
}
