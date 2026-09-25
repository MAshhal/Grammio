package com.mystic.grammio.data.llm.gemini.dto

import kotlinx.serialization.Serializable

// Shared by request and response. Wire models only hold the fields Grammio sends or reads.

@Serializable
internal data class ContentDto(
    val role: String? = null,
    val parts: List<PartDto> = emptyList(),
)

@Serializable
internal data class PartDto(
    val text: String? = null,
    val thought: Boolean? = null,
)
