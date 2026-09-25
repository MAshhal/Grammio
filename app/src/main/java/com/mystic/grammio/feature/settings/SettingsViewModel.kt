package com.mystic.grammio.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasApiKey: Boolean = false,
    val keyInput: String = "",
) {
    val canSave: Boolean get() = keyInput.isNotBlank()
}

sealed interface SettingsAction {
    data class KeyInputChanged(val value: String) : SettingsAction
    data object Save : SettingsAction
    data object Clear : SettingsAction
}

/**
 * Talks to [ApiKeyRepository] directly: saving/clearing a key has no application rules yet, so a
 * use case would be a pass-through. Add one when there is logic (e.g. validating the key online).
 */
class SettingsViewModel(private val apiKeyRepository: ApiKeyRepository) : ViewModel() {

    private val keyInput = MutableStateFlow("")

    val state: StateFlow<SettingsUiState> =
        combine(apiKeyRepository.hasApiKey, keyInput) { hasKey, input -> SettingsUiState(hasKey, input) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.KeyInputChanged -> keyInput.value = action.value

            SettingsAction.Save -> {
                val key = keyInput.value.trim()
                if (key.isEmpty()) return
                viewModelScope.launch {
                    apiKeyRepository.save(key)
                    keyInput.update { "" }
                }
            }

            SettingsAction.Clear -> viewModelScope.launch { apiKeyRepository.clear() }
        }
    }
}
