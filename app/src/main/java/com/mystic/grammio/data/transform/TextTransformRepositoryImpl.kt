package com.mystic.grammio.data.transform

import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.data.provider.ProviderConnectionResolver
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome

/** Orchestrates a transformation: active provider → connection → prompt → LLM → cleaned-up text. */
class TextTransformRepositoryImpl(
    private val connectionResolver: ProviderConnectionResolver,
    private val promptBuilder: PromptBuilder,
    private val llmDataSources: LlmDataSourceRegistry,
    private val sanitizer: ModelOutputSanitizer,
) : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> {
        val provider = connectionResolver.activeProvider()
        val connection = when (val resolved = connectionResolver.resolve(provider)) {
            is Outcome.Failure -> return resolved
            is Outcome.Success -> resolved.value
        }
        val prompt = promptBuilder.build(text, transformation)

        return when (val outcome = llmDataSources.forProvider(provider).generate(prompt, connection)) {
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
}
