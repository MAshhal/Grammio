package com.mystic.grammio.data.transform.log.mapper

import com.mystic.grammio.data.db.Transformation_log
import com.mystic.grammio.data.transform.log.transformErrorForStorageKey
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.result.Outcome
import kotlin.time.Instant

/** A row without output failed; an error key this version doesn't know reads as [TransformError.Unknown]. */
fun Transformation_log.toDomain(): HistoryEntry = HistoryEntry(
    id = id,
    startedAt = Instant.fromEpochMilliseconds(created_at),
    transformationId = transformation,
    targetLanguageTag = target_language,
    modelId = model_id,
    inputText = input_text,
    result = output_text?.let { Outcome.Success(it) }
        ?: Outcome.Failure(error?.let(::transformErrorForStorageKey) ?: TransformError.Unknown),
)
