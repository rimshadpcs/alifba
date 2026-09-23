package com.alifba.alifba.presenation.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alifba.alifba.data.models.Story
import com.alifba.alifba.presenation.stories.domain.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val stepIndex: Int = 0,
    val childName: String = "",
    val selectedMotivations: Set<String> = emptySet(),
    val score: Int = 0,
    val answeredQuestionIds: Set<String> = emptySet(),
    val selectedHabitTime: String? = null,
    val selectedReminderHour: Int = 18,
    val selectedReminderMinute: Int = 30,
    val audioPreviewStory: Story? = null,
    val isAudioPreviewLoading: Boolean = false,
    val audioPreviewError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val storyRepository: StoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    fun nextStep() {
        _uiState.update { state ->
            state.copy(stepIndex = (state.stepIndex + 1).coerceAtMost(OnboardingCopy.lastStepIndex))
        }
    }

    fun previousStep(): Boolean {
        var movedBack = false
        _uiState.update { state ->
            if (state.stepIndex > 0) {
                movedBack = true
                state.copy(stepIndex = state.stepIndex - 1)
            } else {
                state
            }
        }
        return movedBack
    }

    fun setChildName(name: String) {
        _uiState.update { it.copy(childName = name) }
    }

    fun toggleMotivation(motivation: String) {
        // TODO analytics: onboarding_motivation_selected
        _uiState.update { state ->
            state.copy(
                selectedMotivations = if (motivation in state.selectedMotivations) {
                    state.selectedMotivations - motivation
                } else {
                    state.selectedMotivations + motivation
                }
            )
        }
    }

    fun answerQuestion(questionId: String, isCorrect: Boolean) {
        // TODO analytics: onboarding_quiz_answered
        _uiState.update { state ->
            if (questionId in state.answeredQuestionIds) {
                state
            } else {
                state.copy(
                    score = state.score + if (isCorrect) 1 else 0,
                    answeredQuestionIds = state.answeredQuestionIds + questionId
                )
            }
        }
    }

    fun selectHabitTime(habitTime: String, hour: Int, minute: Int) {
        // TODO analytics: onboarding_habit_time_selected
        _uiState.update {
            it.copy(
                selectedHabitTime = habitTime,
                selectedReminderHour = hour,
                selectedReminderMinute = minute
            )
        }
    }

    fun loadAudioPreviewStory() {
        val state = _uiState.value
        if (state.audioPreviewStory != null || state.isAudioPreviewLoading) return

        _uiState.update { it.copy(isAudioPreviewLoading = true, audioPreviewError = null) }
        viewModelScope.launch {
            try {
                val storiesOfProphets = storyRepository.getStories()
                val story = storiesOfProphets.getOrNull(2)?.takeIf { it.audio.isNotBlank() }
                    ?: storiesOfProphets.firstOrNull {
                        it.audio.isNotBlank() &&
                            (it.name.contains("nuh", ignoreCase = true) ||
                                it.name.contains("ark", ignoreCase = true))
                    }
                    ?: storiesOfProphets.firstOrNull { it.audio.isNotBlank() }

                _uiState.update {
                    it.copy(
                        audioPreviewStory = story,
                        isAudioPreviewLoading = false,
                        audioPreviewError = if (story == null) "No audio preview available yet." else null
                    )
                }
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Failed to load onboarding audio preview", e)
                _uiState.update {
                    it.copy(
                        isAudioPreviewLoading = false,
                        audioPreviewError = "Audio preview unavailable."
                    )
                }
            }
        }
    }
}
