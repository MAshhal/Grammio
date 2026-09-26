package com.mystic.grammio.presentation.settings.transformations.editor

sealed interface TransformationEditorEffect {
    /** Saved, deleted, or there was nothing to edit: leave the editor. */
    data object Close : TransformationEditorEffect
}
