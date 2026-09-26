package com.mystic.grammio.data.llm

/** Where and as whom to reach a provider, before any model is chosen. Enough to list models. */
data class LlmEndpoint(
    val apiKey: String,
    val baseUrl: String,
) {
    // Data class toString would print the key into any log or crash report that includes this object.
    override fun toString(): String = "LlmEndpoint(baseUrl=$baseUrl)"
}
