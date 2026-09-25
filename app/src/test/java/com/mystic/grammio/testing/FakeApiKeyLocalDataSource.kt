package com.mystic.grammio.testing

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory stand-in for the Keystore-backed store, which cannot run on the JVM. */
class FakeApiKeyLocalDataSource(storedKey: String? = null) : ApiKeyLocalDataSource {
    private val key = MutableStateFlow(storedKey)

    override val hasKey = key.map { it != null }

    override suspend fun read(): String? = key.value

    override suspend fun write(apiKey: String) {
        key.value = apiKey
    }

    override suspend fun clear() {
        key.value = null
    }
}
