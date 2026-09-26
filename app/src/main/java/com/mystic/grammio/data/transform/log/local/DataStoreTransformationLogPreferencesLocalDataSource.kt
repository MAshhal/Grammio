package com.mystic.grammio.data.transform.log.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** The history opt-in in its own DataStore file. */
class DataStoreTransformationLogPreferencesLocalDataSource(private val dataStore: DataStore<Preferences>) :
    TransformationLogPreferencesLocalDataSource {

    override val isEnabled: Flow<Boolean> = dataStore.data.map { it[ENABLED] ?: false }.distinctUntilChanged()

    override suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[ENABLED] = enabled }
    }

    companion object {
        const val DATASTORE_NAME = "history_settings"
        private val ENABLED = booleanPreferencesKey("enabled")
    }
}
