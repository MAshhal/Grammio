package com.mystic.grammio.data.apikey.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import co.touchlab.kermit.Logger
import com.mystic.grammio.data.apikey.crypto.EncryptedValue
import com.mystic.grammio.data.apikey.crypto.KeystoreCipher
import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Keeps each provider's key encrypted by [KeystoreCipher] in its own DataStore file, which is excluded from backups. */
class EncryptedApiKeyLocalDataSource(
    private val dataStore: DataStore<Preferences>,
    private val cipher: KeystoreCipher,
) : ApiKeyLocalDataSource {

    override fun hasKey(provider: AiProvider): Flow<Boolean> {
        val cipherTextKey = ApiKeyPreferenceKeys.cipherText(provider)
        return dataStore.data.map { it[cipherTextKey] != null }.distinctUntilChanged()
    }

    override suspend fun read(provider: AiProvider): String? {
        val prefs = dataStore.data.first()
        val iv = prefs[ApiKeyPreferenceKeys.iv(provider)] ?: return null
        val cipherText = prefs[ApiKeyPreferenceKeys.cipherText(provider)] ?: return null
        return try {
            withContext(Dispatchers.Default) { cipher.decrypt(EncryptedValue(iv, cipherText)) }
        } catch (e: Exception) {
            // Keystore key lost (e.g. data restored onto another device): the ciphertext is useless.
            Logger.withTag("ApiKey").w { "Stored API key could not be decrypted (${e::class.simpleName}); clearing it" }
            clear(provider)
            null
        }
    }

    override suspend fun write(
        provider: AiProvider,
        apiKey: String,
    ) {
        val encrypted = withContext(Dispatchers.Default) { cipher.encrypt(apiKey) }
        dataStore.edit {
            it[ApiKeyPreferenceKeys.iv(provider)] = encrypted.iv
            it[ApiKeyPreferenceKeys.cipherText(provider)] = encrypted.cipherText
        }
    }

    override suspend fun clear(provider: AiProvider) {
        dataStore.edit {
            it.remove(ApiKeyPreferenceKeys.iv(provider))
            it.remove(ApiKeyPreferenceKeys.cipherText(provider))
        }
    }

    companion object {
        const val DATASTORE_NAME = "api_key"
    }
}
