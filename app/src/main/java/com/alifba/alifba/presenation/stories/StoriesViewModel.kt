package com.alifba.alifba.presenation.stories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import com.alifba.alifba.data.local.FavoritesDataStore
import com.alifba.alifba.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.posthog.PostHog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val favoritesDataStore: FavoritesDataStore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _prophetMuhammadStories = MutableStateFlow<List<Story>>(emptyList())
    val prophetMuhammadStories: StateFlow<List<Story>> = _prophetMuhammadStories.asStateFlow()

    private val _sahabaStories = MutableStateFlow<List<Story>>(emptyList())
    val sahabaStories: StateFlow<List<Story>> = _sahabaStories.asStateFlow()

    private val _womenAndMothersStories = MutableStateFlow<List<Story>>(emptyList())
    val womenAndMothersStories: StateFlow<List<Story>> = _womenAndMothersStories.asStateFlow()

    private val _miraclesStories = MutableStateFlow<List<Story>>(emptyList())
    val miraclesStories: StateFlow<List<Story>> = _miraclesStories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isProphetMuhammadLoading = MutableStateFlow(true)
    val isProphetMuhammadLoading: StateFlow<Boolean> = _isProphetMuhammadLoading.asStateFlow()

    private val _isSahabaLoading = MutableStateFlow(true)
    val isSahabaLoading: StateFlow<Boolean> = _isSahabaLoading.asStateFlow()

    private val _isWomenAndMothersLoading = MutableStateFlow(true)
    val isWomenAndMothersLoading: StateFlow<Boolean> = _isWomenAndMothersLoading.asStateFlow()

    private val _isMiraclesLoading = MutableStateFlow(true)
    val isMiraclesLoading: StateFlow<Boolean> = _isMiraclesLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        Log.d("StoriesViewModel", "StoriesViewModel initialized")
        Log.d("StoriesViewModel", "StoryRepository: $storyRepository")
        loadStories()
        loadProphetMuhammadStories()
        loadSahabaStories()
        loadWomenAndMothersStories()
        loadMiraclesStories()
    }
    
    private fun loadStories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("StoriesViewModel", "Loading stories from Firestore")
                val fetchedStories = storyRepository.getStories()
                
                Log.d("StoriesViewModel", "Fetched ${fetchedStories.size} stories")
                fetchedStories.forEach { story ->
                    Log.d("StoriesViewModel", "Story: ${story.name}, isBedtime: ${story.isBedtime}")
                }
                _stories.value = fetchedStories
                
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getStoriesByCategory(isBedtime: Boolean): List<Story> {
        return _stories.value.filter { it.isBedtime == isBedtime }
    }

    // ===== Favorites Management =====

    // isFavorite(...) is called directly (uncached) from many composables on the Stories screen
    // — every card in every section, on every recomposition. Without caching, each call used to
    // launch a brand-new coroutine collecting the DataStore flow forever, which piled up fast
    // during scroll (recompositions never got a stable Flow instance to key `remember` off of)
    // and was the actual cause of the scroll lag. Caching by key means repeat calls for the same
    // story return the SAME shared StateFlow, and WhileSubscribed(5000) lets the underlying
    // DataStore collection stop once nothing observes it instead of running forever.
    private val favoriteFlowCache = mutableMapOf<String, StateFlow<Boolean>>()

    /**
     * Check if a story is favorited for current user/profile
     */
    fun isFavorite(storyId: String, category: String, profileIndex: Int = 0): StateFlow<Boolean> {
        val userId = firebaseAuth.currentUser?.uid ?: return MutableStateFlow(false)
        val compositeId = buildCompositeId(category, storyId)
        val cacheKey = "$userId:$profileIndex:$compositeId"
        return favoriteFlowCache.getOrPut(cacheKey) {
            favoritesDataStore.isFavorite(userId, profileIndex, compositeId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        }
    }

    /**
     * Toggle favorite status for a story
     */
    fun toggleFavorite(storyId: String, category: String, profileIndex: Int = 0) {
        val userId = firebaseAuth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val compositeId = buildCompositeId(category, storyId)
                val favoritesBefore = favoritesDataStore.getFavorites(userId, profileIndex).first()
                val plainId = compositeId.substringAfter(":")
                val wasFavorite = compositeId in favoritesBefore ||
                    (plainId != compositeId && plainId in favoritesBefore)

                favoritesDataStore.toggleFavorite(userId, profileIndex, compositeId)

                val isNowFavorite = !wasFavorite
                val safeCategory = if (category.isBlank()) "stories" else category
                val favoritesAfter = if (wasFavorite) {
                    favoritesBefore - compositeId - plainId
                } else {
                    favoritesBefore + compositeId
                }
                val storyName = findStoryName(storyId)
                val properties = mapOf(
                    "story_id" to storyId,
                    "category" to safeCategory,
                    "profile_index" to profileIndex,
                    "story_title" to (storyName ?: "unknown"),
                    "favorites_count" to favoritesAfter.size
                )
                val eventName = if (isNowFavorite) {
                    "audio_story_favourited"
                } else {
                    "audio_story_unfavourited"
                }
                if (BuildConfig.DEBUG) {
                    Log.d("PostHog", "capture $eventName $properties")
                }
                PostHog.capture(event = eventName, properties = properties)
                if (BuildConfig.DEBUG) {
                    PostHog.flush()
                }

                Log.d("StoriesViewModel", "Toggled favorite for story: $storyId in $safeCategory, nowFavorite=$isNowFavorite")
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error toggling favorite: ${e.message}")
            }
        }
    }

    // Same caching rationale as favoriteFlowCache above.
    private val favoriteSetFlowCache = mutableMapOf<String, StateFlow<Set<String>>>()

    /**
     * Get all favorited stories for current profile
     */
    fun getFavoriteStories(profileIndex: Int = 0): StateFlow<Set<String>> {
        val userId = firebaseAuth.currentUser?.uid ?: return MutableStateFlow(emptySet())
        val cacheKey = "$userId:$profileIndex"
        return favoriteSetFlowCache.getOrPut(cacheKey) {
            favoritesDataStore.getFavorites(userId, profileIndex)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
        }
    }

    private fun buildCompositeId(category: String, storyId: String): String {
        val safeCategory = if (category.isBlank()) "stories" else category
        return "$safeCategory:$storyId"
    }

    private fun findStoryName(storyId: String): String? {
        val allStories = _stories.value +
            _prophetMuhammadStories.value +
            _sahabaStories.value +
            _womenAndMothersStories.value +
            _miraclesStories.value
        return allStories.firstOrNull { it.documentId == storyId }?.name
    }
    
    private fun loadProphetMuhammadStories() {
        viewModelScope.launch {
            try {
                _isProphetMuhammadLoading.value = true
                _error.value = null
                
                Log.d("StoriesViewModel", "Loading Prophet Muhammad stories from Firestore")
                val fetchedStories = storyRepository.getProphetMuhammadStories()
                
                Log.d("StoriesViewModel", "Fetched ${fetchedStories.size} Prophet Muhammad stories")
                fetchedStories.forEach { story ->
                    Log.d("StoriesViewModel", "Prophet Muhammad Story: ${story.name}")
                }
                _prophetMuhammadStories.value = fetchedStories
                
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading Prophet Muhammad stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isProphetMuhammadLoading.value = false
            }
        }
    }

    private fun loadSahabaStories() {
        viewModelScope.launch {
            try {
                _isSahabaLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Loading Sahaba stories from Firestore")
                val fetchedStories = storyRepository.getSahabaStories()

                Log.d("StoriesViewModel", "Fetched ${fetchedStories.size} Sahaba stories")
                fetchedStories.forEach { story ->
                    Log.d("StoriesViewModel", "Sahaba Story: ${story.name}")
                }
                _sahabaStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading Sahaba stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isSahabaLoading.value = false
            }
        }
    }

    private fun loadWomenAndMothersStories() {
        viewModelScope.launch {
            try {
                _isWomenAndMothersLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Loading Women and Mothers stories from Firestore")
                val fetchedStories = storyRepository.getWomenAndMothersStories()

                Log.d("StoriesViewModel", "Fetched ${fetchedStories.size} Women and Mothers stories")
                fetchedStories.forEach { story ->
                    Log.d("StoriesViewModel", "Women and Mothers Story: ${story.name}")
                }
                _womenAndMothersStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading Women and Mothers stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isWomenAndMothersLoading.value = false
            }
        }
    }

    private fun loadMiraclesStories() {
        viewModelScope.launch {
            try {
                _isMiraclesLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Loading Miracles stories from Firestore")
                val fetchedStories = storyRepository.getMiraclesStories()

                Log.d("StoriesViewModel", "Fetched ${fetchedStories.size} Miracles stories")
                fetchedStories.forEach { story ->
                    Log.d("StoriesViewModel", "Miracles Story: ${story.name}")
                }
                _miraclesStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error loading Miracles stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isMiraclesLoading.value = false
            }
        }
    }

    fun refreshStories() {
        loadStories()
        loadProphetMuhammadStories()
        loadSahabaStories()
        loadWomenAndMothersStories()
        loadMiraclesStories()
    }
    
    fun forceRefreshStories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                Log.d("StoriesViewModel", "Force refreshing stories (bypassing cache)")
                val fetchedStories = storyRepository.forceRefreshStories()
                
                Log.d("StoriesViewModel", "Force refreshed ${fetchedStories.size} stories")
                _stories.value = fetchedStories
                
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error force refreshing stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun forceRefreshProphetMuhammadStories() {
        viewModelScope.launch {
            try {
                _isProphetMuhammadLoading.value = true
                _error.value = null
                
                Log.d("StoriesViewModel", "Force refreshing Prophet Muhammad stories (bypassing cache)")
                val fetchedStories = storyRepository.forceRefreshProphetMuhammadStories()
                
                Log.d("StoriesViewModel", "Force refreshed ${fetchedStories.size} Prophet Muhammad stories")
                _prophetMuhammadStories.value = fetchedStories
                
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error force refreshing Prophet Muhammad stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isProphetMuhammadLoading.value = false
            }
        }
    }
    
    fun forceRefreshSahabaStories() {
        viewModelScope.launch {
            try {
                _isSahabaLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Force refreshing Sahaba stories (bypassing cache)")
                val fetchedStories = storyRepository.forceRefreshSahabaStories()

                Log.d("StoriesViewModel", "Force refreshed ${fetchedStories.size} Sahaba stories")
                _sahabaStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error force refreshing Sahaba stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isSahabaLoading.value = false
            }
        }
    }

    fun forceRefreshWomenAndMothersStories() {
        viewModelScope.launch {
            try {
                _isWomenAndMothersLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Force refreshing Women and Mothers stories (bypassing cache)")
                val fetchedStories = storyRepository.forceRefreshWomenAndMothersStories()

                Log.d("StoriesViewModel", "Force refreshed ${fetchedStories.size} Women and Mothers stories")
                _womenAndMothersStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error force refreshing Women and Mothers stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isWomenAndMothersLoading.value = false
            }
        }
    }

    fun forceRefreshMiraclesStories() {
        viewModelScope.launch {
            try {
                _isMiraclesLoading.value = true
                _error.value = null

                Log.d("StoriesViewModel", "Force refreshing Miracles stories (bypassing cache)")
                val fetchedStories = storyRepository.forceRefreshMiraclesStories()

                Log.d("StoriesViewModel", "Force refreshed ${fetchedStories.size} Miracles stories")
                _miraclesStories.value = fetchedStories

            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error force refreshing Miracles stories: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            } finally {
                _isMiraclesLoading.value = false
            }
        }
    }
    
    fun forceRefreshAll() {
        forceRefreshStories()
        forceRefreshProphetMuhammadStories()
        forceRefreshSahabaStories()
        forceRefreshWomenAndMothersStories()
        forceRefreshMiraclesStories()
    }
    
    fun clearAllCache() {
        viewModelScope.launch {
            try {
                Log.d("StoriesViewModel", "Clearing all story cache")
                storyRepository.clearAllCache()
                
                // Reload data after clearing cache
                refreshStories()
                
            } catch (e: Exception) {
                Log.e("StoriesViewModel", "Error clearing cache: ${e.localizedMessage}")
                _error.value = e.localizedMessage
            }
        }
    }
}
