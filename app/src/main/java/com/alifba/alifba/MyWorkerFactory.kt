package com.alifba.alifba

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import javax.inject.Inject
import javax.inject.Singleton

//@Singleton
//class MyWorkerFactory @Inject constructor(
//    private val workerFactories: @JvmSuppressWildcards Map<Class<out CoroutineWorker>>
//) : WorkerFactory() {
//
//    override fun createWorker(
//        appContext: Context,
//        workerClassName: String,
//        workerParameters: WorkerParameters
//    ): ListenableWorker? {
//        val clazz = Class.forName(workerClassName).asSubclass(CoroutineWorker::class.java)
//        val factory = workerFactories[clazz] ?: return null
//        return factory.create(appContext, workerParameters)
//    }
//}

