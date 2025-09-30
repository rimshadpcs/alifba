package com.alifba.alifba.presenation.stories

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    
    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()
    
    private val _prophetMuhammadStories = MutableStateFlow<List<Story>>(emptyList())
    val prophetMuhammadStories: StateFlow<List<Story>> = _prophetMuhammadStories.asStateFlow()
    
    private val _sahabaStories = MutableStateFlow<List<Story>>(emptyList())
    val sahabaStories: StateFlow<List<Story>> = _sahabaStories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _isProphetMuhammadLoading = MutableStateFlow(true)
    val isProphetMuhammadLoading: StateFlow<Boolean> = _isProphetMuhammadLoading.asStateFlow()
    
    private val _isSahabaLoading = MutableStateFlow(true)
    val isSahabaLoading: StateFlow<Boolean> = _isSahabaLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        Log.d("StoriesViewModel", "StoriesViewModel initialized")
        Log.d("StoriesViewModel", "StoryRepository: $storyRepository")
        loadStories()
        loadProphetMuhammadStories()
        loadSahabaStories()
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

    fun refreshStories() {
        loadStories()
        loadProphetMuhammadStories()
        loadSahabaStories()
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
    
    fun forceRefreshAll() {
        forceRefreshStories()
        forceRefreshProphetMuhammadStories()
        forceRefreshSahabaStories()
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