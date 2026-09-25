package com.mystic.grammio.data.llm.gemini.dto

import kotlinx.serialization.Serializable

/** Body of `models.generateContent`. */
@Serializable
internal data class GenerateContentRequestDto(
    val systemInstruction: ContentDto,
    val contents: List<ContentDto>,
    val generationConfig: GenerationConfigDto,
)

@Serializable
internal data class GenerationConfigDto(
    val temperature: Double,
    val maxOutputTokens: Int,
)
