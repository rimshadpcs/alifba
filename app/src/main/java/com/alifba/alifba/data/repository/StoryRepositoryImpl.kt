package com.alifba.alifba.data.repository

import android.util.Log
import com.alifba.alifba.data.cache.CachedStoryEntity
import com.alifba.alifba.data.cache.StoryCacheDao
import com.alifba.alifba.data.cache.StorySerializer
import com.alifba.alifba.data.firebase.FirestoreStoryService
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryRepositoryImpl @Inject constructor(
    private val firestoreStoryService: FirestoreStoryService,
    private val cacheDao: StoryCacheDao
) : StoryRepository {
    
    companion object {
        private const val CACHE_TTL = 10 * 60 * 1000L // 10 minutes
        private const val BACKGROUND_REFRESH_THRESHOLD = 5 * 60 * 1000L // 5 minutes
        private const val MIN_ACCEPTABLE_CACHED_COUNT = 5 // trigger refresh if fewer
        private const val CATEGORY_STORIES = "stories"
        private const val CATEGORY_PROPHET_MUHAMMAD = "prophet_muhammad"
        private const val CATEGORY_SAHABA = "sahaba"
        private const val CATEGORY_WOMEN_AND_MOTHERS = "women_and_mothers"
        private const val CATEGORY_MIRACLES = "miracles"
        private const val TAG = "StoryRepositoryImpl"
    }
    
    private val backgroundScope = CoroutineScope(Dispatchers.IO)
    
    // Reactive flows for observing data changes
    private val _storiesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val _prophetMuhammadStoriesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val _sahabaStoriesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val _womenAndMothersStoriesFlow = MutableStateFlow<List<Story>>(emptyList())
    private val _miraclesStoriesFlow = MutableStateFlow<List<Story>>(emptyList())
    
    init {
        Log.d(TAG, "StoryRepositoryImpl initialized with caching support")
        Log.d(TAG, "FirestoreStoryService: $firestoreStoryService")
        Log.d(TAG, "StoryCacheDao: $cacheDao")
        
        // Initialize flows with cached data if available
        backgroundScope.launch {
            try {
                _storiesFlow.value = getCachedStories(CATEGORY_STORIES)
                _prophetMuhammadStoriesFlow.value = getCachedStories(CATEGORY_PROPHET_MUHAMMAD)
                _sahabaStoriesFlow.value = getCachedStories(CATEGORY_SAHABA)
                _womenAndMothersStoriesFlow.value = getCachedStories(CATEGORY_WOMEN_AND_MOTHERS)
                _miraclesStoriesFlow.value = getCachedStories(CATEGORY_MIRACLES)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing cached data flows", e)
            }
        }
    }
    
    override suspend fun getStories(): List<Story> {
        Log.d(TAG, "getStories() called")
        return getCachedOrFresh(CATEGORY_STORIES) {
            firestoreStoryService.getStories()
        }.also { stories ->
            _storiesFlow.value = stories
        }
    }

    override suspend fun getProphetMuhammadStories(): List<Story> {
        Log.d(TAG, "getProphetMuhammadStories() called")
        return getCachedOrFresh(CATEGORY_PROPHET_MUHAMMAD) {
            firestoreStoryService.getProphetMuhammadStories()
        }.also { stories ->
            _prophetMuhammadStoriesFlow.value = stories
        }
    }

    override suspend fun getSahabaStories(): List<Story> {
        Log.d(TAG, "getSahabaStories() called")
        return getCachedOrFresh(CATEGORY_SAHABA) {
            firestoreStoryService.getSahabaStories()
        }.also { stories ->
            _sahabaStoriesFlow.value = stories
        }
    }

    override suspend fun getWomenAndMothersStories(): List<Story> {
        Log.d(TAG, "getWomenAndMothersStories() called")
        return getCachedOrFresh(CATEGORY_WOMEN_AND_MOTHERS) {
            firestoreStoryService.getWomenAndMothersStories()
        }.also { stories ->
            _womenAndMothersStoriesFlow.value = stories
        }
    }

    override suspend fun getMiraclesStories(): List<Story> {
        Log.d(TAG, "getMiraclesStories() called")
        return getCachedOrFresh(CATEGORY_MIRACLES) {
            firestoreStoryService.getMiraclesStories()
        }.also { stories ->
            _miraclesStoriesFlow.value = stories
        }
    }
    
    override suspend fun forceRefreshStories(): List<Story> {
        Log.d(TAG, "forceRefreshStories() called")
        cacheDao.clearCategory(CATEGORY_STORIES)
        return getStories()
    }
    
    override suspend fun forceRefreshProphetMuhammadStories(): List<Story> {
        Log.d(TAG, "forceRefreshProphetMuhammadStories() called")
        cacheDao.clearCategory(CATEGORY_PROPHET_MUHAMMAD)
        return getProphetMuhammadStories()
    }
    
    override suspend fun forceRefreshSahabaStories(): List<Story> {
        Log.d(TAG, "forceRefreshSahabaStories() called")
        cacheDao.clearCategory(CATEGORY_SAHABA)
        return getSahabaStories()
    }

    override suspend fun forceRefreshWomenAndMothersStories(): List<Story> {
        Log.d(TAG, "forceRefreshWomenAndMothersStories() called")
        cacheDao.clearCategory(CATEGORY_WOMEN_AND_MOTHERS)
        return getWomenAndMothersStories()
    }

    override suspend fun forceRefreshMiraclesStories(): List<Story> {
        Log.d(TAG, "forceRefreshMiraclesStories() called")
        cacheDao.clearCategory(CATEGORY_MIRACLES)
        return getMiraclesStories()
    }
    
    override fun getStoriesFlow(): Flow<List<Story>> = _storiesFlow.asStateFlow()
    override fun getProphetMuhammadStoriesFlow(): Flow<List<Story>> = _prophetMuhammadStoriesFlow.asStateFlow()
    override fun getSahabaStoriesFlow(): Flow<List<Story>> = _sahabaStoriesFlow.asStateFlow()
    override fun getWomenAndMothersStoriesFlow(): Flow<List<Story>> = _womenAndMothersStoriesFlow.asStateFlow()
    override fun getMiraclesStoriesFlow(): Flow<List<Story>> = _miraclesStoriesFlow.asStateFlow()
    
    override suspend fun clearAllCache() {
        Log.d(TAG, "clearAllCache() called")
        cacheDao.clearAll()
    }
    
    override suspend fun clearCacheForCategory(category: String) {
        Log.d(TAG, "clearCacheForCategory($category) called")
        cacheDao.clearCategory(category)
    }
    
    private suspend fun getCachedOrFresh(
        category: String,
        fetchFresh: suspend () -> List<Story>
    ): List<Story> {
        val now = System.currentTimeMillis()
        val cached = cacheDao.getStoriesByCategory(category)
        
        // Check if we have valid cached data
        val validCached = cached.filter { now - it.cachedAt < it.ttl }

        return if (validCached.isNotEmpty()) {
            Log.d(TAG, "Using cached data for category: $category (${validCached.size} stories)")

            // Schedule background refresh if data is getting stale OR cached count looks incomplete
            val oldestCacheTime = validCached.minOfOrNull { it.cachedAt } ?: 0
            val shouldRefresh = (now - oldestCacheTime > BACKGROUND_REFRESH_THRESHOLD) || (validCached.size < MIN_ACCEPTABLE_CACHED_COUNT)
            if (shouldRefresh) {
                Log.d(TAG, "Scheduling background refresh for category: $category (shouldRefresh=$shouldRefresh)")
                backgroundRefreshIfNeeded(category, fetchFresh)
            }

            // Return cached data for immediate UI
            validCached.map { StorySerializer.deserializeStory(it.storyJson) }
        } else {
            Log.d(TAG, "Cache miss or expired for category: $category, fetching fresh data")
            
            // Cache miss or expired - fetch fresh data
            try {
                val fresh = fetchFresh()
                cacheStories(category, fresh)
                fresh
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching fresh data for category: $category", e)
                
                // If fresh fetch fails, try to return expired cached data as fallback
                val expiredCached = cached.takeIf { it.isNotEmpty() }
                if (expiredCached != null) {
                    Log.w(TAG, "Using expired cached data as fallback for category: $category")
                    expiredCached.map { StorySerializer.deserializeStory(it.storyJson) }
                } else {
                    throw e // Re-throw if no fallback available
                }
            }
        }
    }
    
    private fun backgroundRefreshIfNeeded(
        category: String,
        fetchFresh: suspend () -> List<Story>
    ) {
        backgroundScope.launch {
            try {
                Log.d(TAG, "Background refresh started for category: $category")
                val fresh = fetchFresh()
                cacheStories(category, fresh)
                
                // Update reactive flows with fresh data
                when (category) {
                    CATEGORY_STORIES -> _storiesFlow.value = fresh
                    CATEGORY_PROPHET_MUHAMMAD -> _prophetMuhammadStoriesFlow.value = fresh
                    CATEGORY_SAHABA -> _sahabaStoriesFlow.value = fresh
                    CATEGORY_WOMEN_AND_MOTHERS -> _womenAndMothersStoriesFlow.value = fresh
                    CATEGORY_MIRACLES -> _miraclesStoriesFlow.value = fresh
                }
                
                Log.d(TAG, "Background refresh completed for category: $category (${fresh.size} stories)")
                
            } catch (e: Exception) {
                Log.e(TAG, "Background refresh failed for category: $category", e)
            }
        }
    }
    
    private suspend fun cacheStories(category: String, stories: List<Story>) {
        try {
            val now = System.currentTimeMillis()
            val cachedEntities = stories.map { story ->
                CachedStoryEntity(
                    documentId = story.documentId,
                    category = category,
                    storyJson = StorySerializer.serializeStory(story),
                    cachedAt = now,
                    ttl = CACHE_TTL
                )
            }
            
            // Clear existing cache for this category and insert new data
            cacheDao.clearCategory(category)
            cacheDao.insertStories(cachedEntities)
            
            Log.d(TAG, "Cached ${stories.size} stories for category: $category")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error caching stories for category: $category", e)
        }
    }
    
    private suspend fun getCachedStories(category: String): List<Story> {
        return try {
            val cached = cacheDao.getStoriesByCategory(category)
            cached.map { StorySerializer.deserializeStory(it.storyJson) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached stories for category: $category", e)
            emptyList()
        }
    }
}
