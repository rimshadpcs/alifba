package com.alifba.alifba.utils

import android.media.MediaPlayer
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode

//@Composable
//fun PlayAudio(audioResId: String) {
//    val context = LocalContext.current
//
//    if (!LocalInspectionMode.current) {
//        val mediaPlayer = remember { MediaPlayer.create(context, audioResId) }
//
//        DisposableEffect(Unit) {
//            mediaPlayer?.start() ?: Log.e("PlayAudio", "MediaPlayer is null. Unable to play audio.")
//
//            onDispose {
//                mediaPlayer?.apply {
//                    if (isPlaying) {
//                        stop()
//                        Log.d("PlayAudio", "Audio stopped")
//                    }
//                    release()
//                    Log.d("PlayAudio", "MediaPlayer released")
//                }
//            }
//        }
//    }
//}
