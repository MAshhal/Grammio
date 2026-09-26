package com.mystic.grammio.presentation.settings

import com.mystic.grammio.domain.model.AiProvider

sealed interface SettingsAction {
    data class ProviderSelected(val provider: AiProvider) : SettingsAction

    data class KeyInputChanged(val value: String) : SettingsAction

    data object Save : SettingsAction

    data object Clear : SettingsAction

    data class BaseUrlInputChanged(val value: String) : SettingsAction

    data object SaveBaseUrl : SettingsAction

    /** A null [modelId] goes back to the provider's default model. */
    data class ModelSelected(val modelId: String?) : SettingsAction

    data object RefreshModels : SettingsAction
}
