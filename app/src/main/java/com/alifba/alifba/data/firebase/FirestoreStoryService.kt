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
                        durationSeconds = (storyData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "stories",
                        status = (storyData["status"] as? String)?.trim()?.lowercase() ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total stories successfully parsed: ${stories.size}")
            stories.sortedBy { it.id } // Sort by ID for proper order

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching stories", e)
            emptyList()
        }
    }

    suspend fun getProphetMuhammadStories(): List<Story> {
        return try {
            Log.d("FirestoreStoryService", "Starting to fetch Prophet Muhammad stories...")

            // Get all documents from the 'prophet_muhammad_stories' collection
            val querySnapshot = firestore.collection("prophet_muhammad_stories").get().await()
            Log.d("FirestoreStoryService", "Successfully fetched ${querySnapshot.size()} documents from 'prophet_muhammad_stories' collection.")

            // Map the documents to your Story data class
            val stories = querySnapshot.documents.mapNotNull { doc ->
                val storyData = doc.data
                if (storyData != null) {
                    Log.d("FirestoreStoryService", "Processing Prophet Muhammad story document: ${doc.id}")
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
                        durationSeconds = (storyData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "prophet_muhammad",
                        status = (storyData["status"] as? String)?.trim()?.lowercase() ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Prophet Muhammad story document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total Prophet Muhammad stories successfully parsed: ${stories.size}")
            stories.sortedBy { it.id } // Sort by ID for proper order

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching Prophet Muhammad stories", e)
            emptyList()
        }
    }

    suspend fun getSahabaStories(): List<Story> {
        return try {
            Log.d("FirestoreStoryService", "Starting to fetch Sahaba stories...")

            // Get all documents from the 'sahaba_stories' collection
            val querySnapshot = firestore.collection("sahaba_stories").get().await()
            Log.d("FirestoreStoryService", "Successfully fetched ${querySnapshot.size()} documents from 'sahaba_stories' collection.")

            // Map the documents to your Story data class
            val stories = querySnapshot.documents.mapNotNull { doc ->
                val storyData = doc.data
                if (storyData != null) {
                    Log.d("FirestoreStoryService", "Processing Sahaba story document: ${doc.id}")
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
                        durationSeconds = (storyData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "sahaba",
                        status = (storyData["status"] as? String)?.trim()?.lowercase() ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Sahaba story document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total Sahaba stories successfully parsed: ${stories.size}")
            stories.sortedBy { it.id } // Sort by ID for proper order

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching Sahaba stories", e)
            emptyList()
        }
    }

    suspend fun getWomenAndMothersStories(): List<Story> {
        return try {
            Log.d("FirestoreStoryService", "Starting to fetch Women and Mothers stories...")

            // Get all documents from the 'women_and_mothers' collection
            val querySnapshot = firestore.collection("women_and_mothers").get().await()
            Log.d("FirestoreStoryService", "Successfully fetched ${querySnapshot.size()} documents from 'women_and_mothers' collection.")

            // Map the documents to your Story data class
            val stories = querySnapshot.documents.mapNotNull { doc ->
                val storyData = doc.data
                if (storyData != null) {
                    Log.d("FirestoreStoryService", "Processing Women and Mothers story document: ${doc.id}")
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
                        durationSeconds = (storyData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "women_and_mothers",
                        status = (storyData["status"] as? String)?.trim()?.lowercase() ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Women and Mothers story document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total Women and Mothers stories successfully parsed: ${stories.size}")
            stories.sortedBy { it.id } // Sort by ID for proper order

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching Women and Mothers stories", e)
            emptyList()
        }
    }

    suspend fun getMiraclesStories(): List<Story> {
        return try {
            Log.d("FirestoreStoryService", "Starting to fetch Miracles stories...")

            // Get all documents from the 'miracles' collection
            val querySnapshot = firestore.collection("miracles").get().await()
            Log.d("FirestoreStoryService", "Successfully fetched ${querySnapshot.size()} documents from 'miracles' collection.")

            // Map the documents to your Story data class
            val stories = querySnapshot.documents.mapNotNull { doc ->
                val storyData = doc.data
                if (storyData != null) {
                    Log.d("FirestoreStoryService", "Processing Miracles story document: ${doc.id}")
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
                        durationSeconds = (storyData["durationSeconds"] as? Number)?.toLong() ?: 0L,
                        category = storyData["category"] as? String ?: "miracles",
                        status = (storyData["status"] as? String)?.trim()?.lowercase() ?: "free"
                    )
                } else {
                    Log.w("FirestoreStoryService", "Miracles story document ${doc.id} has no data, skipping.")
                    null // This document will be filtered out by mapNotNull
                }
            }

            Log.d("FirestoreStoryService", "Total Miracles stories successfully parsed: ${stories.size}")
            stories.sortedBy { it.id } // Sort by ID for proper order

        } catch (e: Exception) {
            Log.e("FirestoreStoryService", "Error fetching Miracles stories", e)
            emptyList()
        }
    }
}
