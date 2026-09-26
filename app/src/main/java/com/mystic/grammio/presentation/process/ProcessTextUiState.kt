package com.mystic.grammio.presentation.process

import com.mystic.grammio.domain.model.Transformation

data class ProcessTextUiState(
    val originalText: String,
    val canReplace: Boolean,
    val targetLanguageTag: String,
    /** The enabled transformations, in the user's order; null until they have loaded. */
    val transformations: List<Transformation>? = null,
    val selected: Transformation? = null,
    val result: ResultUiState = ResultUiState.Idle,
) {
    val hasInput: Boolean get() = originalText.isNotBlank()

    val hasNoTransformations: Boolean get() = transformations?.isEmpty() == true

    val showsLanguagePicker: Boolean get() = selected?.usesTargetLanguage == true

    val resultText: String? get() = (result as? ResultUiState.Success)?.text
}
