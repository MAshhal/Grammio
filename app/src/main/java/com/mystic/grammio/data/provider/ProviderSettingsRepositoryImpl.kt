package com.mystic.grammio.data.provider

import com.mystic.grammio.data.provider.local.ProviderPreferencesLocalDataSource
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import kotlinx.coroutines.flow.Flow

class ProviderSettingsRepositoryImpl(private val localDataSource: ProviderPreferencesLocalDataSource) :
    ProviderSettingsRepository {

    override val activeProvider: Flow<AiProvider> = localDataSource.activeProvider

    override suspend fun setActiveProvider(provider: AiProvider) = localDataSource.setActiveProvider(provider)

    override fun defaultModel(provider: AiProvider): String? = ProviderDefaults.model(provider)

    override fun selectedModel(provider: AiProvider): Flow<String?> = localDataSource.selectedModel(provider)

    override suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    ) = localDataSource.setSelectedModel(provider, modelId)

    override val customBaseUrl: Flow<String?> = localDataSource.customBaseUrl

    override suspend fun setCustomBaseUrl(url: String) = localDataSource.setCustomBaseUrl(url)
}
