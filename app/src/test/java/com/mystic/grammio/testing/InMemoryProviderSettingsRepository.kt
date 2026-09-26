package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Provider settings held in memory, with a default model for every provider but the custom one. */
class InMemoryProviderSettingsRepository : ProviderSettingsRepository {
    private val models = MutableStateFlow(emptyMap<AiProvider, String>())

    override val activeProvider = MutableStateFlow(AiProvider.Gemini)

    override suspend fun setActiveProvider(provider: AiProvider) {
        activeProvider.value = provider
    }

    override fun defaultModel(provider: AiProvider): String? =
        if (provider == AiProvider.OpenAiCompatible) null else "${provider.name.lowercase()}-default"

    override fun selectedModel(provider: AiProvider): Flow<String?> = models.map { it[provider] }

    override suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    ) {
        models.value = if (modelId == null) models.value - provider else models.value + (provider to modelId)
    }

    override val customBaseUrl = MutableStateFlow<String?>(null)

    override suspend fun setCustomBaseUrl(url: String) {
        customBaseUrl.value = url
    }
}
