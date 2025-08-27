package com.alifba.alifba.di

import com.alifba.alifba.data.firebase.FirestoreStoryService
import com.alifba.alifba.data.repository.StoryRepositoryImpl
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StoryModule {

    @Provides
    @Singleton
    fun provideFirestoreStoryService(firestore: FirebaseFirestore): FirestoreStoryService {
        return FirestoreStoryService(firestore)
    }

    @Provides
    @Singleton
    fun provideStoryRepository(service: FirestoreStoryService): StoryRepository {
        return StoryRepositoryImpl(service)
    }
}