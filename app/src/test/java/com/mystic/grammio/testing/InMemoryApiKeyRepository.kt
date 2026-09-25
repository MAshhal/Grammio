package com.mystic.grammio.testing

import com.mystic.grammio.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** Keeps the last saved key in [saved] so tests can assert on it. */
class InMemoryApiKeyRepository : ApiKeyRepository {
    var saved: String? = null
        private set

    override val hasApiKey = MutableStateFlow(false)

    override suspend fun save(apiKey: String) {
        saved = apiKey
        hasApiKey.value = true
    }

    override suspend fun clear() {
        saved = null
        hasApiKey.value = false
    }
}
