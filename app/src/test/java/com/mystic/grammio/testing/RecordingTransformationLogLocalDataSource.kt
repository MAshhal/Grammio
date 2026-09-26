package com.mystic.grammio.testing

import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.data.transform.log.local.TransformationLogLocalDataSource
import com.mystic.grammio.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/** Keeps recorded entries in memory, in order. Only recording is exercised through it. */
class RecordingTransformationLogLocalDataSource : TransformationLogLocalDataSource {
    val entries = mutableListOf<TransformationLogEntry>()

    override suspend fun record(entry: TransformationLogEntry) {
        entries += entry
    }

    override fun recent(limit: Int): Flow<List<HistoryEntry>> = throw UnsupportedOperationException()

    override suspend fun clear() = entries.clear()
}
