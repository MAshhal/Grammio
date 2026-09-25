package com.mystic.grammio.data.llm.gemini.dto

import kotlinx.serialization.Serializable

/** Successful response of `models.generateContent`. */
@Serializable
internal data class GenerateContentResponseDto(
    val candidates: List<CandidateDto> = emptyList(),
    val promptFeedback: PromptFeedbackDto? = null,
)

@Serializable
internal data class CandidateDto(
    val content: ContentDto? = null,
    val finishReason: String? = null,
)

@Serializable
internal data class PromptFeedbackDto(val blockReason: String? = null)
