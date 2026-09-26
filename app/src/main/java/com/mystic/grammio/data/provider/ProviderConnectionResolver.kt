package com.mystic.grammio.data.provider

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmEndpoint
import com.mystic.grammio.data.provider.local.ProviderPreferencesLocalDataSource
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.result.Outcome
import kotlinx.coroutines.flow.first

/** Turns stored settings into an [LlmEndpoint] or [LlmConnection], or says what is still missing. */
class ProviderConnectionResolver(
    private val apiKeys: ApiKeyLocalDataSource,
    private val preferences: ProviderPreferencesLocalDataSource,
) {

    suspend fun activeProvider(): AiProvider = preferences.activeProvider.first()

    /** [provider]'s key and base URL: enough to list its models. */
    suspend fun endpoint(provider: AiProvider): Outcome<LlmEndpoint, TransformError> {
        val apiKey = apiKeys.read(provider) ?: return Outcome.Failure(TransformError.MissingApiKey)
        val baseUrl = ProviderDefaults.baseUrl(provider)
            ?: preferences.customBaseUrl.first()
            ?: return Outcome.Failure(TransformError.ProviderNotConfigured)
        return Outcome.Success(LlmEndpoint(apiKey = apiKey, baseUrl = baseUrl))
    }

    /** A connection to [provider]'s selected model, falling back to its default model. */
    suspend fun resolve(provider: AiProvider): Outcome<LlmConnection, TransformError> {
        val endpoint = when (val resolved = endpoint(provider)) {
            is Outcome.Failure -> return resolved
            is Outcome.Success -> resolved.value
        }
        val modelId = preferences.selectedModel(provider).first()
            ?: ProviderDefaults.model(provider)
            ?: return Outcome.Failure(TransformError.ProviderNotConfigured)
        return Outcome.Success(LlmConnection(apiKey = endpoint.apiKey, modelId = modelId, baseUrl = endpoint.baseUrl))
    }
}
