package com.mystic.grammio.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mystic.grammio.BuildConfig
import com.mystic.grammio.data.apikey.ApiKeyRepositoryImpl
import com.mystic.grammio.data.apikey.crypto.KeystoreCipher
import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.data.apikey.local.EncryptedApiKeyLocalDataSource
import com.mystic.grammio.data.apikey.local.LegacyApiKeyMigration
import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.data.llm.anthropic.AnthropicDataSource
import com.mystic.grammio.data.llm.gemini.GeminiDataSource
import com.mystic.grammio.data.llm.openai.OpenAiDataSource
import com.mystic.grammio.data.network.HttpClientFactory
import com.mystic.grammio.data.provider.ModelCatalogRepositoryImpl
import com.mystic.grammio.data.provider.ProviderConnectionResolver
import com.mystic.grammio.data.provider.ProviderSettingsRepositoryImpl
import com.mystic.grammio.data.provider.local.DataStoreProviderPreferencesLocalDataSource
import com.mystic.grammio.data.provider.local.ProviderPreferencesLocalDataSource
import com.mystic.grammio.data.transform.TextTransformRepositoryImpl
import com.mystic.grammio.data.transform.log.local.SqlDelightTransformationLogLocalDataSource
import com.mystic.grammio.data.transform.log.local.TransformationLogLocalDataSource
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.ModelCatalogRepository
import com.mystic.grammio.domain.repository.ProviderSettingsRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import io.ktor.client.HttpClient
import kotlin.time.Clock
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

private val apiKeyStore = named("apiKeyStore")
private val providerSettingsStore = named("providerSettingsStore")

val dataModule = module {
    single<HttpClient> { HttpClientFactory.create(enableLogging = BuildConfig.DEBUG) }
    single<Clock> { Clock.System }

    // Two DataStore files, so both are qualified and their consumers use the classic DSL to pick one.

    // API keys: their own file, so it can be excluded from backups by name.
    single<DataStore<Preferences>>(apiKeyStore) {
        PreferenceDataStoreFactory.create(migrations = listOf(LegacyApiKeyMigration())) {
            androidContext().preferencesDataStoreFile(EncryptedApiKeyLocalDataSource.DATASTORE_NAME)
        }
    }
    single<KeystoreCipher>()
    single { EncryptedApiKeyLocalDataSource(get(apiKeyStore), get()) } bind ApiKeyLocalDataSource::class
    single<ApiKeyRepositoryImpl>() bind ApiKeyRepository::class

    // Provider choice, models and custom endpoint: not secret, so backed up.
    single<DataStore<Preferences>>(providerSettingsStore) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(DataStoreProviderPreferencesLocalDataSource.DATASTORE_NAME)
        }
    }
    single {
        DataStoreProviderPreferencesLocalDataSource(get(providerSettingsStore))
    } bind ProviderPreferencesLocalDataSource::class
    single<ProviderSettingsRepositoryImpl>() bind ProviderSettingsRepository::class

    // One data source per API; the registry maps each provider to the one that speaks its API.
    single<GeminiDataSource>()
    single<OpenAiDataSource>()
    single<AnthropicDataSource>()
    single {
        LlmDataSourceRegistry(
            gemini = get<GeminiDataSource>(),
            openAi = get<OpenAiDataSource>(),
            anthropic = get<AnthropicDataSource>(),
        )
    }
    single<ProviderConnectionResolver>()
    single<ModelCatalogRepositoryImpl>() bind ModelCatalogRepository::class

    single<PromptBuilder>()
    single<ModelOutputSanitizer>()
    single<SqlDelightTransformationLogLocalDataSource>() bind TransformationLogLocalDataSource::class
    single<TextTransformRepositoryImpl>() bind TextTransformRepository::class
}
