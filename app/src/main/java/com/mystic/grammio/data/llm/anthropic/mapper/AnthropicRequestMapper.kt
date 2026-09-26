package com.mystic.grammio.data.llm.anthropic.mapper

import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.anthropic.dto.MessageDto
import com.mystic.grammio.data.llm.anthropic.dto.MessagesRequestDto

/** [LlmPrompt] → Messages API request body. */
internal object AnthropicRequestMapper {

    fun map(
        prompt: LlmPrompt,
        modelId: String,
        maxTokens: Int,
        includeTemperature: Boolean,
    ) = MessagesRequestDto(
        model = modelId,
        maxTokens = maxTokens,
        system = prompt.systemInstruction,
        messages = listOf(MessageDto(role = USER_ROLE, content = prompt.userText)),
        temperature = prompt.temperature.takeIf { includeTemperature },
    )

    private const val USER_ROLE = "user"
}
