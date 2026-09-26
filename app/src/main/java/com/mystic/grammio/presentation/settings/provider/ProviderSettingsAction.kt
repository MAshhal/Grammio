package com.mystic.grammio.presentation.settings.provider

import com.mystic.grammio.domain.model.AiProvider

sealed interface ProviderSettingsAction {
    data class ProviderSelected(val provider: AiProvider) : ProviderSettingsAction

    data class KeyInputChanged(val value: String) : ProviderSettingsAction

    data object Save : ProviderSettingsAction

    data object Clear : ProviderSettingsAction

    data class BaseUrlInputChanged(val value: String) : ProviderSettingsAction

    data object SaveBaseUrl : ProviderSettingsAction

    /** A null [modelId] goes back to the provider's default model. */
    data class ModelSelected(val modelId: String?) : ProviderSettingsAction

    data object RefreshModels : ProviderSettingsAction
}
