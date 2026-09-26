package com.mystic.grammio.data.llm.anthropic.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Successful response of `POST /v1/messages`. */
@Serializable
internal data class MessagesResponseDto(
    val content: List<ContentBlockDto> = emptyList(),
    @SerialName("stop_reason") val stopReason: String? = null,
)

/** One output block. Only `text` blocks are read; `thinking` and others are skipped. */
@Serializable
internal data class ContentBlockDto(
    val type: String,
    val text: String? = null,
)
