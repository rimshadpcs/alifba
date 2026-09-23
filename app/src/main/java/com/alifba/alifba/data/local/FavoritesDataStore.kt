package com.alifba.alifba.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.favoritesDataStore: DataStore<Preferences> by preferencesDataStore(name = "favorites")

@Singleton
class FavoritesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.favoritesDataStore

    /**
     * Get favorites key for specific user and profile
     * Format: "favorites_userId_profileIndex"
     */
    private fun getFavoritesKey(userId: String, profileIndex: Int): Preferences.Key<Set<String>> {
        return stringSetPreferencesKey("favorites_${userId}_$profileIndex")
    }

    /**
     * Get favorite story IDs for a specific user profile
     */
    fun getFavorites(userId: String, profileIndex: Int): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            preferences[getFavoritesKey(userId, profileIndex)] ?: emptySet()
        }
    }

    /**
     * Check if a story is favorited
     */
    fun isFavorite(userId: String, profileIndex: Int, storyId: String): Flow<Boolean> {
        return getFavorites(userId, profileIndex).map { favorites ->
            if (favorites.contains(storyId)) true else {
                val plainId = storyId.substringAfter(":", storyId)
                if (plainId != storyId) favorites.contains(plainId) else false
            }
        }
    }

    /**
     * Add a story to favorites
     */
    suspend fun addFavorite(userId: String, profileIndex: Int, storyId: String) {
        val key = getFavoritesKey(userId, profileIndex)
        dataStore.edit { preferences ->
            val currentFavorites = preferences[key] ?: emptySet()
            preferences[key] = currentFavorites + storyId
        }
    }

    /**
     * Remove a story from favorites
     */
    suspend fun removeFavorite(userId: String, profileIndex: Int, storyId: String) {
        val key = getFavoritesKey(userId, profileIndex)
        dataStore.edit { preferences ->
            val currentFavorites = preferences[key] ?: emptySet()
            preferences[key] = currentFavorites - storyId
        }
    }

    /**
     * Toggle favorite status
     */
    suspend fun toggleFavorite(userId: String, profileIndex: Int, storyId: String) {
        val key = getFavoritesKey(userId, profileIndex)
        dataStore.edit { preferences ->
            val currentFavorites = preferences[key] ?: emptySet()
            val plainId = storyId.substringAfter(":", storyId)
            val hasComposite = storyId in currentFavorites
            val hasPlain = plainId != storyId && plainId in currentFavorites

            preferences[key] = when {
                hasComposite || hasPlain -> {
                    // Remove both forms to avoid duplicates
                    currentFavorites - storyId - plainId
                }
                else -> {
                    // Add composite form by default
                    currentFavorites + storyId
                }
            }
        }
    }

    /**
     * Clear all favorites for a specific profile
     */
    suspend fun clearFavorites(userId: String, profileIndex: Int) {
        val key = getFavoritesKey(userId, profileIndex)
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
