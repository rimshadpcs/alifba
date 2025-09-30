package com.alifba.alifba.di

import android.content.Context
import com.alifba.alifba.data.cache.AppDatabase
import com.alifba.alifba.data.cache.StoryCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideStoryCacheDao(database: AppDatabase): StoryCacheDao {
        return database.storyCacheDao()
    }
}