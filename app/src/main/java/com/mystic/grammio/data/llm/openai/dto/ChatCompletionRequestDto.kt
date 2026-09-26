package com.mystic.grammio.data.llm.openai.dto

import kotlinx.serialization.Serializable

/**
 * Body of `POST /chat/completions`. No max-tokens field: OpenAI reasoning models reject `max_tokens`
 * and many compatible servers don't know `max_completion_tokens`, so the server default applies.
 */
@Serializable
internal data class ChatCompletionRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Double? = null,
)

@Serializable
internal data class ChatMessageDto(
    val role: String,
    val content: String,
)
