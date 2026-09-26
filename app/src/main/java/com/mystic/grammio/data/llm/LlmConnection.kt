package com.mystic.grammio.data.llm

/** Where and as whom to call an LLM: resolved per request, so data sources hold no configuration. */
data class LlmConnection(
    val apiKey: String,
    val modelId: String,
    val baseUrl: String,
) {
    // Data class toString would print the key into any log or crash report that includes this object.
    override fun toString(): String = "LlmConnection(modelId=$modelId, baseUrl=$baseUrl)"
}
