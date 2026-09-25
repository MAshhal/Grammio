package com.mystic.grammio.data.llm.gemini.mapper

import com.mystic.grammio.data.llm.gemini.dto.GenerateContentResponseDto
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome

/** Successful Gemini response → generated text, or why there is none. */
internal object GeminiResponseMapper {

    fun map(response: GenerateContentResponseDto): Outcome<String, TransformError> {
        if (response.promptFeedback?.blockReason != null) return Outcome.Failure(TransformError.ContentBlocked)

        val candidate = response.candidates.firstOrNull()
            ?: return Outcome.Failure(TransformError.Unknown)
        if (candidate.finishReason in BLOCKING_FINISH_REASONS) return Outcome.Failure(TransformError.ContentBlocked)

        val text = candidate.content?.parts.orEmpty()
            .filter { it.thought != true }
            .mapNotNull { it.text }
            .joinToString("")
        return if (text.isBlank()) Outcome.Failure(TransformError.Unknown) else Outcome.Success(text)
    }

    private val BLOCKING_FINISH_REASONS = setOf("SAFETY", "PROHIBITED_CONTENT", "BLOCKLIST", "SPII", "RECITATION")
}
