package com.mystic.grammio.presentation.settings.transformations.editor

import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformationIcon

data class TransformationEditorUiState(
    val isNew: Boolean,
    /** True while an existing transformation is being read; the fields are empty until then. */
    val isLoading: Boolean = !isNew,
    val name: String = "",
    val taskPrompt: String = "",
    val icon: TransformationIcon = TransformationIcon.Sparkle,
) {
    val canSave: Boolean get() = !isLoading && name.isNotBlank() && taskPrompt.isNotBlank()

    val usesTargetLanguage: Boolean get() = Transformation.LANGUAGE_PLACEHOLDER in taskPrompt
}
