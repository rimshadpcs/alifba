package com.alifba.alifba.presenation.stories.domain.repository

import com.alifba.alifba.data.models.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun getStories(): List<Story>
    suspend fun getProphetMuhammadStories(): List<Story>
    suspend fun getSahabaStories(): List<Story>
    suspend fun getWomenAndMothersStories(): List<Story>
    suspend fun getMiraclesStories(): List<Story>

    // Force refresh methods that bypass cache
    suspend fun forceRefreshStories(): List<Story>
    suspend fun forceRefreshProphetMuhammadStories(): List<Story>
    suspend fun forceRefreshSahabaStories(): List<Story>
    suspend fun forceRefreshWomenAndMothersStories(): List<Story>
    suspend fun forceRefreshMiraclesStories(): List<Story>

    // Observable data flows for reactive updates
    fun getStoriesFlow(): Flow<List<Story>>
    fun getProphetMuhammadStoriesFlow(): Flow<List<Story>>
    fun getSahabaStoriesFlow(): Flow<List<Story>>
    fun getWomenAndMothersStoriesFlow(): Flow<List<Story>>
    fun getMiraclesStoriesFlow(): Flow<List<Story>>

    // Cache management
    suspend fun clearAllCache()
    suspend fun clearCacheForCategory(category: String)
}