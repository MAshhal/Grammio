package com.mystic.grammio.data.transform.log

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.AiProvider
import com.mystic.grammio.domain.model.Transformation
import com.mystic.grammio.domain.result.Outcome
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * One transformation attempt, as recorded in the local log.
 *
 * @property targetLanguageTag set only when the transformation asked for a target language.
 * @property modelId null when the provider could not be resolved, so no model was called.
 * @property result the cleaned-up text shown to the user, or why there was none.
 */
data class TransformationLogEntry(
    val startedAt: Instant,
    val transformation: Transformation,
    val targetLanguageTag: String?,
    val provider: AiProvider,
    val modelId: String?,
    val inputText: String,
    val result: Outcome<String, TransformError>,
    val duration: Duration,
)
