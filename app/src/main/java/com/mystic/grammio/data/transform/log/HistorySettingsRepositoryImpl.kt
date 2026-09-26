package com.mystic.grammio.data.transform.log

import com.mystic.grammio.data.transform.log.local.TransformationLogPreferencesLocalDataSource
import com.mystic.grammio.domain.repository.HistorySettingsRepository
import kotlinx.coroutines.flow.Flow

/** History is the user-facing name for the transformation log. */
class HistorySettingsRepositoryImpl(private val localDataSource: TransformationLogPreferencesLocalDataSource) :
    HistorySettingsRepository {

    override val isHistoryEnabled: Flow<Boolean> = localDataSource.isEnabled

    override suspend fun setHistoryEnabled(enabled: Boolean) = localDataSource.setEnabled(enabled)
}
