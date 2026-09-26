package com.mystic.grammio.presentation.process

import com.mystic.grammio.domain.model.Transformation

sealed interface ProcessTextAction {
    data class Select(val transformation: Transformation) : ProcessTextAction

    data class ChangeTargetLanguage(val languageTag: String) : ProcessTextAction

    data object Retry : ProcessTextAction

    data object Copy : ProcessTextAction

    data object Replace : ProcessTextAction

    data object Dismiss : ProcessTextAction

    data object OpenSettings : ProcessTextAction

    data object ManageTransformations : ProcessTextAction
}
