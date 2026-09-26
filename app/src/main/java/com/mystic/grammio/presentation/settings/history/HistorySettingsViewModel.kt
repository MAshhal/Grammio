package com.mystic.grammio.presentation.settings.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.HistoryRepository
import com.mystic.grammio.domain.repository.HistorySettingsRepository
import com.mystic.grammio.domain.repository.TransformationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The history opt-in and the latest saved entries. No rules to hold, so it talks to the
 * repositories directly.
 */
class HistorySettingsViewModel(
    private val historySettings: HistorySettingsRepository,
    private val history: HistoryRepository,
    transformations: TransformationRepository,
) : ViewModel() {

    val state: StateFlow<HistorySettingsUiState> = combine(
        historySettings.isHistoryEnabled,
        history.recent(RECENT_LIMIT),
        transformations.transformations,
    ) { enabled, entries, current ->
        val byId = current.associateBy { it.id }
        HistorySettingsUiState(
            isHistoryEnabled = enabled,
            entries = entries.map { HistoryItem(it, byId[it.transformationId]) },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistorySettingsUiState())

    fun onAction(action: HistorySettingsAction) {
        viewModelScope.launch {
            when (action) {
                is HistorySettingsAction.Toggled -> historySettings.setHistoryEnabled(action.enabled)
                HistorySettingsAction.Cleared -> history.clear()
            }
        }
    }

    companion object {
        /** Enough to find something from the last few days without turning the page into an archive. */
        const val RECENT_LIMIT = 50
    }
}
