package com.mystic.grammio.data.transform

import com.mystic.grammio.data.llm.LlmConnection
import com.mystic.grammio.data.llm.LlmDataSourceRegistry
import com.mystic.grammio.data.provider.ProviderConnectionResolver
import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.data.transform.log.local.TransformationLogLocalDataSource
import com.mystic.grammio.data.transform.log.local.TransformationLogPreferencesLocalDataSource
import com.mystic.grammio.data.transform.prompt.PromptBuilder
import com.mystic.grammio.data.transform.sanitize.ModelOutputSanitizer
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.domain.result.map
import kotlin.time.Clock
import kotlin.time.TimeSource
import kotlinx.coroutines.flow.first

/**
 * Orchestrates a transformation: active provider → connection → prompt → LLM → cleaned-up text.
 * When the user has opted in, every attempt that gets this far, successful or not, is recorded in
 * the transformation log.
 */
class TextTransformRepositoryImpl(
    private val connectionResolver: ProviderConnectionResolver,
    private val promptBuilder: PromptBuilder,
    private val llmDataSources: LlmDataSourceRegistry,
    private val sanitizer: ModelOutputSanitizer,
    private val transformationLog: TransformationLogLocalDataSource,
    private val logPreferences: TransformationLogPreferencesLocalDataSource,
    private val clock: Clock,
) : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> {
        val startedAt = clock.now()
        val started = TimeSource.Monotonic.markNow()
        val provider = connectionResolver.activeProvider()
        val connection = connectionResolver.resolve(provider)
        val result = when (connection) {
            is Outcome.Failure -> connection
            is Outcome.Success -> generate(text, transformation, provider, connection.value)
        }

        if (logPreferences.isEnabled.first()) {
            transformationLog.record(
                TransformationLogEntry(
                    startedAt = startedAt,
                    transformation = transformation,
                    provider = provider,
                    modelId = (connection as? Outcome.Success)?.value?.modelId,
                    inputText = text,
                    result = result.map { it.text },
                    duration = started.elapsedNow(),
                ),
            )
        }
        return result
    }

    private suspend fun generate(
        text: String,
        transformation: Transformation,
        provider: AiProvider,
        connection: LlmConnection,
    ): Outcome<TransformedText, TransformError> {
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
