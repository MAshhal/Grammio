package com.mystic.grammio.data.llm.openai.mapper

import com.mystic.grammio.data.llm.openai.dto.ChatCompletionResponseDto
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome

/** Successful Chat Completions response → generated text, or why there is none. */
internal object OpenAiResponseMapper {

    fun map(response: ChatCompletionResponseDto): Outcome<String, TransformError> {
        val choice = response.choices.firstOrNull() ?: return Outcome.Failure(TransformError.Unknown)
        val message = choice.message
        if (choice.finishReason == CONTENT_FILTER || message?.refusal != null) {
            return Outcome.Failure(TransformError.ContentBlocked)
        }

        val text = message?.content.orEmpty()
        return if (text.isBlank()) Outcome.Failure(TransformError.Unknown) else Outcome.Success(text)
    }

    private const val CONTENT_FILTER = "content_filter"
}
