package com.alifba.alifba.data.cache

import androidx.room.Entity

@Entity(tableName = "cached_stories", primaryKeys = ["documentId", "category"])
data class CachedStoryEntity(
    val documentId: String,
    val category: String, // "stories", "prophet_muhammad", "sahaba"
    val storyJson: String, // Serialized Story object
    val cachedAt: Long,
    val ttl: Long = 10 * 60 * 1000 // 10 minutes default TTL
)
