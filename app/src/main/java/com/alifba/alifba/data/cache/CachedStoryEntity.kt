package com.alifba.alifba.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_stories")
data class CachedStoryEntity(
    @PrimaryKey val documentId: String,
    val category: String, // "stories", "prophet_muhammad", "sahaba"
    val storyJson: String, // Serialized Story object
    val cachedAt: Long,
    val ttl: Long = 10 * 60 * 1000 // 10 minutes default TTL
)