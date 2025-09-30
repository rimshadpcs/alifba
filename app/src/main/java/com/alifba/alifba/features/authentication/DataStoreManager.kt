package com.alifba.alifba.features.authentication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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


@Singleton
class DataStoreManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object PreferencesKeys {
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
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


    suspend fun saveUserDetails(email: String, password: String, userId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.PASSWORD] = password
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    object TimeZonePreferences {
        val TIME_ZONE_KEY = stringPreferencesKey("time_zone")
    }
    suspend fun getTimeZone(context: Context): String? {
        val preferences = context.dataStore.data.first()
        return preferences[TimeZonePreferences.TIME_ZONE_KEY]
    }

    suspend fun saveTimeZone(context: Context, timeZone: String) {
        context.dataStore.edit { preferences ->
            preferences[TimeZonePreferences.TIME_ZONE_KEY] = timeZone
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
            // Clear user-specific data but preserve onboarding status
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.EMAIL)
            preferences.remove(PreferencesKeys.PASSWORD)
            preferences.remove(ChapterPrefKeys.COMPLETED_CHAPTER)
            preferences.remove(ChapterPrefKeys.UNLOCKED_CHAPTER)
            preferences.remove(TimeZonePreferences.TIME_ZONE_KEY)
            // Note: We don't clear onboarding status as it's managed by OnboardingDataStoreManager
        }
    }
}
