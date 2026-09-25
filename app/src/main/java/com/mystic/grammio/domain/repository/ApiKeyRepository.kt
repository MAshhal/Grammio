package com.mystic.grammio.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * The user's LLM API key, as seen by the app. Deliberately write-only from this side: the UI can
 * know a key exists but can never read it back.
 */
interface ApiKeyRepository {
    val hasApiKey: Flow<Boolean>
    suspend fun save(apiKey: String)
    suspend fun clear()
}
