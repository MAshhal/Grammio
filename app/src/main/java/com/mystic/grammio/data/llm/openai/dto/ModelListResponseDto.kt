package com.mystic.grammio.data.llm.openai.dto

import kotlinx.serialization.Serializable

/** Response of `GET /models`. */
@Serializable
internal data class ModelListResponseDto(val data: List<OpenAiModelDto> = emptyList())

@Serializable
internal data class OpenAiModelDto(val id: String)
