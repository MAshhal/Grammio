package com.mystic.grammio.data.apikey.local

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import com.mystic.grammio.data.apikey.local.ApiKeyPreferenceKeys.LEGACY_CIPHER_TEXT
import com.mystic.grammio.data.apikey.local.ApiKeyPreferenceKeys.LEGACY_IV
import com.mystic.grammio.domain.model.AiProvider

/**
 * Moves the key saved before providers existed into the Gemini slot. The ciphertext is moved as is:
 * every provider's key is encrypted with the same Keystore key.
 */
class LegacyApiKeyMigration : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        LEGACY_IV in currentData || LEGACY_CIPHER_TEXT in currentData

    override suspend fun migrate(currentData: Preferences): Preferences {
        val prefs = currentData.toMutablePreferences()
        val iv = prefs[LEGACY_IV]
        val cipherText = prefs[LEGACY_CIPHER_TEXT]
        prefs.remove(LEGACY_IV)
        prefs.remove(LEGACY_CIPHER_TEXT)
        if (iv != null && cipherText != null) {
            prefs[ApiKeyPreferenceKeys.iv(AiProvider.Gemini)] = iv
            prefs[ApiKeyPreferenceKeys.cipherText(AiProvider.Gemini)] = cipherText
        }
        return prefs.toPreferences()
    }

    override suspend fun cleanUp() = Unit
}
