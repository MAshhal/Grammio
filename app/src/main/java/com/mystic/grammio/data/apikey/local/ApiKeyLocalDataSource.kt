package com.mystic.grammio.data.apikey.local

import kotlinx.coroutines.flow.Flow

/**
 * The user's API key as stored on this device. Data-layer only: it is the one place the raw key
 * can be read, so nothing outside `data/` may depend on it.
 */
interface ApiKeyLocalDataSource {
    val hasKey: Flow<Boolean>

    /** The stored key, or null when none is saved or it can no longer be decrypted. */
    suspend fun read(): String?

    suspend fun write(apiKey: String)

    suspend fun clear()
}
