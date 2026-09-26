package com.mystic.grammio.data.apikey.local

import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

/**
 * The user's API key for each provider as stored on this device. Data-layer only: it is the one
 * place a raw key can be read, so nothing outside `data/` may depend on it.
 */
interface ApiKeyLocalDataSource {
    fun hasKey(provider: AiProvider): Flow<Boolean>

    /** The stored key, or null when none is saved or it can no longer be decrypted. */
    suspend fun read(provider: AiProvider): String?

    suspend fun write(
        provider: AiProvider,
        apiKey: String,
    )

    suspend fun clear(provider: AiProvider)
}
