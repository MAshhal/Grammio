package com.mystic.grammio.testing

import com.mystic.grammio.domain.repository.HistorySettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** The history opt-in held in memory, off by default like the real one. */
class InMemoryHistorySettingsRepository : HistorySettingsRepository {
    override val isHistoryEnabled = MutableStateFlow(false)

    override suspend fun setHistoryEnabled(enabled: Boolean) {
        isHistoryEnabled.value = enabled
    }
}
