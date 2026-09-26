package com.mystic.grammio.data.transform.log.local

import kotlinx.coroutines.flow.Flow

/** Whether the user opted in to the transformation log. */
interface TransformationLogPreferencesLocalDataSource {
    val isEnabled: Flow<Boolean>

    suspend fun setEnabled(enabled: Boolean)
}
