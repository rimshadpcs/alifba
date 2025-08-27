package com.alifba.alifba.presenation.stories.domain.repository

import com.alifba.alifba.data.models.Story

interface StoryRepository {
    suspend fun getStories(): List<Story>
}