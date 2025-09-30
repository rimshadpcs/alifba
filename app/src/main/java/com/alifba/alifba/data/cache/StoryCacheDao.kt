package com.alifba.alifba.data.cache

import androidx.room.*

@Dao
interface StoryCacheDao {
    
    @Query("SELECT * FROM cached_stories WHERE category = :category")
    suspend fun getStoriesByCategory(category: String): List<CachedStoryEntity>
    
    @Query("SELECT * FROM cached_stories WHERE documentId = :documentId")
    suspend fun getStoryById(documentId: String): CachedStoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: CachedStoryEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<CachedStoryEntity>)
    
    @Query("DELETE FROM cached_stories WHERE category = :category")
    suspend fun clearCategory(category: String)
    
    @Query("DELETE FROM cached_stories WHERE cachedAt < :expiredBefore")
    suspend fun clearExpiredStories(expiredBefore: Long)
    
    @Query("DELETE FROM cached_stories")
    suspend fun clearAll()
    
    @Query("SELECT COUNT(*) FROM cached_stories WHERE category = :category")
    suspend fun getCategoryCount(category: String): Int
    
    @Query("SELECT MAX(cachedAt) FROM cached_stories WHERE category = :category")
    suspend fun getLastCacheTime(category: String): Long?
}