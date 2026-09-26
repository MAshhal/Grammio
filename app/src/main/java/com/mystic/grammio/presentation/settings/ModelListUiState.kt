package com.mystic.grammio.presentation.settings

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiModel

/** The models the active provider offers, as far as Settings has been able to find out. */
sealed interface ModelListUiState {
    /** Nothing to fetch yet: no key, or no endpoint for a custom provider. */
    data object Idle : ModelListUiState

    data object Loading : ModelListUiState

    data class Loaded(val models: List<AiModel>) : ModelListUiState

    data class Failed(val error: TransformError) : ModelListUiState
}
