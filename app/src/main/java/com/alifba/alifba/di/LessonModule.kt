package com.alifba.alifba.di

import com.alifba.alifba.data.firebase.FireStoreLessonService
import com.alifba.alifba.data.repository.LessonRepositoryImpl
import com.alifba.alifba.presenation.lessonScreens.domain.repository.LessonRepository
import com.alifba.alifba.presenation.lessonScreens.usecases.GetLessonUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object LessonModule {



    @Provides
    @Singleton
    fun provideFireStoreService():FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideLessonService(firestore: FirebaseFirestore): FireStoreLessonService{
        return FireStoreLessonService(firestore)
    }

    @Provides
    @Singleton
    fun provideLessonRepository(service: FireStoreLessonService): LessonRepository{
        return LessonRepositoryImpl(service)
    }

    @Provides
    @Singleton
    fun provideGetLessonsUseCase(repository: LessonRepository): GetLessonUseCase {
        return GetLessonUseCase(repository)
    }
}