package com.mystic.grammio.data.transform.log

import com.mystic.grammio.data.transform.log.local.TransformationLogLocalDataSource
import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

/** History is the user-facing name for the transformation log. */
class HistoryRepositoryImpl(private val localDataSource: TransformationLogLocalDataSource) : HistoryRepository {

    override fun recent(limit: Int): Flow<List<HistoryEntry>> = localDataSource.recent(limit)

    override suspend fun clear() = localDataSource.clear()
}
