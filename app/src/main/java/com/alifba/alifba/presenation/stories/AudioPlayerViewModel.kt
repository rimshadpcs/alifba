package com.alifba.alifba.presenation.stories

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.services.AudioPlayerService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AudioPlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private var audioService: AudioPlayerService? = null
    private var isBound = false
    
    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory.asStateFlow()
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlayerService.AudioPlayerBinder
            audioService = binder.getService()
            isBound = true
            
            // Start observing service state flows
            observeServiceStates()
            
            Log.d("AudioPlayerViewModel", "AudioPlayerService connected")
            
            // Load current story if available
            _currentStory.value?.let { story ->
                if (story.audio.isNotEmpty()) {
                    audioService?.loadAudio(story.audio, story.name, story.background)
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            isBound = false
            Log.d("AudioPlayerViewModel", "AudioPlayerService disconnected")
        }
    }
    
    init {
        bindToAudioService()
    }
    
    private fun bindToAudioService() {
        try {
            val intent = Intent(context, AudioPlayerService::class.java)
            context.startService(intent) // Start the service
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d("AudioPlayerViewModel", "Binding to AudioPlayerService")
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Failed to bind to service", e)
            _error.value = "Failed to initialize audio service"
        }
    }
    
    private fun observeServiceStates() {
        audioService?.let { service ->
            viewModelScope.launch {
                service.isPlaying.collect { playing ->
                    _isPlaying.value = playing
                }
            }
            
            viewModelScope.launch {
                service.currentPosition.collect { position ->
                    _currentPosition.value = position
                }
            }
            
            viewModelScope.launch {
                service.duration.collect { duration ->
                    _duration.value = duration
                }
            }
            
            viewModelScope.launch {
                service.isLoading.collect { loading ->
                    _isLoading.value = loading
                }
            }
            
            viewModelScope.launch {
                service.error.collect { error ->
                    _error.value = error
                }
            }
        }
    }
    
    fun loadStory(story: Story) {
        _currentStory.value = story
        _error.value = null
        
        if (story.audio.isNotEmpty()) {
            audioService?.loadAudio(story.audio, story.name, story.background)
            Log.d("AudioPlayerViewModel", "Loading audio for story: ${story.name}")
        } else {
            _error.value = "No audio available for this story"
            Log.w("AudioPlayerViewModel", "Story has no audio URL: ${story.name}")
        }
    }
    
    fun play() {
        audioService?.play()
    }
    
    fun pause() {
        audioService?.pause()
    }
    
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    fun seekTo(position: Long) {
        audioService?.seekTo(position)
    }
    
    fun skipForward() {
        audioService?.skipForward(10)
    }
    
    fun skipBackward() {
        audioService?.skipBackward(10)
    }
    
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun stopAndClearAudio() {
        audioService?.stop()
        _currentStory.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        _error.value = null
        Log.d("AudioPlayerViewModel", "Audio stopped and cleared")
    }
    
    override fun onCleared() {
        super.onCleared()
        try {
            if (isBound) {
                context.unbindService(serviceConnection)
                isBound = false
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerViewModel", "Error unbinding service", e)
        }
        Log.d("AudioPlayerViewModel", "AudioPlayerViewModel cleared")
    }
}