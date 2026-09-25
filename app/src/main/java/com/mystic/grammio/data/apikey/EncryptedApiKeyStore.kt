package com.mystic.grammio.data.apikey

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import com.mystic.grammio.domain.repository.ApiKeyRepository
import java.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Stores the API key encrypted with [KeystoreCipher] in a dedicated DataStore file (excluded from
 * backups). Implements the write-side domain port and the data-only read side.
 */
class EncryptedApiKeyStore(
    private val dataStore: DataStore<Preferences>,
    private val cipher: KeystoreCipher,
) : ApiKeyRepository, ApiKeyProvider {

    override val hasApiKey: Flow<Boolean> =
        dataStore.data.map { it[CIPHER_TEXT] != null }.distinctUntilChanged()

    override suspend fun save(apiKey: String) {
        val encrypted = withContext(Dispatchers.Default) { cipher.encrypt(apiKey.trim().toByteArray()) }
        dataStore.edit {
            it[IV] = encrypted.iv.toBase64()
            it[CIPHER_TEXT] = encrypted.cipherText.toBase64()
        }
    }

    override suspend fun clear() {
        dataStore.edit {
            it.remove(IV)
            it.remove(CIPHER_TEXT)
        }
    }

    override suspend fun apiKey(): String? {
        val prefs = dataStore.data.first()
        val iv = prefs[IV] ?: return null
        val cipherText = prefs[CIPHER_TEXT] ?: return null
        return try {
            withContext(Dispatchers.Default) {
                String(cipher.decrypt(KeystoreCipher.Encrypted(iv.fromBase64(), cipherText.fromBase64())))
            }
        } catch (e: Exception) {
            // Keystore key lost (e.g. data restored onto another device): the ciphertext is useless.
            Logger.withTag("ApiKey").w { "Stored API key could not be decrypted (${e::class.simpleName}); clearing it" }
            clear()
            null
        }
    }

    private fun ByteArray.toBase64(): String = Base64.getEncoder().encodeToString(this)
    private fun String.fromBase64(): ByteArray = Base64.getDecoder().decode(this)

    companion object {
        const val DATASTORE_NAME = "api_key"
        private val IV = stringPreferencesKey("iv")
        private val CIPHER_TEXT = stringPreferencesKey("cipher_text")
    }
}
