package com.alifba.alifba.data.firebase

import android.util.Log
import com.alifba.alifba.data.models.Story
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreStoryService(
    private val firestore: FirebaseFirestore
) {
    init {
        Log.d("FirestoreStoryService", "FirestoreStoryService initialized")
        Log.d("FirestoreStoryService", "FirebaseFirestore instance: $firestore")
    }
    suspend fun getStories(): List<Story> {
        return try {
            Log.d("FirestoreStoryService", "Starting to fetch stories from flat collection structure...")

            // Get all documents directly from the 'stories' collection
            val querySnapshot = firestore.collection("stories").get().await()
            Log.d("FirestoreStoryService", "Successfully fetched ${querySnapshot.size()} documents from 'stories' collection.")

            // Map the documents to your Story data class
            val stories = querySnapshot.documents.mapNotNull { doc ->
                val storyData = doc.data
                if (storyData != null) {
                    Log.d("FirestoreStoryService", "Processing document: ${doc.id}")
                    Log.d("FirestoreStoryService", "Story data: $storyData")

                    // Create the Story object from the document data
                    Story(
                        id = (storyData["id"] as? Number)?.toInt() ?: 0,
                        documentId = doc.id, // Use the actual document ID from Firestore
                        name = (storyData["name"] as? String) ?: (storyData["name "] as? String) ?: doc.id,
                        background = storyData["background"] as? String ?: "",
                        thumbnail = storyData["thumbnail"] as? String ?: "",
                        audio = storyData["audio"] as? String ?: "",
                        backgroundImage = storyData["backgroundImage"] as? String ?: "",
                        isLocked = storyData["isLocked"] as? Boolean ?: false,
                        isBedtime = storyData["isBedtime"] as? Boolean ?: false,
                        duration = (storyData["duration"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "",
                        status = storyData["status"] as? String ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total stories successfully parsed: ${stories.size}")
            stories

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching stories", e)
            emptyList()
        }
    }
}