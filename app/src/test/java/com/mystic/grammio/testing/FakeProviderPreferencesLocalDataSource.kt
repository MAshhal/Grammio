package com.mystic.grammio.testing

import com.mystic.grammio.data.provider.local.ProviderPreferencesLocalDataSource
import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory provider settings, starting on Gemini like a fresh install. */
class FakeProviderPreferencesLocalDataSource(
    activeProvider: AiProvider = AiProvider.Gemini,
    customBaseUrl: String? = null,
) : ProviderPreferencesLocalDataSource {
    private val models = MutableStateFlow(emptyMap<AiProvider, String>())

    override val activeProvider = MutableStateFlow(activeProvider)

    override suspend fun setActiveProvider(provider: AiProvider) {
        activeProvider.value = provider
    }

    override fun selectedModel(provider: AiProvider): Flow<String?> = models.map { it[provider] }

    override suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    ) {
        models.value = if (modelId == null) models.value - provider else models.value + (provider to modelId)
    }

    override val customBaseUrl = MutableStateFlow(customBaseUrl)

    override suspend fun setCustomBaseUrl(url: String) {
        customBaseUrl.value = url
    }
}
