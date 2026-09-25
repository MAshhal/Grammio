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
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.gemini.GeminiDataSource
import com.mystic.grammio.data.network.HttpClientFactory
import com.mystic.grammio.data.transform.TextTransformRepositoryImpl
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import io.ktor.client.HttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dataModule = module {
    single<HttpClient> { HttpClientFactory.create(enableLogging = BuildConfig.DEBUG) }

    // API key: its own DataStore file, so it can be excluded from backups by name.
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(EncryptedApiKeyLocalDataSource.DATASTORE_NAME)
        }
    }
    single<KeystoreCipher>()
    single<EncryptedApiKeyLocalDataSource>() bind ApiKeyLocalDataSource::class
    single<ApiKeyRepositoryImpl>() bind ApiKeyRepository::class

    // Swap the LLM vendor here.
    single<GeminiDataSource>() bind LlmDataSource::class
    single<PromptBuilder>()
    single<ModelOutputSanitizer>()
    single<TextTransformRepositoryImpl>() bind TextTransformRepository::class
}
