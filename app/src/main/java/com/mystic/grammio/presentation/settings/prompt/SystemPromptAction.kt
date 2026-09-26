package com.mystic.grammio.presentation.settings.prompt

sealed interface SystemPromptAction {
    data class InputChanged(val value: String) : SystemPromptAction

    data object Save : SystemPromptAction

    data object ResetToDefault : SystemPromptAction
}
