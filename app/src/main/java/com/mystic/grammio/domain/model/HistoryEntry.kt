package com.mystic.grammio.domain.model

import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.result.Outcome
import kotlin.time.Instant

/**
 * One transformation saved while history was on.
 *
 * @property transformationId may belong to a transformation that has since been edited or deleted.
 * @property targetLanguageTag set only when the transformation asked for a target language.
 * @property modelId null when the provider could not be resolved, so no model was called.
 * @property result the text the user was shown, or why there was none.
 */
data class HistoryEntry(
    val id: Long,
    val startedAt: Instant,
    val transformationId: String,
    val targetLanguageTag: String?,
    val modelId: String?,
    val inputText: String,
    val result: Outcome<String, TransformError>,
)
