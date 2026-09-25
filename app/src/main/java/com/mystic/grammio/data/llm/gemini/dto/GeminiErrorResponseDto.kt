package com.mystic.grammio.data.llm.gemini.dto

import kotlinx.serialization.Serializable

/** Error body Google APIs return with a non-2xx status. */
@Serializable
internal data class GeminiErrorResponseDto(val error: GeminiErrorBodyDto)

@Serializable
internal data class GeminiErrorBodyDto(
    val code: Int = 0,
    val status: String? = null,
    val details: List<GeminiErrorDetailDto> = emptyList(),
)

@Serializable
internal data class GeminiErrorDetailDto(val reason: String? = null)
