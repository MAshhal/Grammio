package com.mystic.grammio.data.llm

import com.mystic.grammio.domain.model.AiProvider

/**
 * Which [LlmDataSource] speaks each provider's API. The `when` is exhaustive, so a new
 * [AiProvider] does not compile until it is wired here.
 */
class LlmDataSourceRegistry(
    private val gemini: LlmDataSource,
    private val openAi: LlmDataSource,
    private val anthropic: LlmDataSource,
) {
    fun forProvider(provider: AiProvider): LlmDataSource = when (provider) {
        AiProvider.Gemini -> gemini
        AiProvider.OpenAi, AiProvider.OpenAiCompatible -> openAi
        AiProvider.Anthropic -> anthropic
    }
}
