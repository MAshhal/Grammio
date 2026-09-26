package com.mystic.grammio.presentation.settings.transformations.editor

import com.mystic.grammio.domain.model.TransformationIcon

sealed interface TransformationEditorAction {
    data class NameChanged(val value: String) : TransformationEditorAction

    data class TaskPromptChanged(val value: String) : TransformationEditorAction

    data class IconSelected(val icon: TransformationIcon) : TransformationEditorAction

    data object Save : TransformationEditorAction

    /** Already confirmed by the user. */
    data object Delete : TransformationEditorAction
}
