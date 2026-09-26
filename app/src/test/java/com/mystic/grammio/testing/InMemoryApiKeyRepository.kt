package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Keeps the saved keys in [saved] so tests can assert on them. */
class InMemoryApiKeyRepository : ApiKeyRepository {
    private val keys = MutableStateFlow(emptyMap<AiProvider, String>())

    val saved: Map<AiProvider, String> get() = keys.value

    override fun hasApiKey(provider: AiProvider): Flow<Boolean> = keys.map { provider in it }

    override suspend fun save(
        provider: AiProvider,
        apiKey: String,
    ) {
        keys.value += provider to apiKey
    }

    override suspend fun clear(provider: AiProvider) {
        keys.value -= provider
    }
}
