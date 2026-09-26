package com.mystic.grammio.data.transformation.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

/**
 * Transformation bookkeeping in its own DataStore file. Backed up together with the database, so a
 * restored device doesn't seed the defaults a second time.
 */
class DataStoreTransformationPreferencesLocalDataSource(private val dataStore: DataStore<Preferences>) :
    TransformationPreferencesLocalDataSource {

    override suspend fun areDefaultsSeeded(): Boolean = dataStore.data.first()[DEFAULTS_SEEDED] ?: false

    override suspend fun markDefaultsSeeded() {
        dataStore.edit { it[DEFAULTS_SEEDED] = true }
    }

    companion object {
        const val DATASTORE_NAME = "transformation_settings"
        private val DEFAULTS_SEEDED = booleanPreferencesKey("defaults_seeded")
    }
}
