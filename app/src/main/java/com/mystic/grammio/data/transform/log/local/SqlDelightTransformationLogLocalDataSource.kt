package com.mystic.grammio.data.transform.log.local

import co.touchlab.kermit.Logger
import com.mystic.grammio.data.db.GrammioDatabase
import com.mystic.grammio.data.provider.storageKey
import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.data.transform.log.storageKey
import com.mystic.grammio.domain.result.Outcome
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The transformation log in the app's SQLite database. */
class SqlDelightTransformationLogLocalDataSource(private val database: GrammioDatabase) :
    TransformationLogLocalDataSource {

    private val log = Logger.withTag("TransformationLog")

    override suspend fun record(entry: TransformationLogEntry) {
        try {
            withContext(Dispatchers.IO) { insert(entry) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Losing a log row must never cost the user their result.
            log.w { "Could not record transformation (${e::class.simpleName})" }
        }
    }

    private fun insert(entry: TransformationLogEntry) {
        database.transformationLogQueries.insert(
            created_at = entry.startedAt.toEpochMilliseconds(),
            transformation = entry.transformation.id,
            target_language = entry.targetLanguageTag,
            provider = entry.provider.storageKey,
            model_id = entry.modelId,
            input_text = entry.inputText,
            output_text = (entry.result as? Outcome.Success)?.value,
            error = (entry.result as? Outcome.Failure)?.error?.storageKey,
            duration_ms = entry.duration.inWholeMilliseconds,
        )
    }
}
