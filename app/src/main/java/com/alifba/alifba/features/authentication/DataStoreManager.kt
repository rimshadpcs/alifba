package com.alifba.alifba.features.authentication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
        val PREMIUM_ACTIVATION_DATE = stringPreferencesKey("premium_activation_date")
        val APP_OPEN_COUNT = intPreferencesKey("app_open_count")
        val LAST_PAYWALL_OPEN_COUNT = intPreferencesKey("last_paywall_open_count")
        val HAS_SHOWN_HOME_PAYWALL = booleanPreferencesKey("has_shown_home_paywall")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val DISCOUNT_EXPIRY_TIMESTAMP = stringPreferencesKey("discount_expiry_timestamp")
        val HAS_SEEN_DOWNSELL_MODAL = booleanPreferencesKey("has_seen_downsell_modal")
        val STANDARD_PAYWALL_SKIP_COUNT = intPreferencesKey("standard_paywall_skip_count")
    }

    object ChapterPrefKeys {
        val COMPLETED_CHAPTER = stringPreferencesKey("completed_chapter")
        val UNLOCKED_CHAPTER = stringPreferencesKey("unlocked_chapter")
    }

    data class DailyLessonResult(
        val allowed: Boolean,
        val currentCount: Int,
        val maxPerDay: Int
    )

    // Declare properties before the init block
    val userId: StateFlow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val email: StateFlow<String?> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.EMAIL] }
        .stateIn(coroutineScope, SharingStarted.Eagerly, null)

    val isPremium: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.IS_PREMIUM] ?: false }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val appOpenCount: StateFlow<Int> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.APP_OPEN_COUNT] ?: 0 }
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0)

    val lastPaywallOpenCount: StateFlow<Int> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.LAST_PAYWALL_OPEN_COUNT] ?: 0 }
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0)

    val hasShownHomePaywall: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.HAS_SHOWN_HOME_PAYWALL] ?: false }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val discountExpiryTimestamp: StateFlow<Long> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.DISCOUNT_EXPIRY_TIMESTAMP]?.toLongOrNull() ?: 0L }
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0L)

    val hasSeenDownsellModal: StateFlow<Boolean> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.HAS_SEEN_DOWNSELL_MODAL] ?: false }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    val standardPaywallSkipCount: StateFlow<Int> = dataStore.data
        .map { preferences -> preferences[PreferencesKeys.STANDARD_PAYWALL_SKIP_COUNT] ?: 0 }
        .stateIn(coroutineScope, SharingStarted.Eagerly, 0)

    suspend fun setPremium(enabled: Boolean) {
        dataStore.edit { preferences ->
            val wasPremium = preferences[PreferencesKeys.IS_PREMIUM] ?: false

            // When transitioning from non-premium to premium, record the activation date
            if (!wasPremium && enabled) {
                preferences[PreferencesKeys.PREMIUM_ACTIVATION_DATE] = getCurrentDateForLimits()
            }

            preferences[PreferencesKeys.IS_PREMIUM] = enabled
            
            if (enabled) {
                // Clear all discount-related state when user upgrades
                preferences.remove(PreferencesKeys.DISCOUNT_EXPIRY_TIMESTAMP)
                preferences.remove(PreferencesKeys.HAS_SEEN_DOWNSELL_MODAL)
                preferences.remove(PreferencesKeys.STANDARD_PAYWALL_SKIP_COUNT)
            }
        }
    }

    suspend fun setDiscountExpiryTimestamp(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISCOUNT_EXPIRY_TIMESTAMP] = timestamp.toString()
        }
    }

    suspend fun setHasSeenDownsellModal(seen: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEN_DOWNSELL_MODAL] = seen
        }
    }

    suspend fun incrementStandardSkipCount() {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.STANDARD_PAYWALL_SKIP_COUNT] ?: 0
            preferences[PreferencesKeys.STANDARD_PAYWALL_SKIP_COUNT] = current + 1
        }
    }

    suspend fun saveUserDetails(email: String?, userId: String) {
        dataStore.edit { preferences ->
            if (email.isNullOrBlank()) {
                preferences.remove(PreferencesKeys.EMAIL)
            } else {
                preferences[PreferencesKeys.EMAIL] = email
            }
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    suspend fun getOrCreateDeviceId(): String {
        var deviceId = ""
        dataStore.edit { preferences ->
            val existing = preferences[PreferencesKeys.DEVICE_ID]
            if (!existing.isNullOrBlank()) {
                deviceId = existing
            } else {
                val newId = java.util.UUID.randomUUID().toString()
                preferences[PreferencesKeys.DEVICE_ID] = newId
                deviceId = newId
            }
        }
        return deviceId
    }

    suspend fun incrementAppOpenCount(): Int {
        var updated = 0
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.APP_OPEN_COUNT] ?: 0
            val next = current + 1
            preferences[PreferencesKeys.APP_OPEN_COUNT] = next
            updated = next
        }
        return updated
    }

    suspend fun setLastPaywallOpenCount(count: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_PAYWALL_OPEN_COUNT] = count
        }
    }

    suspend fun setHasShownHomePaywall(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SHOWN_HOME_PAYWALL] = shown
        }
    }

    suspend fun resetAppOpenTracking() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.APP_OPEN_COUNT)
            preferences.remove(PreferencesKeys.LAST_PAYWALL_OPEN_COUNT)
            preferences.remove(PreferencesKeys.HAS_SHOWN_HOME_PAYWALL)
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

    private fun dailyLessonDateKey(profileId: String) =
        stringPreferencesKey("daily_lesson_date_$profileId")

    private fun dailyLessonCountKey(profileId: String) =
        intPreferencesKey("daily_lesson_count_$profileId")

    private fun getCurrentDateForLimits(): String {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return dateFormat.format(java.util.Date())
    }

    /**
     * Returns the effective per-day limit for new lessons/stories.
     *
     * - Default is 3 new lessons per day.
     * - On the calendar day when the user first becomes premium, the limit is 5,
     *   allowing them to potentially complete up to 5 new lessons that day.
     */
    suspend fun getEffectiveDailyLessonLimit(): Int {
        val today = getCurrentDateForLimits()
        val activationDate = dataStore.data.first()[PreferencesKeys.PREMIUM_ACTIVATION_DATE]
        val premiumNow = isPremium.value

        return if (premiumNow && activationDate == today) {
            5
        } else {
            3
        }
    }

    /**
     * Adjusts the base daily limit (3 or 5) based on the child's overall progress.
     *
     * We only want to grant the higher 5-per-day limit while the child is still
     * within their "first 5" limited chapters. Once they've completed 5 or more
     * lesson/story chapters lifetime, we clamp the limit back to 3 even on the
     * activation day.
     */
    fun adjustDailyLimitForProgress(
        baseLimit: Int,
        lifetimeLimitedCompletedCount: Int
    ): Int {
        if (baseLimit <= 3) return baseLimit
        return if (lifetimeLimitedCompletedCount >= 5) 3 else baseLimit
    }

    /**
     * Local-only daily lesson limiter.
     *
     * Attempts to consume one "new lesson" slot for the given profile.
     * - Resets the counter when the calendar date changes (device local time).
     * - If the daily count is already at or above maxPerDay, does not increment and returns allowed = false.
     */
    suspend fun tryConsumeDailyLessonSlot(
        profileId: String,
        maxPerDay: Int = 3
    ): DailyLessonResult {
        val today = getCurrentDateForLimits()
        var result = DailyLessonResult(
            allowed = true,
            currentCount = 0,
            maxPerDay = maxPerDay
        )

        dataStore.edit { preferences ->
            val dateKey = dailyLessonDateKey(profileId)
            val countKey = dailyLessonCountKey(profileId)

            val storedDate = preferences[dateKey]
            val storedCount = preferences[countKey] ?: 0

            val effectiveCount = if (storedDate == today) storedCount else 0
            val allowed = effectiveCount < maxPerDay
            val newCount = if (allowed) effectiveCount + 1 else effectiveCount

            preferences[dateKey] = today
            preferences[countKey] = newCount

            result = DailyLessonResult(
                allowed = allowed,
                currentCount = newCount,
                maxPerDay = maxPerDay
            )
        }

        return result
    }

    /**
     * Returns the current daily lesson status for a profile without consuming a slot.
     * Used for gating new lessons based on how many completions have happened today.
     */
    suspend fun getDailyLessonStatus(
        profileId: String,
        maxPerDay: Int = 3
    ): DailyLessonResult {
        val today = getCurrentDateForLimits()
        var result = DailyLessonResult(
            allowed = true,
            currentCount = 0,
            maxPerDay = maxPerDay
        )

        dataStore.edit { preferences ->
            val dateKey = dailyLessonDateKey(profileId)
            val countKey = dailyLessonCountKey(profileId)

            val storedDate = preferences[dateKey]
            val storedCount = preferences[countKey] ?: 0

            val effectiveCount = if (storedDate == today) storedCount else 0

            // Persist reset when day changes
            preferences[dateKey] = today
            preferences[countKey] = effectiveCount

            result = DailyLessonResult(
                allowed = effectiveCount < maxPerDay,
                currentCount = effectiveCount,
                maxPerDay = maxPerDay
            )
        }

        return result
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
            preferences.remove(PreferencesKeys.IS_PREMIUM)
            preferences.remove(ChapterPrefKeys.COMPLETED_CHAPTER)
            preferences.remove(ChapterPrefKeys.UNLOCKED_CHAPTER)
            preferences.remove(TimeZonePreferences.TIME_ZONE_KEY)
            preferences.remove(PreferencesKeys.HAS_SHOWN_HOME_PAYWALL)
            // Note: We don't clear onboarding status as it's managed by OnboardingDataStoreManager
        }
    }
}
