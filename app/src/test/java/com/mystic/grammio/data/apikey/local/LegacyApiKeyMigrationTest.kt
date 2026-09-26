package com.mystic.grammio.data.apikey.local

import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.data.apikey.local.ApiKeyPreferenceKeys.LEGACY_CIPHER_TEXT
import com.mystic.grammio.data.apikey.local.ApiKeyPreferenceKeys.LEGACY_IV
import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LegacyApiKeyMigrationTest {

    private val migration = LegacyApiKeyMigration()

    @Test
    fun `legacy key moves to the Gemini slot`() = runTest {
        val legacy = preferencesOf(LEGACY_IV to "iv", LEGACY_CIPHER_TEXT to "ct")
        assertThat(migration.shouldMigrate(legacy)).isTrue()

        val migrated = migration.migrate(legacy)

        assertThat(migrated.asMap()).containsExactly(
            ApiKeyPreferenceKeys.iv(AiProvider.Gemini),
            "iv",
            ApiKeyPreferenceKeys.cipherText(AiProvider.Gemini),
            "ct",
        )
        assertThat(migration.shouldMigrate(migrated)).isFalse()
    }

    @Test
    fun `a half-written legacy key is dropped`() = runTest {
        val migrated = migration.migrate(preferencesOf(LEGACY_IV to "iv"))

        assertThat(migrated.asMap()).isEmpty()
    }

    @Test
    fun `nothing to migrate without a legacy key`() = runTest {
        assertThat(migration.shouldMigrate(emptyPreferences())).isFalse()
    }
}
