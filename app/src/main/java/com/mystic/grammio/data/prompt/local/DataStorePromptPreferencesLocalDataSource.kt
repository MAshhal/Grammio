package com.mystic.grammio.data.prompt.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Prompt settings in their own DataStore file. Nothing here is secret, so it is backed up. */
class DataStorePromptPreferencesLocalDataSource(private val dataStore: DataStore<Preferences>) :
    PromptPreferencesLocalDataSource {

    override val customSystemPrompt: Flow<String?> = dataStore.data.map { it[SYSTEM_PROMPT] }.distinctUntilChanged()

    override suspend fun setCustomSystemPrompt(prompt: String) {
        dataStore.edit { it[SYSTEM_PROMPT] = prompt }
    }

    override suspend fun clearCustomSystemPrompt() {
        dataStore.edit { it.remove(SYSTEM_PROMPT) }
    }

    companion object {
        const val DATASTORE_NAME = "prompt_settings"
        private val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
    }
}
