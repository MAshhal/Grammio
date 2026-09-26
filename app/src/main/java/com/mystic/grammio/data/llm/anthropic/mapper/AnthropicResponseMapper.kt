package com.mystic.grammio.data.llm.anthropic.mapper

import com.mystic.grammio.data.llm.anthropic.dto.MessagesResponseDto
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome

/** Successful Messages API response → generated text, or why there is none. */
internal object AnthropicResponseMapper {

    fun map(response: MessagesResponseDto): Outcome<String, TransformError> {
        if (response.stopReason == REFUSAL) return Outcome.Failure(TransformError.ContentBlocked)

        // Models with thinking on by default also return `thinking` blocks, which are not the answer.
        val text = response.content
            .filter { it.type == TEXT_BLOCK }
            .mapNotNull { it.text }
            .joinToString("")
        return if (text.isBlank()) Outcome.Failure(TransformError.Unknown) else Outcome.Success(text)
    }

    private const val REFUSAL = "refusal"
    private const val TEXT_BLOCK = "text"
}
