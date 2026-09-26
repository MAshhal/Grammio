package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.Flow

/**
 * The user's API key for each provider, as seen by the app. Deliberately write-only from this side:
 * the UI can know a key exists but can never read it back.
 */
interface ApiKeyRepository {
    fun hasApiKey(provider: AiProvider): Flow<Boolean>

    suspend fun save(
        provider: AiProvider,
        apiKey: String,
    )

    suspend fun clear(provider: AiProvider)
}
