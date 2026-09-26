package com.mystic.grammio.presentation.settings.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.HistorySettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The history opt-in. No rules to hold, so it talks to the repository directly. */
class HistorySettingsViewModel(private val historySettings: HistorySettingsRepository) : ViewModel() {

    val state: StateFlow<HistorySettingsUiState> = historySettings.isHistoryEnabled
        .map(::HistorySettingsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorySettingsUiState())

    fun onAction(action: HistorySettingsAction) {
        when (action) {
            is HistorySettingsAction.Toggled -> viewModelScope.launch {
                historySettings.setHistoryEnabled(action.enabled)
            }
        }
    }
}
