package com.mystic.grammio.presentation.process

import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation

data class ProcessTextUiState(
    val originalText: String,
    val canReplace: Boolean,
    val targetLanguageTag: String,
    val selected: Transformation? = null,
    val result: ResultState = ResultState.Idle,
) {
    val hasInput: Boolean get() = originalText.isNotBlank()

    /** Chip list; Translate always reflects the currently chosen target language. */
    val transformations: List<Transformation>
        get() = listOf(
            Transformation.FixGrammar,
            Transformation.Rephrase,
            Transformation.Professional,
            Transformation.Casual,
            Transformation.Shorten,
            Transformation.Expand,
            Transformation.Summarize,
            Transformation.Translate(targetLanguageTag),
        )

    val resultText: String? get() = (result as? ResultState.Success)?.text
}

sealed interface ResultState {
    data object Idle : ResultState
    data object Loading : ResultState
    data class Success(val text: String) : ResultState
    data class Failure(val error: TransformError) : ResultState
}

sealed interface ProcessTextAction {
    data class Select(val transformation: Transformation) : ProcessTextAction
    data class ChangeTargetLanguage(val languageTag: String) : ProcessTextAction
    data object Retry : ProcessTextAction
    data object Copy : ProcessTextAction
    data object Replace : ProcessTextAction
    data object Dismiss : ProcessTextAction
    data object OpenSettings : ProcessTextAction
}

/** One-off events that need the Activity (clipboard, activity result, navigation). */
sealed interface ProcessTextEffect {
    data class CopyToClipboard(val text: String) : ProcessTextEffect
    data class ReturnResult(val text: String) : ProcessTextEffect
    data object Close : ProcessTextEffect
    data object OpenSettings : ProcessTextEffect
}
