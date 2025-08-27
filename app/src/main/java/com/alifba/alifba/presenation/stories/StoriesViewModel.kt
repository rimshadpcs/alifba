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
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        Log.d("StoriesViewModel", "StoriesViewModel initialized")
        Log.d("StoriesViewModel", "StoryRepository: $storyRepository")
        loadStories()
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
    
    fun refreshStories() {
        loadStories()
    }
}