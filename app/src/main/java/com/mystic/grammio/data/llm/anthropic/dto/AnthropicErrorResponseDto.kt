package com.mystic.grammio.data.llm.anthropic.dto

import kotlinx.serialization.Serializable

/** Error body the Claude API returns with a non-2xx status. */
@Serializable
internal data class AnthropicErrorResponseDto(val error: AnthropicErrorBodyDto)

@Serializable
internal data class AnthropicErrorBodyDto(
    val type: String? = null,
    val message: String? = null,
)
