package com.mystic.grammio.data.llm.anthropic.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response of `GET /v1/models`, newest models first. */
@Serializable
internal data class AnthropicModelListResponseDto(val data: List<AnthropicModelDto> = emptyList())

@Serializable
internal data class AnthropicModelDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
)
