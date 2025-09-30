package com.alifba.alifba.data.cache

import android.util.Log
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheManager @Inject constructor(
    private val storyRepository: StoryRepository,
    private val cacheDao: StoryCacheDao
) {
    companion object {
        private const val TAG = "CacheManager"
    }
    
    private val scope = CoroutineScope(Dispatchers.IO)
    
    fun clearAllCache() {
        scope.launch {
            try {
                Log.d(TAG, "Clearing all cache...")
                storyRepository.clearAllCache()
                Log.d(TAG, "All cache cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing all cache", e)
            }
        }
    }
    
    fun clearStoriesCache() {
        scope.launch {
            try {
                Log.d(TAG, "Clearing stories cache...")
                storyRepository.clearCacheForCategory("stories")
                Log.d(TAG, "Stories cache cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing stories cache", e)
            }
        }
    }
    
    fun clearExpiredCache() {
        scope.launch {
            try {
                Log.d(TAG, "Clearing expired cache...")
                val expiredBefore = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours ago
                cacheDao.clearExpiredStories(expiredBefore)
                Log.d(TAG, "Expired cache cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing expired cache", e)
            }
        }
    }
    
    suspend fun getCacheStats(): CacheStats {
        return try {
            val storiesCount = cacheDao.getCategoryCount("stories")
            val prophetMuhammadCount = cacheDao.getCategoryCount("prophet_muhammad")
            val sahabaCount = cacheDao.getCategoryCount("sahaba")
            val lastStoriesCache = cacheDao.getLastCacheTime("stories") ?: 0
            val lastProphetMuhammadCache = cacheDao.getLastCacheTime("prophet_muhammad") ?: 0
            val lastSahabaCache = cacheDao.getLastCacheTime("sahaba") ?: 0
            
            CacheStats(
                storiesCount = storiesCount,
                prophetMuhammadCount = prophetMuhammadCount,
                sahabaCount = sahabaCount,
                lastStoriesCache = lastStoriesCache,
                lastProphetMuhammadCache = lastProphetMuhammadCache,
                lastSahabaCache = lastSahabaCache
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cache stats", e)
            CacheStats()
        }
    }
}

data class CacheStats(
    val storiesCount: Int = 0,
    val prophetMuhammadCount: Int = 0,
    val sahabaCount: Int = 0,
    val lastStoriesCache: Long = 0,
    val lastProphetMuhammadCache: Long = 0,
    val lastSahabaCache: Long = 0
)