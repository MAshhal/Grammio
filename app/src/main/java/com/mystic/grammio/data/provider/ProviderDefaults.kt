package com.mystic.grammio.data.provider

import com.mystic.grammio.domain.model.AiProvider

/** Where each provider lives and which model it uses until the user picks another. */
object ProviderDefaults {

    /** Null for [AiProvider.OpenAiCompatible], whose base URL the user supplies. */
    fun baseUrl(provider: AiProvider): String? = when (provider) {
        AiProvider.Gemini -> "https://generativelanguage.googleapis.com/v1beta"
        AiProvider.OpenAi -> "https://api.openai.com/v1"
        AiProvider.Anthropic -> "https://api.anthropic.com/v1"
        AiProvider.OpenAiCompatible -> null
    }

    /**
     * Fast, low-cost models that suit short rewrites. Null for [AiProvider.OpenAiCompatible]: an
     * arbitrary server has no model we can assume, so the user must pick one.
     */
    fun model(provider: AiProvider): String? = when (provider) {
        AiProvider.Gemini -> "gemini-3.5-flash-lite"
        AiProvider.OpenAi -> "gpt-5.4-nano"
        AiProvider.Anthropic -> "claude-haiku-4-5"
        AiProvider.OpenAiCompatible -> null
    }
}
