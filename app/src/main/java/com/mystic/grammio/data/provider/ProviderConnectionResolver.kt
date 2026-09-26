package com.mystic.grammio.data.provider

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.provider.local.ProviderPreferencesLocalDataSource
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome
import kotlinx.coroutines.flow.first

/** Turns stored settings into an [LlmConnection], or says what is still missing. */
class ProviderConnectionResolver(
    private val apiKeys: ApiKeyLocalDataSource,
    private val preferences: ProviderPreferencesLocalDataSource,
) {

    suspend fun activeProvider(): AiProvider = preferences.activeProvider.first()

    /** A connection to [provider]'s selected model, falling back to its default model. */
    suspend fun resolve(provider: AiProvider): Outcome<LlmConnection, TransformError> {
        val apiKey = apiKeys.read(provider) ?: return Outcome.Failure(TransformError.MissingApiKey)
        val baseUrl = baseUrl(provider) ?: return Outcome.Failure(TransformError.ProviderNotConfigured)
        val modelId = preferences.selectedModel(provider).first()
            ?: ProviderDefaults.model(provider)
            ?: return Outcome.Failure(TransformError.ProviderNotConfigured)
        return Outcome.Success(LlmConnection(apiKey = apiKey, modelId = modelId, baseUrl = baseUrl))
    }

    private suspend fun baseUrl(provider: AiProvider): String? =
        ProviderDefaults.baseUrl(provider) ?: preferences.customBaseUrl.first()
}
