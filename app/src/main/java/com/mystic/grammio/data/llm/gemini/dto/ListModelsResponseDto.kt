package com.mystic.grammio.data.llm.gemini.dto

import kotlinx.serialization.Serializable

/** Response of `GET models`. */
@Serializable
internal data class ListModelsResponseDto(val models: List<GeminiModelDto> = emptyList())

@Serializable
internal data class GeminiModelDto(
    /** Resource name, such as `models/gemini-3.5-flash-lite`. */
    val name: String,
    val displayName: String? = null,
    val supportedGenerationMethods: List<String> = emptyList(),
)
