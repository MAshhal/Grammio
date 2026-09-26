package com.mystic.grammio.data.llm.openai.mapper

import com.mystic.grammio.data.llm.LlmPrompt
import com.mystic.grammio.data.llm.openai.dto.ChatCompletionRequestDto
import com.mystic.grammio.data.llm.openai.dto.ChatMessageDto

/** [LlmPrompt] → Chat Completions request body. */
internal object OpenAiRequestMapper {

    fun map(
        prompt: LlmPrompt,
        modelId: String,
        includeTemperature: Boolean,
    ) = ChatCompletionRequestDto(
        model = modelId,
        messages = listOf(
            ChatMessageDto(role = SYSTEM_ROLE, content = prompt.systemInstruction),
            ChatMessageDto(role = USER_ROLE, content = prompt.userText),
        ),
        temperature = prompt.temperature.takeIf { includeTemperature },
    )

    private const val SYSTEM_ROLE = "system"
    private const val USER_ROLE = "user"
}
