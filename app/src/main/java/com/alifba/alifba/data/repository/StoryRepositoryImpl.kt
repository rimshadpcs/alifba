package com.alifba.alifba.data.repository

import com.alifba.alifba.data.firebase.FirestoreStoryService
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository

class StoryRepositoryImpl(
    private val firestoreStoryService: FirestoreStoryService
) : StoryRepository {
    
    init {
        android.util.Log.d("StoryRepositoryImpl", "StoryRepositoryImpl initialized")
        android.util.Log.d("StoryRepositoryImpl", "FirestoreStoryService: $firestoreStoryService")
    }
    
    override suspend fun getStories(): List<Story> {
        android.util.Log.d("StoryRepositoryImpl", "getStories() called")
        return firestoreStoryService.getStories()
    }
}