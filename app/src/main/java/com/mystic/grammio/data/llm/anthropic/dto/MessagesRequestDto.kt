package com.mystic.grammio.data.llm.anthropic.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Body of `POST /v1/messages`. */
@Serializable
internal data class MessagesRequestDto(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<MessageDto>,
    val temperature: Double? = null,
)

@Serializable
internal data class MessageDto(
    val role: String,
    val content: String,
)
