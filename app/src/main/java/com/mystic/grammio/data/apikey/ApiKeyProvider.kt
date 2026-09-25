package com.mystic.grammio.data.apikey

/** Data-layer-only read access to the raw API key, for LLM providers. */
fun interface ApiKeyProvider {
    suspend fun apiKey(): String?
}
