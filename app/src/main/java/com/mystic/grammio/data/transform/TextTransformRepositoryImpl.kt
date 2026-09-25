package com.mystic.grammio.data.transform

import com.mystic.grammio.data.llm.LlmProvider
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome

class TextTransformRepositoryImpl(
    private val promptBuilder: PromptBuilder,
    private val llmProvider: LlmProvider,
    private val sanitizer: ModelOutputSanitizer,
) : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> =
        when (val outcome = llmProvider.generate(promptBuilder.build(text, transformation))) {
            is Outcome.Failure -> outcome

            is Outcome.Success -> {
                val cleaned = sanitizer.sanitize(outcome.value)
                if (cleaned.isEmpty()) {
                    Outcome.Failure(TransformError.Unknown)
                } else {
                    Outcome.Success(TransformedText(cleaned, transformation))
                }
            }
        }
}
