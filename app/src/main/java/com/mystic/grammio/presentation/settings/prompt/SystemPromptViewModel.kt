package com.mystic.grammio.presentation.settings.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.PromptSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Edits the system prompt. The repository holds the only rule (blank or default means default). */
class SystemPromptViewModel(private val promptSettings: PromptSettingsRepository) : ViewModel() {

    /** What the user is typing. Null means untouched, so the saved prompt shows. */
    private val input = MutableStateFlow<String?>(null)

    val state: StateFlow<SystemPromptUiState> =
        combine(promptSettings.systemPrompt, input) { saved, input ->
            SystemPromptUiState(
                input = input ?: saved,
                savedPrompt = saved,
                defaultPrompt = promptSettings.defaultSystemPrompt,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SystemPromptUiState())

    fun onAction(action: SystemPromptAction) {
        when (action) {
            is SystemPromptAction.InputChanged -> input.value = action.value

            SystemPromptAction.Save -> viewModelScope.launch {
                promptSettings.setSystemPrompt(state.value.input)
                input.value = null
            }

            SystemPromptAction.ResetToDefault -> viewModelScope.launch {
                promptSettings.resetSystemPrompt()
                input.value = null
            }
        }
    }
}
