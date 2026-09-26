package com.mystic.grammio.domain.repository

import kotlinx.coroutines.flow.Flow

/** Whether transformations (input, result, model) are kept in an on-device history. Off by default. */
interface HistorySettingsRepository {
    val isHistoryEnabled: Flow<Boolean>

    suspend fun setHistoryEnabled(enabled: Boolean)
}
