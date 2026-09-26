package com.mystic.grammio.data.provider.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mystic.grammio.data.provider.aiProviderForStorageKey
import com.mystic.grammio.data.provider.storageKey
import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Provider settings in their own DataStore file. Nothing here is secret, so it is backed up. */
class DataStoreProviderPreferencesLocalDataSource(private val dataStore: DataStore<Preferences>) :
    ProviderPreferencesLocalDataSource {

    override val activeProvider: Flow<AiProvider> = dataStore.data
        .map { prefs -> prefs[ACTIVE_PROVIDER]?.let(::aiProviderForStorageKey) ?: DEFAULT_PROVIDER }
        .distinctUntilChanged()

    override suspend fun setActiveProvider(provider: AiProvider) {
        dataStore.edit { it[ACTIVE_PROVIDER] = provider.storageKey }
    }

    override fun selectedModel(provider: AiProvider): Flow<String?> {
        val key = modelKey(provider)
        return dataStore.data.map { it[key] }.distinctUntilChanged()
    }

    override suspend fun setSelectedModel(
        provider: AiProvider,
        modelId: String?,
    ) {
        dataStore.edit {
            if (modelId == null) it.remove(modelKey(provider)) else it[modelKey(provider)] = modelId
        }
    }

    override val customBaseUrl: Flow<String?> = dataStore.data.map { it[CUSTOM_BASE_URL] }.distinctUntilChanged()

    override suspend fun setCustomBaseUrl(url: String) {
        dataStore.edit { it[CUSTOM_BASE_URL] = url }
    }

    private fun modelKey(provider: AiProvider) = stringPreferencesKey("model_${provider.storageKey}")

    companion object {
        const val DATASTORE_NAME = "provider_settings"

        /** Grammio only spoke Gemini before providers existed, so existing users stay on it. */
        private val DEFAULT_PROVIDER = AiProvider.Gemini
        private val ACTIVE_PROVIDER = stringPreferencesKey("active_provider")
        private val CUSTOM_BASE_URL = stringPreferencesKey("custom_base_url")
    }
}
