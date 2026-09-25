package com.mystic.grammio.presentation.process

import com.mystic.grammio.domain.error.TransformError

/** The result card: nothing chosen yet, waiting, the transformed text, or why it failed. */
sealed interface ResultUiState {
    data object Idle : ResultUiState

    data object Loading : ResultUiState

    data class Success(val text: String) : ResultUiState

    data class Failure(val error: TransformError) : ResultUiState
}
