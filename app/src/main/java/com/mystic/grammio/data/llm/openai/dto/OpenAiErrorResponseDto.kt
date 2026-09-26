package com.mystic.grammio.data.llm.openai.dto

import kotlinx.serialization.Serializable

/** Error body OpenAI (and most compatible servers) return with a non-2xx status. */
@Serializable
internal data class OpenAiErrorResponseDto(val error: OpenAiErrorBodyDto)

@Serializable
internal data class OpenAiErrorBodyDto(
    val message: String? = null,
    val type: String? = null,
    val param: String? = null,
)
