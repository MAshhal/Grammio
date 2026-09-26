package com.mystic.grammio.data.provider.local

import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

/** Non-secret provider settings stored on this device. API keys live in `ApiKeyLocalDataSource`. */
interface ProviderPreferencesLocalDataSource {
    val activeProvider: Flow<AiProvider>

    suspend fun setActiveProvider(provider: AiProvider)

    fun selectedModel(provider: AiProvider): Flow<String?>

    /** A null [modelId] goes back to the provider's default. */
    suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    )

    val customBaseUrl: Flow<String?>

    suspend fun setCustomBaseUrl(url: String)
}
