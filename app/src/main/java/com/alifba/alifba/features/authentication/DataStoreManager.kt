package com.alifba.alifba.features.authentication

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")


@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
        val USER_PROFILE_EXISTS = booleanPreferencesKey("user_profile_exists")
    }

    object ChapterPrefKeys {
        val COMPLETED_CHAPTER = stringPreferencesKey("completed_chapter")
        val UNLOCKED_CHAPTER = stringPreferencesKey("unlocked_chapter")
    }

    // Declare properties before the init block
    val userId: StateFlow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val email: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.EMAIL] }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val password: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.PASSWORD] }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    init {
        coroutineScope.launch {
            userId.collect { id ->
                // Log or handle collected userId
                // Example:
                // Log.d("DataStoreManager", "Collected userId: $id")
            }
        }
    }

    suspend fun saveUserDetails(email: String, password: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.PASSWORD] = password
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun saveUserProfileExists(exists: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PROFILE_EXISTS] = exists
        }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun markCompletedChapters(chapterId: Int, nextChapterId: Int?) {
        dataStore.edit { preferences ->
            // Handle Completed Chapters
            val completedChapters = preferences[ChapterPrefKeys.COMPLETED_CHAPTER]
                ?.split(",")
                ?.map { it.trim() }
                ?.toMutableSet()
                ?: mutableSetOf()
            completedChapters.add(chapterId.toString())
            preferences[ChapterPrefKeys.COMPLETED_CHAPTER] = completedChapters.joinToString(",")

            // Handle Unlocked Chapters
            nextChapterId?.let {
                val unlockedChapters = preferences[ChapterPrefKeys.UNLOCKED_CHAPTER]
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.toMutableSet()
                    ?: mutableSetOf()
                unlockedChapters.add(it.toString())
                preferences[ChapterPrefKeys.UNLOCKED_CHAPTER] = unlockedChapters.joinToString(",")
            }
        }

        // Optional: Add logging to verify updates
        coroutineScope.launch {
            val currentStatus = getChapterStatuses().first()
            Log.d("DataStoreManager", "After marking, Current Chapter Status: $currentStatus")
        }
    }

    fun getChapterStatuses(): Flow<Map<String, Boolean>> {
        return dataStore.data.map { preferences ->
            val completedChapters = preferences[ChapterPrefKeys.COMPLETED_CHAPTER]
                ?.split(",")
                ?.map { it.trim() }
                ?.toSet()
                ?: emptySet()

            val unlockedChapters = preferences[ChapterPrefKeys.UNLOCKED_CHAPTER]
                ?.split(",")
                ?.map { it.trim() }
                ?.toSet()
                ?: emptySet()

            // Completed chapters override unlocked chapters
            unlockedChapters.associateWith { false } + completedChapters.associateWith { true }
        }
    }
    suspend fun clearUserDetails() {
        dataStore.edit { preferences ->
            preferences.clear() // Clear all keys
        }
    }
}
