package com.mystic.grammio.presentation.settings.transformations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.TransformationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The list of transformations: turning them on and off, reordering, restoring the defaults. None
 * of that has rules beyond the repository's, so there are no use cases in between.
 */
class TransformationsViewModel(private val repository: TransformationRepository) : ViewModel() {

    val state: StateFlow<TransformationsUiState> = repository.transformations
        .map { TransformationsUiState(transformations = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransformationsUiState())

    fun onAction(action: TransformationsAction) {
        viewModelScope.launch {
            when (action) {
                is TransformationsAction.EnabledChanged -> repository.setEnabled(action.id, action.enabled)
                is TransformationsAction.MoveUp -> repository.moveUp(action.id)
                is TransformationsAction.MoveDown -> repository.moveDown(action.id)
                TransformationsAction.RestoreDefaults -> repository.restoreDefaults()
            }
        }
    }
}
