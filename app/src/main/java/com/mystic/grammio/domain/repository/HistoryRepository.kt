package com.mystic.grammio.domain.repository

import com.mystic.grammio.domain.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/** The transformations kept on this device while history was on. */
interface HistoryRepository {
    /** The latest [limit] entries, newest first. */
    fun recent(limit: Int): Flow<List<HistoryEntry>>

    /** Deletes every saved entry. Whether new ones are saved is up to [HistorySettingsRepository]. */
    suspend fun clear()
}
