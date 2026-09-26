package com.mystic.grammio.domain.usecase

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome

/**
 * Application rules for a transformation that hold regardless of which LLM sits behind the
 * repository: input is trimmed, must be non-blank, and is capped in length.
 */
class TransformTextUseCase(private val repository: TextTransformRepository) {
    suspend operator fun invoke(
        text: String,
        transformation: Transformation,
        targetLanguageTag: String,
    ): Outcome<TransformedText, TransformError> {
        val input = text.trim()
        return when {
            input.isEmpty() -> Outcome.Failure(TransformError.EmptyInput)
            input.length > MAX_INPUT_CHARS -> Outcome.Failure(TransformError.InputTooLong(MAX_INPUT_CHARS))
            else -> repository.transform(input, transformation, targetLanguageTag)
        }
    }

    companion object {
        const val MAX_INPUT_CHARS = 10_000
    }
}
