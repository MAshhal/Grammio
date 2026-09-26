package com.mystic.grammio.presentation.settings.history

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.testing.InMemoryHistorySettingsRepository
import com.mystic.grammio.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HistorySettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val historySettings = InMemoryHistorySettingsRepository()

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy { HistorySettingsViewModel(historySettings) }

    @Test
    fun `history is off until the user turns it on`() = runTest {
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()
        assertThat(viewModel.state.value.isHistoryEnabled).isFalse()

        viewModel.onAction(HistorySettingsAction.Toggled(true))
        advanceUntilIdle()

        assertThat(historySettings.isHistoryEnabled.value).isTrue()
        assertThat(viewModel.state.value.isHistoryEnabled).isTrue()
    }
}
