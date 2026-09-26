package com.mystic.grammio.testing

import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Saved entries held in memory, newest first. */
class InMemoryHistoryRepository(initial: List<HistoryEntry> = emptyList()) : HistoryRepository {
    val entries = MutableStateFlow(initial)

    override fun recent(limit: Int): Flow<List<HistoryEntry>> = entries.map { it.take(limit) }

    override suspend fun clear() {
        entries.value = emptyList()
    }
}
