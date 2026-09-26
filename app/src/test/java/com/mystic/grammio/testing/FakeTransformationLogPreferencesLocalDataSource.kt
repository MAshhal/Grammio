package com.mystic.grammio.testing

import com.mystic.grammio.data.transform.log.local.TransformationLogPreferencesLocalDataSource
import kotlinx.coroutines.flow.MutableStateFlow

/** The history opt-in held in memory. */
class FakeTransformationLogPreferencesLocalDataSource(enabled: Boolean = false) :
    TransformationLogPreferencesLocalDataSource {
    override val isEnabled = MutableStateFlow(enabled)

    override suspend fun setEnabled(enabled: Boolean) {
        isEnabled.value = enabled
    }
}
