package com.mystic.grammio.data.apikey.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.mystic.grammio.data.apikey.crypto.EncryptedValue
import com.mystic.grammio.data.apikey.crypto.KeystoreCipher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/** Keeps the key encrypted by [KeystoreCipher] in its own DataStore file, which is excluded from backups. */
class EncryptedApiKeyLocalDataSource(
    private val dataStore: DataStore<Preferences>,
    private val cipher: KeystoreCipher,
) : ApiKeyLocalDataSource {

    override val hasKey: Flow<Boolean> =
        dataStore.data.map { it[CIPHER_TEXT] != null }.distinctUntilChanged()

    override suspend fun read(): String? {
        val prefs = dataStore.data.first()
        val iv = prefs[IV] ?: return null
        val cipherText = prefs[CIPHER_TEXT] ?: return null
        return try {
            withContext(Dispatchers.Default) { cipher.decrypt(EncryptedValue(iv, cipherText)) }
        } catch (e: Exception) {
            // Keystore key lost (e.g. data restored onto another device): the ciphertext is useless.
            Logger.withTag("ApiKey").w { "Stored API key could not be decrypted (${e::class.simpleName}); clearing it" }
            clear()
            null
        }
    }

    override suspend fun write(apiKey: String) {
        val encrypted = withContext(Dispatchers.Default) { cipher.encrypt(apiKey) }
        dataStore.edit {
            it[IV] = encrypted.iv
            it[CIPHER_TEXT] = encrypted.cipherText
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(IV)
            it.remove(CIPHER_TEXT)
        }
    }

    companion object {
        const val DATASTORE_NAME = "api_key"
        private val IV = stringPreferencesKey("iv")
        private val CIPHER_TEXT = stringPreferencesKey("cipher_text")
    }
}
