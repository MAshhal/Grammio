package com.mystic.grammio.domain.model

/** A service that can run transformations. Each keeps its own API key and model choice. */
enum class AiProvider {
    Gemini,
    OpenAi,
    Anthropic,

    /** Any server speaking the OpenAI Chat Completions API at a base URL the user supplies. */
    OpenAiCompatible,
}
