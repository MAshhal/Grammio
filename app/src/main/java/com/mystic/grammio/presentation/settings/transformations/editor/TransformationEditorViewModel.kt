package com.mystic.grammio.presentation.settings.transformations.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mystic.grammio.domain.repository.TransformationRepository
import com.mystic.grammio.domain.usecase.SaveTransformationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Creates a transformation ([id] null) or edits one. Saving goes through [SaveTransformationUseCase]
 * for its rules; loading and deleting have none, so they use the repository directly.
 */
class TransformationEditorViewModel(
    private val id: String?,
    private val repository: TransformationRepository,
    private val saveTransformation: SaveTransformationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TransformationEditorUiState(isNew = id == null))
    val state: StateFlow<TransformationEditorUiState> = _state.asStateFlow()

    private val _effects = Channel<TransformationEditorEffect>(Channel.BUFFERED)
    val effects: Flow<TransformationEditorEffect> = _effects.receiveAsFlow()

    init {
        if (id != null) viewModelScope.launch { load(id) }
    }

    fun onAction(action: TransformationEditorAction) {
        when (action) {
            is TransformationEditorAction.NameChanged -> _state.update { it.copy(name = action.value) }

            is TransformationEditorAction.TaskPromptChanged -> _state.update { it.copy(taskPrompt = action.value) }

            is TransformationEditorAction.IconSelected -> _state.update { it.copy(icon = action.icon) }

            TransformationEditorAction.Save -> viewModelScope.launch {
                val current = _state.value
                if (current.isLoading) return@launch
                if (saveTransformation(id, current.name, current.taskPrompt, current.icon)) {
                    _effects.send(TransformationEditorEffect.Close)
                }
            }

            TransformationEditorAction.Delete -> viewModelScope.launch {
                if (id != null) repository.delete(id)
                _effects.send(TransformationEditorEffect.Close)
            }
        }
    }

    private suspend fun load(id: String) {
        val transformation = repository.transformation(id)
        if (transformation == null) {
            _effects.send(TransformationEditorEffect.Close)
            return
        }
        _state.update {
            it.copy(
                isLoading = false,
                name = transformation.name,
                taskPrompt = transformation.taskPrompt,
                icon = transformation.icon,
            )
        }
    }
}
