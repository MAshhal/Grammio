package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import kotlinx.coroutines.delay

/** Records calls and answers with [nextResult] (or an echo) after [latencyMs] of virtual time. */
class RecordingTextTransformRepository(var latencyMs: Long = 0) : TextTransformRepository {
    val calls = mutableListOf<Pair<String, Transformation>>()
    var nextResult: Outcome<TransformedText, TransformError>? = null

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> {
        calls += text to transformation
        delay(latencyMs)
        return nextResult ?: Outcome.Success(TransformedText("$transformation:$text", transformation))
    }
}
