package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText
import com.mystic.grammio.domain.result.Outcome

/** Port for "turn this text into that text"; how (which LLM, prompts, HTTP) is a data-layer detail. */
interface TextTransformRepository {
    /** @param targetLanguageTag BCP-47 tag; only used when [Transformation.usesTargetLanguage]. */
    suspend fun transform(
        text: String,
        transformation: Transformation,
        targetLanguageTag: String,
    ): Outcome<TransformedText, TransformError>
}
