package com.mystic.grammio.presentation.settings.prompt

data class SystemPromptUiState(
    val input: String = "",
    val savedPrompt: String = "",
    val defaultPrompt: String = "",
) {
    val canSave: Boolean get() = input.trim() != savedPrompt

    val isDefault: Boolean get() = savedPrompt == defaultPrompt

    /** Offered while either what is saved or what is typed differs from the default. */
    val canReset: Boolean get() = !isDefault || input != defaultPrompt
}
