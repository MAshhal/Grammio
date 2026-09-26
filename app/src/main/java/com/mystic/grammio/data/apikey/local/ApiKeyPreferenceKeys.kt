package com.mystic.grammio.data.apikey.local

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mystic.grammio.data.provider.storageKey
import com.mystic.grammio.domain.model.AiProvider

/** Where each provider's encrypted key lives in the `api_key` DataStore file. */
internal object ApiKeyPreferenceKeys {
    fun iv(provider: AiProvider): Preferences.Key<String> = stringPreferencesKey("${provider.storageKey}_iv")

    fun cipherText(provider: AiProvider): Preferences.Key<String> =
        stringPreferencesKey("${provider.storageKey}_cipher_text")

    /** The single, Gemini-only key slot used before providers existed. */
    val LEGACY_IV = stringPreferencesKey("iv")
    val LEGACY_CIPHER_TEXT = stringPreferencesKey("cipher_text")
}
