package com.mystic.grammio.presentation.settings

import com.mystic.grammio.domain.model.AiProvider

data class SettingsUiState(
    val provider: AiProvider = AiProvider.Gemini,
    val hasApiKey: Boolean = false,
    val keyInput: String = "",
    /** Only used by [AiProvider.OpenAiCompatible]. */
    val savedBaseUrl: String? = null,
    val baseUrlInput: String = "",
    val isBaseUrlInvalid: Boolean = false,
    val defaultModelId: String? = null,
    val selectedModelId: String? = null,
    val models: ModelListUiState = ModelListUiState.Idle,
) {
    val canSave: Boolean get() = keyInput.isNotBlank()

    val needsBaseUrl: Boolean get() = provider == AiProvider.OpenAiCompatible

    val canSaveBaseUrl: Boolean get() = baseUrlInput.isNotBlank() && baseUrlInput.trim() != savedBaseUrl
}
