package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

/** Which provider runs transformations, and how each one is set up (everything except its key). */
interface ProviderSettingsRepository {
    val activeProvider: Flow<AiProvider>

    suspend fun setActiveProvider(provider: AiProvider)

    /** The model used when the user hasn't picked one, or null when [provider] has none. */
    fun defaultModel(provider: AiProvider): String?

    /** The model the user picked for [provider], or null to use the provider's default. */
    fun selectedModel(provider: AiProvider): Flow<String?>

    suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    )

    /** Base URL of the [AiProvider.OpenAiCompatible] endpoint, or null until the user sets one. */
    val customBaseUrl: Flow<String?>

    suspend fun setCustomBaseUrl(url: String)
}
