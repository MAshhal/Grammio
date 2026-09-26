package com.mystic.grammio.data.llm.openai.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Successful response of `POST /chat/completions`. */
@Serializable
internal data class ChatCompletionResponseDto(val choices: List<ChoiceDto> = emptyList())

@Serializable
internal data class ChoiceDto(
    val message: ResponseMessageDto? = null,
    @SerialName("finish_reason") val finishReason: String? = null,
)

@Serializable
internal data class ResponseMessageDto(
    val content: String? = null,
    val refusal: String? = null,
)
