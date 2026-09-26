package com.mystic.grammio.data.transform.log.local

import com.mystic.grammio.data.transform.log.TransformationLogEntry
import com.mystic.grammio.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/** On-device log of every transformation attempt. */
interface TransformationLogLocalDataSource {
    /** Best effort: a failure to record is logged and swallowed, never surfaced to the caller. */
    suspend fun record(entry: TransformationLogEntry)

    /** The latest [limit] entries, newest first, updated as entries are recorded or cleared. */
    fun recent(limit: Int): Flow<List<HistoryEntry>>

    suspend fun clear()
}
