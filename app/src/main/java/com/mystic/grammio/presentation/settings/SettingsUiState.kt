package com.mystic.grammio.presentation.settings

data class SettingsUiState(
    val hasApiKey: Boolean = false,
    val keyInput: String = "",
) {
    val canSave: Boolean get() = keyInput.isNotBlank()
}
