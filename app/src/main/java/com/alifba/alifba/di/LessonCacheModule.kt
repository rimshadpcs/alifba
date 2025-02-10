package com.alifba.alifba.di

import android.content.Context
import androidx.room.Room
import com.alifba.alifba.data.db.DatabaseProvider
import com.alifba.alifba.data.db.LessonCacheDao
import com.alifba.alifba.data.db.LessonCacheDatabase
import com.alifba.alifba.data.repository.LessonCacheRepositoryImpl
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonCacheRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LessonCacheModule {


    @Provides
    @Singleton
    fun provideLessonCacheDatabase(@ApplicationContext context: Context): LessonCacheDatabase {
        return DatabaseProvider.getInstance(context)
    }

    @Provides
    fun provideLessonCacheDao(database: LessonCacheDatabase): LessonCacheDao {
        return database.lessonCacheDao()
    }

    @Provides
    fun provideLessonCacheRepository(impl: LessonCacheRepositoryImpl): LessonCacheRepository {
        return impl
    }


}
