package com.mystic.grammio.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.usecase.SaveApiKeyUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Saving goes through [SaveApiKeyUseCase] because it has rules. Observing and clearing the key have
 * none, so they use [ApiKeyRepository] directly rather than through pass-through use cases.
 */
class SettingsViewModel(
    private val apiKeyRepository: ApiKeyRepository,
    private val saveApiKey: SaveApiKeyUseCase,
) : ViewModel() {

    private val keyInput = MutableStateFlow("")

    val state: StateFlow<SettingsUiState> =
        combine(apiKeyRepository.hasApiKey(AiProvider.Gemini), keyInput) { hasKey, input ->
            SettingsUiState(hasKey, input)
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.KeyInputChanged -> keyInput.value = action.value

            SettingsAction.Save -> viewModelScope.launch {
                if (saveApiKey(AiProvider.Gemini, keyInput.value)) keyInput.update { "" }
            }

            SettingsAction.Clear -> viewModelScope.launch { apiKeyRepository.clear(AiProvider.Gemini) }
        }
    }
}
