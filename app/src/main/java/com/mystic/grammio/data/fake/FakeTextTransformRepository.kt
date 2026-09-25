package com.mystic.grammio.data.fake

import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.repository.TextTransformRepository
import kotlinx.coroutines.delay

/**
 * Stand-in used while the UI flow is built without a network. Deterministic output; any input
 * containing "fail" produces an error so the error UI can be exercised.
 */
class FakeTextTransformRepository : TextTransformRepository {

    override suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError> {
        delay(800)
        if ("fail" in text.lowercase()) return Outcome.Failure(TransformError.ServiceUnavailable)
        val tag = when (transformation) {
            is Transformation.Translate -> "Translate → ${transformation.targetLanguageTag}"
            else -> transformation.toString()
        }
        return Outcome.Success(TransformedText("[$tag] $text", transformation))
    }
}
