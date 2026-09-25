package com.mystic.grammio.data.llm.gemini

import kotlinx.serialization.Serializable

// Wire models for models.generateContent. Only the fields Grammio reads or sends.

@Serializable
internal data class GenerateContentRequest(
    val systemInstruction: ContentDto,
    val contents: List<ContentDto>,
    val generationConfig: GenerationConfigDto,
)

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

@Serializable
internal data class GenerationConfigDto(
    val temperature: Double,
    val maxOutputTokens: Int,
)

@Serializable
internal data class GenerateContentResponse(
    val candidates: List<CandidateDto> = emptyList(),
    val promptFeedback: PromptFeedbackDto? = null,
)

@Serializable
internal data class CandidateDto(
    val content: ContentDto? = null,
    val finishReason: String? = null,
)

@Serializable
internal data class PromptFeedbackDto(
    val blockReason: String? = null,
)

@Serializable
internal data class GeminiErrorResponse(
    val error: GeminiErrorBody,
)

@Serializable
internal data class GeminiErrorBody(
    val code: Int = 0,
    val status: String? = null,
    val details: List<GeminiErrorDetail> = emptyList(),
)

@Serializable
internal data class GeminiErrorDetail(
    val reason: String? = null,
)
