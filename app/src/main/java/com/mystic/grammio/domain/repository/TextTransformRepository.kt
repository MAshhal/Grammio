package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.model.Outcome
import com.mystic.grammio.domain.model.TransformError
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.model.TransformedText

/** Port for "turn this text into that text"; how (which LLM, prompts, HTTP) is a data-layer detail. */
interface TextTransformRepository {
    suspend fun transform(
        text: String,
        transformation: Transformation,
    ): Outcome<TransformedText, TransformError>
}
