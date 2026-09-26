package com.mystic.grammio.testing

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import com.mystic.grammio.domain.result.Outcome
import kotlinx.coroutines.delay

/**
 * Records calls and answers with [nextResult] after [latencyMs] of virtual time. Without one it
 * echoes "id:text", or "id[language]:text" for a transformation that uses the target language.
 */
class RecordingTextTransformRepository(var latencyMs: Long = 0) : TextTransformRepository {
    val calls = mutableListOf<Pair<String, Transformation>>()
    var nextResult: Outcome<TransformedText, TransformError>? = null

    override suspend fun transform(
        text: String,
        transformation: Transformation,
        targetLanguageTag: String,
    ): Outcome<TransformedText, TransformError> {
        calls += text to transformation
        delay(latencyMs)
        val label = transformation.id + if (transformation.usesTargetLanguage) "[$targetLanguageTag]" else ""
        return nextResult ?: Outcome.Success(TransformedText("$label:$text", transformation))
    }
}
