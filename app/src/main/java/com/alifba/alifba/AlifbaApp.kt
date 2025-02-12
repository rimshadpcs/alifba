package com.alifba.alifba

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.alifba.alifba.ui_components.widgets.buttons.SoundEffectManager
import com.google.firebase.FirebaseApp
import dagger.MapKey
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlin.reflect.KClass

@HiltAndroidApp
class AlifbaApp : Application() {



    override fun onCreate() {
        super.onCreate()
        SoundEffectManager.initialize(this)
        FirebaseApp.initializeApp(this)
    }
    override fun onTerminate() {
        super.onTerminate()
        SoundEffectManager.release()
    }
}
