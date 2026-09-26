package com.mystic.grammio.presentation.settings.transformations

sealed interface TransformationsAction {
    data class EnabledChanged(
        val id: String,
        val enabled: Boolean,
    ) : TransformationsAction

    data class MoveUp(val id: String) : TransformationsAction

    data class MoveDown(val id: String) : TransformationsAction

    data object RestoreDefaults : TransformationsAction
}
