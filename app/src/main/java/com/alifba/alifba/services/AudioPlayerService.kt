package com.alifba.alifba.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.palette.graphics.Palette
import android.widget.RemoteViews
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import coil.ImageLoader
import coil.request.ImageRequest
import coil.target.Target
import androidx.core.app.NotificationCompat
import com.alifba.alifba.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class AudioPlayerService : MediaBrowserServiceCompat() {
    
    private val binder = AudioPlayerBinder()
    private var mediaPlayer: MediaPlayer? = null
    
    // Audio state flows
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
    
    private val _currentStoryName = MutableStateFlow<String?>(null)
    val currentStoryName: StateFlow<String?> = _currentStoryName.asStateFlow()
    
    private val _currentAudioUrl = MutableStateFlow<String?>(null)
    val currentAudioUrl: StateFlow<String?> = _currentAudioUrl.asStateFlow()
    
    private val _currentStoryImageUrl = MutableStateFlow<String?>(null)
    val currentStoryImageUrl: StateFlow<String?> = _currentStoryImageUrl.asStateFlow()
    
    private var currentStoryBitmap: Bitmap? = null
    private var enhancedBackgroundBitmap: Bitmap? = null
    private var enhancedBackgroundUri: Uri? = null
    private var dominantColor: Int = android.graphics.Color.parseColor("#6C63FF") // Default purple
    private var accentColor: Int = android.graphics.Color.parseColor("#9C93FF") // Default light purple
    private var mediaSession: MediaSessionCompat? = null
    
    private var positionUpdateRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "AudioPlayerChannel"
        private const val MEDIA_ROOT_ID = "media_root"
        private const val RECENT_ROOT_ID = "recent_root"
        private const val EMPTY_ROOT_ID = "empty_root"
    }
    
    private fun stopForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForegroundService()
        }
    }
    
    inner class AudioPlayerBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createMediaSession()
        
        // Set the MediaSession token for MediaBrowserService
        sessionToken = mediaSession?.sessionToken
        
        Log.d("AudioPlayerService", "AudioPlayerService created with MediaBrowser support")
    }
    
    private fun createMediaSession() {
        mediaSession = MediaSessionCompat(this, "AudioPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    play()
                }
                
                override fun onPause() {
                    pause()
                }
                
                override fun onSeekTo(pos: Long) {
                    seekTo(pos)
                }
                
                override fun onStop() {
                    stop()
                }
            })
            
            // Set initial playback state
            setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SEEK_TO or
                        PlaybackStateCompat.ACTION_STOP
                    )
                    .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
                    .build()
            )
            
            isActive = true
        }
    }
    
    override fun onBind(intent: Intent): IBinder? {
        // Check if this is a MediaBrowserService intent
        return if (SERVICE_INTERFACE == intent.action) {
            super.onBind(intent) // Return MediaBrowserService binder
        } else {
            binder // Return our custom binder for direct service access
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Player",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Controls for audio playback"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
            Log.d("AudioPlayerService", "Notification channel created")
        }
    }
    
    fun loadAudio(audioUrl: String, storyName: String? = null, imageUrl: String? = null) {
        try {
            // Check if same audio is already loaded
            if (_currentAudioUrl.value == audioUrl && mediaPlayer != null) {
                Log.d("AudioPlayerService", "Audio already loaded: $audioUrl")
                return
            }
            
            _isLoading.value = true
            _error.value = null
            _currentAudioUrl.value = audioUrl
            _currentStoryName.value = storyName
            _currentStoryImageUrl.value = imageUrl
            
            // Load story background image if provided
            if (imageUrl != null) {
                loadStoryImage(imageUrl)
            }
            
            // Release existing player
            releasePlayer()
            
            // Create new MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                prepareAsync()
                
                setOnPreparedListener { player ->
                    _duration.value = player.duration.toLong()
                    _isLoading.value = false
                    updateMediaSessionMetadata()
                    updateMediaSessionState()
                    showNotification() // Show notification when audio is prepared
                    Log.d("AudioPlayerService", "Audio prepared, duration: ${player.duration}, notification shown")
                }
                
                setOnCompletionListener {
                    _isPlaying.value = false
                    _currentPosition.value = 0L
                    stopPositionUpdates()
                    stopForegroundService()
                    Log.d("AudioPlayerService", "Audio playback completed")
                }
                
                setOnErrorListener { _, what, extra ->
                    _error.value = "Audio playback error: $what, $extra"
                    _isLoading.value = false
                    _isPlaying.value = false
                    Log.e("AudioPlayerService", "MediaPlayer error: $what, $extra")
                    true
                }
            }
            
        } catch (e: IOException) {
            _error.value = "Failed to load audio: ${e.message}"
            _isLoading.value = false
            Log.e("AudioPlayerService", "Failed to load audio", e)
        } catch (e: Exception) {
            _error.value = "Unexpected error: ${e.message}"
            _isLoading.value = false
            Log.e("AudioPlayerService", "Unexpected error", e)
        }
    }
    
    fun play() {
        try {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    _isPlaying.value = true
                    startPositionUpdates()
                    updateMediaSessionState()
                    showNotification()
                    Log.d("AudioPlayerService", "Audio playback started, notification shown")
                }
            }
        } catch (e: Exception) {
            _error.value = "Failed to start playback: ${e.message}"
            Log.e("AudioPlayerService", "Failed to start playback", e)
        }
    }
    
    fun pause() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                    stopPositionUpdates()
                    updateMediaSessionState()
                    showNotification()
                    Log.d("AudioPlayerService", "Audio playback paused")
                }
            }
        } catch (e: Exception) {
            _error.value = "Failed to pause playback: ${e.message}"
            Log.e("AudioPlayerService", "Failed to pause playback", e)
        }
    }
    
    fun seekTo(position: Long) {
        try {
            mediaPlayer?.let { player ->
                val seekPosition = position.coerceIn(0L, _duration.value)
                player.seekTo(seekPosition.toInt())
                _currentPosition.value = seekPosition
                updateMediaSessionState() // Update MediaSession after seeking
                Log.d("AudioPlayerService", "Seeked to position: $seekPosition")
            }
        } catch (e: Exception) {
            _error.value = "Failed to seek: ${e.message}"
            Log.e("AudioPlayerService", "Failed to seek", e)
        }
    }
    
    fun skipForward(seconds: Int = 10) {
        val newPosition = _currentPosition.value + (seconds * 1000)
        seekTo(newPosition)
    }
    
    fun skipBackward(seconds: Int = 10) {
        val newPosition = _currentPosition.value - (seconds * 1000)
        seekTo(newPosition)
    }
    
    fun stop() {
        try {
            _isPlaying.value = false
            _currentPosition.value = 0L
            _duration.value = 0L
            _currentAudioUrl.value = ""
            _currentStoryName.value = null
            _currentStoryImageUrl.value = null
            currentStoryBitmap = null
            enhancedBackgroundBitmap = null
            enhancedBackgroundUri = null
            dominantColor = android.graphics.Color.parseColor("#6C63FF") // Reset to default
            accentColor = android.graphics.Color.parseColor("#9C93FF") // Reset to default
            stopPositionUpdates()
            releasePlayer()
            stopForegroundService()
            Log.d("AudioPlayerService", "Audio completely stopped and released")
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Failed to stop audio", e)
        }
    }
    
    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateRunnable = object : Runnable {
            override fun run() {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPosition.value = player.currentPosition.toLong()
                        updateMediaSessionState() // Update MediaSession with current position
                        handler.postDelayed(this, 1000) // Update every second
                    }
                }
            }
        }
        handler.post(positionUpdateRunnable!!)
    }
    
    private fun stopPositionUpdates() {
        positionUpdateRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
            positionUpdateRunnable = null
        }
    }
    
    private fun updateMediaSessionState() {
        val state = if (_isPlaying.value) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_STOP
                )
                .setState(state, _currentPosition.value, 1.0f)
                .build()
        )
    }
    
    private fun updateMediaSessionMetadata() {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, _currentStoryName.value ?: "Unknown Story")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Alifba Stories")
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, _duration.value)
        
        // Use enhanced background URI for better media controls appearance (system prefers URI)
        enhancedBackgroundUri?.let { uri ->
            metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, uri.toString())
            Log.d("AudioPlayerService", "MediaSession metadata updated with URI: $uri")
        } ?: run {
            // Fallback to bitmap if URI not available
            val artworkBitmap = enhancedBackgroundBitmap ?: currentStoryBitmap
            artworkBitmap?.let { bitmap ->
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                Log.d("AudioPlayerService", "MediaSession metadata updated with bitmap fallback: ${bitmap.width}x${bitmap.height}")
            }
        }
        
        mediaSession?.setMetadata(metadata.build())
        Log.d("AudioPlayerService", "MediaSession metadata updated: duration=${_duration.value}")
    }
    
    private fun loadStoryImage(imageUrl: String) {
        try {
            val imageLoader = ImageLoader(this)
            val request = ImageRequest.Builder(this)
                .data(imageUrl)
                .target(object : Target {
                    override fun onSuccess(result: Drawable) {
                        // Convert drawable to bitmap
                        val bitmap = Bitmap.createBitmap(
                            result.intrinsicWidth,
                            result.intrinsicHeight,
                            Bitmap.Config.ARGB_8888
                        )
                        val canvas = android.graphics.Canvas(bitmap)
                        result.setBounds(0, 0, canvas.width, canvas.height)
                        result.draw(canvas)
                        
                        currentStoryBitmap = bitmap
                        
                        // Extract colors and create enhanced background
                        extractColorsFromBitmap(bitmap)
                        enhancedBackgroundBitmap = createSimpleBackgroundBitmap(bitmap)
                        
                        // Save enhanced bitmap to file and get URI
                        enhancedBackgroundUri = saveEnhancedBitmapToFile(enhancedBackgroundBitmap!!)
                        
                        Log.d("AudioPlayerService", "Story image loaded successfully with enhanced background and URI: $enhancedBackgroundUri")
                        
                        // Always update MediaSession metadata with new image, even if audio isn't ready yet
                        updateMediaSessionMetadata()
                        
                        // Update notification if audio is ready
                        if (_duration.value > 0) {
                            showNotification()
                        }
                    }
                    
                    override fun onError(error: Drawable?) {
                        Log.e("AudioPlayerService", "Failed to load story image")
                        currentStoryBitmap = null
                        enhancedBackgroundBitmap = null
                    }
                })
                .build()
            
            imageLoader.enqueue(request)
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error loading story image", e)
            currentStoryBitmap = null
            enhancedBackgroundBitmap = null
        }
    }

    private fun extractColorsFromBitmap(bitmap: Bitmap) {
        try {
            // Extract colors synchronously to ensure they're available before creating enhanced background
            val palette = Palette.from(bitmap).generate()
            
            // Extract dominant colors for theming the notification
            dominantColor = palette.getDominantColor(android.graphics.Color.parseColor("#6C63FF"))
            accentColor = palette.getLightVibrantColor(android.graphics.Color.parseColor("#9C93FF"))
                ?: palette.getVibrantColor(android.graphics.Color.parseColor("#9C93FF"))
            
            // Ensure colors aren't too dark (avoid black backgrounds)
            if (android.graphics.Color.luminance(dominantColor) < 0.3f) {
                dominantColor = android.graphics.Color.parseColor("#6C63FF") // Default purple
            }
            if (android.graphics.Color.luminance(accentColor) < 0.3f) {
                accentColor = android.graphics.Color.parseColor("#9C93FF") // Default light purple
            }
            
            Log.d("AudioPlayerService", "Colors extracted - Dominant: ${String.format("#%06X", 0xFFFFFF and dominantColor)}, Accent: ${String.format("#%06X", 0xFFFFFF and accentColor)}")
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error extracting colors from bitmap", e)
            // Keep default colors
            dominantColor = android.graphics.Color.parseColor("#6C63FF")
            accentColor = android.graphics.Color.parseColor("#9C93FF")
        }
    }

    private fun createEnhancedBackgroundBitmap(originalBitmap: Bitmap): Bitmap {
        try {
            val size = 512 // Notification background size
            Log.d("AudioPlayerService", "Creating enhanced background bitmap, size: $size, original: ${originalBitmap.width}x${originalBitmap.height}")
            
            val enhancedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(enhancedBitmap)
            
            // Fill with a solid color first to ensure no black background
            canvas.drawColor(dominantColor)
            
            // Create a rounded rectangle background with gradient
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())
            
            // Create gradient background using extracted colors
            val gradient = LinearGradient(
                0f, 0f, size.toFloat(), size.toFloat(),
                intArrayOf(dominantColor, accentColor, dominantColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            
            // Draw background with rounded corners
            val cornerRadius = 32f
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint)
            
            // Scale and center the original image
            val imageSize = (size * 0.8f).toInt() // 80% of background size
            val imageOffset = (size - imageSize) / 2
            
            val scaledOriginal = Bitmap.createScaledBitmap(originalBitmap, imageSize, imageSize, true)
            
            // Draw the scaled image directly with rounded corners
            val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            val imageRectF = RectF(
                imageOffset.toFloat(), 
                imageOffset.toFloat(), 
                (imageOffset + imageSize).toFloat(), 
                (imageOffset + imageSize).toFloat()
            )
            
            // Create rounded bitmap for the image
            val roundedImageBitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
            val roundedCanvas = Canvas(roundedImageBitmap)
            
            // Draw rounded rectangle as mask
            val roundedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            roundedCanvas.drawRoundRect(
                RectF(0f, 0f, imageSize.toFloat(), imageSize.toFloat()), 
                24f, 24f, roundedPaint
            )
            
            // Apply image with mask
            roundedPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            roundedCanvas.drawBitmap(scaledOriginal, 0f, 0f, roundedPaint)
            
            // Draw the rounded image onto the main canvas
            canvas.drawBitmap(roundedImageBitmap, imageOffset.toFloat(), imageOffset.toFloat(), null)
            
            // Add a subtle frame around the image
            val framePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            framePaint.style = Paint.Style.STROKE
            framePaint.strokeWidth = 4f
            framePaint.color = android.graphics.Color.argb(128, 255, 255, 255)
            canvas.drawRoundRect(imageRectF, 24f, 24f, framePaint)
            
            Log.d("AudioPlayerService", "Enhanced background bitmap created successfully with colors: $dominantColor, $accentColor")
            return enhancedBitmap
            
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error creating enhanced background bitmap", e)
            // Create a simple colored background as fallback
            return createSimpleBackgroundBitmap(originalBitmap)
        }
    }
    
    private fun createSimpleBackgroundBitmap(originalBitmap: Bitmap): Bitmap {
        try {
            val size = 256
            val simpleBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(simpleBitmap)
            
            Log.d("AudioPlayerService", "Creating simple background with colors: ${String.format("#%06X", 0xFFFFFF and dominantColor)}")
            
            // Fill with dominant color as background
            canvas.drawColor(dominantColor)
            
            // Scale and center the original image
            val imageSize = (size * 0.9f).toInt() // 90% of size to leave some border
            val imageOffset = (size - imageSize) / 2
            
            val scaledOriginal = Bitmap.createScaledBitmap(originalBitmap, imageSize, imageSize, true)
            canvas.drawBitmap(scaledOriginal, imageOffset.toFloat(), imageOffset.toFloat(), null)
            
            // Add a subtle border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            borderPaint.style = Paint.Style.STROKE
            borderPaint.strokeWidth = 8f
            borderPaint.color = accentColor
            
            val borderRect = RectF(4f, 4f, size - 4f, size - 4f)
            canvas.drawRoundRect(borderRect, 16f, 16f, borderPaint)
            
            Log.d("AudioPlayerService", "Simple background created successfully")
            return simpleBitmap
            
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error creating simple background bitmap", e)
            return createFallbackBackground(originalBitmap)
        }
    }
    
    private fun createFallbackBackground(originalBitmap: Bitmap): Bitmap {
        try {
            val size = 256
            val fallbackBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(fallbackBitmap)
            
            // Fill with dominant color
            canvas.drawColor(dominantColor)
            
            // Draw scaled original image
            val scaledOriginal = Bitmap.createScaledBitmap(originalBitmap, size, size, true)
            canvas.drawBitmap(scaledOriginal, 0f, 0f, null)
            
            Log.d("AudioPlayerService", "Fallback background created")
            return fallbackBitmap
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Fallback background creation failed", e)
            return originalBitmap
        }
    }
    
    private fun saveEnhancedBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            // Create app-specific directory for cached images
            val cacheDir = File(cacheDir, "media_artwork")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            // Create unique filename based on current story
            val storyName = _currentStoryName.value?.replace("[^a-zA-Z0-9]".toRegex(), "_") ?: "story"
            val fileName = "enhanced_${storyName}_${System.currentTimeMillis()}.png"
            val imageFile = File(cacheDir, fileName)
            
            // Save bitmap to file
            FileOutputStream(imageFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
            }
            
            // Return file URI 
            val uri = Uri.fromFile(imageFile)
            Log.d("AudioPlayerService", "Enhanced bitmap saved to: ${imageFile.absolutePath}")
            
            // Clean up old files (keep only last 5 files)
            cleanupOldArtworkFiles(cacheDir)
            
            uri
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error saving enhanced bitmap to file", e)
            null
        }
    }
    
    private fun cleanupOldArtworkFiles(cacheDir: File) {
        try {
            val files = cacheDir.listFiles { _, name -> name.startsWith("enhanced_") && name.endsWith(".png") }
            files?.let { fileArray ->
                if (fileArray.size > 5) {
                    fileArray.sortBy { it.lastModified() }
                    // Delete oldest files, keep newest 5
                    fileArray.take(fileArray.size - 5).forEach { file ->
                        file.delete()
                        Log.d("AudioPlayerService", "Cleaned up old artwork file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Error cleaning up old artwork files", e)
        }
    }
    
    private fun showNotification() {
        try {
            val storyName = _currentStoryName.value ?: "Unknown Story"
            val isPlaying = _isPlaying.value
            
            val playPauseAction = if (isPlaying) "PAUSE" else "PLAY"
            val playPauseIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            
            // Create intent to open the app when notification is clicked
            val openAppIntent = Intent(this, com.alifba.alifba.presenation.main.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                this, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(storyName)
                .setContentText("Alifba Stories")
                .setSubText(if (isPlaying) "Playing story" else "Paused")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(isPlaying)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(NotificationCompat.Action.Builder(
                    R.drawable.tensecbackward,
                    "Back",
                    createPendingIntent("PREV")
                ).build())
                .addAction(NotificationCompat.Action.Builder(
                    playPauseIcon,
                    if (isPlaying) "Pause" else "Play",
                    createPendingIntent(playPauseAction)
                ).build())
                .addAction(NotificationCompat.Action.Builder(
                    R.drawable.tensecforward,
                    "Forward",
                    createPendingIntent("NEXT")
                ).build())
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSession?.sessionToken))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setAutoCancel(false)
                .setColor(dominantColor) // Apply extracted dominant color as notification accent
            
            // Debug logging for bitmaps
            Log.d("AudioPlayerService", "Notification setup - currentStoryBitmap: ${currentStoryBitmap != null}, enhancedBackgroundBitmap: ${enhancedBackgroundBitmap != null}")
            
            // Use enhanced background bitmap if available, otherwise fall back to current story bitmap
            val largeIconBitmap = enhancedBackgroundBitmap ?: currentStoryBitmap
            largeIconBitmap?.let { bitmap ->
                Log.d("AudioPlayerService", "Setting large icon - bitmap size: ${bitmap.width}x${bitmap.height}, isRecycled: ${bitmap.isRecycled}")
                notificationBuilder.setLargeIcon(bitmap)
            } ?: run {
                Log.w("AudioPlayerService", "No bitmap available for notification large icon")
            }
            
            val notification = notificationBuilder.build()
            
            startForeground(NOTIFICATION_ID, notification)
            Log.d("AudioPlayerService", "Notification shown: $storyName, playing: $isPlaying, hasCurrentBitmap: ${currentStoryBitmap != null}, hasEnhancedBitmap: ${enhancedBackgroundBitmap != null}, mediaSessionActive: ${mediaSession?.isActive}")
        } catch (e: Exception) {
            Log.e("AudioPlayerService", "Failed to show notification", e)
        }
    }
    
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, 
            action.hashCode(), 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "PLAY" -> play()
                "PAUSE" -> pause()
                "PREV" -> skipBackward()
                "NEXT" -> skipForward()
            }
        }
        return START_STICKY
    }
    
    private fun releasePlayer() {
        stopPositionUpdates()
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
        stopForegroundService()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
        mediaSession?.release()
        mediaSession = null
        Log.d("AudioPlayerService", "AudioPlayerService destroyed")
    }
    
    // MediaBrowserServiceCompat required methods
    override fun onGetRoot(
        clientPackageName: String, 
        clientUid: Int, 
        rootHints: android.os.Bundle?
    ): BrowserRoot? {
        Log.d("AudioPlayerService", "onGetRoot called by: $clientPackageName, uid: $clientUid")
        
        // Check if this is a system UI request (for media controls)
        if (isSystemUiRequest(clientPackageName, clientUid)) {
            rootHints?.let { hints ->
                if (hints.getBoolean(BrowserRoot.EXTRA_RECENT)) {
                    // Return recent root for media resumption
                    Log.d("AudioPlayerService", "Returning recent root for media resumption")
                    val extras = android.os.Bundle().apply {
                        putBoolean(BrowserRoot.EXTRA_RECENT, true)
                    }
                    return BrowserRoot(RECENT_ROOT_ID, extras)
                }
            }
            // Return normal media root for system UI
            Log.d("AudioPlayerService", "Returning normal media root")
            return BrowserRoot(MEDIA_ROOT_ID, null)
        }
        
        // For other clients, return empty root (restrict access)
        Log.d("AudioPlayerService", "Returning empty root for restricted access")
        return BrowserRoot(EMPTY_ROOT_ID, null)
    }
    
    override fun onLoadChildren(
        parentId: String, 
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.d("AudioPlayerService", "onLoadChildren called for parentId: $parentId")
        
        when (parentId) {
            RECENT_ROOT_ID -> {
                // Return the most recently played story if available
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                
                _currentStoryName.value?.let { storyName ->
                    val description = MediaDescriptionCompat.Builder()
                        .setMediaId("recent_story")
                        .setTitle(storyName)
                        .setSubtitle("Alifba Stories")
                        .setDescription("Continue listening to $storyName")
                        
                    // Add artwork if available
                    enhancedBackgroundUri?.let { uri ->
                        description.setIconUri(uri)
                    } ?: currentStoryBitmap?.let { bitmap ->
                        description.setIconBitmap(bitmap)
                    }
                    
                    val mediaItem = MediaBrowserCompat.MediaItem(
                        description.build(),
                        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                    )
                    
                    mediaItems.add(mediaItem)
                    Log.d("AudioPlayerService", "Added recent story to media browser: $storyName")
                }
                
                result.sendResult(mediaItems)
            }
            
            MEDIA_ROOT_ID -> {
                // For normal browsing, return empty list (we don't support full browsing)
                result.sendResult(mutableListOf())
            }
            
            EMPTY_ROOT_ID -> {
                // Return empty list for restricted clients
                result.sendResult(mutableListOf())
            }
            
            else -> {
                Log.w("AudioPlayerService", "Unknown parentId: $parentId")
                result.sendResult(mutableListOf())
            }
        }
    }
    
    private fun isSystemUiRequest(clientPackageName: String, clientUid: Int): Boolean {
        // Check if the request is from System UI for media controls
        return clientPackageName == "com.android.systemui" || 
               clientPackageName == "android" ||
               clientUid == 1000 // System UID
    }
}