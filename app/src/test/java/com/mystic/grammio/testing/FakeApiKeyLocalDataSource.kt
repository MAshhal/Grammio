package com.mystic.grammio.testing

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.domain.model.AiProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory stand-in for the Keystore-backed store, which cannot run on the JVM. */
class FakeApiKeyLocalDataSource(storedKeys: Map<AiProvider, String> = emptyMap()) : ApiKeyLocalDataSource {
    private val keys = MutableStateFlow(storedKeys)

    override fun hasKey(provider: AiProvider) = keys.map { provider in it }

    override suspend fun read(provider: AiProvider): String? = keys.value[provider]

    override suspend fun write(
        provider: AiProvider,
        apiKey: String,
    ) {
        keys.value += provider to apiKey
    }

    override suspend fun clear(provider: AiProvider) {
        keys.value -= provider
    }
}
