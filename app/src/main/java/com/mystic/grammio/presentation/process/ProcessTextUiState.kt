package com.mystic.grammio.presentation.process

import com.mystic.grammio.domain.model.Transformation

data class ProcessTextUiState(
    val originalText: String,
    val canReplace: Boolean,
    val targetLanguageTag: String,
    val transformations: List<Transformation>,
    val selected: Transformation? = null,
    val result: ResultUiState = ResultUiState.Idle,
) {
    val hasInput: Boolean get() = originalText.isNotBlank()

    val resultText: String? get() = (result as? ResultUiState.Success)?.text
}
