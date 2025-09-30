package com.alifba.alifba.presenation.stories.domain.repository

import com.alifba.alifba.data.models.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun getStories(): List<Story>
    suspend fun getProphetMuhammadStories(): List<Story>
    suspend fun getSahabaStories(): List<Story>
    
    // Force refresh methods that bypass cache
    suspend fun forceRefreshStories(): List<Story>
    suspend fun forceRefreshProphetMuhammadStories(): List<Story>
    suspend fun forceRefreshSahabaStories(): List<Story>
    
    // Observable data flows for reactive updates
    fun getStoriesFlow(): Flow<List<Story>>
    fun getProphetMuhammadStoriesFlow(): Flow<List<Story>>
    fun getSahabaStoriesFlow(): Flow<List<Story>>
    
    // Cache management
    suspend fun clearAllCache()
    suspend fun clearCacheForCategory(category: String)
}