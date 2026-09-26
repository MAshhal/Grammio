package com.mystic.grammio.data.transform

import com.mystic.grammio.data.apikey.local.ApiKeyLocalDataSource
import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSource
import com.mystic.grammio.data.llm.gemini.GeminiDataSource
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome

/** Orchestrates a transformation: API key → prompt → LLM → cleaned-up text. */
class TextTransformRepositoryImpl(
    private val apiKeyDataSource: ApiKeyLocalDataSource,
    private val promptBuilder: PromptBuilder,
    private val llmDataSource: LlmDataSource,
    private val sanitizer: ModelOutputSanitizer,
) : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> {
        val apiKey = apiKeyDataSource.read() ?: return Outcome.Failure(TransformError.MissingApiKey)
        val prompt = promptBuilder.build(text, transformation)
        val connection = LlmConnection(apiKey, GeminiDataSource.MODEL, GeminiDataSource.BASE_URL)

        return when (val outcome = llmDataSource.generate(prompt, connection)) {
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
