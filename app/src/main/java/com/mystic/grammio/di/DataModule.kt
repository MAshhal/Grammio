package com.mystic.grammio.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mystic.grammio.data.apikey.ApiKeyProvider
import com.mystic.grammio.data.apikey.EncryptedApiKeyStore
import com.mystic.grammio.data.apikey.KeystoreCipher
import com.mystic.grammio.data.fake.FakeTextTransformRepository
import com.mystic.grammio.domain.repository.ApiKeyRepository
import com.mystic.grammio.domain.repository.TextTransformRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val dataModule = module {
    // API key: its own DataStore file, so it can be excluded from backups by name.
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(EncryptedApiKeyStore.DATASTORE_NAME)
        }
    }
    single<KeystoreCipher>()
    single<EncryptedApiKeyStore>() binds arrayOf(ApiKeyRepository::class, ApiKeyProvider::class)

    single<FakeTextTransformRepository>() bind TextTransformRepository::class
}
