package com.mystic.grammio.presentation.settings.history

import com.google.common.truth.Truth.assertThat
import com.mystic.grammio.domain.error.TransformError
import com.mystic.grammio.domain.model.HistoryEntry
import com.mystic.grammio.domain.result.Outcome
import com.mystic.grammio.testing.InMemoryHistoryRepository
import com.mystic.grammio.testing.InMemoryHistorySettingsRepository
import com.mystic.grammio.testing.InMemoryTransformationRepository
import com.mystic.grammio.testing.MainDispatcherRule
import com.mystic.grammio.testing.TestTransformations
import kotlin.time.Instant
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class HistorySettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val historySettings = InMemoryHistorySettingsRepository()
    private val history = InMemoryHistoryRepository()
    private val transformations = InMemoryTransformationRepository(listOf(TestTransformations.fixGrammar))

    // Lazy: must be created after MainDispatcherRule has installed the test Main dispatcher.
    private val viewModel by lazy { HistorySettingsViewModel(historySettings, history, transformations) }

    private fun TestScope.collectState() {
        val state = viewModel.state
        backgroundScope.launch { state.collect {} }
        // advanceUntilIdle stops once only background work is left, so run the first collection here.
        runCurrent()
    }

    @Test
    fun `history is off until the user turns it on`() = runTest {
        collectState()
        assertThat(viewModel.state.value.isHistoryEnabled).isFalse()

        viewModel.onAction(HistorySettingsAction.Toggled(true))
        advanceUntilIdle()

        assertThat(historySettings.isHistoryEnabled.value).isTrue()
        assertThat(viewModel.state.value.isHistoryEnabled).isTrue()
    }

    @Test
    fun `entries are not known until loaded`() {
        assertThat(viewModel.state.value.entries).isNull()
    }

    @Test
    fun `each entry comes with the transformation that ran, if it still exists`() = runTest {
        val kept = entry(id = 2, transformationId = "fix_grammar")
        val orphaned = entry(id = 1, transformationId = "deleted")
        history.entries.value = listOf(kept, orphaned)

        collectState()

        assertThat(viewModel.state.value.entries).containsExactly(
            HistoryItem(kept, TestTransformations.fixGrammar),
            HistoryItem(orphaned, transformation = null),
        ).inOrder()
    }

    @Test
    fun `shows only the most recent entries`() = runTest {
        history.entries.value = (1..HistorySettingsViewModel.RECENT_LIMIT + 5L).map { entry(id = it) }

        collectState()

        assertThat(viewModel.state.value.entries).hasSize(HistorySettingsViewModel.RECENT_LIMIT)
    }

    @Test
    fun `clearing deletes every entry but leaves history on`() = runTest {
        historySettings.setHistoryEnabled(true)
        history.entries.value = listOf(entry(id = 1))
        collectState()

        viewModel.onAction(HistorySettingsAction.Cleared)
        advanceUntilIdle()

        assertThat(viewModel.state.value.entries).isEmpty()
        assertThat(viewModel.state.value.isHistoryEnabled).isTrue()
    }

    private fun entry(
        id: Long,
        transformationId: String = "fix_grammar",
    ) = HistoryEntry(
        id = id,
        startedAt = Instant.fromEpochMilliseconds(id),
        transformationId = transformationId,
        targetLanguageTag = null,
        modelId = "claude-haiku-4-5",
        inputText = "Hello.",
        result = if (id % 2 == 0L) Outcome.Success("Hello!") else Outcome.Failure(TransformError.Network),
    )
}
